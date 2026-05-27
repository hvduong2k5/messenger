package com.midterm.team12345.data.repository;

import android.content.Context;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.midterm.team12345.data.remote.RetrofitClient;
import com.midterm.team12345.data.remote.api.ConversationApiService;
import com.midterm.team12345.data.remote.api.MessageApiService;
import com.midterm.team12345.data.remote.api.MessageStatusApiService;
import com.midterm.team12345.data.remote.dto.request.MessageRequestDTO;
import com.midterm.team12345.data.remote.dto.response.MessageResponseDTO;
import com.midterm.team12345.data.remote.dto.response.MessageStatusResponseDTO;
import com.midterm.team12345.data.remote.dto.response.PageResponse;
import com.midterm.team12345.domain.repository.MessageRepository;
import com.midterm.team12345.utils.Resource;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MessageRepositoryImpl implements MessageRepository {
    private static MessageRepositoryImpl instance;
    private final MessageApiService messageApiService;
    private final MessageStatusApiService messageStatusApiService;
    private final ConversationApiService conversationApiService;
    private final com.midterm.team12345.data.local.dao.MessageDao messageDao;
    private final com.midterm.team12345.data.local.dao.SyncQueueDao syncQueueDao;
    private final com.midterm.team12345.data.local.dao.AttachmentDao attachmentDao;

    private MessageRepositoryImpl(MessageApiService messageApiService, 
                                 MessageStatusApiService messageStatusApiService,
                                 ConversationApiService conversationApiService,
                                 com.midterm.team12345.data.local.dao.MessageDao messageDao,
                                 com.midterm.team12345.data.local.dao.SyncQueueDao syncQueueDao,
                                 com.midterm.team12345.data.local.dao.AttachmentDao attachmentDao) {
        this.messageApiService = messageApiService;
        this.messageStatusApiService = messageStatusApiService;
        this.conversationApiService = conversationApiService;
        this.messageDao = messageDao;
        this.syncQueueDao = syncQueueDao;
        this.attachmentDao = attachmentDao;
    }

    public static synchronized MessageRepositoryImpl getInstance(Context context) {
        if (instance == null) {
            instance = new MessageRepositoryImpl(
                RetrofitClient.getMessageApiService(context),
                RetrofitClient.getMessageStatusApiService(context),
                RetrofitClient.getConversationApiService(context),
                com.midterm.team12345.data.local.database.MessengerDatabase.getInstance(context).messageDao(),
                com.midterm.team12345.data.local.database.MessengerDatabase.getInstance(context).syncQueueDao(),
                com.midterm.team12345.data.local.database.MessengerDatabase.getInstance(context).attachmentDao()
            );
        }
        return instance;
    }

    @Override
    public LiveData<Resource<List<MessageResponseDTO>>> getMessages(Long conversationId, int page, int size) {
        MutableLiveData<Resource<List<MessageResponseDTO>>> data = new MutableLiveData<>();
        data.setValue(Resource.loading(null));

        // 1. Load from DB immediately
        new java.lang.Thread(() -> {
            try {
                List<com.midterm.team12345.data.local.entity.MessageEntity> localEntities = messageDao.getMessagesByConversationIdSync(conversationId);
                if (localEntities != null && !localEntities.isEmpty()) {
                    List<MessageResponseDTO> responseList = loadMessagesWithAttachments(localEntities);
                    data.postValue(Resource.success(responseList));
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            // 2. Fetch from network in background
            conversationApiService.getMessages(conversationId, page, size).enqueue(new Callback<PageResponse<MessageResponseDTO>>() {
                @Override
                public void onResponse(Call<PageResponse<MessageResponseDTO>> call, Response<PageResponse<MessageResponseDTO>> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        new java.lang.Thread(() -> {
                            try {
                                List<MessageResponseDTO> remoteDtos = response.body().getContent();
                                if (remoteDtos != null) {
                                    List<com.midterm.team12345.data.local.entity.MessageEntity> entities = new java.util.ArrayList<>();
                                    for (MessageResponseDTO dto : remoteDtos) {
                                        com.midterm.team12345.data.local.entity.MessageEntity localMsg = messageDao.getMessageByServerId(dto.getMessageId());
                                        if (localMsg == null) {
                                            localMsg = messageDao.getPendingMessage(dto.getConversationId(), dto.getSenderId(), dto.getContent());
                                        }
                                        if (localMsg != null) {
                                            com.midterm.team12345.data.local.entity.MessageEntity mapped = 
                                                    com.midterm.team12345.data.mapper.MessageMapper.toEntity(dto, localMsg.getClientMessageId());
                                            mapped.setLocalId(localMsg.getLocalId());
                                            entities.add(mapped);
                                            saveAttachments(dto.getAttachments(), localMsg.getClientMessageId());
                                        } else {
                                            com.midterm.team12345.data.local.entity.MessageEntity mapped = com.midterm.team12345.data.mapper.MessageMapper.toEntity(dto);
                                            entities.add(mapped);
                                            saveAttachments(dto.getAttachments(), mapped.getClientMessageId());
                                        }
                                    }
                                    messageDao.insertMessages(entities);
                                }

                                // Load updated list from DB to UI
                                List<com.midterm.team12345.data.local.entity.MessageEntity> updatedLocal = messageDao.getMessagesByConversationIdSync(conversationId);
                                List<MessageResponseDTO> updatedResponses = loadMessagesWithAttachments(updatedLocal);
                                data.postValue(Resource.success(updatedResponses));
                            } catch (Exception e) {
                                e.printStackTrace();
                                data.postValue(Resource.error("Lỗi Room DB: " + e.getMessage(), null));
                            }
                        }).start();
                    } else {
                        String errorMsg = "Lỗi lấy danh sách tin nhắn (HTTP " + response.code() + ")";
                        try {
                            if (response.errorBody() != null) {
                                errorMsg += ": " + response.errorBody().string();
                            }
                        } catch (Exception ignored) {}
                        data.setValue(Resource.error(errorMsg, null));
                    }
                }

                @Override
                public void onFailure(Call<PageResponse<MessageResponseDTO>> call, Throwable t) {
                    data.setValue(Resource.error("Lỗi kết nối: " + t.getMessage(), null));
                }
            });
        }).start();

        return data;
    }

    @Override
    public LiveData<Resource<MessageResponseDTO>> sendMessage(MessageRequestDTO request) {
        MutableLiveData<Resource<MessageResponseDTO>> data = new MutableLiveData<>();
        
        final String clientMessageId = (request.getClientMessageId() != null && !request.getClientMessageId().isEmpty()) 
                ? request.getClientMessageId() 
                : java.util.UUID.randomUUID().toString();
        request.setClientMessageId(clientMessageId);

        // 1. Save to DB with syncState = PENDING immediately
        new java.lang.Thread(() -> {
            try {
                com.midterm.team12345.data.local.entity.MessageEntity entity = new com.midterm.team12345.data.local.entity.MessageEntity();
                entity.setClientMessageId(clientMessageId);
                entity.setConversationId(request.getConversationId());
                entity.setSenderId(request.getSenderId());
                entity.setContent(request.getContent());
                entity.setType("TEXT");
                entity.setSyncState(com.midterm.team12345.data.local.entity.SyncState.PENDING);
                entity.setDeliveryStatus(com.midterm.team12345.data.local.entity.DeliveryStatus.PENDING);
                entity.setLocalCreatedAt(System.currentTimeMillis());
                
                Long localId = messageDao.insertMessage(entity);
                entity.setLocalId(localId);

                MessageResponseDTO pendingResponse = com.midterm.team12345.data.mapper.MessageMapper.toResponse(entity);
                data.postValue(Resource.loading(pendingResponse));

                // 2. Call API Service to send message
                messageApiService.sendMessage(
                        request.getSenderId(),
                        request.getConversationId(),
                        request.getContent(),
                        clientMessageId
                ).enqueue(new Callback<MessageResponseDTO>() {
                    @Override
                    public void onResponse(Call<MessageResponseDTO> call, Response<MessageResponseDTO> response) {
                        new java.lang.Thread(() -> {
                            if (response.isSuccessful() && response.body() != null) {
                                try {
                                    MessageResponseDTO responseDto = response.body();
                                    Long serverCreatedAt = responseDto.getCreatedAt();
                                    
                                    // 3. On success, update Room row to syncState = SENT
                                    messageDao.updateSyncSuccess(
                                            clientMessageId,
                                            responseDto.getMessageId(),
                                            serverCreatedAt,
                                            com.midterm.team12345.data.local.entity.SyncState.SENT,
                                            com.midterm.team12345.data.local.entity.DeliveryStatus.SENT
                                    );
                                    
                                    com.midterm.team12345.data.local.entity.MessageEntity updatedEntity = messageDao.getMessageByClientMessageId(clientMessageId);
                                    MessageResponseDTO finalMsg = com.midterm.team12345.data.mapper.MessageMapper.toResponse(updatedEntity);
                                    data.postValue(Resource.success(finalMsg));
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            } else {
                                handleSendFailure(clientMessageId, request, data);
                            }
                        }).start();
                    }

                    @Override
                    public void onFailure(Call<MessageResponseDTO> call, Throwable t) {
                        new java.lang.Thread(() -> {
                            handleSendFailure(clientMessageId, request, data);
                        }).start();
                    }
                });
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();

        return data;
    }

    private void handleSendFailure(String clientMessageId, MessageRequestDTO request, MutableLiveData<Resource<MessageResponseDTO>> data) {
        try {
            // 4. Update syncState to FAILED in Room DB
            messageDao.updateSyncFailure(
                    clientMessageId,
                    com.midterm.team12345.data.local.entity.SyncState.FAILED,
                    System.currentTimeMillis()
            );

            // 5. Throw task into SyncQueueDao for background/offline worker synchronization
            com.midterm.team12345.data.local.entity.SyncQueueEntity task = new com.midterm.team12345.data.local.entity.SyncQueueEntity();
            task.setOperationType(com.midterm.team12345.data.local.entity.SyncOperationType.SEND_MESSAGE);
            task.setPayload(new com.google.gson.Gson().toJson(request));
            task.setStatus(com.midterm.team12345.data.local.entity.SyncStatus.PENDING);
            syncQueueDao.insertTask(task);

            com.midterm.team12345.data.local.entity.MessageEntity failedEntity = messageDao.getMessageByClientMessageId(clientMessageId);
            MessageResponseDTO failedMsg = com.midterm.team12345.data.mapper.MessageMapper.toResponse(failedEntity);
            data.postValue(Resource.error("Lỗi kết nối. Tin nhắn đã được lưu để gửi lại khi trực tuyến.", failedMsg));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public LiveData<Resource<MessageResponseDTO>> sendMessageWithAttachments(Long conversationId, String content, String clientMessageId, List<File> files) {
        MutableLiveData<Resource<MessageResponseDTO>> data = new MutableLiveData<>();
        data.setValue(Resource.loading(null));

        RequestBody conversationIdBody = RequestBody.create(MediaType.parse("text/plain"), String.valueOf(conversationId));
        RequestBody contentBody = RequestBody.create(MediaType.parse("text/plain"), content != null ? content : "");
        RequestBody clientMessageIdBody = RequestBody.create(MediaType.parse("text/plain"), clientMessageId);

        List<MultipartBody.Part> multipartFiles = new ArrayList<>();
        if (files != null) {
            for (File file : files) {
                RequestBody requestFile = RequestBody.create(MediaType.parse("application/octet-stream"), file);
                MultipartBody.Part body = MultipartBody.Part.createFormData("files", file.getName(), requestFile);
                multipartFiles.add(body);
            }
        }

        messageApiService.sendMessageMultipart(conversationIdBody, contentBody, clientMessageIdBody, multipartFiles).enqueue(new Callback<MessageResponseDTO>() {
            @Override
            public void onResponse(Call<MessageResponseDTO> call, Response<MessageResponseDTO> response) {
                if (response.isSuccessful() && response.body() != null) {
                    new java.lang.Thread(() -> {
                        try {
                            MessageResponseDTO responseDto = response.body();
                            com.midterm.team12345.data.local.entity.MessageEntity existing = 
                                    messageDao.getMessageByServerId(responseDto.getMessageId());
                            if (existing != null) {
                                existing.setClientMessageId(clientMessageId);
                                messageDao.updateMessage(existing);
                            } else {
                                com.midterm.team12345.data.local.entity.MessageEntity entity = 
                                        com.midterm.team12345.data.mapper.MessageMapper.toEntity(responseDto, clientMessageId);
                                messageDao.insertMessage(entity);
                            }
                            
                            // Save attachments to Room
                            saveAttachments(responseDto.getAttachments(), clientMessageId);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }).start();
                    data.setValue(Resource.success(response.body()));
                } else {
                    String errorMsg = "Gửi tin nhắn đính kèm thất bại";
                    try {
                        if (response.errorBody() != null) {
                            errorMsg += ": " + response.errorBody().string();
                        }
                    } catch (Exception ignored) {}
                    data.setValue(Resource.error(errorMsg, null));
                }
            }

            @Override
            public void onFailure(Call<MessageResponseDTO> call, Throwable t) {
                data.setValue(Resource.error(t.getMessage(), null));
            }
        });

        return data;
    }

    @Override
    public LiveData<Resource<Void>> revokeMessage(Long messageId) {
        MutableLiveData<Resource<Void>> data = new MutableLiveData<>();
        data.setValue(Resource.loading(null));
        messageApiService.revokeMessage(messageId).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) data.setValue(Resource.success(null));
                else data.setValue(Resource.error("Thu hồi tin nhắn thất bại", null));
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                data.setValue(Resource.error(t.getMessage(), null));
            }
        });
        return data;
    }

    @Override
    public LiveData<Resource<MessageResponseDTO>> editMessage(Long messageId, MessageRequestDTO request) {
        MutableLiveData<Resource<MessageResponseDTO>> data = new MutableLiveData<>();
        data.setValue(Resource.loading(null));
        messageApiService.editMessage(messageId, request).enqueue(new Callback<MessageResponseDTO>() {
            @Override
            public void onResponse(Call<MessageResponseDTO> call, Response<MessageResponseDTO> response) {
                if (response.isSuccessful() && response.body() != null) {
                    data.setValue(Resource.success(mapMessageToDomain(response.body())));
                } else {
                    data.setValue(Resource.error("Chỉnh sửa tin nhắn thất bại", null));
                }
            }

            @Override
            public void onFailure(Call<MessageResponseDTO> call, Throwable t) {
                data.setValue(Resource.error(t.getMessage(), null));
            }
        });
        return data;
    }

    @Override
    public LiveData<Resource<Void>> markConversationAsRead(Long conversationId) {
        MutableLiveData<Resource<Void>> data = new MutableLiveData<>();
        data.setValue(Resource.loading(null));
        messageStatusApiService.markConversationAsRead(conversationId).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) data.setValue(Resource.success(null));
                else data.setValue(Resource.error("Đánh dấu đã đọc thất bại", null));
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                data.setValue(Resource.error(t.getMessage(), null));
            }
        });
        return data;
    }

    @Override
    public LiveData<Resource<Void>> updateMessageStatus(Long messageId, String status) {
        MutableLiveData<Resource<Void>> data = new MutableLiveData<>();
        data.setValue(Resource.loading(null));
        Map<String, String> body = new HashMap<>();
        body.put("status", status);
        messageStatusApiService.updateMessageStatus(messageId, body).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) data.setValue(Resource.success(null));
                else data.setValue(Resource.error("Cập nhật trạng thái tin nhắn thất bại", null));
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                data.setValue(Resource.error(t.getMessage(), null));
            }
        });
        return data;
    }

    @Override
    public LiveData<Resource<List<MessageStatusResponseDTO>>> getMessageStatuses(Long messageId) {
        MutableLiveData<Resource<List<MessageStatusResponseDTO>>> data = new MutableLiveData<>();
        data.setValue(Resource.loading(null));
        messageStatusApiService.getMessageStatuses(messageId).enqueue(new Callback<List<MessageStatusResponseDTO>>() {
            @Override
            public void onResponse(Call<List<MessageStatusResponseDTO>> call, Response<List<MessageStatusResponseDTO>> response) {
                if (response.isSuccessful()) data.setValue(Resource.success(response.body()));
                else data.setValue(Resource.error("Lấy trạng thái tin nhắn thất bại", null));
            }

            @Override
            public void onFailure(Call<List<MessageStatusResponseDTO>> call, Throwable t) {
                data.setValue(Resource.error(t.getMessage(), null));
            }
        });
        return data;
    }

    private MessageResponseDTO mapMessageToDomain(MessageResponseDTO dto) {
        return dto;
    }

    private List<MessageResponseDTO> loadMessagesWithAttachments(List<com.midterm.team12345.data.local.entity.MessageEntity> localEntities) {
        List<MessageResponseDTO> responseList = new ArrayList<>();
        if (localEntities != null) {
            for (com.midterm.team12345.data.local.entity.MessageEntity entity : localEntities) {
                MessageResponseDTO msgDto = com.midterm.team12345.data.mapper.MessageMapper.toResponse(entity);
                if (msgDto != null) {
                    List<com.midterm.team12345.data.local.entity.AttachmentEntity> attEntities = 
                            attachmentDao.getAttachmentsByClientMessageIdSync(entity.getClientMessageId());
                    if (attEntities != null && !attEntities.isEmpty()) {
                        List<com.midterm.team12345.data.remote.dto.response.AttachmentResponseDTO> attDtos = new ArrayList<>();
                        for (com.midterm.team12345.data.local.entity.AttachmentEntity attEnt : attEntities) {
                            attDtos.add(com.midterm.team12345.data.mapper.MessageMapper.toAttachmentResponse(attEnt));
                        }
                        msgDto.setAttachments(attDtos);
                    }
                    responseList.add(msgDto);
                }
            }
        }
        return responseList;
    }

    private void saveAttachments(List<com.midterm.team12345.data.remote.dto.response.AttachmentResponseDTO> attachments, String clientMessageId) {
        if (attachments == null || attachments.isEmpty()) return;
        List<com.midterm.team12345.data.local.entity.AttachmentEntity> attachmentEntities = new ArrayList<>();
        for (com.midterm.team12345.data.remote.dto.response.AttachmentResponseDTO attDto : attachments) {
            com.midterm.team12345.data.local.entity.AttachmentEntity entity = com.midterm.team12345.data.mapper.MessageMapper.toAttachmentEntity(attDto, clientMessageId);
            attachmentEntities.add(entity);
        }
        attachmentDao.deleteAttachmentsByClientMessageId(clientMessageId);
        attachmentDao.insertAttachments(attachmentEntities);
    }
}

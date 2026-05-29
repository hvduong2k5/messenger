package com.midterm.team12345.data.remote.mqtt;

import android.content.Context;
import android.util.Log;

import com.google.gson.Gson;
import com.midterm.team12345.data.remote.dto.MqttMessageDTO;

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallbackExtended;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

public class MqttManager {
    private static final String TAG = "MqttManager";
    private static MqttManager instance;
    private MqttClient mqttClient;
    private final Gson gson = new Gson();
    private MqttCallback callback;
    private Context context;
    private Long currentUserId;
    private final java.util.concurrent.ExecutorService executor = java.util.concurrent.Executors.newSingleThreadExecutor();

    public interface MqttCallback {
        void onMessageReceived(String topic, MqttMessageDTO message);
        void onConnectionLost(Throwable cause);
        void onConnectComplete(boolean reconnect, String serverURI);
    }

    private MqttManager() {}

    public static synchronized MqttManager getInstance() {
        if (instance == null) {
            instance = new MqttManager();
        }
        return instance;
    }

    public void init(Context context, String serverUri, String clientId, String username, String token, Long userId, MqttCallback callback) {
        this.context = context.getApplicationContext();
        this.currentUserId = userId;
        this.callback = callback;
        try {
            mqttClient = new MqttClient(serverUri, clientId, new MemoryPersistence());
            MqttConnectOptions options = new MqttConnectOptions();
            options.setUserName(username);
            options.setPassword(token.toCharArray());
            options.setAutomaticReconnect(true);
            options.setCleanSession(false);
            
            // Last Will and Testament
            String lwtTopic = "user/" + userId + "/presence";
            String lwtPayload = "offline";
            options.setWill(lwtTopic, lwtPayload.getBytes(), 1, true);

            mqttClient.setCallback(new MqttCallbackExtended() {
                @Override
                public void connectComplete(boolean reconnect, String serverURI) {
                    Log.d(TAG, "Connected to: " + serverURI);
                    if (currentUserId != null) {
                        subscribe("user/" + currentUserId + "/messages");
                        subscribe("user/" + currentUserId + "/presence");
                    }
                    if (MqttManager.this.context != null) {
                        com.midterm.team12345.data.repository.ConversationRepositoryImpl.getInstance((android.app.Application) MqttManager.this.context.getApplicationContext()).updateConnectionStatus(true);
                        com.midterm.team12345.data.repository.MessageRepositoryImpl.getInstance(MqttManager.this.context).syncPendingMessages();
                    }
                    if (callback != null) callback.onConnectComplete(reconnect, serverURI);
                }

                @Override
                public void connectionLost(Throwable cause) {
                    Log.e(TAG, "Connection lost", cause);
                    if (MqttManager.this.context != null) {
                        com.midterm.team12345.data.repository.ConversationRepositoryImpl.getInstance((android.app.Application) MqttManager.this.context.getApplicationContext()).updateConnectionStatus(false);
                    }
                    if (callback != null) callback.onConnectionLost(cause);
                }

                @Override
                public void messageArrived(String topic, MqttMessage message) {
                    Log.d(TAG, "Message arrived on topic: " + topic);
                    try {
                        String payloadStr = new String(message.getPayload()).trim();
                        if (topic.endsWith("/presence")) {
                            Log.d(TAG, "Presence message received on topic " + topic + ": " + payloadStr);
                            // Avoid parsing raw presence strings (like "online", "offline") as MqttMessageDTO
                            return;
                        }
                        if (payloadStr.startsWith("{")) {
                            MqttMessageDTO dto = gson.fromJson(payloadStr, MqttMessageDTO.class);
                            handleIncomingMqttMessage(topic, dto);
                        } else {
                            Log.w(TAG, "Expected JSON message but got: " + payloadStr);
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "Error parsing MQTT message", e);
                    }
                }

                @Override
                public void deliveryComplete(IMqttDeliveryToken token) {
                    Log.d(TAG, "Delivery complete");
                }
            });

            connect(options);
        } catch (MqttException e) {
            Log.e(TAG, "Initialization failed", e);
        }
    }

    private void handleIncomingMqttMessage(String topic, MqttMessageDTO message) {
        if (message == null || context == null) return;

        executor.execute(() -> {
            try {
                if (topic != null && topic.contains("/message-status")) {
                    com.midterm.team12345.data.local.dao.MessageDao messageDao = 
                            com.midterm.team12345.data.local.database.MessengerDatabase.getInstance(context)
                                    .messageDao();
                    Long messageId = message.getMessageId();
                    String status = message.getStatus();
                    if (messageId != null && status != null) {
                        messageDao.updateDeliveryStatus(messageId, com.midterm.team12345.data.local.entity.DeliveryStatus.valueOf(status.toUpperCase()));
                    }
                } else if (topic != null && topic.contains("/read")) {
                    com.midterm.team12345.data.local.dao.MessageDao messageDao = 
                            com.midterm.team12345.data.local.database.MessengerDatabase.getInstance(context)
                                    .messageDao();
                    Long conversationId = message.getConversationId();
                    Long readerId = message.getSenderId(); // userId alternate maps to senderId
                    if (conversationId != null && readerId != null) {
                        messageDao.markAllReceivedMessagesAsRead(conversationId, readerId);
                    }
                } else {
                    String type = message.getType();
                    if ("NEW_MESSAGE".equals(type) || "text".equalsIgnoreCase(type) || "media".equalsIgnoreCase(type)) {
                        // 1. Map MQTT packet to MessageEntity using MessageMapper
                        com.midterm.team12345.data.local.entity.MessageEntity entity = 
                                com.midterm.team12345.data.mapper.MessageMapper.toEntity(message);
                        
                        if (entity != null) {
                            com.midterm.team12345.data.local.dao.MessageDao messageDao = 
                                    com.midterm.team12345.data.local.database.MessengerDatabase.getInstance(context)
                                            .messageDao();
                            com.midterm.team12345.data.local.dao.ConversationDao conversationDao = 
                                    com.midterm.team12345.data.local.database.MessengerDatabase.getInstance(context)
                                            .conversationDao();
                            
                            boolean exists = false;
                            if (entity.getMessageId() != null) {
                                exists = (messageDao.getMessageByServerId(entity.getMessageId()) != null);
                            }
                            if (!exists && entity.getClientMessageId() != null) {
                                exists = (messageDao.getMessageByClientMessageId(entity.getClientMessageId()) != null);
                            }
                            
                            if (!exists) {
                                com.midterm.team12345.data.local.entity.MessageEntity pending = messageDao.getPendingMessage(
                                        entity.getConversationId(), 
                                        entity.getSenderId(), 
                                        entity.getContent()
                                );
                                if (pending != null) {
                                    messageDao.updateSyncSuccess(
                                            pending.getClientMessageId(),
                                            entity.getMessageId(),
                                            entity.getServerCreatedAt() != null ? entity.getServerCreatedAt() : entity.getLocalCreatedAt(),
                                            com.midterm.team12345.data.local.entity.SyncState.SENT,
                                            com.midterm.team12345.data.local.entity.DeliveryStatus.SENT
                                    );
                                } else {
                                    messageDao.insertMessage(entity);
                                    
                                    // Save attachments to Room immediately for real-time incoming messages
                                    com.midterm.team12345.data.remote.dto.response.MessageResponseDTO responseDto = message.toMessageResponseDTO();
                                    if (responseDto != null && responseDto.getAttachments() != null && !responseDto.getAttachments().isEmpty()) {
                                        com.midterm.team12345.data.local.dao.AttachmentDao attachmentDao = 
                                                com.midterm.team12345.data.local.database.MessengerDatabase.getInstance(context)
                                                        .attachmentDao();
                                        java.util.List<com.midterm.team12345.data.local.entity.AttachmentEntity> attachmentEntities = new java.util.ArrayList<>();
                                        for (com.midterm.team12345.data.remote.dto.response.AttachmentResponseDTO attDto : responseDto.getAttachments()) {
                                            com.midterm.team12345.data.local.entity.AttachmentEntity attEntity = 
                                                    com.midterm.team12345.data.mapper.MessageMapper.toAttachmentEntity(attDto, entity.getClientMessageId());
                                            attachmentEntities.add(attEntity);
                                        }
                                        attachmentDao.insertAttachments(attachmentEntities);
                                    }
                                }

                                // Auto-report DELIVERED status for incoming messages when app is open:
                                Long senderId = entity.getSenderId();
                                if (senderId != null && currentUserId != null && !senderId.equals(currentUserId)) {
                                    Long msgId = entity.getMessageId();
                                    if (msgId != null) {
                                        java.util.Map<String, String> statusBody = new java.util.HashMap<>();
                                        statusBody.put("status", "DELIVERED");
                                        com.midterm.team12345.data.remote.RetrofitClient.getMessageStatusApiService(context)
                                                .updateMessageStatus(msgId, statusBody)
                                                .enqueue(new retrofit2.Callback<Void>() {
                                                    @Override
                                                    public void onResponse(retrofit2.Call<Void> call, retrofit2.Response<Void> response) {}
                                                    @Override
                                                    public void onFailure(retrofit2.Call<Void> call, Throwable t) {}
                                                });
                                    }
                                }
                            }

                            // Update last message in conversation table (runs in same executor/thread)
                            conversationDao.updateLastMessage(
                                    entity.getConversationId(),
                                    entity.getContent(),
                                    entity.getSenderId(),
                                    entity.getServerCreatedAt() != null ? entity.getServerCreatedAt() : entity.getLocalCreatedAt()
                            );
                            
                            // Check if sender is not current user, increment unread count:
                            Long senderId = entity.getSenderId();
                            if (senderId != null && currentUserId != null && !senderId.equals(currentUserId)) {
                                conversationDao.incrementUnreadCount(entity.getConversationId());
                            }
                        }

                        // Show push notification
                        showPushNotification(message);
                    } else if ("REVOKE_MESSAGE".equals(type) || "EDIT_MESSAGE".equals(type)) {
                        com.midterm.team12345.data.remote.dto.response.MessageResponseDTO responseDto = message.toMessageResponseDTO();
                        Long serverMsgId = (responseDto != null) ? responseDto.getMessageId() : null;
                        if (serverMsgId != null) {
                            com.midterm.team12345.data.local.dao.MessageDao messageDao = 
                                    com.midterm.team12345.data.local.database.MessengerDatabase.getInstance(context)
                                            .messageDao();
                            com.midterm.team12345.data.local.entity.MessageEntity existing = messageDao.getMessageByServerId(serverMsgId);
                            if (existing != null) {
                                if ("REVOKE_MESSAGE".equals(type)) {
                                    existing.setDeletedAt(System.currentTimeMillis());
                                } else {
                                    existing.setContent(message.getPayload());
                                    existing.setEditedAt(System.currentTimeMillis());
                                }
                                messageDao.updateMessage(existing);
                            } else {
                                com.midterm.team12345.data.local.entity.MessageEntity entity = 
                                        com.midterm.team12345.data.mapper.MessageMapper.toEntity(message);
                                if (entity != null) {
                                    messageDao.insertMessage(entity);
                                }
                            }
                        }
                    }
                }

                // Emit real-time message to let other components handle it if necessary
                com.midterm.team12345.data.repository.ConversationRepositoryImpl.getInstance((android.app.Application) context.getApplicationContext()).emitRealTimeMessage(message);

            } catch (Exception e) {
                Log.e(TAG, "Error processing incoming MQTT message in executor", e);
            }
        });
    }

    private void showPushNotification(MqttMessageDTO message) {
        if (context == null) return;
        android.app.NotificationManager notificationManager = (android.app.NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        
        android.content.Intent intent = new android.content.Intent(context, com.midterm.team12345.MainActivity.class);
        android.app.PendingIntent pendingIntent = android.app.PendingIntent.getActivity(context, 0, intent, android.app.PendingIntent.FLAG_IMMUTABLE);

        androidx.core.app.NotificationCompat.Builder builder = new androidx.core.app.NotificationCompat.Builder(context, "MessagingServiceChannel")
                .setContentTitle(message.getSender())
                .setContentText(message.getPayload())
                .setSmallIcon(com.midterm.team12345.R.drawable.ic_messenger_logo)
                .setAutoCancel(true)
                .setContentIntent(pendingIntent)
                .setPriority(androidx.core.app.NotificationCompat.PRIORITY_DEFAULT);

        if (notificationManager != null) {
            notificationManager.notify((int) System.currentTimeMillis(), builder.build());
        }
    }

    private void connect(MqttConnectOptions options) {
        new Thread(() -> {
            try {
                if (mqttClient != null && !mqttClient.isConnected()) {
                    mqttClient.connect(options);
                }
            } catch (MqttException e) {
                Log.e(TAG, "Connection failed", e);
            }
        }).start();
    }

    public void subscribe(String topic) {
        try {
            if (mqttClient != null && mqttClient.isConnected()) {
                mqttClient.subscribe(topic, 1);
                Log.d(TAG, "Subscribed to: " + topic);
            }
        } catch (MqttException e) {
            Log.e(TAG, "Subscription failed", e);
        }
    }

    public void publish(String topic, String payload) {
        try {
            if (mqttClient != null && mqttClient.isConnected()) {
                MqttMessage message = new MqttMessage(payload.getBytes());
                message.setQos(1);
                mqttClient.publish(topic, message);
                Log.d(TAG, "Published to: " + topic);
            }
        } catch (MqttException e) {
            Log.e(TAG, "Publish failed", e);
        }
    }

    public void unsubscribe(String topic) {
        try {
            if (mqttClient != null && mqttClient.isConnected()) {
                mqttClient.unsubscribe(topic);
                Log.d(TAG, "Unsubscribed from: " + topic);
            }
        } catch (MqttException e) {
            Log.e(TAG, "Unsubscription failed", e);
        }
    }

    public void disconnect() {
        try {
            if (mqttClient != null && mqttClient.isConnected()) {
                mqttClient.disconnect();
            }
        } catch (MqttException e) {
            Log.e(TAG, "Disconnect failed", e);
        }
    }
}

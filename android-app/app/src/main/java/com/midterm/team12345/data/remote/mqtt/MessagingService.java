package com.midterm.team12345.data.remote.mqtt;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import com.midterm.team12345.MainActivity;
import com.midterm.team12345.R;
import com.midterm.team12345.data.local.TokenManager;
import com.midterm.team12345.data.remote.dto.MqttMessageDTO;
import com.midterm.team12345.data.repository.ConversationRepositoryImpl;
import com.midterm.team12345.domain.model.MqttEventType;

public class MessagingService extends Service {
    private static final String CHANNEL_ID = "MessagingServiceChannel";
    public static final String EXTRA_USERNAME = "extra_username";
    public static final String EXTRA_USER_ID = "extra_user_id";

    private MqttManager mqttManager;
    private TokenManager tokenManager;

    @Override
    public void onCreate() {
        super.onCreate();
        tokenManager = new TokenManager(this);
        mqttManager = MqttManager.getInstance();
        createNotificationChannel();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        String username = intent != null ? intent.getStringExtra(EXTRA_USERNAME) : null;
        if (username == null) username = tokenManager.getUsername();
        
        Long userId = null;
        if (intent != null && intent.hasExtra(EXTRA_USER_ID)) {
            long id = intent.getLongExtra(EXTRA_USER_ID, -1L);
            if (id != -1L) {
                userId = id;
            }
        }
        if (userId == null) {
            userId = tokenManager.getUserId();
        }
        
        String token = tokenManager.getToken();

        if (username != null && token != null && userId != null) {
            startForeground(1, getNotification("Connecting to chat..."));
            connectMqtt(username, token, userId);
        } else {
            stopSelf();
        }

        return START_STICKY;
    }

    private void connectMqtt(String username, String token, Long userId) {
        String brokerUrl = "tcp://broker.emqx.io:1883";
        String clientId = "android_" + username + "_" + System.currentTimeMillis();

        mqttManager.init(this, brokerUrl, clientId, username, token, userId, new MqttManager.MqttCallback() {
            @Override
            public void onMessageReceived(String topic, MqttMessageDTO message) {
                if (message == null) return;

                if (topic != null && topic.contains("/message-status")) {
                    new java.lang.Thread(() -> {
                        try {
                            com.midterm.team12345.data.local.dao.MessageDao messageDao = 
                                    com.midterm.team12345.data.local.database.MessengerDatabase.getInstance(getApplicationContext())
                                            .messageDao();
                            Long messageId = message.getMessageId();
                            String status = message.getStatus();
                            if (messageId != null && status != null) {
                                messageDao.updateDeliveryStatus(messageId, com.midterm.team12345.data.local.entity.DeliveryStatus.valueOf(status.toUpperCase()));
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }).start();
                } else if (topic != null && topic.contains("/read")) {
                    new java.lang.Thread(() -> {
                        try {
                            com.midterm.team12345.data.local.dao.MessageDao messageDao = 
                                    com.midterm.team12345.data.local.database.MessengerDatabase.getInstance(getApplicationContext())
                                            .messageDao();
                            Long conversationId = message.getConversationId();
                            Long readerId = message.getSenderId(); // userId alternate maps to senderId
                            if (conversationId != null && readerId != null) {
                                messageDao.markAllReceivedMessagesAsRead(conversationId, readerId);
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }).start();
                } else {
                    String type = message.getType();
                    if ("NEW_MESSAGE".equals(type) || "text".equalsIgnoreCase(type) || "media".equalsIgnoreCase(type)) {
                        new java.lang.Thread(() -> {
                            try {
                                // 1. Map MQTT packet to MessageEntity using MessageMapper
                                com.midterm.team12345.data.local.entity.MessageEntity entity = 
                                        com.midterm.team12345.data.mapper.MessageMapper.toEntity(message);
                                
                                if (entity != null) {
                                    com.midterm.team12345.data.local.dao.MessageDao messageDao = 
                                            com.midterm.team12345.data.local.database.MessengerDatabase.getInstance(getApplicationContext())
                                                    .messageDao();
                                    
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
                                        }

                                        // Auto-report DELIVERED status for incoming messages when app is open:
                                        Long senderId = entity.getSenderId();
                                        Long currentUserId = tokenManager.getUserId();
                                        if (senderId != null && currentUserId != null && !senderId.equals(currentUserId)) {
                                            Long msgId = entity.getMessageId();
                                            if (msgId != null) {
                                                java.util.Map<String, String> statusBody = new java.util.HashMap<>();
                                                statusBody.put("status", "DELIVERED");
                                                com.midterm.team12345.data.remote.RetrofitClient.getMessageStatusApiService(getApplicationContext())
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
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }).start();

                        showPushNotification(message);
                    } else if ("REVOKE_MESSAGE".equals(type) || "EDIT_MESSAGE".equals(type)) {
                        new java.lang.Thread(() -> {
                            try {
                                com.midterm.team12345.data.remote.dto.response.MessageResponseDTO responseDto = message.toMessageResponseDTO();
                                Long serverMsgId = (responseDto != null) ? responseDto.getMessageId() : null;
                                if (serverMsgId != null) {
                                    com.midterm.team12345.data.local.dao.MessageDao messageDao = 
                                            com.midterm.team12345.data.local.database.MessengerDatabase.getInstance(getApplicationContext())
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
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }).start();
                    }
                }

                // Emit real-time message to let other components handle it if necessary
                ConversationRepositoryImpl.getInstance(getApplication()).emitRealTimeMessage(message);
            }

            @Override
            public void onConnectionLost(Throwable cause) {
                updateNotification("Connection lost. Retrying...");
                ConversationRepositoryImpl.getInstance(getApplication()).updateConnectionStatus(false);
            }

            @Override
            public void onConnectComplete(boolean reconnect, String serverURI) {
                updateNotification("Connected to Messenger");
                ConversationRepositoryImpl.getInstance(getApplication()).updateConnectionStatus(true);
                mqttManager.subscribe("user/" + userId + "/messages");
                mqttManager.subscribe("user/" + userId + "/presence");

                // Sync pending messages when connection is complete
                com.midterm.team12345.data.repository.MessageRepositoryImpl.getInstance(getApplicationContext()).syncPendingMessages();
            }
        });
    }

    private void showPushNotification(MqttMessageDTO message) {
        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        
        Intent intent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_IMMUTABLE);

        Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle(message.getSender())
                .setContentText(message.getPayload())
                .setSmallIcon(R.drawable.ic_messenger_logo)
                .setAutoCancel(true)
                .setContentIntent(pendingIntent)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .build();

        notificationManager.notify((int) System.currentTimeMillis(), notification);
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel serviceChannel = new NotificationChannel(
                    CHANNEL_ID,
                    "Messaging Service Channel",
                    NotificationManager.IMPORTANCE_LOW
            );
            NotificationManager manager = getSystemService(NotificationManager.class);
            if (manager != null) {
                manager.createNotificationChannel(serviceChannel);
            }
        }
    }

    private Notification getNotification(String text) {
        Intent notificationIntent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this,
                0, notificationIntent, PendingIntent.FLAG_IMMUTABLE);

        return new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("Messenger")
                .setContentText(text)
                .setSmallIcon(R.drawable.ic_messenger_logo)
                .setContentIntent(pendingIntent)
                .build();
    }

    private void updateNotification(String text) {
        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        if (notificationManager != null) {
            notificationManager.notify(1, getNotification(text));
        }
    }

    @Override
    public void onDestroy() {
        ConversationRepositoryImpl.getInstance(getApplication()).updateConnectionStatus(false);
        mqttManager.disconnect();
        super.onDestroy();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}

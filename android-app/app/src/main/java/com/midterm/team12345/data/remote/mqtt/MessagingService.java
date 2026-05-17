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
        
        String token = tokenManager.getToken();

        if (username != null && token != null) {
            startForeground(1, getNotification("Connecting to chat..."));
            connectMqtt(username, token);
        } else {
            stopSelf();
        }

        return START_STICKY;
    }

    private void connectMqtt(String username, String token) {
        String brokerUrl = "tcp://192.168.1.166:1883"; 
        String clientId = "android_" + username + "_" + System.currentTimeMillis();

        mqttManager.init(this, brokerUrl, clientId, username, token, new MqttManager.MqttCallback() {
            @Override
            public void onMessageReceived(MqttMessageDTO message) {
                if (MqttEventType.NEW_MESSAGE.name().equals(message.getType())) {
                    new java.lang.Thread(() -> {
                        try {
                            // 1. Map MQTT packet to MessageEntity using MessageMapper
                            com.midterm.team12345.data.local.entity.MessageEntity entity = 
                                    com.midterm.team12345.data.mapper.MessageMapper.toEntity(message);
                            
                            // 2. Insert directly into Room DB
                            com.midterm.team12345.data.local.database.MessengerDatabase.getInstance(getApplicationContext())
                                    .messageDao()
                                    .insertMessage(entity);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }).start();

                    showPushNotification(message);
                }

                // Emit real-time message to let other components handle it if necessary
                ConversationRepositoryImpl.getInstance(getApplicationContext()).emitRealTimeMessage(message);
            }

            @Override
            public void onConnectionLost(Throwable cause) {
                updateNotification("Connection lost. Retrying...");
                ConversationRepositoryImpl.getInstance(getApplicationContext()).updateConnectionStatus(false);
            }

            @Override
            public void onConnectComplete(boolean reconnect, String serverURI) {
                updateNotification("Connected to Messenger");
                ConversationRepositoryImpl.getInstance(getApplicationContext()).updateConnectionStatus(true);
                mqttManager.subscribe("users/" + username + "/receive");
                mqttManager.subscribe("users/" + username + "/presence");
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
        ConversationRepositoryImpl.getInstance(getApplicationContext()).updateConnectionStatus(false);
        mqttManager.disconnect();
        super.onDestroy();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}

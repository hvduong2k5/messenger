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
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.google.gson.Gson;
import com.midterm.team12345.MainActivity;
import com.midterm.team12345.R;
import com.midterm.team12345.data.dto.MqttMessageDTO;
import com.midterm.team12345.data.local.TokenManager;

public class MessagingService extends Service {
    private static final String TAG = "MessagingService";
    private static final String CHANNEL_ID = "MessagingServiceChannel";
    public static final String ACTION_NEW_MESSAGE = "com.midterm.team12345.NEW_MESSAGE";
    public static final String EXTRA_MESSAGE = "extra_message";

    private MqttManager mqttManager;
    private TokenManager tokenManager;
    private final Gson gson = new Gson();

    @Override
    public void onCreate() {
        super.onCreate();
        tokenManager = new TokenManager(this);
        mqttManager = MqttManager.getInstance();
        createNotificationChannel();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        String username = intent != null ? intent.getStringExtra("username") : null;
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
        String brokerUrl = "tcp://10.0.2.2:1883"; // Change as needed
        String clientId = "android_" + username + "_" + System.currentTimeMillis();

        mqttManager.init(this, brokerUrl, clientId, username, token, new MqttManager.MqttCallback() {
            @Override
            public void onMessageReceived(MqttMessageDTO message) {
                broadcastMessage(message);
                showPushNotification(message);
            }

            @Override
            public void onConnectionLost(Throwable cause) {
                updateNotification("Connection lost. Retrying...");
            }

            @Override
            public void onConnectComplete(boolean reconnect, String serverURI) {
                updateNotification("Connected to Messenger");
                mqttManager.subscribe("users/" + username + "/receive");
                mqttManager.subscribe("users/" + username + "/presence");
            }
        });
    }

    private void broadcastMessage(MqttMessageDTO message) {
        Intent intent = new Intent(ACTION_NEW_MESSAGE);
        intent.putExtra(EXTRA_MESSAGE, gson.toJson(message));
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }

    private void showPushNotification(MqttMessageDTO message) {
        // Only show if not in background or logic to check if ChatDetail is open
        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle(message.getSender())
                .setContentText(message.getPayload())
                .setSmallIcon(R.drawable.ic_messenger_logo)
                .setAutoCancel(true)
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
            manager.createNotificationChannel(serviceChannel);
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
        notificationManager.notify(1, getNotification(text));
    }

    @Override
    public void onDestroy() {
        mqttManager.disconnect();
        super.onDestroy();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}

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

    public interface MqttCallback {
        void onMessageReceived(MqttMessageDTO message);
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
                    if (callback != null) callback.onConnectComplete(reconnect, serverURI);
                }

                @Override
                public void connectionLost(Throwable cause) {
                    Log.e(TAG, "Connection lost", cause);
                    if (callback != null) callback.onConnectionLost(cause);
                }

                @Override
                public void messageArrived(String topic, MqttMessage message) {
                    Log.d(TAG, "Message arrived on topic: " + topic);
                    try {
                        MqttMessageDTO dto = gson.fromJson(new String(message.getPayload()), MqttMessageDTO.class);
                        if (callback != null) callback.onMessageReceived(dto);
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

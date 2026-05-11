package com.team12345.messenger.gateway;

import org.springframework.integration.annotation.MessagingGateway;
import org.springframework.integration.mqtt.support.MqttHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

@Component
@MessagingGateway(defaultRequestChannel = "mqttOutboundChannel")
public interface MqttGateway {

    /**
     * Sends a message to a specific MQTT topic.
     *
     * @param data  The message payload.
     * @param topic The target topic.
     */
    void sendToMqtt(String data, @Header(MqttHeaders.TOPIC) String topic);

    /**
     * Sends a message to a specific MQTT topic with a dynamic QoS level.
     *
     * @param data  The message payload.
     * @param topic The target topic.
     * @param qos   The Quality of Service level (0, 1, or 2).
     */
    void sendToMqtt(String data, @Header(MqttHeaders.TOPIC) String topic, @Header(MqttHeaders.QOS) int qos);
}
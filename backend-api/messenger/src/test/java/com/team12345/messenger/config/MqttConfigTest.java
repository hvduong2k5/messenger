package com.team12345.messenger.config;

import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.integration.mqtt.core.DefaultMqttPahoClientFactory;
import org.springframework.integration.mqtt.core.MqttPahoClientFactory;
import org.springframework.integration.mqtt.inbound.MqttPahoMessageDrivenChannelAdapter;
import org.springframework.integration.mqtt.outbound.MqttPahoMessageHandler;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.MessageHandler;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@ExtendWith(MockitoExtension.class)
public class MqttConfigTest {

    @InjectMocks
    private MqttConfig mqttConfig;

    @BeforeEach
    void setUp() {
        // Set @Value properties via Reflection since it's not a full Spring Context test
        // This avoids requiring a real MQTT broker to be running during tests
        ReflectionTestUtils.setField(mqttConfig, "brokerUrl", "tcp://localhost:1883");
        ReflectionTestUtils.setField(mqttConfig, "username", "testuser");
        ReflectionTestUtils.setField(mqttConfig, "password", "testpass");
        ReflectionTestUtils.setField(mqttConfig, "clientId", "test-client");
        ReflectionTestUtils.setField(mqttConfig, "defaultTopic", "test/topic");
    }

    @Test
    void mqttClientFactory_ShouldConfigureCorrectly() {
        MqttPahoClientFactory factory = mqttConfig.mqttClientFactory();
        assertNotNull(factory);
        assertThat(factory).isInstanceOf(DefaultMqttPahoClientFactory.class);

        MqttConnectOptions options = factory.getConnectionOptions();
        assertThat(options.getServerURIs()).containsExactly("tcp://localhost:1883");
        assertThat(options.getUserName()).isEqualTo("testuser");
        assertThat(options.getPassword()).isEqualTo("testpass".toCharArray());
        assertThat(options.isCleanSession()).isFalse();
        assertThat(options.getConnectionTimeout()).isEqualTo(30);
        assertThat(options.getKeepAliveInterval()).isEqualTo(60);
        assertThat(options.isAutomaticReconnect()).isTrue();
    }

    @Test
    void mqttOutboundChannel_ShouldReturnMessageChannel() {
        MessageChannel channel = mqttConfig.mqttOutboundChannel();
        assertNotNull(channel);
    }

    @Test
    void mqttOutbound_ShouldReturnMessageHandlerWithCorrectConfig() {
        MessageHandler handler = mqttConfig.mqttOutbound();
        assertNotNull(handler);
        assertThat(handler).isInstanceOf(MqttPahoMessageHandler.class);

        MqttPahoMessageHandler mqttHandler = (MqttPahoMessageHandler) handler;
        
        // Use reflection since getDefaultTopic() is protected and isAsync() doesn't exist directly
        String defaultTopic = (String) ReflectionTestUtils.getField(mqttHandler, "defaultTopic");
        assertThat(defaultTopic).isEqualTo("test/topic");
        
        Boolean async = (Boolean) ReflectionTestUtils.getField(mqttHandler, "async");
        assertThat(async).isTrue();

        Integer defaultQos = (Integer) ReflectionTestUtils.getField(mqttHandler, "defaultQos");
        assertThat(defaultQos).isEqualTo(1);
    }

    @Test
    void mqttInboundChannel_ShouldReturnMessageChannel() {
        MessageChannel channel = mqttConfig.mqttInboundChannel();
        assertNotNull(channel);
    }

    @Test
    void inbound_ShouldReturnMessageProducerWithCorrectConfig() {
        Object producer = mqttConfig.inbound();
        assertNotNull(producer);
        assertThat(producer).isInstanceOf(MqttPahoMessageDrivenChannelAdapter.class);

        MqttPahoMessageDrivenChannelAdapter adapter = (MqttPahoMessageDrivenChannelAdapter) producer;
        // Verify topics
        String[] topics = adapter.getTopic();
        assertThat(topics).containsExactly("chat/server/incoming/#");
        
        // Verify QoS
        // For MqttPahoMessageDrivenChannelAdapter, the getQos() method returns an int array
        int[] qos = adapter.getQos();
        assertThat(qos).containsExactly(1); // Since we set qos to 1 for all topics
    }

    @Test
    void mqttErrorChannel_ShouldReturnMessageChannel() {
        MessageChannel channel = mqttConfig.mqttErrorChannel();
        assertNotNull(channel);
    }

    @Test
    void mqttErrorHandler_ShouldReturnMessageHandler() {
        MessageHandler handler = mqttConfig.mqttErrorHandler();
        assertNotNull(handler);
    }
}
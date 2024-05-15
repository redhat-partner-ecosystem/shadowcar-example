package mqtt.producer.example;

import mqtt.producer.example.base.MqttProducerBase;

public class MqttProducerApp extends MqttProducerBase {

    public static void main(String[] args) {
        new MqttProducerApp().sendMessage() ;
    }
}

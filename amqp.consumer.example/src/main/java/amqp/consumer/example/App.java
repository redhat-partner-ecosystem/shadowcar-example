package amqp.consumer.example;

import amqp.consumer.example.base.AmqpConsumerBase;

public class App extends AmqpConsumerBase {
    //private static final Logger logger = LoggerFactory.getLogger(App.class);

    public static void main(String[] args) {
        new App().consumeMessages();
    }
}

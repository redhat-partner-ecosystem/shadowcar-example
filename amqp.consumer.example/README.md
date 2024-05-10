# amqp.consumer.example

#### Configuration

* consumer.messaging.host
* consumer.messaging.port

#### Local development

```shell
mvn clean compile
```

```shell
mvn exec:java -Dexec.mainClass=amqp.consumer.example.App
```

```shell
mvn install && java -jar target/amqp.consumer.example-0.1.0-jar-with-dependencies.jar
```

```shell
clear && java -D -jar amqp.consumer.example/target/client-all-0.1.0.jar
```
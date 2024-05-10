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
clear && java -Dconsumer.tenant=DEFAULT_TENANT -jar amqp.consumer.example/target/client-all-0.1.0.jar
```
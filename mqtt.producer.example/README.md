# amqp.consumer.example

## Getting started

```shell
source ../.env
oc project shadowcar-develop

oc create configmap shadowcar-hono-example-config \
    --from-literal=tenant_id=$TENANT_ID \
    --from-literal=default_device_id=$DEVICE_ID

oc get configmap shadowcar-hono-example-trust-store --template="{{index .data \"ca.crt\"}}" -n shadowcar-hono > ca.crt && \
    oc create configmap shadowcar-hono-example-trust-store --from-file=ca.crt && \
    rm ca.crt

oc import-image ubi9/openjdk-17:1.18-4 --from=registry.access.redhat.com/ubi9/openjdk-17:1.18-4 --confirm

oc apply -f src/main/deploy
```

## Local development

```shell
mvn clean compile
```

```shell
mvn exec:java -Dexec.mainClass=mqtt.producer.example.MqttProducerApp
```

```shell
clear && java -Dconsumer.tenant=$TENANT_ID -jar mqtt.producer.example/target/mqtt.producer.example-0.1.0-jar-with-dependencies.jar
```
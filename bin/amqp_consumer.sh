#!/bin/sh
source .env

java -Dconsumer.tenant=${TENANT_ID} -jar amqp.consumer.example/target/client-all-0.1.0.jar


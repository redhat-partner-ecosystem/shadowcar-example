# shadowcar-examples

This repository contains several examples and additional services to showcase the integration of Eclipse Hono into a SDV solution.

## Getting started

### Export the Hono endpoints and certs

```shell
./bin/export_env.sh
```

### Create the Python virtual environment

```shell
python -m venv venv
source venv/bin/activate

pip install -r requirements.txt
```

### Create a tenant and sample device

```shell
source venv/bin/activate
cd setup.python.example

python setup.py
```

## Other examples

##### Push telemetry data via curl/HTTP

```shell
source .env

curl -i -u ${DEVICE_ID}@${TENANT_ID}:${DEVICE_PASSWORD} '--insecure' -H 'Content-Type: application/json' --data-binary '{"temp": 5}' https://${ADAPTER_HTTPS}/telemetry
```

#### References

- https://github.com/eclipse/paho.mqtt.golang

- https://github.com/eclipse/paho.mqtt.java
- https://eclipse.dev/paho/files/javadoc/org/eclipse/paho/client/mqttv3/MqttClient.html#MqttClient-java.lang.String-java.lang.String-

- https://www.baeldung.com/java-mqtt-client

- https://github.com/quarkusio/quarkus-quickstarts
- https://github.com/iakko/mqtt-paho-tls-example

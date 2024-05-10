## MQTT Examples

#### Setup

```shell
python -m venv venv
source venv/bin/activate
pip install -r requirements.txt
```

#### Examples

#### Push data via HTTP

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

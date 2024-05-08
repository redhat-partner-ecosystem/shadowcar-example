## MQTT Examples


#### Prepare the ENV

```shell
oc project shadowcar-hono

echo "export DEVICE_REGISTRY=$(oc get route device-registry-ext --output="jsonpath={.status.ingress[0].host}")" > .env
echo "export ADAPTER_HTTP=$(oc get route shadowcar-hono-adapter-http --output="jsonpath={.status.ingress[0].host}")" >> .env
echo "export ADAPTER_HTTPS=$(oc get route shadowcar-hono-adapter-http-sec --output="jsonpath={.status.ingress[0].host}")" >> .env
echo "export ADAPTER_MQTT=$(oc get route shadowcar-hono-adapter-mqtt --output="jsonpath={.status.ingress[0].host}")" >> .env
echo "export ADAPTER_MQTTS=$(oc get route shadowcar-hono-adapter-mqtt-sec --output="jsonpath={.status.ingress[0].host}")" >> .env
echo "export AMQP_NETWORK=$(oc get route shadowcar-hono-dispatch-router --output="jsonpath={.status.ingress[0].host}")" >> .env

TRUSTSTORE_PATH=certs/truststore.pem
oc get configmap shadowcar-hono-example-trust-store --template="{{index .data \"ca.crt\"}}" > ${TRUSTSTORE_PATH}

echo "export APP_OPTIONS='--amqp -H ${AMQP_NETWORK} -P 80 -u consumer@HONO -p verysecret --ca-file ${TRUSTSTORE_PATH} --disable-hostname-verification'" >> .env
echo "export CURL_OPTIONS='--insecure'" >> .env
echo "export MOSQUITTO_OPTIONS='--cafile ${TRUSTSTORE_PATH} --insecure'" >> .env
```

#### Get the Tenant ID

```shell
source .env
curl -i -X POST ${CURL_OPTIONS} -H "content-type: application/json" --data-binary '{
  "ext": {
    "messaging-type": "amqp"
  }
}' https://${DEVICE_REGISTRY}/v1/tenants
```

{"id":"f73fcb00-9868-42aa-af22-a59f79d5b554"}
```shell
echo "export MY_TENANT=f73fcb00-9868-42aa-af22-a59f79d5b554" >> .env
```

#### Register a device

```shell
source .env
curl -i -X POST ${CURL_OPTIONS} https://${DEVICE_REGISTRY}/v1/devices/${MY_TENANT}
```

{"id":"9ae50869-8ba0-4a51-94a7-da074cac794d"}
```shell
echo "export MY_DEVICE=9ae50869-8ba0-4a51-94a7-da074cac794d" >> .env
```

#### Setting a password

```shell
echo "export MY_PWD=this-is-my-password" >> .env
```

```shell
source .env

curl -i -X PUT ${CURL_OPTIONS} -H "content-type: application/json" --data-binary '[{
  "type": "hashed-password",
  "auth-id": "'${MY_DEVICE}'",
  "secrets": [{
      "pwd-plain": "'${MY_PWD}'"
  }]
}]' https://${DEVICE_REGISTRY}/v1/credentials/${MY_TENANT}/${MY_DEVICE}
```

#### Push data via HTTP

```shell
source .env

curl -i -u ${MY_DEVICE}@${MY_TENANT}:${MY_PWD} ${CURL_OPTIONS} -H 'Content-Type: application/json' --data-binary '{"temp": 5}' https://${ADAPTER_HTTPS}/telemetry
```


#### References

- https://github.com/eclipse/paho.mqtt.golang

- https://github.com/eclipse/paho.mqtt.java
- https://eclipse.dev/paho/files/javadoc/org/eclipse/paho/client/mqttv3/MqttClient.html#MqttClient-java.lang.String-java.lang.String-

- https://www.baeldung.com/java-mqtt-client

- https://github.com/quarkusio/quarkus-quickstarts
- https://github.com/iakko/mqtt-paho-tls-example

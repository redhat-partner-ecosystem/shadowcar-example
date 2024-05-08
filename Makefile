VERSION = 0.1.0

NAMESPACE = shadowcar
TRUSTSTORE_PATH = certs/truststore.pem

.PHONY: env
env:
	rm -f .env
	rm -f ${TRUSTSTORE_PATH}
	oc project ${NAMESPACE}-hono
	echo "export DEVICE_REGISTRY=`oc get route device-registry-ext --output="jsonpath={.status.ingress[0].host}"`" > .env
	echo "export ADAPTER_HTTP=`oc get route shadowcar-hono-adapter-http --output="jsonpath={.status.ingress[0].host}"`" >> .env
	echo "export ADAPTER_HTTPS=`oc get route shadowcar-hono-adapter-http-sec --output="jsonpath={.status.ingress[0].host}"`" >> .env
	echo "export ADAPTER_MQTT=`oc get route shadowcar-hono-adapter-mqtt --output="jsonpath={.status.ingress[0].host}"`" >> .env
	echo "export ADAPTER_MQTTS=`oc get route shadowcar-hono-adapter-mqtt-sec --output="jsonpath={.status.ingress[0].host}"`" >> .env
	echo "export AMQP_NETWORK=`oc get route shadowcar-hono-dispatch-router --output="jsonpath={.status.ingress[0].host}"`" >> .env
	oc get configmap ${NAMESPACE}-hono-example-trust-store --template="{{index .data \"ca.crt\"}}" > ${TRUSTSTORE_PATH}
	echo "export TRUSTSTORE_PATH=${TRUSTSTORE_PATH}" >> .env

.PHONY: build
build: build-telemetry-amqp-consumer

.PHONY: build-telemetry-amqp-consumer
build-telemetry-amqp-consumer:
	rm -rf telemetry.amqp.consumer/target
	cd telemetry.amqp.consumer && mvn install -DskipTest=true 
	mv telemetry.amqp.consumer/target/telemetry.amqp.consumer-${VERSION}-jar-with-dependencies.jar telemetry.amqp.consumer/target/client-all-${VERSION}.jar
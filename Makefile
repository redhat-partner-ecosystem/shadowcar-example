VERSION = 0.1.0

NAMESPACE = shadowcar

.PHONY: build
build: build-hono-telemetry-client

.PHONY: build-hono-telemetry-client
build-hono-telemetry-client:
	cd hono.telemetry.client && mvn install && mv target/shadowcar.hono.telemetry.client-${VERSION}-jar-with-dependencies.jar target/client-all-${VERSION}.jar
	
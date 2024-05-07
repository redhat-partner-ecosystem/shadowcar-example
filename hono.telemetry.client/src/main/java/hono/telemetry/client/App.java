package hono.telemetry.client;

import hono.telemetry.client.base.HonoClientApplicationBase;

public class App extends HonoClientApplicationBase {

    public static void main(final String[] args) {
        new App().consumeData();
    }
}

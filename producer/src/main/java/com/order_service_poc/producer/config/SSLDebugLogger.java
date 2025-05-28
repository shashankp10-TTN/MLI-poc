package com.order_service_poc.producer.config;

public class SSLDebugLogger {
    public static void enableSSLDebugUsingSystemProperties() {
        System.setProperty("javax.net.debug", "ssl");
    }

    public static void disableSSLDebug() {
        System.clearProperty("javax.net.debug");
    }
}

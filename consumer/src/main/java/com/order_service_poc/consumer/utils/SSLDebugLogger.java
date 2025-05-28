package com.order_service_poc.consumer.utils;

public class SSLDebugLogger {
    public static void enableSSLDebugUsingSystemProperties() {
        System.setProperty("javax.net.debug", "ssl");
    }

    public static void disableSSLDebug() {
        System.clearProperty("javax.net.debug");
    }
}

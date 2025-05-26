package com.order_service_poc.consumer;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class ConsumerApplication {

	public static void main(String[] args) {
		System.setProperty("javax.net.debug", "ssl:handshake");
		SpringApplication.run(ConsumerApplication.class, args);
	}

}

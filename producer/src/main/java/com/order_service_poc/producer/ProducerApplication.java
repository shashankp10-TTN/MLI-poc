package com.order_service_poc.producer;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class ProducerApplication {

	public static void main(String[] args) {
		System.setProperty("javax.net.debug", "ssl:handshake");
		SpringApplication.run(ProducerApplication.class, args);
	}

}

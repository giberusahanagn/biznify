package com.biznify.outbound;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class OutboundShipmentServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(OutboundShipmentServiceApplication.class, args);
		System.out.println("Running OutboundShipmentServiceApplication in 8086 port");
	}

}

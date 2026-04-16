package com.example;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class ContactSecurityApplication {

	public static void main(String[] args) {
		SpringApplication.run(ContactSecurityApplication.class, args);
                
                Runtime.getRuntime().addShutdownHook(new Thread());
	}

}

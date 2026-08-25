package com.example;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling 
public class ContactSecurityApplication {

	public static void main(String[] args) {
		SpringApplication.run(ContactSecurityApplication.class, args);
                
                Runtime.getRuntime().addShutdownHook(new Thread());
	}

}

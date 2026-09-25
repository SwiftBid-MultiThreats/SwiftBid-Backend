package com.example.SwiftBid;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class SwiftBidApplication {

	public static void main(String[] args) {
		SpringApplication.run(SwiftBidApplication.class, args);
	}

}

package com.twb.stringtoclass;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@SpringBootApplication
public class StringtoclassApplication {

	public static void main(String[] args) {
		SpringApplication.run(StringtoclassApplication.class, args);
	}

}

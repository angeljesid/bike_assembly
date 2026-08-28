package com.bike_assembly;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class BikeAssemblyApplication {

	public static void main(String[] args) {
		SpringApplication.run(BikeAssemblyApplication.class, args);
	}

}

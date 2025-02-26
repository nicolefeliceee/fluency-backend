package com.skripsi.Fluency;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class FluencyApplication {

	public static void main(String[] args) {
		SpringApplication.run(FluencyApplication.class, args);
	}

}

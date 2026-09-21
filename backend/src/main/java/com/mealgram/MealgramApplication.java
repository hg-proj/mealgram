package com.mealgram;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

// application 진입점

@EnableJpaAuditing
@SpringBootApplication
public class MealgramApplication {

	public static void main(String[] args) {
		SpringApplication.run(MealgramApplication.class, args);
	}

}

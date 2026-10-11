package com.mealgram;

import java.util.TimeZone;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

// application 진입점

@EnableJpaAuditing
@SpringBootApplication
public class MealgramApplication {

	public static final String TIME_ZONE = "Asia/Seoul";

	public static void main(String[] args) {
		fixTimeZone();
		SpringApplication.run(MealgramApplication.class, args);
	}

	static void fixTimeZone() {
		TimeZone.setDefault(TimeZone.getTimeZone(TIME_ZONE));
	}

}

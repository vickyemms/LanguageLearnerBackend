package com.languagelearner.languagelearner;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class LanguagelearnerApplication {

	public static void main(String[] args) {
		SpringApplication.run(LanguagelearnerApplication.class, args);
	}

}

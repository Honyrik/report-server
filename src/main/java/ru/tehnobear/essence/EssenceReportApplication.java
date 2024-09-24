package ru.tehnobear.essence;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;

@SpringBootApplication(exclude = {SecurityAutoConfiguration.class})
@EnableAutoConfiguration
public class EssenceReportApplication {

	public static void main(String[] args) {
		SpringApplication.run(EssenceReportApplication.class, args);
	}

}

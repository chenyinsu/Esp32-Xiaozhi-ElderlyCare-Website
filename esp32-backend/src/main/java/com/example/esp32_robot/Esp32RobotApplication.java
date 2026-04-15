package com.example.esp32_robot;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class Esp32RobotApplication {

	public static void main(String[] args) {
		SpringApplication.run(Esp32RobotApplication.class, args);
	}

}

package htwb.ai.controller;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.EnableEurekaClient;

@SpringBootApplication
@EnableEurekaClient
public class SpringSongsApplication {
	public static void main(String[] args) {
		SpringApplication.run(SpringSongsApplication.class, args);
	}
}

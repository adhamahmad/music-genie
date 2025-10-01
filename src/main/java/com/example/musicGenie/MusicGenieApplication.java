package com.example.musicGenie;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.session.data.redis.config.annotation.web.http.EnableRedisHttpSession;
import org.springframework.stereotype.Service;

@SpringBootApplication
@EnableJpaRepositories("com.example.musicGenie.repos")
@EntityScan("com.example.musicGenie.models")
public class MusicGenieApplication {

	public static void main(String[] args) {
		SpringApplication.run(MusicGenieApplication.class, args);
		System.out.println("Server running on port: " + System.getenv("PORT"));
	}

}

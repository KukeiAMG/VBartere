package com.example.Referral;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.neo4j.repository.config.EnableNeo4jRepositories;

@SpringBootApplication
@EnableNeo4jRepositories
public class ReferralApplication {

	public static void main(String[] args) {
		SpringApplication.run(ReferralApplication.class, args);
	}

}

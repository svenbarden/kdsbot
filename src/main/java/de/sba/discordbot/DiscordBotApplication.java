package de.sba.discordbot;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication(scanBasePackages = "de.sba.discordbot")
@EnableConfigurationProperties
@EnableScheduling
public class DiscordBotApplication {

	public static void main(String[] args) {
		SpringApplication.run(DiscordBotApplication.class, args);
	}
}

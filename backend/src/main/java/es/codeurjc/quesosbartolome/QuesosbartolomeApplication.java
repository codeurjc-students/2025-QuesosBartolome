package es.codeurjc.quesosbartolome;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class QuesosbartolomeApplication {

	public static void main(String[] args) {
		SpringApplication.run(QuesosbartolomeApplication.class, args);
	}
  
}

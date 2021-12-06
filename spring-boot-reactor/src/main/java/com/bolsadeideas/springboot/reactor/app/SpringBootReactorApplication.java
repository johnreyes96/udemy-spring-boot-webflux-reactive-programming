package com.bolsadeideas.springboot.reactor.app;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import reactor.core.publisher.Flux;

@SpringBootApplication
public class SpringBootReactorApplication implements CommandLineRunner {
	
	private static final Logger logger = LoggerFactory.getLogger(SpringBootReactorApplication.class);

	public static void main(String[] args) {
		SpringApplication.run(SpringBootReactorApplication.class, args);
	}

	@Override
	public void run(String... args) throws Exception {
		Flux<String> nombres = Flux.just("Andres", "Pedro", "Maria", "Diego", "Juan")
				.map(nombre -> {
					return nombre.toUpperCase();
				})
				.doOnNext(e -> {
					if (e.isEmpty()) {
						throw new RuntimeException("Nombres no pueden ser vacíos");
					}
					System.out.println(e);
				})
				.map(nombre -> {
					return nombre.toLowerCase();
				});
		nombres.subscribe(logger::info,
				error -> logger.error(error.getMessage()),
				new Runnable() {
					
					@Override
					public void run() {
						logger.info("Ha finalizado la ejecución del observable con éxito!");
					}
				});
	}
}
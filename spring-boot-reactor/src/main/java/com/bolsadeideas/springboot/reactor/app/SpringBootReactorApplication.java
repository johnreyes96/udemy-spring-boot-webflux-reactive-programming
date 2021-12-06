package com.bolsadeideas.springboot.reactor.app;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import com.bolsadeideas.springboot.reactor.app.models.Usuario;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@SpringBootApplication
public class SpringBootReactorApplication implements CommandLineRunner {
	
	private static final Logger logger = LoggerFactory.getLogger(SpringBootReactorApplication.class);

	public static void main(String[] args) {
		SpringApplication.run(SpringBootReactorApplication.class, args);
	}

	@Override
	public void run(String... args) throws Exception {
		ejemploFlatMap();
	}

	private void ejemploFlatMap() {
		List<String> nombresList = new ArrayList<>();
		nombresList.add("Andres Guzman");
		nombresList.add("Pedro Jimenez");
		nombresList.add("Maria Sulivan");
		nombresList.add("Diego Jaramillo");
		nombresList.add("Juan Ramirez");
		nombresList.add("Bruce Lee");
		nombresList.add("Bruce Willis");
		
		Flux.fromIterable(nombresList)
				.map(nombre -> new Usuario(nombre.split(" ")[0].toUpperCase(), nombre.split(" ")[1].toUpperCase()))
				.flatMap(usuario -> {
					if ("bruce".equalsIgnoreCase(usuario.getNombre())) {
						return Mono.just(usuario);
					}
					return Mono.empty();
				})
				.map(usuario -> {
					String nombre = usuario.getNombre().toLowerCase();
					usuario.setNombre(nombre);
					return usuario;
				})
				.subscribe(usuario -> logger.info(usuario.toString()));
	}

	private void ejemploIterable() {
		List<String> nombresList = new ArrayList<>();
		nombresList.add("Andres Guzman");
		nombresList.add("Pedro Jimenez");
		nombresList.add("Maria Sulivan");
		nombresList.add("Diego Jaramillo");
		nombresList.add("Juan Ramirez");
		nombresList.add("Bruce Lee");
		nombresList.add("Bruce Willis");
		
		Flux<String> nombres = Flux.fromIterable(nombresList); 
		Flux<Usuario> usuarios = nombres.map(nombre -> new Usuario(nombre.split(" ")[0].toUpperCase(), nombre.split(" ")[1].toUpperCase()))
				.filter(usuario -> "bruce".equalsIgnoreCase(usuario.getNombre()))
				.doOnNext(usuario -> {
					if (usuario == null) {
						throw new RuntimeException("Nombres no pueden ser vacíos");
					}
					System.out.println(usuario.getNombre().concat(" ").concat(usuario.getApellido()));
				})
				.map(usuario -> {
					String nombre = usuario.getNombre().toLowerCase();
					usuario.setNombre(nombre);
					return usuario;
				});
		usuarios.subscribe(e -> logger.info(e.toString()),
				error -> logger.error(error.getMessage()),
				new Runnable() {
					
					@Override
					public void run() {
						logger.info("Ha finalizado la ejecución del observable con éxito!");
					}
				});
	}
}
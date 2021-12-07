package com.bolsadeideas.springboot.reactor.app;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import com.bolsadeideas.springboot.reactor.app.models.Comentarios;
import com.bolsadeideas.springboot.reactor.app.models.Usuario;
import com.bolsadeideas.springboot.reactor.app.models.UsuarioComentarios;

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
		ejemploDelayElements();
	}

	private void ejemploDelayElements() throws InterruptedException {
		Flux<Integer> rangos = Flux.range(1, 12)
				.delayElements(Duration.ofSeconds(1))
				.doOnNext(elemento -> logger.info(elemento.toString()));
		rangos.blockLast();
	}

	private void ejemploInterval() {
		Flux<Integer> rangos = Flux.range(1, 12);
		Flux<Long> retraso = Flux.interval(Duration.ofSeconds(1));
		rangos.zipWith(retraso, (rango, re) -> rango)
				.doOnNext(elemento -> logger.info(elemento.toString()))
				.blockLast();
	}

	private void ejemploZipWithRangos() {
		Flux<Integer> rangos = Flux.range(1, 5);
		Flux.just(1, 2, 3, 4)
				.map(elemento -> (elemento * 2))
				.zipWith(rangos, (uno, dos) -> String.format("Primer Flux %d, Segundo Flux %d", uno, dos))
				.subscribe(texto -> logger.info(texto));
	}

	private void ejemploUsuarioComentariosZipWithForma2() {
		Mono<Usuario> usuarioMono = Mono.fromCallable(() -> new Usuario("John", "Reyes"));
		Mono<Comentarios> comentariosUsuarioMono = Mono.fromCallable(() -> {
			Comentarios comentarios = new Comentarios();
			comentarios.addComentario("Hola pepe, qué tal!");
			comentarios.addComentario("Mañana voy para la playa!");
			comentarios.addComentario("Estoy tomando el curso de spring con reactor");
			return comentarios;
		});
		
		Mono<UsuarioComentarios> usuarioComentarios = usuarioMono
				.zipWith(comentariosUsuarioMono)
				.map(tupla -> {
					Usuario usuario = tupla.getT1();
					Comentarios comentarios = tupla.getT2();
					return new UsuarioComentarios(usuario, comentarios);
				});
		usuarioComentarios.subscribe(usuarioComentario -> logger.info(usuarioComentario.toString()));
	}

	private void ejemploUsuarioComentariosZipWith() {
		Mono<Usuario> usuarioMono = Mono.fromCallable(() -> new Usuario("John", "Reyes"));
		Mono<Comentarios> comentariosUsuarioMono = Mono.fromCallable(() -> {
			Comentarios comentarios = new Comentarios();
			comentarios.addComentario("Hola pepe, qué tal!");
			comentarios.addComentario("Mañana voy para la playa!");
			comentarios.addComentario("Estoy tomando el curso de spring con reactor");
			return comentarios;
		});
		
		Mono<UsuarioComentarios> usuarioComentarios = usuarioMono.zipWith(comentariosUsuarioMono, (usuario, comentariosUsuario) -> 
				new UsuarioComentarios(usuario, comentariosUsuario));
		usuarioComentarios.subscribe(usuarioComentario -> logger.info(usuarioComentario.toString()));
	}

	private void ejemploUsuarioComentariosFlatMap() {
		Mono<Usuario> usuarioMono = Mono.fromCallable(() -> new Usuario("John", "Reyes"));
		Mono<Comentarios> comentariosUsuarioMono = Mono.fromCallable(() -> {
			Comentarios comentarios = new Comentarios();
			comentarios.addComentario("Hola pepe, qué tal!");
			comentarios.addComentario("Mañana voy para la playa!");
			comentarios.addComentario("Estoy tomando el curso de spring con reactor");
			return comentarios;
		});
		
		Mono<UsuarioComentarios> usuarioComentarios = usuarioMono.flatMap(usuario -> comentariosUsuarioMono.map(comentario -> 
				new UsuarioComentarios(usuario, comentario)));
		usuarioComentarios.subscribe(usuarioComentario -> logger.info(usuarioComentario.toString()));
	}

	private void ejemploCollectList() {
		List<Usuario> usuariosList = new ArrayList<>();
		usuariosList.add(new Usuario("Andres", "Guzman"));
		usuariosList.add(new Usuario("Pedro", "Jimenez"));
		usuariosList.add(new Usuario("Maria", "Sulivan"));
		usuariosList.add(new Usuario("Diego", "Jaramillo"));
		usuariosList.add(new Usuario("Juan", "Ramirez"));
		usuariosList.add(new Usuario("Bruce", "Lee"));
		usuariosList.add(new Usuario("Bruce", "Willis"));
		
		Flux.fromIterable(usuariosList)
				.collectList()
				.subscribe(lista -> {
					lista.forEach(item -> logger.info(item.toString()));
				});
	}

	private void ejemploToString() {
		List<Usuario> usuariosList = new ArrayList<>();
		usuariosList.add(new Usuario("Andres", "Guzman"));
		usuariosList.add(new Usuario("Pedro", "Jimenez"));
		usuariosList.add(new Usuario("Maria", "Sulivan"));
		usuariosList.add(new Usuario("Diego", "Jaramillo"));
		usuariosList.add(new Usuario("Juan", "Ramirez"));
		usuariosList.add(new Usuario("Bruce", "Lee"));
		usuariosList.add(new Usuario("Bruce", "Willis"));
		
		Flux.fromIterable(usuariosList)
				.map(usuario -> usuario.getNombre().toUpperCase().concat(" ").concat(usuario.getApellido().toUpperCase()))
				.flatMap(nombre -> {
					if (nombre.contains("bruce".toUpperCase())) {
						return Mono.just(nombre);
					}
					return Mono.empty();
				})
				.map(nombre -> {
					return nombre.toLowerCase();
				})
				.subscribe(nombre -> logger.info(nombre.toString()));
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
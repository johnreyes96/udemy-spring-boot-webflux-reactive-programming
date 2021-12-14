package com.bolsadeideas.springboot.webflux.client.app.models.services;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import com.bolsadeideas.springboot.webflux.client.app.models.Producto;

import static org.springframework.http.MediaType.APPLICATION_JSON;;

public class ProductoServiceImpl implements IProductoService {
	
	@Autowired
	private WebClient client;

	@Override
	public Flux<Producto> findAll() {
		return client.get()
				.accept(APPLICATION_JSON)
				.retrieve()
				.bodyToFlux(Producto.class);
	}

	@Override
	public Mono<Producto> findById(String id) {
		Map<String, Object> params = new HashMap<>();
		params.put("id", id);
		return client.get()
				.uri("/{id}", params)
				.accept(APPLICATION_JSON)
				.retrieve()
				.bodyToMono(Producto.class);
	}

	@Override
	public Mono<Producto> save(Producto producto) {
		return client.post()
				.contentType(APPLICATION_JSON)
				.accept(APPLICATION_JSON)
				.bodyValue(producto)
				.retrieve()
				.bodyToMono(Producto.class);
	}

	@Override
	public Mono<Producto> update(Producto producto, String id) {
		return client.put()
				.uri("/{id}", Collections.singletonMap("id", id))
				.contentType(APPLICATION_JSON)
				.accept(APPLICATION_JSON)
				.bodyValue(producto)
				.retrieve()
				.bodyToMono(Producto.class);
	}

	@Override
	public Mono<Void> delete(String id) {
		return client.delete()
				.uri("/{id}", Collections.singletonMap("id", id))
				.retrieve()
				.bodyToMono(Void.class);
	}
}
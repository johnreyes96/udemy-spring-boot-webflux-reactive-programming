package com.bolsadeideas.springboot.webflux.client.app.models.services;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.WebClient;

import com.bolsadeideas.springboot.webflux.client.app.models.Producto;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public class ProductoServiceImpl implements IProductoService {
	
	@Autowired
	private WebClient client;

	@Override
	public Flux<Producto> findAll() {
		return client.get()
				.accept(MediaType.APPLICATION_JSON)
				.retrieve()
				.bodyToFlux(Producto.class);
	}

	@Override
	public Mono<Producto> findById(String id) {
		Map<String, Object> params = new HashMap<>();
		params.put("id", id);
		return client.get()
				.uri("/{id}", params)
				.accept(MediaType.APPLICATION_JSON)
				.retrieve()
				.bodyToMono(Producto.class);
	}

	@Override
	public Mono<Producto> save(Producto producto) {
		return null;
	}

	@Override
	public Mono<Producto> update(Producto producto, String id) {
		return null;
	}

	@Override
	public Mono<Void> delete(String id) {
		return null;
	}
}
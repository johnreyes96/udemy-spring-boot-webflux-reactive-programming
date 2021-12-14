package com.bolsadeideas.springboot.webflux.client.app.handler;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;

import reactor.core.publisher.Mono;

import com.bolsadeideas.springboot.webflux.client.app.models.Producto;
import com.bolsadeideas.springboot.webflux.client.app.models.services.IProductoService;

import static org.springframework.http.MediaType.APPLICATION_JSON;

import java.net.URI;
import java.util.Date;;

@Component
public class ProductoHandler {

	@Autowired
	private IProductoService service;
	
	public Mono<ServerResponse> listar(ServerRequest request) {
		return ServerResponse.ok()
				.contentType(APPLICATION_JSON)
				.body(service.findAll(), Producto.class);
	}
	
	public Mono<ServerResponse> ver(ServerRequest request) {
		String id = request.pathVariable("id");
		return service.findById(id).flatMap(producto -> ServerResponse
				.ok()
				.contentType(APPLICATION_JSON)
				.bodyValue(producto))
				.switchIfEmpty(ServerResponse.notFound().build());
	}

	public Mono<ServerResponse> crear(ServerRequest request) {
		Mono<Producto> producto = request.bodyToMono(Producto.class);
		return producto.flatMap(product -> {
			if (product.getCreateAt() == null) {
				product.setCreateAt(new Date());
			}
			return service.save(product);
		}).flatMap(product -> ServerResponse
				.created(URI.create("/api/client/".concat(product.getId())))
				.contentType(APPLICATION_JSON)
				.bodyValue(product));
	}

	public Mono<ServerResponse> editar(ServerRequest request) {
		Mono<Producto> producto = request.bodyToMono(Producto.class);
		String id = request.pathVariable("id");
		
		return producto.flatMap(product -> ServerResponse
				.created(URI.create("/api/client/".concat(id)))
				.contentType(MediaType.APPLICATION_JSON)
				.body(service.update(product, id), Producto.class));
	}

	public Mono<ServerResponse> eliminar(ServerRequest request) {
		String id = request.pathVariable("id");
		return service.delete(id).then(ServerResponse.noContent().build());
	}
}
package com.bolsadeideas.springboot.webflux.client.app.handler;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;
import java.net.URI;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import com.bolsadeideas.springboot.webflux.client.app.models.Producto;
import com.bolsadeideas.springboot.webflux.client.app.models.services.IProductoService;

import static org.springframework.http.MediaType.APPLICATION_JSON;

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
				.switchIfEmpty(ServerResponse.notFound().build())
				.onErrorResume(error -> {
					WebClientResponseException errorResponse = (WebClientResponseException) error;
					if (errorResponse.getStatusCode() == HttpStatus.NOT_FOUND) {
						Map<String, Object> body = new HashMap<>();
						body.put("error", "No existe el producto: ".concat(errorResponse.getMessage()));
						body.put("timestamp", new Date());
						body.put("status", errorResponse.getStatusCode().value());
						return ServerResponse.status(HttpStatus.NOT_FOUND).bodyValue(body);
					}
					return Mono.error(errorResponse);
				});
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
				.bodyValue(product))
		.onErrorResume(error -> {
			WebClientResponseException errorResponse = (WebClientResponseException) error;
			if (errorResponse.getStatusCode() == HttpStatus.BAD_REQUEST) {
				return ServerResponse.badRequest()
						.contentType(APPLICATION_JSON)
						.bodyValue(errorResponse.getResponseBodyAsString());
			}
			return Mono.error(errorResponse);
		});
	}

	public Mono<ServerResponse> editar(ServerRequest request) {
		Mono<Producto> producto = request.bodyToMono(Producto.class);
		String id = request.pathVariable("id");
		
		return producto.flatMap(product -> service.update(product, id))
				.flatMap(product -> ServerResponse
				.created(URI.create("/api/client/".concat(id)))
				.contentType(MediaType.APPLICATION_JSON)
				.bodyValue(product))
				.onErrorResume(error -> {
					WebClientResponseException errorResponse = (WebClientResponseException) error;
					if (errorResponse.getStatusCode() == HttpStatus.NOT_FOUND) {
						return ServerResponse.notFound().build();
					}
					return Mono.error(errorResponse);
				});
	}

	public Mono<ServerResponse> eliminar(ServerRequest request) {
		String id = request.pathVariable("id");
		return service.delete(id).then(ServerResponse.noContent().build())
				.onErrorResume(error -> {
					WebClientResponseException errorResponse = (WebClientResponseException) error;
					if (errorResponse.getStatusCode() == HttpStatus.NOT_FOUND) {
						return ServerResponse.notFound().build();
					}
					return Mono.error(errorResponse);
				});
	}

	public Mono<ServerResponse> upload(ServerRequest request) {
		String id = request.pathVariable("id");
		return request.multipartData().map(multipart -> multipart.toSingleValueMap().get("file"))
				.cast(FilePart.class)
				.flatMap(file -> service.upload(file, id))
				.flatMap(producto -> ServerResponse.created(URI.create("/api/client/".concat(producto.getId())))
						.contentType(MediaType.APPLICATION_JSON)
						.bodyValue(producto))
				.onErrorResume(error -> {
					WebClientResponseException errorResponse = (WebClientResponseException) error;
					if (errorResponse.getStatusCode() == HttpStatus.NOT_FOUND) {
						return ServerResponse.notFound().build();
					}
					return Mono.error(errorResponse);
				});
	}
}
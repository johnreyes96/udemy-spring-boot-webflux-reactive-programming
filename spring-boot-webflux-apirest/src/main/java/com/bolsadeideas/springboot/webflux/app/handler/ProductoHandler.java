package com.bolsadeideas.springboot.webflux.app.handler;

import java.net.URI;
import java.util.Date;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;

import com.bolsadeideas.springboot.webflux.app.models.documents.Producto;
import com.bolsadeideas.springboot.webflux.app.models.services.ProductoService;

import reactor.core.publisher.Mono;

@Component
public class ProductoHandler {
	
	@Autowired
	private ProductoService service;

	public Mono<ServerResponse> listar(ServerRequest request) {
		return ServerResponse.ok()
				.contentType(MediaType.APPLICATION_JSON)
				.body(service.findAll(), Producto.class);
	}

	public Mono<ServerResponse> ver(ServerRequest request) {
		String id = request.pathVariable("id");
		return service.findById(id).flatMap(producto -> ServerResponse
				.ok()
				.contentType(MediaType.APPLICATION_JSON)
				.bodyValue(producto)
				.switchIfEmpty(ServerResponse.notFound().build()));
	}

	public Mono<ServerResponse> crear(ServerRequest request) {
		Mono<Producto> producto = request.bodyToMono(Producto.class);
		return producto.flatMap(product -> {
			if (product.getCreateAt() == null) {
				product.setCreateAt(new Date());
			}
			return service.save(product);
		}).flatMap(product -> ServerResponse
				.created(URI.create("/api/v2/productos/".concat(product.getId())))
				.contentType(MediaType.APPLICATION_JSON)
				.bodyValue(product));
	}

	public Mono<ServerResponse> editar(ServerRequest request) {
		Mono<Producto> productoRequest = request.bodyToMono(Producto.class);
		String id = request.pathVariable("id");
		Mono<Producto> productoPersisted = service.findById(id);
		
		return productoPersisted.zipWith(productoRequest, (productPersisted, productRequest) -> {
			productPersisted.setNombre(productRequest.getNombre());
			productPersisted.setPrecio(productRequest.getPrecio());
			productPersisted.setCategoria(productRequest.getCategoria());
			return productPersisted;
		}).flatMap(product -> ServerResponse
				.created(URI.create("/api/v2/productos/".concat(product.getId())))
				.contentType(MediaType.APPLICATION_JSON)
				.body(service.save(product), Producto.class))
		.switchIfEmpty(ServerResponse.notFound().build());
	}

	public Mono<ServerResponse> eliminar(ServerRequest request) {
		String id = request.pathVariable("id");
		Mono<Producto> producto = service.findById(id);
		return producto.flatMap(product -> service.delete(product).then(ServerResponse.noContent().build()))
				.switchIfEmpty(ServerResponse.notFound().build());
	}
}
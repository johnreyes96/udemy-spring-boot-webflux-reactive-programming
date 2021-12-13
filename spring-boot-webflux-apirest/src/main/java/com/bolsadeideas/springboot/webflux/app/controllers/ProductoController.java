package com.bolsadeideas.springboot.webflux.app.controllers;

import java.net.URI;
import java.util.Date;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.bolsadeideas.springboot.webflux.app.models.documents.Producto;
import com.bolsadeideas.springboot.webflux.app.models.services.ProductoService;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/productos")
public class ProductoController {

	@Autowired
	private ProductoService service;
	
	@GetMapping
	public Mono<ResponseEntity<Flux<Producto>>> lista() {
		return Mono.just(ResponseEntity.ok()
				.contentType(MediaType.APPLICATION_JSON)
				.body(service.findAll()));
	}
	
	@GetMapping("/{id}")
	public Mono<ResponseEntity<Producto>> ver(@PathVariable String id) {
		return service.findById(id).map(producto -> ResponseEntity.ok()
				.contentType(MediaType.APPLICATION_JSON)
				.body(producto))
				.defaultIfEmpty(ResponseEntity.notFound().build());
	}
	
	@PostMapping
	public Mono<ResponseEntity<Producto>> crear(@RequestBody Producto producto) {
		if (producto.getCreateAt() == null) {
			producto.setCreateAt(new Date());
		}
		
		return service.save(producto).map(product -> ResponseEntity
				.created(URI.create("/api/productos/".concat(product.getId())))
				.contentType(MediaType.APPLICATION_JSON)
				.body(product));
	}
	
	@PutMapping("/{id}")
	public Mono<ResponseEntity<Producto>> editar(@RequestBody Producto producto, @PathVariable String id) {
		return service.findById(id).flatMap(product -> {
			product.setNombre(producto.getNombre());
			product.setPrecio(producto.getPrecio());
			product.setCategoria(producto.getCategoria());
			return service.save(product);
		}).map(product -> ResponseEntity.created(URI.create("/api/productos/".concat(product.getId())))
				.contentType(MediaType.APPLICATION_JSON)
				.body(product))
		.defaultIfEmpty(ResponseEntity.notFound().build());
	}
}
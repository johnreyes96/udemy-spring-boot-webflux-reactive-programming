package com.bolsadeideas.springboot.webflux.app.controllers;

import java.io.File;
import java.net.URI;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.support.WebExchangeBindException;

import com.bolsadeideas.springboot.webflux.app.models.documents.Producto;
import com.bolsadeideas.springboot.webflux.app.models.services.ProductoService;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/productos")
public class ProductoController {

	@Autowired
	private ProductoService service;
	
	@Value("${config.uploads.path}")
	private String path;
	
	@PostMapping("/v2")
	public Mono<ResponseEntity<Producto>> crearConFoto(Producto producto, @RequestPart FilePart file) {
		if (producto.getCreateAt() == null) {
			producto.setCreateAt(new Date());
		}
		
		producto.setFoto(UUID.randomUUID().toString() + "-" + file.filename()
		.replace(" ", "")
		.replace(":", "")
		.replace("\\", ""));
		
		return file.transferTo(new File(path + producto.getFoto()))
				.then(service.save(producto))
				.map(product -> ResponseEntity
				.created(URI.create("/api/productos/".concat(product.getId())))
				.contentType(MediaType.APPLICATION_JSON)
				.body(product));
	}
	
	@PostMapping("/upload/{id}")
	public Mono<ResponseEntity<Producto>> upload(@PathVariable String id, @RequestPart FilePart file) {
		return service.findById(id).flatMap(product -> {
			product.setFoto(UUID.randomUUID().toString() + "-" + file.filename()
			.replace(" ", "")
			.replace(":", "")
			.replace("\\", ""));
			return file.transferTo(new File(path + product.getFoto())).then(service.save(product));
		}).map(product -> ResponseEntity.ok(product))
		.defaultIfEmpty(ResponseEntity.notFound().build());
	}
	
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
	public Mono<ResponseEntity<Map<String, Object>>> crear(@Valid @RequestBody Mono<Producto> monoProducto) {
		Map<String, Object> respuesta = new HashMap<>();
		return monoProducto.flatMap(producto -> {
			if (producto.getCreateAt() == null) {
				producto.setCreateAt(new Date());
			}
			
			return service.save(producto).map(product -> {
				respuesta.put("producto", product);
				respuesta.put("mensaje", "Producto creado con éxito");
				respuesta.put("timestamp", new Date());
				return ResponseEntity
					.created(URI.create("/api/productos/".concat(product.getId())))
					.contentType(MediaType.APPLICATION_JSON)
					.body(respuesta);
			});
		}).onErrorResume(throwable -> {
			return Mono.just(throwable).cast(WebExchangeBindException.class)
					.flatMap(error -> Mono.just(error.getFieldErrors()))
					.flatMapMany(Flux::fromIterable)
					.map(fieldError -> "El campo " + fieldError.getField() + " " + fieldError.getDefaultMessage())
					.collectList()
					.flatMap(lista -> {
						respuesta.put("errors", lista);
						respuesta.put("timestamp", new Date());
						respuesta.put("status", HttpStatus.BAD_REQUEST.value());
						return Mono.just(ResponseEntity.badRequest().body(respuesta));
					});
		});
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
	
	@DeleteMapping("/{id}")
	public Mono<ResponseEntity<Void>> eliminar(@PathVariable String id) {
		return service.findById(id).flatMap(product -> {
			return service.delete(product).then(Mono.just(new ResponseEntity<Void>(HttpStatus.NO_CONTENT)));
		}).defaultIfEmpty(new ResponseEntity<Void>(HttpStatus.NOT_FOUND));
	}
}
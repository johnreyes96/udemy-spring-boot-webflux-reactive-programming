package com.bolsadeideas.springboot.webflux.app.controllers;

import java.io.File;
import java.net.URI;
import java.nio.file.Path;
import java.nio.file.Paths;
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
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import com.bolsadeideas.springboot.webflux.app.constants.RouteEnum;
import com.bolsadeideas.springboot.webflux.app.models.documents.Product;
import com.bolsadeideas.springboot.webflux.app.models.services.ProductService;

@RestController
@RequestMapping("/api/productos")
public class ProductController {

	@Autowired
	private ProductService productService;

	@Value("${config.uploads.path}")
	private String path;

	@PostMapping("/v2")
	public Mono<ResponseEntity<Product>> createWithPhoto(Product product, @RequestPart FilePart file) {
		if (product.getCreateAt() == null)
			product.setCreateAt(new Date());

		product.setPhotoWithFormattedName(UUID.randomUUID().toString(), file.filename());
		return file.transferTo(new File(path + product.getPhoto()))
				.then(productService.save(product))
				.map(productPersisted -> ResponseEntity
						.created(URI.create(RouteEnum.API_PRODUCTS.getRoute().concat(productPersisted.getId())))
						.contentType(MediaType.APPLICATION_JSON)
						.body(productPersisted));
	}

	@PostMapping("/upload/{id}")
	public Mono<ResponseEntity<Product>> upload(@PathVariable String id, @RequestPart FilePart file) {
		return productService.findById(id)
				.flatMap(product -> {
					product.setPhotoWithFormattedName(UUID.randomUUID().toString(), file.filename());
					return file.transferTo(new File(path + product.getPhoto()))
							.then(productService.save(product));
				}).map(product -> ResponseEntity.ok(product))
				.defaultIfEmpty(ResponseEntity.notFound().build());
	}

	@GetMapping
	public Mono<ResponseEntity<Flux<Product>>> list() {
		return Mono.just(ResponseEntity.ok()
				.contentType(MediaType.APPLICATION_JSON)
				.body(productService.findAll()));
	}

	@GetMapping("/{id}")
	public Mono<ResponseEntity<Product>> view(@PathVariable String id) {
		return productService.findById(id)
				.map(product -> ResponseEntity.ok()
						.contentType(MediaType.APPLICATION_JSON)
						.body(product))
				.defaultIfEmpty(ResponseEntity.notFound().build());
	}

	@PostMapping
	public Mono<ResponseEntity<Map<String, Object>>> create(@Valid @RequestBody Mono<Product> monoProducto) {
		Map<String, Object> response = new HashMap<>();
		return monoProducto.flatMap(product -> {
			if (product.getCreateAt() == null)
				product.setCreateAt(new Date());

			return productService.save(product)
					.map(productPersisted -> {
						response.put("product", productPersisted);
						response.put("message", "Product created successfully");
						response.put("timestamp", new Date());
						return ResponseEntity
								.created(URI.create(RouteEnum.API_PRODUCTS.getRoute()
										.concat(productPersisted.getId())))
								.contentType(MediaType.APPLICATION_JSON)
								.body(response);
					});
		}).onErrorResume(throwable -> {
			return Mono.just(throwable)
					.cast(WebExchangeBindException.class)
					.flatMap(error -> Mono.just(error.getFieldErrors()))
					.flatMapMany(Flux::fromIterable)
					.map(fieldError -> "The field " + fieldError.getField() + " " + fieldError.getDefaultMessage())
					.collectList()
					.flatMap(list -> {
						response.put("errors", list);
						response.put("timestamp", new Date());
						response.put("status", HttpStatus.BAD_REQUEST.value());
						return Mono.just(ResponseEntity.badRequest()
								.body(response));
					});
		});
	}

	@PutMapping("/{id}")
	public Mono<ResponseEntity<Product>> edit(@RequestBody Product product, @PathVariable String id) {
		return productService.findById(id)
				.flatMap(productPersisted -> {
					productPersisted.setName(product.getName());
					productPersisted.setPrice(product.getPrice());
					productPersisted.setCategory(product.getCategory());
					return productService.save(productPersisted);
				}).map(productPersisted -> ResponseEntity
						.created(URI.create(RouteEnum.API_PRODUCTS.getRoute().concat(productPersisted.getId())))
						.contentType(MediaType.APPLICATION_JSON)
						.body(productPersisted))
				.defaultIfEmpty(ResponseEntity.notFound().build());
	}

	@DeleteMapping("/{id}")
	public Mono<ResponseEntity<Void>> delete(@PathVariable String id) {
		return productService.findById(id)
				.flatMap(product -> {
					return productService.delete(product)
							.then(Mono.just(new ResponseEntity<Void>(HttpStatus.NO_CONTENT)));
				}).defaultIfEmpty(new ResponseEntity<Void>(HttpStatus.NOT_FOUND));
	}

	protected Path getPath() {
		return Paths.get(path);
	}
}

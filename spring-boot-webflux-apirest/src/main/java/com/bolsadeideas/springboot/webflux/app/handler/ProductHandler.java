package com.bolsadeideas.springboot.webflux.app.handler;

import java.io.File;
import java.net.URI;
import java.util.Date;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.http.codec.multipart.FormFieldPart;
import org.springframework.stereotype.Component;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import com.bolsadeideas.springboot.webflux.app.models.documents.Category;
import com.bolsadeideas.springboot.webflux.app.models.documents.Product;
import com.bolsadeideas.springboot.webflux.app.models.services.ProductService;

@Component
public class ProductHandler {

	@Autowired
	private ProductService service;

	@Value("${config.uploads.path}")
	private String path;

	@Autowired
	private Validator validator;

	public Mono<ServerResponse> crearConFoto(ServerRequest request) {
		Mono<Product> productMono = request.multipartData().map(multipart -> {
			FormFieldPart name = (FormFieldPart) multipart.toSingleValueMap().get("nombre");
			FormFieldPart price = (FormFieldPart) multipart.toSingleValueMap().get("precio");
			FormFieldPart categoryId = (FormFieldPart) multipart.toSingleValueMap().get("categoria.id");
			FormFieldPart categoryName = (FormFieldPart) multipart.toSingleValueMap().get("categoria.nombre");
			Category category = new Category(categoryName.value());
			category.setId(categoryId.value());
			return new Product(name.value(), Double.parseDouble(price.value()), category);
		});

		return request.multipartData()
				.map(multipart -> multipart.toSingleValueMap().get("file"))
				.cast(FilePart.class)
				.flatMap(file -> productMono.flatMap(product -> {
					product.setPhotoWithFormattedName(UUID.randomUUID().toString(), file.filename());
					product.setCreateAt(new Date());
					return file.transferTo(new File(path + product.getPhoto()))
							.then(service.save(product));
				})).flatMap(product -> ServerResponse.created(URI.create("/api/v2/productos/".concat(product.getId())))
						.contentType(MediaType.APPLICATION_JSON)
						.bodyValue(product));
	}

	public Mono<ServerResponse> upload(ServerRequest request) {
		String id = request.pathVariable("id");
		return request.multipartData()
				.map(multipart -> multipart.toSingleValueMap().get("file"))
				.cast(FilePart.class)
				.flatMap(file -> service.findById(id).flatMap(product -> {
					product.setPhotoWithFormattedName(UUID.randomUUID().toString(), file.filename());
					return file.transferTo(new File(path + product.getPhoto()))
							.then(service.save(product));
				}))
				.flatMap(product -> ServerResponse.created(URI.create("/api/v2/productos/".concat(product.getId())))
						.contentType(MediaType.APPLICATION_JSON)
						.bodyValue(product))
				.switchIfEmpty(ServerResponse.notFound().build());
	}

	public Mono<ServerResponse> listar(ServerRequest request) {
		return ServerResponse.ok()
				.contentType(MediaType.APPLICATION_JSON)
				.body(service.findAll(), Product.class);
	}

	public Mono<ServerResponse> ver(ServerRequest request) {
		String id = request.pathVariable("id");
		return service.findById(id).flatMap(producto -> ServerResponse.ok()
				.contentType(MediaType.APPLICATION_JSON)
				.bodyValue(producto)
				.switchIfEmpty(ServerResponse.notFound().build()));
	}

	public Mono<ServerResponse> crear(ServerRequest request) {
		Mono<Product> productMono = request.bodyToMono(Product.class);
		return productMono.flatMap(product -> {
			Errors errors = new BeanPropertyBindingResult(product, Product.class.getName());
			validator.validate(product, errors);
			if (errors.hasErrors()) {
				return Flux.fromIterable(errors.getFieldErrors())
						.map(fieldError -> "El campo " + fieldError.getField() + " " + fieldError.getDefaultMessage())
						.collectList()
						.flatMap(list -> ServerResponse.badRequest()
								.bodyValue(list));
			} else {
				if (product.getCreateAt() == null)
					product.setCreateAt(new Date());
				
				return service.save(product)
						.flatMap(productPersisted -> ServerResponse
								.created(URI.create("/api/v2/productos/".concat(productPersisted.getId())))
								.contentType(MediaType.APPLICATION_JSON)
								.bodyValue(productPersisted));
			}
		});
	}

	public Mono<ServerResponse> editar(ServerRequest request) {
		Mono<Product> productRequestMono = request.bodyToMono(Product.class);
		String id = request.pathVariable("id");
		Mono<Product> productPersistedMono = service.findById(id);

		return productPersistedMono.zipWith(productRequestMono, (productPersisted, productRequest) -> {
			productPersisted.setName(productRequest.getName());
			productPersisted.setPrice(productRequest.getPrice());
			productPersisted.setCategory(productRequest.getCategory());
			return productPersisted;
		}).flatMap(product -> ServerResponse.created(URI.create("/api/v2/productos/".concat(product.getId())))
				.contentType(MediaType.APPLICATION_JSON)
				.body(service.save(product), Product.class))
		.switchIfEmpty(ServerResponse.notFound().build());
	}

	public Mono<ServerResponse> eliminar(ServerRequest request) {
		String id = request.pathVariable("id");
		Mono<Product> productPersisted = service.findById(id);
		return productPersisted.flatMap(product -> service.delete(product)
				.then(ServerResponse.noContent().build()))
				.switchIfEmpty(ServerResponse.notFound().build());
	}
}

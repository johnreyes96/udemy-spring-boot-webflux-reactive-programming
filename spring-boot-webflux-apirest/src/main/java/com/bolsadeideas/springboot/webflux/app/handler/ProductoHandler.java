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

import com.bolsadeideas.springboot.webflux.app.models.documents.Categoria;
import com.bolsadeideas.springboot.webflux.app.models.documents.Producto;
import com.bolsadeideas.springboot.webflux.app.models.services.ProductoService;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Component
public class ProductoHandler {
	
	@Autowired
	private ProductoService service;
	
	@Value("${config.uploads.path}")
	private String path;
	
	@Autowired
	private Validator validator;

	public Mono<ServerResponse> crearConFoto(ServerRequest request) {
		Mono<Producto> producto = request.multipartData().map(multipart -> {
			FormFieldPart nombre = (FormFieldPart) multipart.toSingleValueMap().get("nombre");
			FormFieldPart precio = (FormFieldPart) multipart.toSingleValueMap().get("precio");
			FormFieldPart categoriaId = (FormFieldPart) multipart.toSingleValueMap().get("categoria.id");
			FormFieldPart categoriaNombre = (FormFieldPart) multipart.toSingleValueMap().get("categoria.nombre");
			Categoria categoria = new Categoria(categoriaNombre.value());
			categoria.setId(categoriaId.value());
			return new Producto(nombre.value(), Double.parseDouble(precio.value()), categoria);
		});
		
		return request.multipartData().map(multipart -> multipart.toSingleValueMap().get("file"))
				.cast(FilePart.class)
				.flatMap(file -> producto
						.flatMap(product -> {
							product.setFoto(UUID.randomUUID().toString() + "-" + file.filename()
							.replace(" ", "-")
							.replace(":", "")
							.replace("\\", ""));
							product.setCreateAt(new Date());
							return file.transferTo(new File(path + product.getFoto())).then(service.save(product));
						}))
				.flatMap(product -> ServerResponse.created(URI.create("/api/v2/productos/".concat(product.getId())))
						.contentType(MediaType.APPLICATION_JSON)
						.bodyValue(product));
	}

	public Mono<ServerResponse> upload(ServerRequest request) {
		String id = request.pathVariable("id");
		return request.multipartData().map(multipart -> multipart.toSingleValueMap().get("file"))
				.cast(FilePart.class)
				.flatMap(file -> service.findById(id)
						.flatMap(producto -> {
							producto.setFoto(UUID.randomUUID().toString() + "-" + file.filename()
							.replace(" ", "-")
							.replace(":", "")
							.replace("\\", ""));
							return file.transferTo(new File(path + producto.getFoto())).then(service.save(producto));
						}))
				.flatMap(producto -> ServerResponse.created(URI.create("/api/v2/productos/".concat(producto.getId())))
						.contentType(MediaType.APPLICATION_JSON)
						.bodyValue(producto))
				.switchIfEmpty(ServerResponse.notFound().build());
	}

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
			Errors errors = new BeanPropertyBindingResult(product, Producto.class.getName());
			validator.validate(product, errors);
			if (errors.hasErrors()) {
				return Flux.fromIterable(errors.getFieldErrors())
						.map(fieldError -> "El campo " + fieldError.getField() + " " + fieldError.getDefaultMessage())
						.collectList()
						.flatMap(list -> ServerResponse.badRequest().bodyValue(list));
			} else {				
				if (product.getCreateAt() == null) {
					product.setCreateAt(new Date());
				}
				return service.save(product).flatMap(productPersisted -> ServerResponse
						.created(URI.create("/api/v2/productos/".concat(productPersisted.getId())))
						.contentType(MediaType.APPLICATION_JSON)
						.bodyValue(productPersisted));
			}
		});
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
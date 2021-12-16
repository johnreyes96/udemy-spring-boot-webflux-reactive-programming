package com.bolsadeideas.springboot.webflux.app.controllers;

import java.io.File;
import java.time.Duration;
import java.util.Date;
import java.util.UUID;

import javax.validation.Valid;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.SessionAttributes;
import org.springframework.web.bind.support.SessionStatus;
import org.thymeleaf.spring5.context.webflux.ReactiveDataDriverContextVariable;

import com.bolsadeideas.springboot.webflux.app.models.documents.Categoria;
import com.bolsadeideas.springboot.webflux.app.models.documents.Producto;
import com.bolsadeideas.springboot.webflux.app.models.services.IProductoService;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@SessionAttributes("producto")
@Controller
public class ProductoController {

	@Autowired
	private IProductoService service;
	
	@Value("${config.uploads.path}")
	private String path;
	
	private static final Logger logger = LoggerFactory.getLogger(ProductoController.class);
	
	@ModelAttribute("categorias")
	public Flux<Categoria> cagetorias() {
		return service.findAllCategoria();
	}
	
	@GetMapping({"/", "/listar"})
	public String listar(Model model) {
		Flux<Producto> productos = service.findAllConNombreUpperCase();
		productos.subscribe(producto -> logger.info(producto.getNombre()));
		model.addAttribute("productos", productos);
		model.addAttribute("titulo", "Listado de productos");
		return "listar";
	}
	
	@GetMapping("/form")
	public Mono<String> crear(Model model) {
		model.addAttribute("producto", new Producto());
		model.addAttribute("titulo", "Formulario de producto");
		model.addAttribute("boton", "Crear");
		return Mono.just("form");
	}
	
	@GetMapping("/form/{id}")
	public Mono<String> editar(@PathVariable String id, Model model) {
		Mono<Producto> producto = service.findById(id)
				.doOnNext(product -> {
					logger.info("Producto: " + product.getNombre());
				}).defaultIfEmpty(new Producto());
		model.addAttribute("boton", "Editar");
		model.addAttribute("titulo", "Editar Producto");
		model.addAttribute("producto", producto);
		return Mono.just("form");
	}
	
	@GetMapping("/form-v2/{id}")
	public Mono<String> editarV2(@PathVariable String id, Model model) {
		return service.findById(id)
				.doOnNext(producto -> {
					logger.info("Producto: " + producto.getNombre());
					model.addAttribute("boton", "Editar");
					model.addAttribute("titulo", "Editar Producto");
					model.addAttribute("producto", producto);
				}).defaultIfEmpty(new Producto())
				.flatMap(producto -> {
					if (producto.getId() == null) {
						return Mono.error(new InterruptedException("No existe el producto"));
					}
					return Mono.just(producto);
				})
				.then(Mono.just("form"))
				.onErrorResume(exception -> Mono.just("redirect:/listar?error=no+existe+el+producto"));
	}
	
	@PostMapping("/form")
	public Mono<String> guardar(@Valid Producto producto, BindingResult result, Model model, @RequestPart FilePart file, SessionStatus status) {
		//TODO: Don't run the .hasErrors
		if (result.hasErrors()) {
			model.addAttribute("titulo", "Errores en formulario producto");
			model.addAttribute("botton", "Guardar");
			return Mono.just("form");
		} else {			
			status.setComplete();
			Mono<Categoria> categoria = service.findCategoriaById(producto.getCategoria().getId());
			return categoria.flatMap(category -> {
				if (producto.getCreateAt() == null) {
					producto.setCreateAt(new Date());
				}
				if (!file.filename().isEmpty()) {
					producto.setFoto(UUID.randomUUID().toString() + "-" + file.filename()
						.replace(" ", "")
						.replace(":", "")
						.replace("\\", "")
					);
				}
				producto.setCategoria(category);
				return service.save(producto);
			}).doOnNext(product -> {
				logger.info("Categoria asignada: " + product.getCategoria().getNombre() + " Id Cat: " + product.getCategoria().getId());
				logger.info("Producto guardado: " + product.getNombre() + " Id: " + product.getId());
			}).flatMap(product -> {
				if (!file.filename().isEmpty()) {
					return file.transferTo(new File(path + product.getFoto()));
				}
				return Mono.empty();
			}).thenReturn("redirect:/listar?success=producto+guardado+con+exito");
		}
	}
	
	@GetMapping("/eliminar/{id}")
	public Mono<String> eliminar(@PathVariable String id) {
		return service.findById(id)
				.defaultIfEmpty(new Producto())
				.flatMap(producto -> {
					if (producto.getId() == null) {
						return Mono.error(new InterruptedException("No existe el producto a eliminar!"));
					}
					return Mono.just(producto);
				})
				.flatMap(producto -> {
					logger.info("Eliminando producto: " + producto.getNombre());
					logger.info("Eliminando producto Id: " + producto.getId());
					return service.delete(producto);
				})
				.then(Mono.just("redirect:/listar?success=producto+eliminado+con+exito"))
				.onErrorResume(exception -> Mono.just("redirect:/listar?error=no+existe+el+producto+a+eliminar"));
	}
	
	@GetMapping("/listar-datadriver")
	public String listarDataDriver(Model model) {
		Flux<Producto> productos = service.findAllConNombreUpperCase()
				.delayElements(Duration.ofSeconds(1));
		productos.subscribe(producto -> logger.info(producto.getNombre()));
		model.addAttribute("productos", new ReactiveDataDriverContextVariable(productos, 1));
		model.addAttribute("titulo", "Listado de productos");
		return "listar";
	}
	
	@GetMapping("/listar-full")
	public String listarFull(Model model) {
		Flux<Producto> productos = service.findAllConNombreUpperCaseRepeat();
		model.addAttribute("productos", productos);
		model.addAttribute("titulo", "Listado de productos");
		return "listar";
	}
	
	@GetMapping("/listar-chunked")
	public String listarChunked(Model model) {
		Flux<Producto> productos = service.findAllConNombreUpperCaseRepeat();
		model.addAttribute("productos", productos);
		model.addAttribute("titulo", "Listado de productos");
		return "listar-chunked";
	}
}
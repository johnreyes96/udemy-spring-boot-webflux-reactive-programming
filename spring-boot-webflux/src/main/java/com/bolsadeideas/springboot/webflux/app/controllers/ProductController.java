package com.bolsadeideas.springboot.webflux.app.controllers;

import java.io.File;
import java.net.MalformedURLException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.util.Date;
import java.util.UUID;
import javax.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
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
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import com.bolsadeideas.springboot.webflux.app.models.documents.Category;
import com.bolsadeideas.springboot.webflux.app.models.documents.Product;
import com.bolsadeideas.springboot.webflux.app.models.services.IProductService;

@SessionAttributes("product")
@Controller
public class ProductController {

	@Autowired
	private IProductService service;

	@Value("${config.uploads.path}")
	private String path;

	private static final Logger logger = LoggerFactory.getLogger(ProductController.class);

	@ModelAttribute("categories")
	public Flux<Category> categories() {
		return service.findAllCategories();
	}

	@GetMapping("/uploads/img/{namePhoto:.+}")
	public Mono<ResponseEntity<Resource>> viewPhoto(@PathVariable String namePhoto) throws MalformedURLException {
		Path path = getPath().resolve(namePhoto).toAbsolutePath();
		Resource image = getResource(path);
		return Mono.just(ResponseEntity.ok()
				.header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + image.getFilename() + "\"")
				.body(image));
	}

	@GetMapping("/view/{id}")
	public Mono<String> view(Model model, @PathVariable String id) {
		return service.findById(id)
				.doOnNext(product -> {
					model.addAttribute("product", product);
					model.addAttribute("title", "Detalle Producto");
				}).switchIfEmpty(Mono.just(new Product()))
				.flatMap(product -> {
					if (product.getId() == null) {
						return Mono.error(new InterruptedException("No existe el producto"));
					}
					return Mono.just(product);
				}).then(Mono.just("view"))
				.onErrorResume(exception -> Mono.just("redirect:/list?error=no+existe+el+producto"));
	}

	@GetMapping({ "/", "/list" })
	public String list(Model model) {
		Flux<Product> products = service.findAllWithNameUpperCase();
		products.subscribe(product -> logger.info(product.getName()));
		model.addAttribute("products", products);
		model.addAttribute("title", "Listado de productos");
		return "list";
	}

	@GetMapping("/form")
	public Mono<String> create(Model model) {
		model.addAttribute("product", new Product());
		model.addAttribute("title", "Formulario de producto");
		model.addAttribute("button", "Crear");
		return Mono.just("form");
	}

	@GetMapping("/form/{id}")
	public Mono<String> edit(@PathVariable String id, Model model) {
		Mono<Product> productMono = service.findById(id)
				.doOnNext(product -> {
					logger.info("Producto: " + product.getName());
				}).defaultIfEmpty(new Product());
		model.addAttribute("product", productMono);
		model.addAttribute("title", "Editar Producto");
		model.addAttribute("button", "Editar");
		return Mono.just("form");
	}

	@GetMapping("/form-v2/{id}")
	public Mono<String> editV2(@PathVariable String id, Model model) {
		return service.findById(id)
				.doOnNext(product -> {
					logger.info("Producto: " + product.getName());
					model.addAttribute("product", product);
					model.addAttribute("title", "Editar Producto");
					model.addAttribute("button", "Editar");
				}).defaultIfEmpty(new Product())
				.flatMap(product -> {
					if (product.getId() == null) {
						return Mono.error(new InterruptedException("No existe el producto"));
					}
					return Mono.just(product);
				}).then(Mono.just("form"))
				.onErrorResume(exception -> Mono.just("redirect:/list?error=no+existe+el+producto"));
	}

	@PostMapping("/form")
	public Mono<String> save(@Valid Product product, BindingResult result, Model model, @RequestPart FilePart file,
			SessionStatus status) {
		// TODO: Don't run the .hasErrors
		if (result.hasErrors()) {
			model.addAttribute("title", "Errores en formulario producto");
			model.addAttribute("button", "Guardar");
			return Mono.just("form");
		} else {
			status.setComplete();
			return service.findCategoryById(product.getCategory().getId())
					.flatMap(category -> {
						if (product.getCreateAt() == null)
							product.setCreateAt(new Date());
		
						if (!file.filename().isEmpty()) {
							product.setPhoto(UUID.randomUUID().toString() + "-"
									+ file.filename().replace(" ", "").replace(":", "").replace("\\", ""));
						}
						return service.save(product);
					}).doOnNext(productSaved -> {
						logger.info("Categoria asignada: " + productSaved.getCategory().getName() + " Id Cat: "
								+ productSaved.getCategory().getId());
						logger.info("Producto guardado: " + productSaved.getName() + " Id: " + productSaved.getId());
					}).flatMap(productSaved -> {
						if (!file.filename().isEmpty()) {
							return file.transferTo(new File(getUploadPath() + "\\" + productSaved.getPhoto()));
						}
						return Mono.empty();
					}).thenReturn("redirect:/list?success=producto+guardado+con+exito");
		}
	}

	@GetMapping("/delete/{id}")
	public Mono<String> delete(@PathVariable String id) {
		return service.findById(id)
				.defaultIfEmpty(new Product())
				.flatMap(product -> {
					if (product.getId() == null) {
						return Mono.error(new InterruptedException("No existe el producto a eliminar"));
					}
					return Mono.just(product);
				}).flatMap(product -> {
					logger.info("Eliminando producto Id: " + product.getId());
					logger.info("Eliminando producto: " + product.getName());
					return service.delete(product);
				}).then(Mono.just("redirect:/list?success=producto+eliminado+con+exito"))
				.onErrorResume(exception -> Mono.just("redirect:/list?error=no+existe+el+producto+a+eliminar"));
	}

	@GetMapping("/list-datadriver")
	public String listDataDriver(Model model) {
		Flux<Product> products = service.findAllWithNameUpperCase()
				.delayElements(Duration.ofSeconds(1));
		products.subscribe(product -> logger.info(product.getName()));
		model.addAttribute("products", new ReactiveDataDriverContextVariable(products, 1));
		model.addAttribute("title", "Listado de productos");
		return "list";
	}

	@GetMapping("/list-full")
	public String listFull(Model model) {
		Flux<Product> products = service.findAllWithNameUpperCaseRepeat();
		model.addAttribute("products", products);
		model.addAttribute("title", "Listado de productos");
		return "list";
	}

	@GetMapping("/list-chunked")
	public String listChunked(Model model) {
		Flux<Product> products = service.findAllWithNameUpperCaseRepeat();
		model.addAttribute("products", products);
		model.addAttribute("title", "Listado de productos");
		return "list-chunked";
	}

	protected Resource getResource(Path path) throws MalformedURLException {
		return new UrlResource(path.toUri());
	}

	protected String getUploadPath() {
		return new File(getPath().toUri()).getAbsolutePath();
	}

	protected Path getPath() {
		return Paths.get(path);
	}
}
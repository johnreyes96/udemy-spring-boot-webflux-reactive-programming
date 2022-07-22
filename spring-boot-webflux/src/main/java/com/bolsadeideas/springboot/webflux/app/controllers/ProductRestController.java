package com.bolsadeideas.springboot.webflux.app.controllers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import com.bolsadeideas.springboot.webflux.app.models.dao.IProductDao;
import com.bolsadeideas.springboot.webflux.app.models.documents.Product;

@RestController
@RequestMapping("/api/productos")
public class ProductRestController {

	@Autowired
	private IProductDao productoDao;

	private static final Logger logger = LoggerFactory.getLogger(ProductController.class);

	@GetMapping
	public Flux<Product> index() {
		Flux<Product> products = productoDao.findAll()
				.map(product -> {
					product.setName(product.getName().toUpperCase());
					return product;
				}).doOnNext(product -> logger.info(product.getName()));
		return products;
	}

	@GetMapping("/{id}")
	public Mono<Product> show(@PathVariable String id) {
		return productoDao.findById(id);
	}
}
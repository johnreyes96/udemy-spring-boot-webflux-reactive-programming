package com.bolsadeideas.springboot.webflux.app.models.services;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import com.bolsadeideas.springboot.webflux.app.models.documents.Category;
import com.bolsadeideas.springboot.webflux.app.models.documents.Product;

public interface IProductService {

	public Flux<Product> findAll();

	public Flux<Product> findAllWithNameUpperCase();

	public Flux<Product> findAllWithNameUpperCaseRepeat();
	
	public Mono<Product> findById(String id);
	
	public Mono<Product> save(Product product);

	public Mono<Void> delete(Product product);

	public Flux<Category> findAllCategories();
	
	public Mono<Category> findCategoryById(String id);
	
	public Mono<Category> saveCategory(Category category);
}
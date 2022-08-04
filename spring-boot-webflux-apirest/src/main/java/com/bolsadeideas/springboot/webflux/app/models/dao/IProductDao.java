package com.bolsadeideas.springboot.webflux.app.models.dao;

import org.springframework.data.mongodb.repository.Query;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import reactor.core.publisher.Mono;

import com.bolsadeideas.springboot.webflux.app.models.documents.Product;

public interface IProductDao extends ReactiveMongoRepository<Product, String> {

	 public Mono<Product> findByName(String name);
	 
	 @Query("{ 'nombre': ?0 }")
	 public Mono<Product> getByName(String name);
}
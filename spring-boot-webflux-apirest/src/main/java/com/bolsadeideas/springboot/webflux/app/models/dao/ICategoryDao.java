package com.bolsadeideas.springboot.webflux.app.models.dao;

import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import reactor.core.publisher.Mono;

import com.bolsadeideas.springboot.webflux.app.models.documents.Category;

public interface ICategoryDao extends ReactiveMongoRepository<Category, String> {

	public Mono<Category> findByName(String name);
}
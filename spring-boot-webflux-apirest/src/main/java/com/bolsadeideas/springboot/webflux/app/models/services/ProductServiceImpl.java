package com.bolsadeideas.springboot.webflux.app.models.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import com.bolsadeideas.springboot.webflux.app.models.dao.CategoryDao;
import com.bolsadeideas.springboot.webflux.app.models.dao.ProductDao;
import com.bolsadeideas.springboot.webflux.app.models.documents.Category;
import com.bolsadeideas.springboot.webflux.app.models.documents.Product;

@Service
public class ProductServiceImpl implements ProductService{

	@Autowired
	private ProductDao dao;
	
	@Autowired
	private CategoryDao categoryDao;
	
	@Override
	public Flux<Product> findAll() {
		return dao.findAll();
	}

	@Override
	public Mono<Product> findById(String id) {
		return dao.findById(id);
	}

	@Override
	public Mono<Product> save(Product product) {
		return dao.save(product);
	}

	@Override
	public Mono<Void> delete(Product product) {
		return dao.delete(product);
	}

	@Override
	public Flux<Product> findAllWithNameUpperCase() {
		return dao.findAll().map(product -> {
			product.setName(product.getName().toUpperCase());
			return product;
		});
	}

	@Override
	public Flux<Product> findAllWithNameUpperCaseRepeat() {
		return findAllWithNameUpperCase().repeat(5);
	}

	@Override
	public Flux<Category> findAllCategory() {
		return categoryDao.findAll();
	}

	@Override
	public Mono<Category> findCategoryById(String id) {
		return categoryDao.findById(id);
	}

	@Override
	public Mono<Category> saveCategory(Category category) {
		return categoryDao.save(category);
	}

	@Override
	public Mono<Product> findByName(String name) {
		return dao.getByName(name);
	}

	@Override
	public Mono<Category> findCategoryByName(String Name) {
		return categoryDao.findByName(Name);
	}
}

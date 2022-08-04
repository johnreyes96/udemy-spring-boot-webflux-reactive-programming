package com.bolsadeideas.springboot.webflux.app.models.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import com.bolsadeideas.springboot.webflux.app.models.dao.ICategoryDao;
import com.bolsadeideas.springboot.webflux.app.models.dao.IProductDao;
import com.bolsadeideas.springboot.webflux.app.models.documents.Category;
import com.bolsadeideas.springboot.webflux.app.models.documents.Product;

@Service
public class ProductServiceImpl implements ProductService {

	@Autowired
	private IProductDao productDao;
	
	@Autowired
	private ICategoryDao categoryDao;
	
	@Override
	public Flux<Product> findAll() {
		return productDao.findAll();
	}

	@Override
	public Mono<Product> findById(String id) {
		return productDao.findById(id);
	}

	@Override
	public Mono<Product> save(Product product) {
		return productDao.save(product);
	}

	@Override
	public Mono<Void> delete(Product product) {
		return productDao.delete(product);
	}

	@Override
	public Flux<Product> findAllWithNameUpperCase() {
		return productDao.findAll().map(Product::setNameToUpperCase);
	}

	@Override
	public Flux<Product> findAllWithNameUpperCaseRepeat() {
		return findAllWithNameUpperCase().repeat(5);
	}

	@Override
	public Flux<Category> findAllCategories() {
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
		return productDao.getByName(name);
	}

	@Override
	public Mono<Category> findCategoryByName(String Name) {
		return categoryDao.findByName(Name);
	}
}

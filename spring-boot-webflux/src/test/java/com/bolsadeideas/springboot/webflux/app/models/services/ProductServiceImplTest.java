package com.bolsadeideas.springboot.webflux.app.models.services;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;
import org.springframework.context.annotation.ComponentScan;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import com.bolsadeideas.springboot.webflux.app.models.dao.ICategoryDao;
import com.bolsadeideas.springboot.webflux.app.models.dao.IProductDao;
import com.bolsadeideas.springboot.webflux.app.models.documents.Category;
import com.bolsadeideas.springboot.webflux.app.models.documents.Product;

import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.verify;

@ComponentScan(basePackages = "com.bolsadeideas.springboot.webflux.app.models.services")
public class ProductServiceImplTest {

	@Spy
	@InjectMocks
	private ProductServiceImpl productService;
	@Mock
	private IProductDao productDao;
	@Mock
	private ICategoryDao categoryDao;

	@BeforeEach
	void init() {
		productService = new ProductServiceImpl();
		MockitoAnnotations.openMocks(this);
	}

	@Test
	public void findAllWhenIsInvokedThenMustFindAllProductsTest() {
		doReturn(Flux.empty()).when(productDao).findAll();

		productService.findAll();

		verify(productDao).findAll();
	}

	@Test
	public void findByIdWhenIsInvokedThenMustFindProductByIdTest() {
		String id = "id";
		doReturn(Mono.empty()).when(productDao).findById(id);

		productService.findById(id);

		verify(productDao).findById(id);
	}

	@Test
	public void saveWhenIsInvokedThenMustSaveProductTest() {
		Product product = new Product();
		doReturn(Mono.empty()).when(productDao).save(product);

		productService.save(product);

		verify(productDao).save(product);
	}

	@Test
	public void deleteWhenIsInvokedThenMustDeleteProductTest() {
		Product product = new Product();
		doReturn(Mono.empty()).when(productDao).delete(product);

		productService.delete(product);

		verify(productDao).delete(product);
	}

	@Test
	public void findAllWithNameUpperCaseWhenIsInvokedThenMustUpperCaseToProductsNameTest() {
		Product product = new Product();
		product.setName("Sony Notebook");
		doReturn(Flux.just(product)).when(productDao).findAll();

		StepVerifier.create(productService.findAllWithNameUpperCase())
				.expectNextMatches(productExpected -> "SONY NOTEBOOK".equals(productExpected.getName()))
				.expectComplete().verify();

		verify(productDao).findAll();
	}

	@Test
	public void findAllWithNameUpperCaseRepeatWhenIsInvokedThenFindAllWithNameUpperCaseRepeatTest() {
		doReturn(Flux.empty()).when(productService).findAllWithNameUpperCase();

		productService.findAllWithNameUpperCaseRepeat();

		verify(productService).findAllWithNameUpperCase();
	}

	@Test
	public void findAllCategoriesWhenIsInvokedThenMustFindAllCategoriesTest() {
		doReturn(Flux.empty()).when(categoryDao).findAll();

		productService.findAllCategories();

		verify(categoryDao).findAll();
	}

	@Test
	public void findCategoriaByIdWhenIsInvokedThenMustFindCategoryByIdTest() {
		String id = "id";
		doReturn(Mono.empty()).when(categoryDao).findById(id);

		productService.findCategoryById(id);

		verify(categoryDao).findById(id);
	}

	@Test
	public void saveCategoryWhenIsInvokedThenMustSaveCategoryTest() {
		Category category = new Category();
		doReturn(Mono.empty()).when(categoryDao).save(category);

		productService.saveCategory(category);

		verify(categoryDao).save(category);
	}
}

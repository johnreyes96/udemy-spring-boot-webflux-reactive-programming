package com.bolsadeideas.springboot.webflux.app.models.services;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
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
	public void findAllWhenIsInvokedThenMustFindAndReturnAllProductsTest() {
		Product product = new Product();
		doReturn(Flux.just(product)).when(productDao).findAll();

		StepVerifier.create(productService.findAll())
			.expectNext(product)
			.expectComplete()
			.verify();

		verify(productDao).findAll();
	}

	@Test
	public void findByIdWhenIsInvokedThenMustFindProductByIdAndReturnItTest() {
		Product product = new Product();
		String id = "id";
		doReturn(Mono.just(product)).when(productDao).findById(id);

		StepVerifier.create(productService.findById(id))
			.expectNext(product)
			.expectComplete()
			.verify();

		verify(productDao).findById(id);
	}

	@Test
	public void saveWhenIsInvokedThenMustSaveAndReturnProductTest() {
		Product product = new Product();
		doReturn(Mono.just(product)).when(productDao).save(product);

		StepVerifier.create(productService.save(product))
			.expectNext(product)
			.expectComplete()
			.verify();

		verify(productDao).save(product);
	}

	@Test
	public void deleteWhenIsInvokedThenMustDeleteProductAndReturnMonoVoidTest() {
		Product product = new Product();
		doReturn(Mono.empty()).when(productDao).delete(product);

		StepVerifier.create(productService.delete(product))
			.expectNextCount(0)
			.expectComplete()
			.verify();

		verify(productDao).delete(product);
	}

	@Test
	public void findAllWithNameUpperCaseWhenIsInvokedThenMustUpperCaseToProductsNameTest() {
		Product productMock = Mockito.mock(Product.class);
		Product product = new Product();
		product.setName("SONY NOTEBOOK");
		doReturn(Flux.just(productMock)).when(productDao).findAll();
		doReturn(product).when(productMock).setNameToUpperCase();

		StepVerifier.create(productService.findAllWithNameUpperCase())
			.expectNextMatches(productExpected -> "SONY NOTEBOOK".equals(productExpected.getName()))
			.expectComplete()
			.verify();

		verify(productDao).findAll();
		verify(productMock).setNameToUpperCase();
	}

	@Test
	public void findAllWithNameUpperCaseRepeatWhenIsInvokedThenInvokeFindAllWithNameUpperCaseAndDuplicateFiveTimesSameItemsTest() {
		Product product = new Product();
		doReturn(Flux.just(product)).when(productService).findAllWithNameUpperCase();

		StepVerifier.create(productService.findAllWithNameUpperCaseRepeat())
			.expectNext(product)
			.expectNextCount(5)
			.expectComplete()
			.verify();

		verify(productService).findAllWithNameUpperCase();
	}

	@Test
	public void findAllCategoriesWhenIsInvokedThenMustFindAndReturnAllCategoriesTest() {
		Category category = new Category();
		doReturn(Flux.just(category)).when(categoryDao).findAll();

		StepVerifier.create(productService.findAllCategories())
			.expectNext(category)
			.expectComplete()
			.verify();

		verify(categoryDao).findAll();
	}

	@Test
	public void findCategoriaByIdWhenIsInvokedThenMustFindCategoryByIdAndReturnItTest() {
		Category category = new Category();
		String id = "id";
		doReturn(Mono.just(category)).when(categoryDao).findById(id);

		StepVerifier.create(productService.findCategoryById(id))
			.expectNext(category)
			.expectComplete()
			.verify();

		verify(categoryDao).findById(id);
	}

	@Test
	public void saveCategoryWhenIsInvokedThenMustSaveAndReturnCategoryTest() {
		Category category = new Category();
		doReturn(Mono.just(category)).when(categoryDao).save(category);

		StepVerifier.create(productService.saveCategory(category))
			.expectNext(category)
			.expectComplete()
			.verify();

		verify(categoryDao).save(category);
	}

	@Test
	public void findByNameWhenIsInvokedThenMustFindProductByNameAndReturnItTest() {
		Product product = new Product();
		String name = "Sony Notebook";
		doReturn(Mono.just(product)).when(productDao).getByName(name);

		StepVerifier.create(productService.findByName(name))
			.expectNext(product)
			.expectComplete()
			.verify();

		verify(productDao).getByName(name);
	}

	@Test
	public void findCategoryByNameWhenIsInvokedThenMustFindCategoryByNameAndReturnItTest() {
		Category category = new Category();
		String name = "Deporte";
		doReturn(Mono.just(category)).when(categoryDao).findByName(name);

		StepVerifier.create(productService.findCategoryByName(name))
			.expectNext(category)
			.expectComplete()
			.verify();

		verify(categoryDao).findByName(name);
	}
}

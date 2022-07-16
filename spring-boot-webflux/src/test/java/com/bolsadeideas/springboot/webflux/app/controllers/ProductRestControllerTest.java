package com.bolsadeideas.springboot.webflux.app.controllers;

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

import com.bolsadeideas.springboot.webflux.app.models.dao.IProductoDao;
import com.bolsadeideas.springboot.webflux.app.models.documents.Producto;

import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.verify;

@ComponentScan(basePackages = "com.bolsadeideas.springboot.webflux.app.controllers")
public class ProductRestControllerTest {

	@Spy
	@InjectMocks
	private ProductoRestController productRestController;
	@Mock
	private IProductoDao productDao;

	@BeforeEach
	void init() {
		productRestController = new ProductoRestController();
		MockitoAnnotations.openMocks(this);
	}

	@Test
	public void indexWhenFindAllReturnZeroElementsThenMustReturnFluxEmptyTest() {
		doReturn(Flux.empty()).when(productDao).findAll();

		StepVerifier.create(productRestController.index())
		.expectNextCount(0)
		.expectComplete()
		.verify();

		verify(productDao).findAll();
	}

	@Test
	public void indexWhenFindAllReturnOneElementThenMustReturnFluxWithOneProductWithNameUpperCaseTest() {
		Producto product = new Producto();
		product.setNombre("Sony Notebook");
		doReturn(Flux.just(product)).when(productDao).findAll();

		StepVerifier.create(productRestController.index())
		.expectNextMatches(productExpected -> "SONY NOTEBOOK".equals(productExpected.getNombre()))
		.expectComplete()
		.verify();

		verify(productDao).findAll();
	}

	@Test
	public void indexWhenFindAllReturnElementsThenMustReturnFluxWithProductsWithNameUpperCaseTest() {
		Producto laptop = new Producto();
		laptop.setNombre("Sony Notebook");
		Producto smartphone = new Producto();
		smartphone.setNombre("Apple iPod");
		doReturn(Flux.just(laptop, smartphone)).when(productDao).findAll();

		StepVerifier.create(productRestController.index())
		.expectNextMatches(productExpected -> "SONY NOTEBOOK".equals(productExpected.getNombre()))
		.expectNextMatches(productExpected -> "APPLE IPOD".equals(productExpected.getNombre()))
		.expectComplete()
		.verify();

		verify(productDao).findAll();
	}

	@Test
	public void showWhenFindByIdReturnElementThenMustReturnMonoWithProductTest() {
		String id = "62d249977dacbc5b8ab52014";
		Producto product = new Producto();
		product.setId(id);
		doReturn(Mono.just(product)).when(productDao).findById(id);

		StepVerifier.create(productRestController.show(id))
		.expectNextMatches(productExpected -> "62d249977dacbc5b8ab52014".equals(productExpected.getId()))
		.expectComplete()
		.verify();

		verify(productDao).findById(id);
	}
}

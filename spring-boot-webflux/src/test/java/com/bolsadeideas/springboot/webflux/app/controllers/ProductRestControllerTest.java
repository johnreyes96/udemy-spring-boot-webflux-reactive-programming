package com.bolsadeideas.springboot.webflux.app.controllers;

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

import com.bolsadeideas.springboot.webflux.app.models.dao.IProductDao;
import com.bolsadeideas.springboot.webflux.app.models.documents.Product;

import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.verify;

@ComponentScan(basePackages = "com.bolsadeideas.springboot.webflux.app.controllers")
public class ProductRestControllerTest {

	@Spy
	@InjectMocks
	private ProductRestController productRestController;
	@Mock
	private IProductDao productDao;

	@BeforeEach
	void init() {
		productRestController = new ProductRestController();
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
		Product productMock = Mockito.mock(Product.class);
		Product product = new Product();
		product.setName("SONY NOTEBOOK");
		doReturn(Flux.just(productMock)).when(productDao).findAll();
		doReturn(product).when(productMock).setNameToUpperCase();

		StepVerifier.create(productRestController.index())
			.expectNextMatches(productExpected -> "SONY NOTEBOOK".equals(productExpected.getName()))
			.expectComplete()
			.verify();

		verify(productDao).findAll();
		verify(productMock).setNameToUpperCase();
	}

	@Test
	public void indexWhenFindAllReturnElementsThenMustReturnFluxWithProductsWithNameUpperCaseTest() {
		Product laptopMock = Mockito.mock(Product.class);
		Product smartphoneMock = Mockito.mock(Product.class);
		Product laptop = new Product();
		laptop.setName("SONY NOTEBOOK");
		Product smartphone = new Product();
		smartphone.setName("APPLE IPOD");
		doReturn(Flux.just(laptopMock, smartphoneMock)).when(productDao).findAll();
		doReturn(laptop).when(laptopMock).setNameToUpperCase();
		doReturn(smartphone).when(smartphoneMock).setNameToUpperCase();

		StepVerifier.create(productRestController.index())
			.expectNextMatches(productExpected -> "SONY NOTEBOOK".equals(productExpected.getName()))
			.expectNextMatches(productExpected -> "APPLE IPOD".equals(productExpected.getName()))
			.expectComplete()
			.verify();

		verify(productDao).findAll();
		verify(laptopMock).setNameToUpperCase();
		verify(smartphoneMock).setNameToUpperCase();
	}

	@Test
	public void showWhenFindByIdReturnElementThenMustReturnMonoWithProductTest() {
		String id = "62d249977dacbc5b8ab52014";
		Product product = new Product();
		product.setId(id);
		doReturn(Mono.just(product)).when(productDao).findById(id);

		StepVerifier.create(productRestController.show(id))
			.expectNextMatches(productExpected -> "62d249977dacbc5b8ab52014".equals(productExpected.getId()))
			.expectComplete()
			.verify();

		verify(productDao).findById(id);
	}
}

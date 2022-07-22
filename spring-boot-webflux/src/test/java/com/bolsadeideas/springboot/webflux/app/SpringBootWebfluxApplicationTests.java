package com.bolsadeideas.springboot.webflux.app;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;
import org.springframework.test.context.junit4.SpringRunner;
import reactor.core.publisher.Mono;

import com.bolsadeideas.springboot.webflux.app.models.services.IProductService;

import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.verify;

@SpringBootApplication
@RunWith(SpringRunner.class)
@SpringBootTest(classes = SpringBootWebfluxApplication.class)
class SpringBootWebfluxApplicationTests {
	
	@Spy
	@InjectMocks
	private SpringBootWebfluxApplication application;
	@Mock
	private IProductService service;
	@Mock
	private ReactiveMongoTemplate mongoTemplate;

	@BeforeEach
    void init() {
		application = new SpringBootWebfluxApplication();
		MockitoAnnotations.openMocks(this);
	}

	@Test
	public void populateDBWhenIsInvokedThenMustDropCollectionsTest() {
		doReturn(Mono.empty()).when(service).saveCategory(Mockito.any());
		doReturn(Mono.empty()).when(service).save(Mockito.any());
		
		application.populateDB();
		
		verify(service, Mockito.times(4)).saveCategory(Mockito.any());
		verify(service, Mockito.times(9)).save(Mockito.any());
	}

	@Test
	public void restoreDBWhenIsInvokedThenMustDropCollectionsTest() {
		doReturn(Mono.empty()).when(mongoTemplate).dropCollection("productos");
		doReturn(Mono.empty()).when(mongoTemplate).dropCollection("categorias");
		
		application.restoreDB();
		
		verify(mongoTemplate).dropCollection("productos");
		verify(mongoTemplate).dropCollection("categorias");
	}
}

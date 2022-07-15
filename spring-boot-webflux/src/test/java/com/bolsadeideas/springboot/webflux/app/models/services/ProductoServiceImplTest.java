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

import com.bolsadeideas.springboot.webflux.app.models.dao.ICategoriaDao;
import com.bolsadeideas.springboot.webflux.app.models.dao.IProductoDao;
import com.bolsadeideas.springboot.webflux.app.models.documents.Categoria;
import com.bolsadeideas.springboot.webflux.app.models.documents.Producto;

import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.verify;

@ComponentScan(basePackages = "com.bolsadeideas.springboot.webflux.app.models.services")
public class ProductoServiceImplTest {
	
	@Spy
	@InjectMocks
	private ProductoServiceImpl productoService;
	@Mock
	private IProductoDao productoDao;
	@Mock
	private ICategoriaDao categoriaDao;

	@BeforeEach
    void init() {
		productoService = new ProductoServiceImpl();
		MockitoAnnotations.openMocks(this);
	}

	@Test
	public void findAllWhenIsInvokedThenMustFindAllProductsTest() {
		doReturn(Flux.empty()).when(productoDao).findAll();
		
		productoService.findAll();
		
		verify(productoDao).findAll();
	}

	@Test
	public void findByIdWhenIsInvokedThenMustFindProductByIdTest() {
		String id = "id";
		doReturn(Mono.empty()).when(productoDao).findById(id);
		
		productoService.findById(id);
		
		verify(productoDao).findById(id);
	}

	@Test
	public void saveWhenIsInvokedThenMustSaveProductTest() {
		Producto producto = new Producto();
		doReturn(Mono.empty()).when(productoDao).save(producto);
		
		productoService.save(producto);
		
		verify(productoDao).save(producto);
	}

	@Test
	public void deleteWhenIsInvokedThenMustDeleteProductTest() {
		Producto producto = new Producto();
		doReturn(Mono.empty()).when(productoDao).delete(producto);
		
		productoService.delete(producto);
		
		verify(productoDao).delete(producto);
	}

	@Test
	public void findAllConNombreUpperCaseWhenIsInvokedThenMustUpperCaseToProductsNameTest() {
		Producto producto = new Producto();
		producto.setNombre("Sony Notebook");
		doReturn(Flux.just(producto)).when(productoDao).findAll();
		
		StepVerifier.create(productoService.findAllConNombreUpperCase())
		.expectNextMatches(productExpected -> "SONY NOTEBOOK".equals(productExpected.getNombre()))
		.expectComplete()
		.verify();
		
		verify(productoDao).findAll();
	}

	@Test
	public void findAllConNombreUpperCaseRepeatWhenIsInvokedThenFindAllConNombreUpperCaseRepeatTest() {
		doReturn(Flux.empty()).when(productoService).findAllConNombreUpperCase();
		
		productoService.findAllConNombreUpperCaseRepeat();
		
		verify(productoService).findAllConNombreUpperCase();
	}

	@Test
	public void findAllCategoriaWhenIsInvokedThenMustFindAllCategoriesTest() {
		doReturn(Flux.empty()).when(categoriaDao).findAll();
		
		productoService.findAllCategoria();
		
		verify(categoriaDao).findAll();
	}

	@Test
	public void findCategoriaByIdWhenIsInvokedThenMustFindCategoriaByIdTest() {
		String id = "id";
		doReturn(Mono.empty()).when(categoriaDao).findById(id);
		
		productoService.findCategoriaById(id);
		
		verify(categoriaDao).findById(id);
	}

	@Test
	public void saveCategoryWhenIsInvokedThenMustSaveCategoryTest() {
		Categoria category= new Categoria();
		doReturn(Mono.empty()).when(categoriaDao).save(category);
		
		productoService.saveCategoria(category);
		
		verify(categoriaDao).save(category);
	}
}

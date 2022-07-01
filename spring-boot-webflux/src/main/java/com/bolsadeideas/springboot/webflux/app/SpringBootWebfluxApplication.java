package com.bolsadeideas.springboot.webflux.app;

import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;

import com.bolsadeideas.springboot.webflux.app.models.documents.Categoria;
import com.bolsadeideas.springboot.webflux.app.models.documents.Producto;
import com.bolsadeideas.springboot.webflux.app.models.services.IProductoService;

import reactor.core.publisher.Flux;

@SpringBootApplication
public class SpringBootWebfluxApplication implements CommandLineRunner {

	@Autowired
	private IProductoService service;
	
	@Autowired
	private ReactiveMongoTemplate mongoTemplate;
	
	private static final Logger logger = LoggerFactory.getLogger(SpringBootWebfluxApplication.class);

	public static void main(String[] args) {
		SpringApplication.run(SpringBootWebfluxApplication.class, args);
	}

	@Override
	public void run(String... args) throws Exception {
		restoreDB();
		populateDB();
	}

	private void populateDB() {
		Categoria electronico = new Categoria("Electrónico");
		Categoria deporte = new Categoria("Deporte");
		Categoria computacion = new Categoria("Computación");
		Categoria muebles = new Categoria("Muebles");
		
		Flux.just(electronico, deporte, computacion, muebles)
		.flatMap(service::saveCategoria)
		.doOnNext(this::printCategoryCreated)
		.thenMany(
			Flux.just(new Producto("TV Panasonic Pantalla LCD", 456.89, electronico),
					new Producto("Sony Camara HD Digital", 177.89, electronico),
					new Producto("Apple iPod", 46.89, electronico),
					new Producto("Sony Notebook", 846.89, computacion),
					new Producto("Hewlett Packard Multifuncional", 200.89, computacion),
					new Producto("Bianchi Bicicleta", 70.89, deporte),
					new Producto("HP Notebook Omen 17", 2500.89, computacion),
					new Producto("Mica Cómoda 5 Cajones", 150.89, muebles),
					new Producto("TV Sony Bravia OLED 4K Ultra HD", 2255.89, electronico)
					)
			.flatMap(producto -> {
				producto.setCreateAt(new Date());
				return service.save(producto);
			})
		).subscribe(this::printProductCreated);
	}

	public void restoreDB() {
		mongoTemplate.dropCollection("productos").subscribe();
		mongoTemplate.dropCollection("categorias").subscribe();
	}

	private void printCategoryCreated(Categoria categoria) {
		logger.info("Categoria creada: " + categoria.getNombre() + ", Id: " + categoria.getId());
	}
	
	private void printProductCreated(Producto producto) {
		logger.info("Insert: " + producto.getId() + " " + producto.getNombre());
	}
}

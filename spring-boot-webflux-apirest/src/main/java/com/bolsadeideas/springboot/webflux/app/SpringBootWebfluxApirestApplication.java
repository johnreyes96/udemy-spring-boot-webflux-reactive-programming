package com.bolsadeideas.springboot.webflux.app;

import java.util.Date;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.EnableEurekaClient;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;
import reactor.core.publisher.Flux;

import com.bolsadeideas.springboot.webflux.app.models.documents.Category;
import com.bolsadeideas.springboot.webflux.app.models.documents.Product;
import com.bolsadeideas.springboot.webflux.app.models.services.ProductService;

@EnableEurekaClient
@SpringBootApplication
public class SpringBootWebfluxApirestApplication implements CommandLineRunner {

	@Autowired
	private ProductService service;

	@Autowired
	private ReactiveMongoTemplate mongoTemplate;

	private static final Logger logger = LoggerFactory.getLogger(SpringBootWebfluxApirestApplication.class);

	public static void main(String[] args) {
		SpringApplication.run(SpringBootWebfluxApirestApplication.class, args);
	}

	@Override
	public void run(String... args) throws Exception {
		restoreDB();
		populateDB();
	}

	protected void populateDB() {
		Category electronic = new Category("Electrónico");
		Category sport = new Category("Deporte");
		Category computing = new Category("Computación");
		Category furniture = new Category("Muebles");

		Flux.just(electronic, sport, computing, furniture).flatMap(service::saveCategory)
				.doOnNext(this::printCategoryCreated)
				.thenMany(Flux.just(new Product("TV Panasonic Pantalla LCD", 456.89, electronic),
							new Product("Sony Camara HD Digital", 177.89, electronic),
							new Product("Apple iPod", 46.89, electronic),
							new Product("Sony Notebook", 846.89, computing),
							new Product("Hewlett Packard Multifuncional", 200.89, computing),
							new Product("Bianchi Bicicleta", 70.89, sport),
							new Product("HP Notebook Omen 17", 2500.89, computing),
							new Product("Mica Cómoda 5 Cajones", 150.89, furniture),
							new Product("TV Sony Bravia OLED 4K Ultra HD", 2255.89, electronic)
						).flatMap(product -> {
							product.setCreateAt(new Date());
							return service.save(product);
						}))
				.subscribe(this::printProductCreated);
	}

	protected void restoreDB() {
		mongoTemplate.dropCollection("productos").subscribe();
		mongoTemplate.dropCollection("categorias").subscribe();
	}

	private void printCategoryCreated(Category category) {
		logger.info("Categoria creada: " + category.getName() + ", Id: " + category.getId());
	}

	private void printProductCreated(Product product) {
		logger.info("Insert: " + product.getId() + " " + product.getName());
	}
}

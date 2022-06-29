package com.bolsadeideas.springboot.reactor.app;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.CountDownLatch;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import com.bolsadeideas.springboot.reactor.app.models.Comments;
import com.bolsadeideas.springboot.reactor.app.models.User;
import com.bolsadeideas.springboot.reactor.app.models.UserComments;

@SpringBootApplication
public class SpringBootReactorApplication implements CommandLineRunner {
	
	private static final Logger logger = LoggerFactory.getLogger(SpringBootReactorApplication.class);

	public static void main(String[] args) {
		SpringApplication.run(SpringBootReactorApplication.class, args);
	}

	@Override
	public void run(String... args) throws Exception {
		int option = getOptionMenu();
		executeOption(option);
	}

	private void executeOption(int option) throws InterruptedException {
		switch (option) {
		case 1: {
			exampleAgainstPressure();
			break;
		}
		case 2: {
			exampleIntervalFromCreate();
			break;
		}
		case 3: {
			exampleIntervalInfinite();
			break;
		}
		case 4: {
			exampleDelayElements();
			break;
		}
		case 5: {
			exampleInterval();
			break;
		}
		case 6: {
			exampleZipWithRanges();
			break;
		}
		case 7: {
			exampleUserCommentsZipWithForm2();
			break;
		}
		case 8: {
			exampleUserCommentsZipWith();
			break;
		}
		case 9: {
			exampleUserCommentsFlatMap();
			break;
		}
		case 10: {
			exampleCollectList();
			break;
		}
		case 11: {
			exampleToString();
			break;
		}
		case 12: {
			exampleFlatMap();
			break;
		}
		case 13: {
			exampleIterable();
			break;
		}
		default:
			break;
		}
	}

	private int getOptionMenu() {
		Scanner scanner = new Scanner(System.in);
		System.out.println("Menú");
		System.out.println("1. Contra presión");
		System.out.println("2. Intervalo desde crear");
		System.out.println("3. Intervalo infinito");
		System.out.println("4. Recorrido con delay");
		System.out.println("5. Intervalo");
		System.out.println("6. ZipWith con rangos");
		System.out.println("7. Usuario comentarios con otra forma ZipWith");
		System.out.println("8. Usuario comentarios con ZipWith");
		System.out.println("9. Usuario comentarios con FlatMap");
		System.out.println("10. Lista de colección");
		System.out.println("11. A ToString");
		System.out.println("12. A FlatMap");
		System.out.println("13. Iterable");
		int option = scanner.nextInt();
		scanner.close();
		return option;
	}

	private void exampleAgainstPressure() {
		Flux.range(1, 10)
				.log()
				.subscribe(new Subscriber<Integer>() {
					
					private Subscription subscription;
					private Integer limit = 5;
					private Integer used = 0;

					@Override
					public void onSubscribe(Subscription subscription) {
						this.subscription = subscription;
						subscription.request(limit);
					}

					@Override
					public void onNext(Integer element) {
						logger.info(element.toString());
						used++;
						if (used == limit) {
							used = 0;
							this.subscription.request(limit);
						}
					}

					@Override
					public void onError(Throwable t) { }

					@Override
					public void onComplete() { }
				});
	}

	private void exampleIntervalFromCreate() {
		Flux.create(emitter -> {
			Timer timer = new Timer();
			timer.schedule(new TimerTask() {
				
				private Integer counter = 0;
				
				@Override
				public void run() {
					emitter.next(++counter);
					if (counter == 10) {
						timer.cancel();
						emitter.complete();
					}
					
					if (counter == 5) {
						timer.cancel();
						emitter.error(new InterruptedException("Error, se ha detenido el flux en 5!"));
					}
				}
			}, 1000, 1000);
		})
		.subscribe(
				contador -> logger.info(contador.toString()),
				error -> logger.error(error.getMessage()),
				() -> logger.info("Hemos terminado")
		);
	}

	private void exampleIntervalInfinite() throws InterruptedException {
		CountDownLatch latch = new CountDownLatch(1);
		Flux.interval(Duration.ofSeconds(1))
				.doOnTerminate(latch::countDown)
				.flatMap(element -> {
					if (element >= 5) {
						return Flux.error(new InterruptedException("Solo hasta 5!"));
					}
					return Flux.just(element);
				})
				.map(element -> "Hola " + element)
				.retry(2)
				.subscribe(
						text -> logger.info(text),
						error -> logger.error(error.getMessage())
				);
		latch.await();
	}

	private void exampleDelayElements() throws InterruptedException {
		Flux<Integer> ranges = Flux.range(1, 12)
				.delayElements(Duration.ofSeconds(1))
				.doOnNext(element -> logger.info(element.toString()));
		ranges.blockLast();
	}

	private void exampleInterval() {
		Flux<Integer> ranges = Flux.range(1, 12);
		Flux<Long> delay = Flux.interval(Duration.ofSeconds(1));
		ranges.zipWith(delay, (rango, re) -> rango)
				.doOnNext(element -> logger.info(element.toString()))
				.blockLast();
	}

	private void exampleZipWithRanges() {
		Flux<Integer> ranges = Flux.range(1, 5);
		Flux.just(1, 2, 3, 4)
				.map(element -> (element * 2))
				.zipWith(ranges, (one, two) -> String.format("Primer Flux %d, Segundo Flux %d", one, two))
				.subscribe(text -> logger.info(text));
	}

	private void exampleUserCommentsZipWithForm2() {
		Mono<User> userMono = Mono.fromCallable(() -> new User("John", "Reyes"));
		Mono<Comments> commentsUserMono = Mono.fromCallable(() -> {
			Comments comments = new Comments();
			comments.addComment("Hola pepe, qué tal!");
			comments.addComment("Mañana voy para la playa!");
			comments.addComment("Estoy tomando el curso de spring con reactor");
			return comments;
		});
		
		Mono<UserComments> userComments = userMono
				.zipWith(commentsUserMono)
				.map(tuple -> {
					User user = tuple.getT1();
					Comments comments = tuple.getT2();
					return new UserComments(user, comments);
				});
		userComments.subscribe(userComment -> logger.info(userComment.toString()));
	}

	private void exampleUserCommentsZipWith() {
		Mono<User> userMono = Mono.fromCallable(() -> new User("John", "Reyes"));
		Mono<Comments> commentsUserMono = Mono.fromCallable(() -> {
			Comments comments = new Comments();
			comments.addComment("Hola pepe, qué tal!");
			comments.addComment("Mañana voy para la playa!");
			comments.addComment("Estoy tomando el curso de spring con reactor");
			return comments;
		});
		
		Mono<UserComments> userComments = userMono.zipWith(commentsUserMono, (user, commentsUser) -> 
				new UserComments(user, commentsUser));
		userComments.subscribe(userComment -> logger.info(userComment.toString()));
	}

	private void exampleUserCommentsFlatMap() {
		Mono<User> userMono = Mono.fromCallable(() -> new User("John", "Reyes"));
		Mono<Comments> commentsUserMono = Mono.fromCallable(() -> {
			Comments comments = new Comments();
			comments.addComment("Hola pepe, qué tal!");
			comments.addComment("Mañana voy para la playa!");
			comments.addComment("Estoy tomando el curso de spring con reactor");
			return comments;
		});
		
		Mono<UserComments> userComments = userMono.flatMap(user -> commentsUserMono.map(comment -> 
				new UserComments(user, comment)));
		userComments.subscribe(userComment -> logger.info(userComment.toString()));
	}

	private void exampleCollectList() {
		List<User> users = new ArrayList<>();
		users.add(new User("Andres", "Guzman"));
		users.add(new User("Pedro", "Jimenez"));
		users.add(new User("Maria", "Sulivan"));
		users.add(new User("Diego", "Jaramillo"));
		users.add(new User("Juan", "Ramirez"));
		users.add(new User("Bruce", "Lee"));
		users.add(new User("Bruce", "Willis"));
		
		Flux.fromIterable(users)
				.collectList()
				.subscribe(list -> {
					list.forEach(item -> logger.info(item.toString()));
				});
	}

	private void exampleToString() {
		List<User> users = new ArrayList<>();
		users.add(new User("Andres", "Guzman"));
		users.add(new User("Pedro", "Jimenez"));
		users.add(new User("Maria", "Sulivan"));
		users.add(new User("Diego", "Jaramillo"));
		users.add(new User("Juan", "Ramirez"));
		users.add(new User("Bruce", "Lee"));
		users.add(new User("Bruce", "Willis"));
		
		Flux.fromIterable(users)
				.map(user -> user.getName().toUpperCase().concat(" ").concat(user.getLastName().toUpperCase()))
				.flatMap(name -> {
					if (name.contains("bruce".toUpperCase())) {
						return Mono.just(name);
					}
					return Mono.empty();
				})
				.map(name -> {
					return name.toLowerCase();
				})
				.subscribe(name -> logger.info(name.toString()));
	}

	private void exampleFlatMap() {
		List<String> names = new ArrayList<>();
		names.add("Andres Guzman");
		names.add("Pedro Jimenez");
		names.add("Maria Sulivan");
		names.add("Diego Jaramillo");
		names.add("Juan Ramirez");
		names.add("Bruce Lee");
		names.add("Bruce Willis");
		
		Flux.fromIterable(names)
				.map(name -> new User(name.split(" ")[0].toUpperCase(), name.split(" ")[1].toUpperCase()))
				.flatMap(user -> {
					if ("bruce".equalsIgnoreCase(user.getName())) {
						return Mono.just(user);
					}
					return Mono.empty();
				})
				.map(user -> {
					String name = user.getName().toLowerCase();
					user.setName(name);
					return user;
				})
				.subscribe(user -> logger.info(user.toString()));
	}

	private void exampleIterable() {
		List<String> names = new ArrayList<>();
		names.add("Andres Guzman");
		names.add("Pedro Jimenez");
		names.add("Maria Sulivan");
		names.add("Diego Jaramillo");
		names.add("Juan Ramirez");
		names.add("Bruce Lee");
		names.add("Bruce Willis");
		
		Flux<String> namesFlux = Flux.fromIterable(names); 
		Flux<User> users = namesFlux.map(name -> new User(name.split(" ")[0].toUpperCase(), name.split(" ")[1].toUpperCase()))
				.filter(user -> "bruce".equalsIgnoreCase(user.getName()))
				.doOnNext(user -> {
					if (user == null) {
						throw new RuntimeException("Nombres no pueden ser vacíos");
					}
					System.out.println(user.getName().concat(" ").concat(user.getLastName()));
				})
				.map(user -> {
					String name = user.getName().toLowerCase();
					user.setName(name);
					return user;
				});
		users.subscribe(user -> logger.info(user.toString()),
				error -> logger.error(error.getMessage()),
				new Runnable() {
					
					@Override
					public void run() {
						logger.info("Ha finalizado la ejecución del observable con éxito!");
					}
				});
	}
}
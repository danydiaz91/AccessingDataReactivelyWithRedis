package io.danydiaz.reactive.loader;

import java.util.UUID;

import javax.annotation.PostConstruct;

import org.springframework.data.redis.connection.ReactiveRedisConnectionFactory;
import org.springframework.data.redis.core.ReactiveRedisOperations;
import org.springframework.stereotype.Component;

import io.danydiaz.reactive.domain.Coffee;
import reactor.core.publisher.Flux;

/**
 * 
 * @author Dany Diaz
 *
 */
@Component
public class CoffeeLoader {

	private final ReactiveRedisConnectionFactory factory;
	private final ReactiveRedisOperations<String, Coffee> coffeeOps;
	
	public CoffeeLoader(ReactiveRedisConnectionFactory factory, ReactiveRedisOperations<String, Coffee> coffeeOps) {
		
		this.factory = factory;
		this.coffeeOps = coffeeOps;
	}
	
	@PostConstruct
	public void loadData() {
		
		factory.getReactiveConnection()
			.serverCommands()
			.flushAll()
			.thenMany(createCoffeeObjects())
			.thenMany(getAllCoffeeObjects())
			.subscribe(System.out::println);
	}
	
	public Flux<Boolean> createCoffeeObjects() {
		
		return Flux.just("Jet Black Redis", "Darth Redis", "Black Alert Redis")
				.map(name -> new Coffee(UUID.randomUUID().toString(), name))
				.flatMap(coffee -> coffeeOps.opsForValue().set(coffee.getId(), coffee));
	}
	
	public Flux<Coffee> getAllCoffeeObjects() {
		
		return coffeeOps.keys("*").flatMap(coffeeOps.opsForValue()::get);
	}
}

package api;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
@SpringBootApplication
@ComponentScan(basePackages = {"configuration", "api"})
public class ApplicationStartup {

	/**
	 * This event is executed as late as conceivably possible to indicate that 
	 * the application is ready to service requests. 
	 */
	@EventListener(ContextRefreshedEvent.class)
	public void contextRefreshedEvent() {
		return;
	}

	public static void main(String[] args) {
		SpringApplication.run(ApplicationStartup.class, args);
    }
}

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
		populateDB();
		return;
	}

	public static void main(String[] args) {
		SpringApplication.run(ApplicationStartup.class, args);
    }

	public void populateDB() {
		// Freezer saveIt = new Freezer(
		// 	"my freezerD",
		// 	null
		// );

		// Aliment alToSave = new Aliment(saveIt, "boeuf hachéD", "viande", "icon-batch1_kebab-2", 200.0, "g", null, null);
		// aliments.save(alToSave);

		

		// aliments.saveAll(saveIt.content);

				// List<Aliment> alimentss = (List<Aliment>) aliments.findAll();
		// System.out.println("alimentss" + alimentss);
		
		// // Set<Freezer> freezerss = (Set<Freezer>) freezers.findAll();
		// // Set<Freezer> freezerss = (Set<Freezer>) freezers.findAllWithAliments();
		// System.out.println("freezerss" + freezerss);
		
		// // Freezer freezer1 = freezers.findByIdWithContent(Long.valueOf('4'));
		// // System.out.println("freezer1" + freezer1);
		// // // System.out.println("freezer1.content" + freezer1.content);
	}
}

package api;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@SpringBootApplication
public class ApplicationStartup {

	@Autowired
	private FreezerRepository freezers;
	@Autowired
	private AlimentRepository aliments;
	/**
	 * This event is executed as late as conceivably possible to indicate that 
	 * the application is ready to service requests.
	 */
	@EventListener(ContextRefreshedEvent.class)
	public void contextRefreshedEvent() {
		System.out.println("inAppEvent");
		populateDB();
		return;
	}
	public static void main(String[] args) {
        SpringApplication.run(ApplicationStartup.class, args);
    }

	public void populateDB() {
		System.out.println("inPopulateDB");

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

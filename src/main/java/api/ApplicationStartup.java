package api;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
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

	public void populateDB() {
		System.out.println("inPopulateDB");

		aliments.save(new Aliment("boeuf haché", "viande", "icon--viande", 200.0, "g", null, null));
		aliments.save(new Aliment("frites", "fritur", "icon--frite", 1.0, "kg", null, null));
		aliments.save(new Aliment("icecream", "desert", "icon--icecream", 10.0, "", null, null));
		Aliment[] tab = {new Aliment(1L), new Aliment(2L), new Aliment(3L)};
		freezers.save(new Freezer(
				"my freezer",
				(List<Aliment>)aliments.findAll()
				));
	}
}

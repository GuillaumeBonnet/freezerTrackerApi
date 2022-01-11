package api.controller;

import java.util.Optional;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.converter.json.MappingJacksonValue;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import api.exceptionHandling.CustomException;
import api.model.Aliment;
import api.model.Freezer;
import api.model.JsonViews;
import api.model.User;
import api.repository.AlimentRepository;
import api.repository.FreezerRepository;
import api.repository.UserRepository;
import api.service.UserService;
import io.vavr.collection.HashSet;

// import io.vavr.collection.HashSet;

@Controller
@RequestMapping("/api")
public class MainController {

		
	@Autowired
	private FreezerRepository freezers;
	@Autowired
	private AlimentRepository aliments;
	@Autowired
	private UserRepository userRepo;
	@Autowired
	private UserService userService;

	/* -------------------------------------------------------------------------- */
	/*                                  freezers                                  */
	/* -------------------------------------------------------------------------- */
	
	@RequestMapping(path="/freezers", method=RequestMethod.POST, headers="Content-type=application/json")
	@ResponseBody
	public /*Freezer*/MappingJacksonValue saveFreezer(HttpServletRequest request, @RequestBody Freezer freezer) {
		User currentUser = this.userService.getCurrentUser();
		freezer.setUser(currentUser);
		freezers.save(freezer);

		final MappingJacksonValue result = new MappingJacksonValue(freezer);
		result.setSerializationView(JsonViews.Summary.class);
		return result;
	}
		
	@RequestMapping(path="/freezers", method=RequestMethod.GET)
	@ResponseBody
	public /*Set<Freezer>*/MappingJacksonValue getFreezers(HttpServletRequest request) {
		User currentUser = this.userService.getCurrentUser();
		Class<?> jsonViewClass;
		Set<Freezer> freezers;
		if ( request.getParameter("details") == null || ! request.getParameter("details").equals("all")) {
			freezers = userRepo.findByEmail_WithFreezers(currentUser.getEmail()).getFreezers();
			jsonViewClass = JsonViews.Summary.class;
		}
		else {
			freezers = userRepo.findByEmail_WithFreezersAndContent(currentUser.getEmail()).getFreezers();
			jsonViewClass = JsonViews.Details.class;
		}
		final MappingJacksonValue result = new MappingJacksonValue(freezers);
		result.setSerializationView(jsonViewClass);
		return result;
	}

	@RequestMapping(path="/freezers/{idFreezer}", method=RequestMethod.PUT, headers="Content-type=application/json")
	@ResponseBody
	public Freezer updateFreezer(HttpServletRequest request, @RequestBody Freezer freezer, @PathVariable("idFreezer") String idFreezer) {
		Long id = Long.valueOf(idFreezer);
		if (id == null) {
			throw new Error("Invalid idFreezer in the url.");
		}
		Freezer sourceFreezer = null;
		sourceFreezer = freezers.findById(id).get();
		if(sourceFreezer == null) {
			throw new Error("Freezer could not be retrieved from the database.");
		}

		User currentUser = this.userService.getCurrentUser();
		if(sourceFreezer.getUser() == null 
			|| !sourceFreezer.getUser().getId().equals(currentUser.getId())) {
			throw new Error("You cannot change a freezer you don't own.");
		}

		sourceFreezer.setName(freezer.getName());
		return freezers.save(sourceFreezer);
	}

	@RequestMapping(path="/freezers/{idFreezer}", method=RequestMethod.DELETE, headers="Content-type=application/json")
	@ResponseBody
	public void deleteFreezer(HttpServletRequest request, @PathVariable("idFreezer") String idFreezer) {
		// ToDoBetter: trigger on before delete which delete children instances
		// TODO : In Unit Test check that the children aliments are really deleted.
		Long id = Long.valueOf(idFreezer);
		if (id == null) {
			throw new Error("Invalid idFreezer in the url.");
		}
		Freezer sourceFreezer = null;
		sourceFreezer = freezers.findByIdWithAliments(id).get();
		
		if(sourceFreezer == null) {
			throw new Error("Freezer could not be retrieved from the database.");
		}

		User currentUser = this.userService.getCurrentUser();
		if(sourceFreezer.getUser() == null
			|| ! sourceFreezer.getUser().getId().equals(currentUser.getId()) ) {
			throw new Error("You cannot delete a freezer you don't own.");
		}

		Set<Aliment> content = sourceFreezer.getContent();
		if( content != null ) {
			aliments.deleteAll(content);
		}

		freezers.delete(sourceFreezer);
	}
	
	/* -------------------------------------------------------------------------- */
	/*                                  aliments                                  */
	/* -------------------------------------------------------------------------- */
	
	// @RequestMapping(path="/freezers/{idFreezer}/aliments", method=RequestMethod.GET, headers="Content-type=application/json")
	@RequestMapping(path="/freezers/{idFreezer}/aliments", method=RequestMethod.GET)
	@ResponseBody
	public Set<Aliment> getFreezerContent(HttpServletRequest request, @PathVariable("idFreezer") String idFreezer) {
		Optional<Freezer> freezer = freezers.findByIdWithAliments(Long.valueOf(idFreezer));

		checksIfFreezerOwned(freezer);
		return freezer.get().getContent();
	}
	
	@RequestMapping(path="/freezers/{idFreezer}/aliments", method=RequestMethod.POST, headers="Content-type=application/json")
	@ResponseBody
	public Aliment saveAliment(HttpServletRequest request, @PathVariable("idFreezer") String idFreezer, @RequestBody Aliment aliment) {

		Optional<Freezer> freezerOpt = freezers.findById(Long.valueOf(idFreezer));
		checksIfFreezerOwned(freezerOpt);
		Freezer freezer = freezerOpt.get();
		//TODO:  catch NoSuchElementException for custom error output
		aliment.setFreezer(
			freezer
		);
		aliments.save(aliment);
		aliment.setFreezer(null);
		return aliment;
	}
	
	@RequestMapping(path="/freezers/{idFreezer}/aliments/{idAliment}", method=RequestMethod.PUT, headers="Content-type=application/json")
	@ResponseBody
	public Aliment editAliment(HttpServletRequest request, @PathVariable("idFreezer") String idFreezer, @PathVariable("idAliment") String idAliment, @RequestBody Aliment aliment) {
		
		Optional<Freezer> freezerOpt = this.freezers.findByIdWithAliments(Long.valueOf(idFreezer));
		checksIfFreezerOwned(freezerOpt);
		Freezer freezer = freezerOpt.get();
		Long id = Long.valueOf(idAliment);
		if (id == null) {
			throw new Error("Invalid idAliment in the url.");
		}
		Aliment sourceAliment = HashSet.ofAll(freezer.getContent())
			.find((Aliment alim) -> alim.getId().equals(id))
			.get()
		;
		if(sourceAliment == null) {
			throw new Error("The aliment could not be found in this freezer.");		
		}
		sourceAliment.setCategory(aliment.getCategory());
		sourceAliment.setStoredDate(aliment.getStoredDate());
		sourceAliment.setExpirationDate(aliment.getExpirationDate());
		sourceAliment.setIconicFontName(aliment.getIconicFontName());
		sourceAliment.setName(aliment.getName());
		sourceAliment.setQuantityUnit(aliment.getQuantityUnit());		
		sourceAliment.setQuantity(aliment.getQuantity());
		return aliments.save(sourceAliment);
	}
	
	@RequestMapping(path="/freezers/{idFreezer}/aliments/{idAliment}", method=RequestMethod.DELETE)
	@ResponseBody
	public void deleteAliment(HttpServletRequest request, @PathVariable("idFreezer") String idFreezer, @PathVariable("idAliment") String idAliment) {
		Optional<Freezer> freezerOpt = freezers.findByIdWithAliments(Long.valueOf(idFreezer));
		checksIfFreezerOwned(freezerOpt);

		Long id = Long.valueOf(idAliment);
		if (id == null) {
			throw new Error("Invalid idAliment in the url.");
		}

		Boolean isAlimentInFreezer = HashSet.ofAll(freezerOpt.get().getContent())
			.exists((Aliment aliment) -> aliment.getId().equals(id));

		if(!isAlimentInFreezer) {
			throw new CustomException(String.format("The aliment of Id %s could not be found in freezer %s", idAliment, idFreezer));
		}
		
		aliments.deleteById(id);
	}

	/* -------------------------------------------------------------------------- */
	/*                                Utils Methods                               */
	/* -------------------------------------------------------------------------- */
	public void checksIfFreezerOwned(Optional<Freezer> freezer) {		
		if(!freezer.isPresent() ||
		! freezer.get().getUser().getId().equals(this.userService.getCurrentUser().getId())) { // TODO understand why there was no unboxing and Long != Long failed
			throw new CustomException("You can only interact with content of Freezers you own.");
		}		
	}

}

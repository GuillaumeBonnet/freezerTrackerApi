package api.controller;

import java.util.Set;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import api.model.Aliment;
import api.model.Freezer;
import api.repository.AlimentRepository;
import api.repository.FreezerRepository;

@Controller
@RequestMapping("/api")
public class MainController {

		
	@Autowired
	private FreezerRepository freezers;
	@Autowired
	private AlimentRepository aliments;

	/* -------------------------------------------------------------------------- */
	/*                                  freezers                                  */
	/* -------------------------------------------------------------------------- */
	
	@RequestMapping(path="/freezers", method=RequestMethod.POST, headers="Content-type=application/json")
	@ResponseBody
	public Freezer saveFreezer(HttpServletRequest request, @RequestBody Freezer freezer) {
		freezers.save(freezer);
		return freezer;
	}
		
	@RequestMapping(path="/freezers", method=RequestMethod.GET) //TODO: undo
	@ResponseBody
	public Set<Freezer> getFreezers(HttpServletRequest request) {
		Set<Freezer> fromQuery = freezers.findAllWithAliments();

		if ( request.getParameter("details") == null || ! request.getParameter("details").equals("complete")) {
			for(Freezer item : fromQuery) {
				item.setContent(null);
			}
		}

		return fromQuery;
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
		sourceFreezer = freezers.findByIdWithContent(id);
		
		if(sourceFreezer == null) {
			throw new Error("Freezer could not be retrieved from the database.");
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
		return aliments.findFreezerContent(Long.valueOf(idFreezer));
	}
	
	@RequestMapping(path="/freezers/{idFreezer}/aliments", method=RequestMethod.POST, headers="Content-type=application/json")
	@ResponseBody
	public Aliment saveAliment(HttpServletRequest request, @PathVariable("idFreezer") String idFreezer, @RequestBody Aliment aliment) {

		Freezer freezer = freezers.findById(Long.valueOf(idFreezer)).get();
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
	public Aliment editAliment(HttpServletRequest request, @PathVariable("idAliment") String idFreezer, @PathVariable("idAliment") String idAliment, @RequestBody Aliment aliment) {
		Long id = Long.valueOf(idAliment);
		if (id == null) {
			throw new Error("Invalid idAliment in the url.");
		}
		Aliment sourceAliment = null;
		try {
			sourceAliment = aliments.findById(id).get();			
		} catch(Exception e) {
			System.out.println("error" + e.getMessage());
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
	public void deleteAliment(HttpServletRequest request, @PathVariable("idAliment") String idAliment) {
		Long id = Long.valueOf(idAliment);
		if (id == null) {
			throw new Error("Invalid idAliment in the url.");
		}
		else {
			aliments.deleteById(id);
		}
	}
}

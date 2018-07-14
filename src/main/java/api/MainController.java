package api;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.*;
import org.springframework.boot.autoconfigure.*;
import org.springframework.stereotype.*;
import org.springframework.web.bind.annotation.*;


@CrossOrigin(origins = "http://localhost:4200")
@Controller
public class MainController {
	

	@Autowired
	private FreezerRepository freezers;
	@Autowired
	private AlimentRepository aliments;
	
	public static void main(String[] args) throws Exception {
        SpringApplication.run(MainController.class, args);      
    }
	
	@RequestMapping("/aliments**")
	@ResponseBody
	public List<Aliment> getAliments(HttpServletRequest request) {
    	return (List<Aliment>)aliments.findAll(); //"error no aliment for this id";
	}
	
	@RequestMapping(path="/aliment", method=RequestMethod.POST, headers="Content-type=application/json")
	@ResponseBody
	public Aliment saveAliment(HttpServletRequest request, @RequestBody Aliment aliment) {
		System.out.println("todo debug");
		System.out.println("todo debug" + request);
		return aliments.save(aliment);
	}
	
	@RequestMapping(path="aliment/{idAliment}", method=RequestMethod.PUT, headers="Content-type=application/json")
	@ResponseBody
	public Aliment editAliment(HttpServletRequest request, @PathVariable("idAliment") String idAliment, @RequestBody Aliment aliment) {
		Long id = null;
		id = Long.valueOf(idAliment);
		if (id == null) {
			return null;
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
	
	@RequestMapping(path="/aliment/{idAliment}", method=RequestMethod.DELETE, headers="Content-type=application/json")
	@ResponseBody
	public void deleteAliment(HttpServletRequest request, @PathVariable("idAliment") String idAliment) {
		Long id = null;
		id = Long.valueOf(idAliment);
		if (id == null) {
		}
		else {
			aliments.deleteById(id);
		}
	}
	
	@RequestMapping(path="/freezers", method=RequestMethod.POST, headers="Content-type=application/json")
	@ResponseBody
	public boolean saveFreezer(HttpServletRequest request, @RequestBody Freezer freezer) {
		try {
			freezers.save(freezer);
			return true;
		} catch(Exception exception) {
			return false;
		}
	}
	
	@RequestMapping("/freezer/{idFreezer}/aliments")
	@ResponseBody
	public Boolean saveAliment(@PathVariable("idFreezer") String idFreezer, @RequestBody Aliment alimentToSave, HttpServletRequest request) {
	    	Optional<Freezer> freezer = null;
	    	freezer = freezers.findById(Long.valueOf(idFreezer));
	    	if(freezer == null) {
	    		return false;
	    	}
	    	if(! aliments.existsById(alimentToSave.getId())) {
	    		try {
	    			alimentToSave = aliments.save(alimentToSave);
	    			return true;
	    		} catch(Exception exception) {
	    			return false;
	    		}
    		}
	    	freezer.get().content.add(alimentToSave);
	    	
	    	return true;
	}
	
	@RequestMapping("/freezer/{idFreezer}/aliment/{idAliment}**")
	@ResponseBody
	public Aliment getAliment(@PathVariable("idFreezer") String idFreezer, @PathVariable("idAliment") String idAliment, HttpServletRequest request) {
		if( freezers.existsById(Long.valueOf(idFreezer)) ) {
	    	Optional<Freezer> freezer = freezers.findById(Long.valueOf(idFreezer));
	    	for(Aliment aliment:freezer.get().content) {
	    		if(aliment.getId() == Long.valueOf(idAliment)) {
	    			return aliment;
	    		}
	    	}
	    	return null; //"error no aliment for this id";
	    }
	    else {
	    	return null; // "error no freezer n" + idFreezer;
	    }
	}
	
	@RequestMapping("/freezer/{idFreezer}**")
	@ResponseBody
	public Optional<Freezer> getFreezerContent(@PathVariable("idFreezer") String idFreezer, HttpServletRequest request) {
		if( freezers.existsById(Long.valueOf(idFreezer)) ) {
	    	return freezers.findById(Long.valueOf(idFreezer)); //"error no aliment for this id";
	    }
	    else {
	    	return null; // "error no freezer n" + idFreezer;
	    }
	}
	
	@RequestMapping(path="/freezers**", method=RequestMethod.GET, headers="Content-type=application/json")
	@ResponseBody
	public List<Freezer> getFreezers(HttpServletRequest request) {
    	return (List<Freezer>)freezers.findAll(); //"error no aliment for this id";
	}    
}

package api;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.*;
import org.springframework.boot.autoconfigure.*;
import org.springframework.stereotype.*;
import org.springframework.web.bind.annotation.*;



@Controller
@EnableAutoConfiguration
public class MainController {
	

	@Autowired
	private FreezerRepository freezers;
	@Autowired
	private AlimentRepository aliments;
	
	public static void main(String[] args) throws Exception {
        SpringApplication.run(MainController.class, args);      
    }
	
//	@RequestMapping("/freezer/{idFreezer}/aliment/{idAliment}**")
//	@ResponseBody
//	public Aliment getAliment(@PathVariable("idFreezer") String idFreezer, @PathVariable("idAliment") String idAliment, HttpServletRequest request) {
//		if( freezers.exists(Long.valueOf(idFreezer)) ) {
//	    	Freezer freezer = freezers.findOne(Long.valueOf(idFreezer));
//	    	for(Aliment aliment:freezer.content) {
//	    		if(aliment.getId() == Long.valueOf(idAliment)) {
//	    			return aliment;
//	    		}
//	    	}
//	    	return null; //"error no aliment for this id";
//	    }
//	    else {
//	    	return null; // "error no freezer n" + idFreezer;
//	    }
//	}
//	
//	@RequestMapping("/freezer/{idFreezer}**")
//	@ResponseBody
//	public Freezer getFreezerContent(@PathVariable("idFreezer") String idFreezer, HttpServletRequest request) {
//		if( freezers.exists(Long.valueOf(idFreezer)) ) {
//	    	return freezers.findOne(Long.valueOf(idFreezer)); //"error no aliment for this id";
//	    }
//	    else {
//	    	return null; // "error no freezer n" + idFreezer;
//	    }
//	}
//	
//	@RequestMapping(path="/freezers**", method=RequestMethod.GET, headers="Content-type=application/json")
//	@ResponseBody
//	public List<Freezer> getFreezers(HttpServletRequest request) {
//    	return (List<Freezer>)freezers.findAll(); //"error no aliment for this id";
//	}
//	
//	@RequestMapping("/aliments**")
//	@ResponseBody
//	public List<Aliment> getAliments(HttpServletRequest request) {
//    	return (List<Aliment>)aliments.findAll(); //"error no aliment for this id";
//	}

    
}

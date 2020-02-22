package api.controller.test;


import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.io.File;
import java.sql.Date;
import java.util.Optional;
import java.util.Set;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.assertj.core.util.Files;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.web.SpringJUnitWebConfig;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import api.ApplicationStartup;
import api.model.Aliment;
import api.model.Freezer;
import api.model.User;
import api.repository.AlimentRepository;
import api.repository.FreezerRepository;
import api.repository.UserRepository;
import api.service.UserService;
import io.vavr.collection.HashSet;




@SpringBootTest(classes = ApplicationStartup.class)
@AutoConfigureTestDatabase
@SpringJUnitWebConfig
@TestPropertySource(locations = "classpath:application-integration-test.properties")
public class MainController_TEST {
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepo;

    @Autowired
    private FreezerRepository freezerRepo;
    
    @Autowired
    private AlimentRepository alimentRepo;

    @Autowired
    UserService userService;

    @BeforeEach
    public void setup(WebApplicationContext wac) {
        this.mockMvc = MockMvcBuilders
            .webAppContextSetup(wac)
            // .apply(springSecurity())
            .build();
        this.alimentRepo.deleteAll();
        this.freezerRepo.deleteAll();
        this.userRepo.deleteAll();
        createData();
        this.userService.authenticateUser(this.userRepo.findByUsername("username"), "password");
    }

    @Test
	public void saveFreezer() throws Exception {
        String saveFreezerJson = Files.contentOf(new File(getClass().getResource("/mocks/saveFreezer.json").getFile()), "UTF-8");
        Long countBeforeWS = this.freezerRepo.count();
        String jsonResult = this.mockMvc.perform(
                post("/api/freezers")
                .header("Content-type", "application/json")
                .content(saveFreezerJson)
            )
            .andDo(print())
            .andExpect(status().is2xxSuccessful())
            .andReturn()
            .getResponse().getContentAsString()
        ;
        assertEquals(1, this.freezerRepo.count()-countBeforeWS, "Only 1 Freezer should have been created.");
        ObjectMapper objectMapper = new ObjectMapper();
        Freezer freezerSave_jsonResult = objectMapper.readValue(jsonResult, Freezer.class);
        Freezer freezerSave_db = this.freezerRepo.findByName(freezerSave_jsonResult.getName());

        assertEquals(freezerSave_jsonResult.getName(), freezerSave_db.getName());
        assertEquals(freezerSave_jsonResult.getId(), freezerSave_db.getId());
    }

    //TODO getFreezersWithDetails
    
    @Test
	public void getFreezers() throws Exception {        
        String jsonResult = this.mockMvc.perform(
            get("/api/freezers")
        )
            .andDo(print())
            .andExpect(status().isOk())
            .andReturn()
            .getResponse().getContentAsString()
        ;
        ObjectMapper objectMapper = new ObjectMapper();
        Set<Freezer> returnedFreezers = objectMapper.readValue(jsonResult, new TypeReference<Set<Freezer>>() {});
        
        assertEquals(2, returnedFreezers.size(), "There should be only 2 freezers in the returned Response.");
        for(Freezer freezer: returnedFreezers) {
            assertNotNull(freezer.getId(), "Every Freezer should have an Id.");
            freezer.setId(null);
        }
        HashSet<String> returnedFreezerNames = HashSet.ofAll(returnedFreezers)
            .map(freezera -> freezera.getName());

        assertTrue(
            returnedFreezerNames.contains("Main Freezer")
            , "One of the freezer should be the Main Freezer."
        );
        assertTrue(returnedFreezerNames.contains("Beer Freezer")
            , "One of the freezer should be the Beer Freezer."
        );
    }

    @Test
	public void updateFreezer() throws Exception {
        String updateFreezerJson = Files.contentOf(new File(getClass().getResource("/mocks/updateFreezer.json").getFile()), "UTF-8");
        Long countBeforeWS = this.freezerRepo.count();
        Long mainFreezerId = this.freezerRepo.findByName("Main Freezer").getId();
        this.mockMvc.perform(
                put("/api/freezers/" + mainFreezerId)
                .header("Content-type", "application/json")
                .content(updateFreezerJson)
            )
            .andDo(print())
            .andExpect(status().is2xxSuccessful())
            .andReturn()
            .getResponse().getContentAsString()
        ;
        assertEquals(0, this.freezerRepo.count()-countBeforeWS, "Freezer count should stay the same.");
        Freezer freezer_db = this.freezerRepo.findById(mainFreezerId).get();

        assertEquals("newFreezerModified", freezer_db.getName());
    }

    @Test
    public void deleteFreezer() throws Exception {
        Long countBeforeWS = this.freezerRepo.count();
        Long mainFreezerId = this.freezerRepo.findByName("Main Freezer").getId();
        this.mockMvc.perform(
                delete("/api/freezers/" + mainFreezerId)
                .header("Content-type", "application/json")
            )
            .andDo(print())
            .andExpect(status().is2xxSuccessful())
            .andReturn()
            .getResponse().getContentAsString()
        ;
        assertEquals(-1, this.freezerRepo.count()-countBeforeWS, "Freezer count should be -1.");
        assertFalse(this.freezerRepo.findById(mainFreezerId).isPresent(), "The freezer should not be in DB anymore.");
    }

    @Test
    public void saveAliment() throws Exception {
        Long countBeforeWS = this.alimentRepo.count();
        Long mainFreezerId = this.freezerRepo.findByName("Main Freezer").getId();

        String newAlimentJson = Files.contentOf(new File(getClass().getResource("/mocks/newAliment.json").getFile()), "UTF-8");


        String responseJson = this.mockMvc.perform(
                post("/api/freezers/" + mainFreezerId + "/aliments")
                .header("Content-type", "application/json")
                .content(newAlimentJson)
            )
            .andDo(print())
            .andExpect(status().is2xxSuccessful())
            .andReturn()
            .getResponse().getContentAsString()
        ;
        assertEquals(1, this.alimentRepo.count()-countBeforeWS, "There should be 1 more aliment in DB.");
        User mainUser = this.userRepo.findByEmail_WithFreezersAndContent("email");
        
        Freezer mainFreezer = null;
        for(Freezer freezer : mainUser.getFreezers())
            if(freezer.getName() == "Main Freezer")
                mainFreezer = freezer;

        assertNotNull(mainFreezer);

        Aliment newAliment = null;
        for(Aliment aliment : mainFreezer.getContent())
            if( aliment.getName().equals("newName"))
                newAliment = aliment;

        assertNotNull(newAliment);

        final Aliment newAlimentFinal = newAliment;

        assertAll("New aliment has not been stored correctly.",
            () -> assertNotNull(newAlimentFinal.getId(), "Id should not be empty"),
            () -> assertEquals("newName", newAlimentFinal.getName(), "Wrong aliment.Name"),
            () -> assertEquals("newCategory", newAlimentFinal.getCategory(), "Wrong aliment.Category"),
            () -> assertEquals(Date.valueOf("2234-5-6").toString(), newAlimentFinal.getExpirationDate().toString(), "Wrong aliment.ExpirationDate"),
            () -> assertEquals("newIconicFontName", newAlimentFinal.getIconicFontName(), "Wrong aliment.IconicFontName"),
            () -> assertEquals(1.0, newAlimentFinal.getQuantity(), "Wrong aliment.Quantity"),
            () -> assertEquals("newQuantityUnit", newAlimentFinal.getQuantityUnit(), "Wrong aliment.QuantityUnit"),
            () -> assertEquals(Date.valueOf("1234-5-6").toString(), newAlimentFinal.getStoredDate().toString(), "Wrong aliment.StoredDate")
        );
    }

    @Test
    public void editAliment() throws Exception {
        Long countBeforeWS = this.alimentRepo.count();
        Long mainFreezerId = this.freezerRepo.findByName("Main Freezer").getId();
        Long camembertId = this.alimentRepo.findByName("camembert").getId();

        String editAlimentJson = Files.contentOf(new File(getClass().getResource("/mocks/editAliment.json").getFile()), "UTF-8");


        String responseJson = this.mockMvc.perform(
                put("/api/freezers/" + mainFreezerId + "/aliments/" + camembertId)
                .header("Content-type", "application/json")
                .content(editAlimentJson)
            )
            .andDo(print())
            .andExpect(status().is2xxSuccessful())
            .andReturn()
            .getResponse().getContentAsString()
        ;
        assertEquals(0, this.alimentRepo.count()-countBeforeWS, "Aliment count should not change.");
        
        Aliment changedCamembert = this.alimentRepo.findById(camembertId).get();    
        assertNotNull(changedCamembert);  

        assertAll("Changed aliment has not been updated correctly.",
            () -> assertNotNull(changedCamembert.getId(), "Id should not be empty"),
            () -> assertEquals("changedName", changedCamembert.getName(), "Wrong aliment.Name"),
            () -> assertEquals("fromage", changedCamembert.getCategory(), "Wrong aliment.Category"),
            () -> assertEquals(Date.valueOf("2040-1-1").toString(), changedCamembert.getExpirationDate().toString(), "Wrong aliment.ExpirationDate"),
            () -> assertEquals("icon-batch1_steak", changedCamembert.getIconicFontName(), "Wrong aliment.IconicFontName"),
            () -> assertEquals(1.0, changedCamembert.getQuantity(), "Wrong aliment.Quantity"),
            () -> assertEquals("fromages", changedCamembert.getQuantityUnit(), "Wrong aliment.QuantityUnit"),
            () -> assertEquals(Date.valueOf("2030-1-1").toString(), changedCamembert.getStoredDate().toString(), "Wrong aliment.StoredDate")
        );
    }

    @Test
    public void deleteAliment() throws Exception {
        Long countBeforeWS = this.alimentRepo.count();
        Long mainFreezerId = this.freezerRepo.findByName("Main Freezer").getId();
        Long camembertId = this.alimentRepo.findByName("camembert").getId();

        this.mockMvc.perform(
                delete("/api/freezers/" + mainFreezerId + "/aliments/" + camembertId)
                .header("Content-type", "application/json")
            )
            .andDo(print())
            .andExpect(status().is2xxSuccessful())
            .andReturn()
        ;
        assertEquals(-1, this.alimentRepo.count()-countBeforeWS, "There should be one less Aliment in DB.");
        
        Optional<Aliment> changedCamembert_opt = this.alimentRepo.findById(camembertId);    
        assertFalse(changedCamembert_opt.isPresent(), "Camembert Aliment should have been deleted");
    }

    @Test
    public void deleteAliment_fromAnotherFreezer() throws Exception {
        Long countBeforeWS = this.alimentRepo.count();
        Long mainFreezerId = this.freezerRepo.findByName("Beer Freezer").getId();
        Long camembertId = this.alimentRepo.findByName("camembert").getId();

        String jsonDeleteReturned = this.mockMvc.perform(
                delete("/api/freezers/" + mainFreezerId + "/aliments/" + camembertId)
                .header("Content-type", "application/json")
            )
            .andDo(print())
            .andExpect(status().is4xxClientError())
            .andExpect(jsonPath("$.message").value(String.format("The aliment of Id %s could not be found in freezer %s", camembertId, mainFreezerId)))
            .andReturn().getResponse().getContentAsString()
        ;
        assertEquals(0, this.alimentRepo.count()-countBeforeWS, "Aliment count should not change.");

        Optional<Aliment> changedCamembert_opt = this.alimentRepo.findById(camembertId);    
        assertTrue(changedCamembert_opt.isPresent(), "Camembert Aliment should NOT have been deleted");
    }

    /* -------------------------------------------------------------------------- */
    /*                                Utils methods                               */
    /* -------------------------------------------------------------------------- */
    
    private void createData() {

        /* -------------------------------------------------------------------------- */
        /*                                  Main User                                 */
        /* -------------------------------------------------------------------------- */

        Aliment camembert = new Aliment();
            camembert.setCategory("fromage");
            camembert.setExpirationDate(Date.valueOf("2040-1-1"));
            camembert.setIconicFontName("icon-batch1_steak");
            camembert.setName("camembert");
            camembert.setQuantity(1.0);
            camembert.setQuantityUnit("fromages");
            camembert.setStoredDate(Date.valueOf("2030-1-1"));

        Aliment beer = new Aliment();
            beer.setCategory("beverage");
            beer.setExpirationDate(Date.valueOf("2040-1-1"));
            beer.setIconicFontName("icon-batch1_steak");
            beer.setName("beer");
            beer.setQuantity(6D);
            beer.setQuantityUnit("bottles");
            beer.setStoredDate(Date.valueOf("2030-1-1"));

        Freezer mainFreezer = new Freezer();
            mainFreezer.setName("Main Freezer");
            mainFreezer.setContent(Set.of(camembert));
        
        Freezer beerFreezer = new Freezer();
            beerFreezer.setName("Beer Freezer");
            beerFreezer.setContent(Set.of(beer));

        User mainUser = new User("username", "{noop}password", "email", true, "ROLE_USER");
            mainUser.setFreezers(Set.of(mainFreezer, beerFreezer));
            mainFreezer.setUser(mainUser);
            beerFreezer.setUser(mainUser);
            camembert.setFreezer(mainFreezer);
            beer.setFreezer(beerFreezer);

        /* -------------------------------------------------------------------------- */
        /*                               Vegetarian User                              */
        /* -------------------------------------------------------------------------- */

        Aliment carrots = new Aliment();
            beer.setCategory("veggies");
            beer.setExpirationDate(Date.valueOf("2040-1-1"));
            beer.setIconicFontName("icon-batch1_steak");
            beer.setName("carrots");
            beer.setQuantity(1D);
            beer.setQuantityUnit("kg");
            beer.setStoredDate(Date.valueOf("2030-1-1"));

        Freezer freezer_vegetarian = new Freezer();
            freezer_vegetarian.setName("freezer_vegetarian");
            freezer_vegetarian.setContent(Set.of(carrots));

        User vegetarianUser = new User("vegetarian_Username", "{noop}vegetarian_Password", "vegetarian_Email", true, "ROLE_USER");
            vegetarianUser.setFreezers(Set.of(freezer_vegetarian));
            freezer_vegetarian.setUser(vegetarianUser);
            carrots.setFreezer(freezer_vegetarian);
        
        this.userRepo.saveAll(Set.of(mainUser, vegetarianUser)); // freezers and aliments are saved too
    }
}

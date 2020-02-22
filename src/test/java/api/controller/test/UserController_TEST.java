package api.controller.test;

import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.io.File;
import java.util.Calendar;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.fasterxml.jackson.databind.ObjectMapper;

import org.assertj.core.util.Files;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.test.context.support.WithAnonymousUser;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.web.SpringJUnitWebConfig;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import api.ApplicationStartup;
import api.controller.UserController;
import api.model.User;
import api.model.VerificationToken;
import api.repository.UserRepository;
import api.repository.VerificationTokenRepository;
import api.service.UserService;


@SpringBootTest(classes = ApplicationStartup.class)
@AutoConfigureTestDatabase
@SpringJUnitWebConfig
@TestPropertySource(locations = "classpath:application-integration-test.properties")
public class UserController_TEST {
    
    private MockMvc mockMvc;
    @Autowired
    private UserRepository userRepo;
    @Autowired
    private VerificationTokenRepository tokenRepo;
    @Autowired
    private UserService userService;
    @MockBean
    private JavaMailSender mailSender;

    @BeforeEach
    @WithAnonymousUser
    void setup(WebApplicationContext wac) {
        this.mockMvc = MockMvcBuilders
            .webAppContextSetup(wac)
            // .apply(springSecurity())
            .build();
        this.tokenRepo.deleteAll();
        this.userRepo.deleteAll();
    }

    @Test
	public void loginFail() throws Exception {
        String requestBody = Files.contentOf(new File(getClass().getResource("/mocks/login.json").getFile()), "UTF-8");
        this.mockMvc.perform(
                post("/api/users/login")
                .content(requestBody)
                .header("Content-type", "application/json")
            )
            .andDo(print())
            .andExpect(status().is4xxClientError())
            .andExpect(jsonPath("$.message").value("There is no User which has the username given."));
        ;    
        
        
	}    

    @Test
    @WithAnonymousUser
	public void register() throws Exception {
        ArgumentCaptor<SimpleMailMessage> mailCaptor = ArgumentCaptor.forClass(SimpleMailMessage.class);
        Mockito.doNothing().when(this.mailSender).send(isA(SimpleMailMessage.class));

        Long nbUsers_BeforeRequest = this.userRepo.count();
        String requestBody = Files.contentOf(new File(getClass().getResource("/mocks/registration.json").getFile()), "UTF-8");
        
        MvcResult registerResult = this.mockMvc.perform(
            post("/api/users/registration")
            .content(requestBody)
            .header("Content-type", "application/json")
            )
            .andDo(print())
            .andExpect(status().is2xxSuccessful())
            .andReturn()
        ;

        /* -------------------------------------------------------------------------- */
        /*                                User Created                                */
        /* -------------------------------------------------------------------------- */
            
        Long nbUsers_AfterRequest = this.userRepo.count();
        Long nbUserCreated = nbUsers_AfterRequest - nbUsers_BeforeRequest;
        assertEquals(1, nbUserCreated, "1 User should have been created.");
        User userCreated = this.userRepo.findByEmail("email1@email.com");
        assertEquals(false, userCreated.getIsEnabled(), "The User should not be activated yet.");

        /* -------------------------------------------------------------------------- */
        /*                                  Mail sent                                 */
        /* -------------------------------------------------------------------------- */
        verify(this.mailSender, times(1)).send(mailCaptor.capture());
        assertTrue("The Username should be included in the body of the email.", mailCaptor.getValue().getText().contains(userCreated.getUsername()));
        
        /* -------------------------------------------------------------------------- */
        /*                                 Verif Token                                */
        /* -------------------------------------------------------------------------- */
        VerificationToken verifToken = this.tokenRepo.findByUser(userCreated);
        Long minutesTillExpiration = (verifToken.expirationDate.getTime() - Calendar.getInstance().getTimeInMillis())/(1000*60);
        Long EXPIRATION_IN_MIN = 60L * 24;
        assertTrue("The expiration date of the token is wrong.", minutesTillExpiration > 0.90 * EXPIRATION_IN_MIN && minutesTillExpiration <= EXPIRATION_IN_MIN);

        /* -------------------------------------------------------------------------- */
        /*                               Activation link                              */
        /* -------------------------------------------------------------------------- */
        Pattern patt = Pattern.compile("(https?|ftp|file)://[-a-zA-Z0-9+&@#/%?=~_|!:,.;]*[-a-zA-Z0-9+&@#/%=~_|]");
        Matcher matcher = patt.matcher(mailCaptor.getValue().getText());
        matcher.find();
        matcher.find();
        String frontEndUrlFromEmail = matcher.group(0);
        String tokenFromEmail = frontEndUrlFromEmail.split("/confirm-registration/")[1];        
        
        MvcResult activationResult = this.mockMvc.perform(
                post("/api/users/confirm-registration")
                .content(tokenFromEmail)
                .header("Content-type", "application/json")
            )
            .andDo(print())
            .andExpect(status().is2xxSuccessful())
            .andReturn()
        ;
        userCreated = this.userRepo.findByEmail(userCreated.getEmail());
        assertTrue("User should have been activated by the email link", userCreated.isEnabled());
        User currentlyAuthentifiedUser = (User)SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        assertEquals(userCreated.getUsername(), currentlyAuthentifiedUser.getUsername(), "The User authentified should be the User created.");
    }

    @Test
    public void info() throws Exception {
        User registeredUser = createActivatedUser();
        this.userService.authenticateUser(registeredUser, "password1");
        this.mockMvc.perform(
                get("/api/users/info")
            )
            .andDo(print())
            .andExpect(status().is2xxSuccessful())
            .andExpect(jsonPath("$.username").value("username1"))
            .andExpect(jsonPath("$.email").value("email1@email.com"))
        ;
        
    }
    
    @Test
    @WithAnonymousUser
    public void login() throws Exception {
        assertFalse(this.userService.isLoggedIn(), "No User should be logged in before the test.");
        User registeredUser = createActivatedUser();
        String loginJson = Files.contentOf(new File(getClass().getResource("/mocks/login.json").getFile()), "UTF-8");
        this.mockMvc.perform(
                post("/api/users/login")
                .header("Content-type", "application/json")
                .content(loginJson)
            )
            .andDo(print())
            .andExpect(status().is2xxSuccessful())
        ;

        ObjectMapper objectMapper = new ObjectMapper();
        UserController.UserLoginDto loginData = objectMapper.readValue(loginJson, UserController.UserLoginDto.class);
        assertEquals(
            loginData.username
            , this.userService.getCurrentUser().getUsername()
            , "The user of the loginJson should have been logged in."
        );
    }

    @Test
    public void logout() throws Exception { //TODO fix test
        User registeredUser = createActivatedUser();
        this.userService.authenticateUser(registeredUser, "password1");
        assertEquals(
            this.userService.getCurrentUser().getUsername()
            , "username1"
            , "The user 'username1' should be logged in before the test occurs."
        );
        this.mockMvc.perform(
                post("/api/users/logout")
                // .with(csrf().asHeader())
            )
            .andDo(print())
            .andExpect(status().is2xxSuccessful())
        ;
        assertEquals(
            this.userService.getCurrentUser().getUsername()
            , "anonymous"
            , "No user should be logged in."
        );
    }

/* -------------------------------------------------------------------------- */
/*                                    Utils                                   */
/* -------------------------------------------------------------------------- */

    public User createActivatedUser() throws Exception {
        String registrationJson = Files.contentOf(new File(getClass().getResource("/mocks/registration.json").getFile()), "UTF-8");
        ObjectMapper objectMapper = new ObjectMapper();
        UserController.UserRegistrationDto registrationData = objectMapper.readValue(registrationJson, UserController.UserRegistrationDto.class);
        User registeredUser = this.userService.registerUser(registrationData);
        registeredUser.setIsEnabled(true);
        return this.userRepo.save(registeredUser);
    }
}

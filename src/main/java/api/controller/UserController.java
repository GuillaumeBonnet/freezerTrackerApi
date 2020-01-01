package api.controller;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import javax.persistence.EntityExistsException;
import javax.servlet.http.HttpSession;
import javax.validation.Constraint;
import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import javax.validation.Payload;
import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import org.hibernate.exception.ConstraintViolationException;
import org.postgresql.util.PSQLException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.stereotype.*;
import org.springframework.validation.BindingResult;
import org.springframework.validation.Errors;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.request.WebRequest;

import api.event.OnRegistrationCompleteEvent;
import api.exceptionHandling.CustomException;
import api.model.User;
import api.model.VerificationToken;
import api.repository.UserRepository;
import api.repository.VerificationTokenRepository;

//@CrossOrigin(origins = "http://localhost:4200/ https://freezer-practice-front.herokuapp.com/")
@CrossOrigin(origins = "*")
@Controller
@RequestMapping("/api/users")
public class UserController {

	private static final RetentionPolicy RUNTIME = null;

	// TODO: dig into db xss/SQL sanization

	/* -------------------------------------------------------------------------- */
	/*                             Instance variables                             */
	/* -------------------------------------------------------------------------- */

		@Autowired
		ApplicationEventPublisher eventPublisher;

		@Autowired
		private UserRepository userRepo;

		@Autowired
		private VerificationTokenRepository tokenRepo;

		@Autowired
		private AuthenticationManager authenticationManager;

	/* -------------------------------------------------------------------------- */
	/*                                   Methods                                  */
	/* -------------------------------------------------------------------------- */

		@RequestMapping(value = "/confirm-registration", method = RequestMethod.POST)
		@ResponseBody
		public void activateConfirmedAccount(@RequestBody String token) {
			if( !SecurityContextHolder.getContext().getAuthentication().getName().equals("anonymousUser") ) {
				return;
			}
			VerificationToken tokenEntity = this.tokenRepo.findByToken(token);
			if(tokenEntity == null) {
				throw new CustomException("The token could not be found.");
			}
			//time
			tokenEntity.user.setIsEnabled(true);
			userRepo.save(tokenEntity.user);

			Authentication authentication =  new UsernamePasswordAuthenticationToken(tokenEntity.user, null, tokenEntity.user.getAuthorities());
			SecurityContextHolder.getContext().setAuthentication(authentication);

			this.tokenRepo.delete(tokenEntity);
		}

		@RequestMapping(value = "/registration", method = RequestMethod.POST)
		@ResponseBody
		public void registerUserAccount(
			@RequestBody @Valid UserRegistrationDto accountDto
			, BindingResult result
			, WebRequest request) {


				//TODO: validity of username & password 20char, email: 255char (in DTO)

			abortIfValidationErrors(result);

			User registered = createUserAccount(accountDto, result);

			if (registered == null) {
				throw new CustomException("User not registered");
			}

			try {
				eventPublisher.publishEvent(
					new OnRegistrationCompleteEvent(
						registered
						, request.getLocale()
					)
				);
			} catch (Exception me) {
				throw new CustomException("User was registered but the activation email could not be sent."); //TODO:Label
			}
		}

		@RequestMapping(value = "/info")
		@ResponseBody
		public UserInfoDto getUserInfo() {
			User loggedUser = (User)SecurityContextHolder.getContext().getAuthentication().getPrincipal();
			if( !loggedUser.isEnabled() || loggedUser.getUsername().equals("anonymousUser") ) {
				throw new CustomException("User is not logged-in.");
			}
			return new UserInfoDto(loggedUser.getUsername(), loggedUser.getEmail());
		}

		@RequestMapping(value = "/login", method = RequestMethod.POST)
		@ResponseBody
		public void login(@RequestBody @Valid UserLoginDto loginDto, BindingResult result, WebRequest request) {
			abortIfValidationErrors(result);

			User userToLogin = this.userRepo.findByUsername(loginDto.username);

			if(userToLogin == null) {
				throw new CustomException("There is no User which has the username given.");
			}
			Authentication authenticationRequest =  new UsernamePasswordAuthenticationToken(userToLogin, loginDto.password, userToLogin.getAuthorities());
			Authentication authenticationResponse;
			try {
				authenticationResponse = this.authenticationManager.authenticate(authenticationRequest);
			}
			catch(AuthenticationException e) {
				throw new CustomException(e.getMessage());
			}

			SecurityContextHolder.getContext().setAuthentication(authenticationResponse);
		}

	/* -------------------------------------------------------------------------- */
	/*                                Utils methods                               */
	/* -------------------------------------------------------------------------- */

		private void abortIfValidationErrors(BindingResult result) {
			if (result.hasErrors()) {	
				List<ObjectError> errors = result.getAllErrors();
				List<String> exceptionDetails = new ArrayList<String>();
				for(ObjectError error : errors) {
					if(error instanceof FieldError) {
						exceptionDetails.add(
							String.format(
								"The field [%s] has an error: %s.", //TODO:Label
								((FieldError)error).getField(), ((FieldError)error).getDefaultMessage()
							)
						);
					}
					else {
						exceptionDetails.add(
							error.getDefaultMessage()
						);
					}
				}
				throw new CustomException("JSON Validation Error", exceptionDetails);
			}
		}
		
		private User createUserAccount(UserRegistrationDto accountDto, BindingResult result) {

			BCryptPasswordEncoder encoder = new BCryptPasswordEncoder(16);
			User newUser = new User(accountDto.username, "{bcrypt}" + encoder.encode(accountDto.password), accountDto.email, false, "ROLE_USER");

			// TOIMPROVE: one querry findBy for both email and username
			if( this.userRepo.findByEmail(accountDto.email) != null ) {
				throw new CustomException("There is already a user registered with this email address: '" + accountDto.email + "'.");
			}

			if( this.userRepo.findByUsername(accountDto.username) != null ) {
				throw new CustomException("There is already a user registered with this username: '" + accountDto.username + "'.");
			}

			newUser = this.userRepo.save(newUser);
			return newUser;
		}
	/* -------------------------------------------------------------------------- */
	/*                                    inner DTOs                              */
	/* -------------------------------------------------------------------------- */

		@PasswordMatches
		public static class UserRegistrationDto {
			@NotNull
			@NotEmpty
			public String username;

			@NotNull
			@NotEmpty
			public String password;

			@NotNull
			@NotEmpty
			public String matchingPassword;

			@NotNull
			@NotEmpty
			@ValidEmail
			public String email;
		}
		public static class UserLoginDto {
			@NotNull
			@NotEmpty
			public String username;

			@NotNull
			@NotEmpty
			public String password;
		}

		public static class UserInfoDto {
			public String username;
			public String email;

			public UserInfoDto(String username, String email) {
				this.username = username;
				this.email = email;
			}
		}

	/* -------------------------------------------------------------------------- */
	/*                        Password Matching Annotation                        */
	/* -------------------------------------------------------------------------- */

		@Target({ElementType.TYPE, ElementType.ANNOTATION_TYPE}) 
		@Retention(RetentionPolicy.RUNTIME)
		@Constraint(validatedBy = PasswordMatchesValidator.class)
		@Documented
		public @interface PasswordMatches { 
			String message() default "Passwords don't match";
			Class<?>[] groups() default {}; 
			Class<? extends Payload>[] payload() default {};
		}

		public class PasswordMatchesValidator implements ConstraintValidator<PasswordMatches, Object> { 	
			@Override
			public void initialize(PasswordMatches constraintAnnotation) {       
			}
			
			@Override
			public boolean isValid(Object obj, ConstraintValidatorContext context){   
				UserRegistrationDto user = (UserRegistrationDto) obj;
				return user.password.equals(user.matchingPassword);    
			}     
		}

	/* -------------------------------------------------------------------------- */
	/*                          Email checking annotation                         */
	/* -------------------------------------------------------------------------- */
		@Target({ElementType.TYPE, ElementType.FIELD, ElementType.ANNOTATION_TYPE}) 
		@Retention(RetentionPolicy.RUNTIME)
		@Constraint(validatedBy = EmailValidator.class)
		@Documented
		public @interface ValidEmail {   
			String message() default "Invalid email";
			Class<?>[] groups() default {}; 
			Class<? extends Payload>[] payload() default {};
		}

		public class EmailValidator implements ConstraintValidator<ValidEmail, String> {     
			private Pattern pattern;
			private Matcher matcher;
			private static final String EMAIL_PATTERN = "^[_A-Za-z0-9-+]+(.[_A-Za-z0-9-+]+)*@" 
				+ "[A-Za-z0-9-]+(.[A-Za-z0-9]+)*(.[A-Za-z]{2,})$";
				
			@Override
			public void initialize(ValidEmail constraintAnnotation) {       
			}
			@Override
			public boolean isValid(String email, ConstraintValidatorContext context){   
				return (validateEmail(email));
			} 
			private boolean validateEmail(String email) {
				pattern = Pattern.compile(EMAIL_PATTERN);
				matcher = pattern.matcher(email);
				return matcher.matches();
			}
		}
}

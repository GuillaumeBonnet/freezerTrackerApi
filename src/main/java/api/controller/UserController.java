package api.controller;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.validation.Constraint;
import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import javax.validation.Payload;
import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.context.request.WebRequest;

import api.event.OnRegistrationCompleteEvent;
import api.exceptionHandling.CustomException;
import api.model.User;
import api.model.VerificationToken;
import api.repository.UserRepository;
import api.repository.VerificationTokenRepository;
import api.service.UserService;

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
		private UserService userService;

	/* -------------------------------------------------------------------------- */
	/*                                   Methods                                  */
	/* -------------------------------------------------------------------------- */

		@RequestMapping(value = "/confirm-registration", method = RequestMethod.POST)
		@ResponseBody
		public void activateConfirmedAccount(@RequestBody String token) {
			if( this.userService.isLoggedIn() ) {
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

			User registered = this.userService.registerUser(accountDto);

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
			} catch (CustomException me) { //don't catch CustomExceptions
				throw me;
			} catch (Exception me) {
				System.out.println("[exception email not sent] :" +  me);				
				throw new CustomException("User was registered but the activation email could not be sent."); //TODO:Label
			}
		}

		@RequestMapping(value = "/info")
		@ResponseBody
		public UserInfoDto getUserInfo() {
			if( this.userService.isNotLoggedIn() ) {
				throw new CustomException("User is not logged-in.");
			}
			User loggedUser = (User)SecurityContextHolder.getContext().getAuthentication().getPrincipal();
			if( !loggedUser.isEnabled() ) {
				throw new CustomException("User is not activated.");
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
			this.userService.authenticateUser(userToLogin, loginDto.password);
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

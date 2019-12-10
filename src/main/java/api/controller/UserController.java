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
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.stereotype.*;
import org.springframework.validation.BindingResult;
import org.springframework.validation.Errors;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.request.WebRequest;

import api.exceptionHandling.CustomException;
import api.model.User;
import api.repository.UserRepository;

//@CrossOrigin(origins = "http://localhost:4200/ https://freezer-practice-front.herokuapp.com/")
@CrossOrigin(origins = "*")
@Controller
public class UserController {

	private static final RetentionPolicy RUNTIME = null;

	// TODO: dig into db xss sanization
	@Autowired
	ApplicationEventPublisher eventPublisher;

	@Autowired
	private UserRepository userRepo;

	@RequestMapping("/csrf")
	@ResponseBody
    public void csrf() {
    }

	@RequestMapping(value = "/users/registration", method = RequestMethod.POST)
	@ResponseBody
	public void registerUserAccount(
		@RequestBody @Valid UserDto accountDto
		, BindingResult result
		, WebRequest request) {


			//TODO: validity of username & password 20char, email: 255char

		
		if (result.hasErrors()) {
			System.out.println("gboDebug [hasErrors] :");
			
			List<ObjectError> errors = result.getAllErrors();
			List<String> exceptionDetails = new ArrayList<String>();
			for(ObjectError error : errors) {
				if(error instanceof FieldError) {
					exceptionDetails.add(
						String.format(
							"The field [%s] has an error: %s.", //TODO:customLabel
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

		User registered = createUserAccount(accountDto, result);

		if (registered == null) {
			throw new CustomException("User not registered");
		}
	}

	/* -------------------------------------------------------------------------- */
	/*                                Utils methods                               */
	/* -------------------------------------------------------------------------- */

	// TODO! sql db unique username & email

	private User createUserAccount(UserDto accountDto, BindingResult result) {
		User newUser = new User(accountDto.userName, accountDto.password, accountDto.email, false);

		if( ! this.userRepo.findByEmail(accountDto.email).isEmpty() ) {
			throw new CustomException("There is already a user registered with this email address: '" + accountDto.email + "'.");
		}

		if( ! this.userRepo.findByUsername(accountDto.userName).isEmpty() ) {
			throw new CustomException("There is already a user registered with this username: '" + accountDto.userName + "'.");
		}

		newUser = this.userRepo.save(newUser);
		return newUser;
	}

	/* -------------------------------------------------------------------------- */
	/*                                    inner DTOs                              */
	/* -------------------------------------------------------------------------- */

	@PasswordMatches
	public static class UserDto {
		@NotNull
		@NotEmpty
		public String userName;

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
			UserDto user = (UserDto) obj;
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
		private static final String EMAIL_PATTERN = "^[_A-Za-z0-9-+]+(.[_A-Za-z0-9-]+)*@" 
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

package api.controller;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import javax.persistence.EntityExistsException;
import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.stereotype.*;
import org.springframework.validation.BindingResult;
import org.springframework.validation.Errors;
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

	//TODO: dig into db xss sanization

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
	public String registerUserAccount(
		@RequestBody @Valid UserDto accountDto
		, BindingResult result
		, WebRequest request) {


			//TODO: validity of username & password 20char, email: 255char

		
		if (result.hasErrors()) {
			System.out.println("gboDebug [hasErrors] :");
			
			List<ObjectError> errors = result.getAllErrors();
			List<String> exceptionDetails = errors
				.stream()
				.map((errorItem) -> String.format("The field [%s] has an error: %s.", errorItem.getObjectName(), errorItem.getDefaultMessage())) //TODO:customLabel
				.collect(Collectors.toList())
			;
			throw new CustomException("JSON Validation Error", exceptionDetails);

		}
		User registered = createUserAccount(accountDto, result);
		if (registered == null) {
			result.rejectValue("email", "message.regError");
			return "error";
		}
		return "\"true\"";
	}

	/* -------------------------------------------------------------------------- */
	/*                                Utils methods                               */
	/* -------------------------------------------------------------------------- */

	// TODO! sql db unique username & email

	private User createUserAccount(UserDto accountDto, BindingResult result) {
		User newUser = new User(accountDto.userName, accountDto.password, accountDto.email, false);
		try {
			newUser = this.userRepo.save(newUser);
		} catch (EntityExistsException e) {
			return null;
		}    
		return newUser;
	}

	/* -------------------------------------------------------------------------- */
	/*                                    inner DTOs                              */
	/* -------------------------------------------------------------------------- */

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
		public String email;
	}
}

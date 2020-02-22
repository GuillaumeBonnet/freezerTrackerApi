package api.service;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import api.controller.UserController.UserRegistrationDto;
import api.exceptionHandling.CustomException;
import api.model.User;
import api.repository.UserRepository;

@Service
public class UserService {

    @Autowired
    public AuthenticationManager authenticationManager;
    @Autowired
    public UserRepository userRepo;
    
    public User getCurrentUser() {
		return (User)SecurityContextHolder.getContext().getAuthentication().getPrincipal();
	}

	public Boolean isLoggedIn() {
        Authentication currentAuth = SecurityContextHolder.getContext().getAuthentication();
		return currentAuth != null && !(currentAuth instanceof AnonymousAuthenticationToken);
	}

	public Boolean isNotLoggedIn() {
		return !isLoggedIn();
    }
    
    public void authenticateUser(@NotNull User userToLogin, @NotNull @NotEmpty String password) {
		Authentication authenticationRequest =  new UsernamePasswordAuthenticationToken(userToLogin, password, userToLogin.getAuthorities());
		Authentication authenticationResponse;
		try {
			authenticationResponse = this.authenticationManager.authenticate(authenticationRequest);
		}
		catch(AuthenticationException e) {
            throw new CustomException(e.getMessage());
        }
		SecurityContextHolder.getContext().setAuthentication(authenticationResponse);
    }
    
    public User registerUser(UserRegistrationDto accountDto) {

        // TOIMPROVE: one querry findBy for both email and username
        if( this.userRepo.findByEmail(accountDto.email) != null ) {
            throw new CustomException("There is already a user registered with this email address: '" + accountDto.email + "'.");
        }

        if( this.userRepo.findByUsername(accountDto.username) != null ) {
            throw new CustomException("There is already a user registered with this username: '" + accountDto.username + "'.");
        }

        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder(16);

        User newUser = new User(
            accountDto.username
            , "{bcrypt}" + encoder.encode(accountDto.password)
            , accountDto.email
            , false
            , "ROLE_USER"
        );

        newUser = this.userRepo.save(newUser);
        return newUser;
    }
}
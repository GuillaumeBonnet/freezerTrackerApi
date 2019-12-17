package security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import api.exceptionHandling.CustomException;
import api.model.User;
import api.repository.UserRepository;

@Service
public class CustomUserDetailsService implements UserDetailsService {
    
    @Autowired
    UserRepository userRepository;
    
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
                
        User user;
        try {
            user = userRepository.findByUsername(username).get(0);
        } catch(IndexOutOfBoundsException ex) {
            throw new CustomException(
                String.format("User of username: '%s' could not be found.", username)
            );
        }
        return user;
    }


}
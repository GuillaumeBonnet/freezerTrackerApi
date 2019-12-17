package security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.boot.autoconfigure.security.SecurityProperties;


@EnableWebSecurity(debug = true)
@Order(SecurityProperties.BASIC_AUTH_ORDER)
public class WebSecurityConfig extends WebSecurityConfigurerAdapter {


	@Override
	protected void configure(final HttpSecurity http) throws Exception {
        http
            .authorizeRequests(authorizeRequests ->
                authorizeRequests
                    .antMatchers("/users/registration", "/csrf").permitAll()
                    .anyRequest().authenticated()
            )
            .csrf(csrf ->
                csrf
                    .csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse())
            )
            .formLogin(formLogin -> 
                {}
            )
        ;
    }
    
    @Autowired
    CustomUserDetailsService customUserDetailsService;
    

    @Autowired
    public void configureGlobal(final AuthenticationManagerBuilder auth) throws Exception {
        // ensure the passwords are encoded properly        
        auth
            .userDetailsService(customUserDetailsService);
        ;
    }
}
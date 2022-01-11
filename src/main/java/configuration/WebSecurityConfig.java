package configuration;

import java.io.IOException;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.security.SecurityProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.logout.LogoutSuccessHandler;
import org.springframework.security.web.header.writers.StaticHeadersWriter;
import org.springframework.web.cors.CorsConfiguration;

@Configuration
@EnableWebSecurity(debug = true)
@Order(SecurityProperties.BASIC_AUTH_ORDER)
public class WebSecurityConfig extends WebSecurityConfigurerAdapter {

    @Value("${FRONT_END_ROOT_URL:http://localhost:8080}") // TODO!: add this var to heroku conf
    private String frontEndRootUrl;
	private String expectedHostUrl = "http://localhost:4200/";
    
	@Override
	protected void configure(final HttpSecurity http) throws Exception {
		http.cors().configurationSource(
				request -> {
					CorsConfiguration corsConf = new CorsConfiguration().applyPermitDefaultValues();
					corsConf.setAllowedOrigins(List.of("http://localhost:4201"));
					corsConf.setAllowCredentials(true);
					corsConf.addAllowedHeader("*");
					corsConf.setAllowedMethods(List.of("GET", "POST", "DELETE", "PUT"));
					// exposedHeaders(HttpHeaders.SET_COOKIE).maxAge(3600L); //maxAge(3600)
					// indicates that in 3600 seconds, there is no need to send a pre check
					// request,
					// and the result can be cached
					return corsConf;
				});
		http// by default uses a Bean by the name of corsConfigurationSource
				.csrf().disable()
				// .csrf(csrf -> csrf
				// .csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse()))
				.authorizeRequests(authorizeRequests -> authorizeRequests
						.antMatchers("/api/users/confirm-registration").permitAll()
						.antMatchers("/api/users/registration").permitAll()
						.antMatchers("/api/users/login").permitAll()
						.antMatchers("/api/**").authenticated()
						.antMatchers("/**").permitAll())
				.headers(headers -> {
					headers.contentSecurityPolicy("frame-ancestors " +
							expectedHostUrl + ";");
					headers.frameOptions().disable();
					headers.addHeaderWriter(
							new StaticHeadersWriter("X-FRAME-OPTIONS", "ALLOW-FROM " +
									expectedHostUrl));
				})
				.logout(logout -> logout
						.logoutUrl("/api/users/logout")
						.logoutSuccessHandler(new LogoutSuccessHandler() {
							@Override
							public void onLogoutSuccess(HttpServletRequest request,
									HttpServletResponse response,
									Authentication authentication) throws IOException, ServletException {
								// Do Nothing the redirection is handled by the SPA.
							}
						})
						.invalidateHttpSession(true) // by default true
				);
	}

    @Autowired
	CustomUserDetailsService customUserDetailsService;

    @Autowired
    public void configureGlobal(final AuthenticationManagerBuilder auth) throws Exception {
		// ensure the passwords are encoded properly
        auth.userDetailsService(customUserDetailsService);
    }

    @Bean
	@Override
	public AuthenticationManager authenticationManagerBean() throws Exception {
		return super.authenticationManagerBean();
	}
}
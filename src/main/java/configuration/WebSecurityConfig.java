package configuration;

import java.io.IOException;
import java.net.URI;
import java.util.Arrays;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.security.SecurityProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.core.annotation.Order;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.logout.LogoutSuccessHandler;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.security.web.header.writers.StaticHeadersWriter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.reactive.CorsConfigurationSource;
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource;

@EnableWebSecurity(debug = true)
@Order(SecurityProperties.BASIC_AUTH_ORDER)
public class WebSecurityConfig extends WebSecurityConfigurerAdapter {

    @Value("${FRONT_END_ROOT_URL:http://localhost:8080}") // TODO!: add this var to heroku conf
    private String frontEndRootUrl;
	private String expectedHostUrl = "http://localhost:4200/";
    
	@Override
	protected void configure(final HttpSecurity http) throws Exception {
        http
				.cors(corsCustomizer -> {
				}) // by default uses a Bean by the name of corsConfigurationSource
				.authorizeRequests(authorizeRequests -> authorizeRequests
						.antMatchers("/api/users/info").authenticated()
						.antMatchers("/api/users/**").permitAll()
						.antMatchers("/api/**").authenticated()
						.antMatchers("/**").permitAll()
						.anyRequest().authenticated())
				.csrf(csrf -> csrf
						.csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse()))
				.logout(logout -> logout
						.logoutUrl("/api/users/logout")
						.logoutSuccessHandler(new LogoutSuccessHandler() {
							@Override
							public void onLogoutSuccess(HttpServletRequest request, HttpServletResponse response,
									Authentication authentication) throws IOException, ServletException {
								// Do Nothing the redirection is handled by the SPA.
							}
						})
						.invalidateHttpSession(true) // by default true
				);
		http.antMatcher("/").headers().contentSecurityPolicy("frame-ancestors " + expectedHostUrl + ";");
		http.antMatcher("/").headers().frameOptions().disable()
				.addHeaderWriter(
						new StaticHeadersWriter("X-FRAME-OPTIONS", "ALLOW-FROM " + expectedHostUrl));

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

	// @Bean
	// CorsConfigurationSource corsConfigurationSource() {
	// CorsConfiguration configuration = new CorsConfiguration();
	// configuration.setAllowedOrigins(Arrays.asList("http://localhost:4201"));

	// // configuration.setAllowedOrigins(Arrays.asList(frontEndRootUrl));
	// configuration.setAllowedMethods(Arrays.asList("GET", "POST"));
	// configuration.setAllowedHeaders(Arrays.asList("X-XSRF-TOKEN"));
	// UrlBasedCorsConfigurationSource source = new
	// UrlBasedCorsConfigurationSource();
	// source.registerCorsConfiguration("/**", configuration);
	// return source;
	// }
}
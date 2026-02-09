package es.codeurjc.quesosbartolome.security;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

import es.codeurjc.quesosbartolome.security.jwt.CustomAccessDeniedHandler;
import es.codeurjc.quesosbartolome.security.jwt.JwtRequestFilter;
import es.codeurjc.quesosbartolome.security.jwt.UnauthorizedHandlerJwt;
import es.codeurjc.quesosbartolome.service.RepositoryUserDetailsService;

@Configuration
@EnableWebSecurity
public class SecurityConfiguration {

    @Autowired
    private JwtRequestFilter jwtRequestFilter;

    @Autowired
    private UnauthorizedHandlerJwt unauthorizedHandlerJwt;

    @Autowired
	public RepositoryUserDetailsService userDetailsService;

    @Autowired
    private CustomAccessDeniedHandler customAccessDeniedHandler;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public CorsFilter corsFilter() {
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowCredentials(true);
        config.setAllowedOrigins(List.of("http://localhost:4200","http://localhost:9876"));
        config.setAllowedHeaders(List.of("*"));
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        source.registerCorsConfiguration("/**", config);
        return new CorsFilter(source);
    }

    @Bean
	public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig) throws Exception {
		return authConfig.getAuthenticationManager();
	}

	@Bean
	public DaoAuthenticationProvider authenticationProvider() {
		DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();

		authProvider.setUserDetailsService(userDetailsService);
		authProvider.setPasswordEncoder(passwordEncoder());

		return authProvider;
	}

    @Bean
    @Order(1)
    public SecurityFilterChain apiSecurity(HttpSecurity http) throws Exception {

        http
            .securityMatcher("/api/**")
            .exceptionHandling(handling -> handling
                .authenticationEntryPoint(unauthorizedHandlerJwt)
                .accessDeniedHandler(customAccessDeniedHandler))
            .authorizeHttpRequests(auth -> auth
                    // Endpoints públicos
                    .requestMatchers(HttpMethod.POST, "/api/v1/auth/**").permitAll()
                    .requestMatchers(HttpMethod.GET, "/api/v1/cheeses/**").permitAll()
                    .requestMatchers(HttpMethod.GET, "/api/v1/cheeses").permitAll()
                    .requestMatchers(HttpMethod.GET, "/api/v1/users/{id:[0-9]+}").permitAll()
                    .requestMatchers(HttpMethod.GET, "/api/v1/users/*/image").permitAll()

                    // Endpoint de perfil
                    .requestMatchers(HttpMethod.GET, "/api/v1/users").authenticated()
                    .requestMatchers(HttpMethod.GET, "/api/v1/cart").hasAnyRole("USER")
                    .requestMatchers(HttpMethod.PUT, "/api/v1/cart/**").hasAnyRole("USER")
                    .requestMatchers(HttpMethod.POST, "/api/v1/orders/confirm").hasAnyRole("USER")
                    .requestMatchers(HttpMethod.GET, "/api/v1/orders").hasAnyRole("ADMIN")
                    .requestMatchers(HttpMethod.GET, "/api/v1/users/all").hasAnyRole("ADMIN")
                    .requestMatchers(HttpMethod.POST, "/api/v1/cheeses/new").hasAnyRole("ADMIN")
                    .requestMatchers(HttpMethod.POST, "/api/v1/cheeses/*/image").hasAnyRole("ADMIN")
                    .requestMatchers(HttpMethod.PUT, "/api/v1/cheeses/*").hasAnyRole("ADMIN")
                    .requestMatchers(HttpMethod.PUT, "/api/v1/cheeses/*/image").hasAnyRole("ADMIN")
                    .requestMatchers(HttpMethod.DELETE, "/api/v1/cheeses/*").hasAnyRole("ADMIN")


            );
        http.cors(cors -> {});
        // Disable CSRF protection (it is difficult to implement in REST APIs)
        http.csrf(csrf -> csrf.disable());
        // Disable Form login Authentication
        http.formLogin(form -> form.disable());
        // Disable Basic Authentication
        http.httpBasic(basic -> basic.disable());
        http.sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS));
        // Add the JWT filter before the standard authentication filter
        http.addFilterBefore(jwtRequestFilter, UsernamePasswordAuthenticationFilter.class);

        
        return http.build();
    }
}

package fr.sle.config;

import fr.sle.config.jwt.JwtAuthenticationFilter;
import fr.sle.config.jwt.JwtAuthenticationProvider;

import java.util.Arrays;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.Http401AuthenticationEntryPoint;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

/**
 * @author slemoine
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

	@Autowired
    static CorsConfigurationSource corsConfigurationSource;
	
    /**
     * Rest security configuration for /api/
     */
    @Configuration
    @Order(1)
    public static class RestApiSecurityConfig extends WebSecurityConfigurerAdapter {

        private static final String apiMatcher = "/api/**";

        @Bean
        public CorsConfigurationSource corsConfigurationSource() {
            CorsConfiguration configuration = new CorsConfiguration();
            configuration.setAllowedOrigins(Arrays.asList("*"));
            configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
            configuration.setAllowedHeaders(Arrays.asList("authorization", "content-type", "application/json"));
            configuration.setExposedHeaders(Arrays.asList("application/json"));//xsrf-token
            configuration.setAllowCredentials(true);
            configuration.setMaxAge(3600L);
            UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
            source.registerCorsConfiguration("/**", configuration);
            return source;
        }
        
        @Override
        protected void configure(HttpSecurity http) throws Exception {

            http.addFilterBefore(new JwtAuthenticationFilter(apiMatcher, super.authenticationManager()), UsernamePasswordAuthenticationFilter.class);

            http.antMatcher(apiMatcher).authorizeRequests()
                    .anyRequest()
                    .authenticated();
            
            http
    	    .cors()
    	    .configurationSource(corsConfigurationSource)
            .and();
        }

        @Override
        protected void configure(AuthenticationManagerBuilder auth) {
            auth.authenticationProvider(new JwtAuthenticationProvider());
        }
    }

    /**
     * Rest security configuration for /api/
     */
    @Configuration
    @Order(2)
    public static class AuthSecurityConfig extends WebSecurityConfigurerAdapter {

        private static final String apiMatcher = "/auth/token";

        @Override
        protected void configure(HttpSecurity http) throws Exception {

            http
                    .exceptionHandling()
                    .authenticationEntryPoint(new Http401AuthenticationEntryPoint("SAML2.0 - WEBSSO"));

            http.antMatcher(apiMatcher).authorizeRequests()
                    .anyRequest().authenticated();
            
            http
    	    .cors()
    	    .configurationSource(corsConfigurationSource)
            .and();
        }
    }

    /**
     * Saml security config
     */
    @Configuration
    @Import(SamlSecurityConfig.class)
    public static class SamlConfig {

    }

}

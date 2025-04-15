package com.coderscampus.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import static org.springframework.security.config.Customizer.withDefaults;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    @Value("${admin.emails}")
    private String[] adminEmails;

    @Autowired
    private OAuth2UserService<OAuth2UserRequest, OAuth2User> oauth2UserService;
    
    @Autowired
    private ClientRegistrationRepository clientRegistrationRepository;

    @Bean
    CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(Arrays.asList("http://localhost:3000"));
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(Arrays.asList("Authorization", "Cache-Control", "Content-Type"));
        configuration.setAllowCredentials(true);
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .authorizeHttpRequests(auth -> {
                    auth.requestMatchers("/").permitAll();
                    auth.requestMatchers("/login").permitAll();
                    auth.requestMatchers("/register").permitAll();
                    auth.requestMatchers("/css/**", "/js/**", "/images/**").permitAll();
                    auth.requestMatchers("/api/auth/**").permitAll(); // Allow access to auth endpoints
                    auth.requestMatchers("/h2-console/**").permitAll(); // Allow access to H2 console
                    auth.requestMatchers("/admin/**").hasRole("ADMIN"); // Only admins can access /admin/**
                    auth.requestMatchers("/dashboard/**", "/milestones/**", "/reports/**", "/profile/**").authenticated();
                    auth.anyRequest().authenticated();
                })
                .csrf(csrf -> csrf
                    .ignoringRequestMatchers("/h2-console/**")
                    .ignoringRequestMatchers("/api/**")) // Disable CSRF for API endpoints
                .headers(headers -> headers
                        .frameOptions(frame -> frame.disable())) // Disable frame options for H2 console
                .oauth2Login(oauth2 -> oauth2
                        .loginPage("/login")
                        .userInfoEndpoint(userInfo -> userInfo.userService(oauth2UserService))
                        .authorizationEndpoint(authEndpoint -> 
                            authEndpoint.authorizationRequestResolver(
                                new CustomAuthorizationRequestResolver(clientRegistrationRepository)))
                        .successHandler(oauthSuccessHandler()))
                .formLogin(form -> form
                        .loginPage("/login")
                        .loginProcessingUrl("/login")
                        .usernameParameter("username") // This matches the form field name
                        .passwordParameter("password") // This matches the form field name
                        .permitAll()
                        .defaultSuccessUrl("/dashboard", true)
                        .failureUrl("/login?error"))
                .logout(logout -> logout
                        .logoutUrl("/logout") // This is the default value
                        .logoutSuccessUrl("/login?logout")
                        .invalidateHttpSession(true)
                        .clearAuthentication(true)
                        .permitAll())
                .build();
    }
    
    private AuthenticationSuccessHandler oauthSuccessHandler() {
        return new AuthenticationSuccessHandler() {
            @Override
            public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                    Authentication authentication) throws IOException, ServletException {
                        
                if (authentication instanceof OAuth2AuthenticationToken) {
                    OAuth2AuthenticationToken oauthToken = (OAuth2AuthenticationToken) authentication;
                    OAuth2User oauthUser = oauthToken.getPrincipal();
                    
                    String email = (String) oauthUser.getAttribute("email");
                    if (email != null) {
                        boolean isAdmin = Arrays.asList(adminEmails).contains(email);
                        
                        // Set the appropriate authority based on admin email check
                        Set<SimpleGrantedAuthority> authorities = new HashSet<>();
                        if (isAdmin) {
                            authorities.add(new SimpleGrantedAuthority("ROLE_ADMIN"));
                        } else {
                            authorities.add(new SimpleGrantedAuthority("ROLE_USER"));
                        }
                        
                        // Create a new authentication token with the correct authorities
                        UsernamePasswordAuthenticationToken newAuth = 
                            new UsernamePasswordAuthenticationToken(oauthUser, "N/A", authorities);
                        
                        // Update the security context with our new authentication
                        SecurityContextHolder.getContext().setAuthentication(newAuth);
                    }
                }
                
                // Redirect to dashboard
                response.sendRedirect("/dashboard");
            }
        };
    }
}














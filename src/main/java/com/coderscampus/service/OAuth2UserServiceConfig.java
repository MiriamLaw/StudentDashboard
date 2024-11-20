package com.coderscampus.service;//package com.coderscampus.social_login.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;


// Uncomment the OAuth2UserService configuration below if you want to customize roles or map additional user data.
// This configuration is optional and only needed if you wish to assign custom roles (e.g., ROLE_ADMIN) or handle extra user attributes.
// NOTE: If you uncomment and use this configuration, remember to add `.userInfoEndpoint().userService(oauth2UserService())`
// in the `SecurityConfig` to connect this custom service to your OAuth2 login.

 @Configuration
 public class OAuth2UserServiceConfig {

     @Value("${admin.emails}")
     private String[] adminEmails;

     @Bean
     public OAuth2UserService<OAuth2UserRequest, OAuth2User> oauth2UserService() {
         DefaultOAuth2UserService delegate = new DefaultOAuth2UserService();

         return userRequest -> {
             // Load user information from the OAuth2 provider (Google in this example)
             OAuth2User oAuth2User = delegate.loadUser(userRequest);

             // Initialize the set of authorities
             Set<SimpleGrantedAuthority> authorities = new HashSet<>();

             // Get user's email from the OAuth2 provider's attributes
             String email = oAuth2User.getAttribute("email");

             // Assign ROLE_ADMIN if the user's email is in the adminEmails list
             if (email != null && Arrays.asList(adminEmails).contains(email)) {
                 authorities.add(new SimpleGrantedAuthority("ROLE_ADMIN"));
             } else {
                 authorities.add(new SimpleGrantedAuthority("ROLE_USER"));
             }


             // Return a new user with the assigned roles and Google’s identifier ("sub").
             // "sub" is the attribute key for Google’s unique user identifier.
             // Replace "sub" with a different key if using a provider other than Google, or if using custom attributes.
             return new DefaultOAuth2User(authorities, oAuth2User.getAttributes(), "sub");
         };
     }
 }



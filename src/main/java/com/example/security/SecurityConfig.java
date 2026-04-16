package com.example.security;

import jakarta.servlet.http.HttpServletResponse;
import javax.swing.text.PlainDocument;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import static org.springframework.security.config.Customizer.withDefaults;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.NoOpPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(securedEnabled = true, jsr250Enabled = true)
public class SecurityConfig {

    @Autowired
    private CustomUserDetailsService userDetailsService;//trae a los usuarios que estan registrados en la base de datos 

    @Autowired
    private JwtsRequestFilter JwtsRequestFilter;//para filtrar los token

    /*Este metodo es para filtrar e interceptar los vinculos*/
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http, AuthenticationManager authManager) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(authz -> authz
                .requestMatchers("/login", "/ws/**","/error").permitAll()//autoriza a que cualquier persona acceda a esta url
                .anyRequest().authenticated())
                .httpBasic(withDefaults())
                .sessionManagement(session -> session
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .exceptionHandling(ex -> ex
                .authenticationEntryPoint((request, response, authException) -> {
                    if (response.getStatus() == HttpServletResponse.SC_NOT_FOUND) {
                        response.sendError(HttpServletResponse.SC_NOT_FOUND, "Ruta no encontrada");
                    } else {
                        response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "No autorizado");
                    }

                }));
        http.addFilterBefore(JwtsRequestFilter, UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }

    /*Este metodo es para crear un userDetailsService para crear un usuario en memoria*/
 /*@Bean
    public UserDetailsService userDetailsServices() {
        InMemoryUserDetailsManager manager = new InMemoryUserDetailsManager();
        manager.createUser(User.withUsername("user")
                .password(passwordEncoder().encode("12345678"))
                .roles()
                .build());
        return manager;
    }*/
    @Bean
    public AuthenticationManager authManager(HttpSecurity http) throws Exception {
        AuthenticationManagerBuilder authMangerBuilder
                = http.getSharedObject(AuthenticationManagerBuilder.class);

        authMangerBuilder.userDetailsService(userDetailsService)
                .passwordEncoder(passwordEncoder());

        return authMangerBuilder.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return NoOpPasswordEncoder.getInstance();
    }

    /*@Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();//esto es para encriptar y desencriptar conBCript
    }*/
}

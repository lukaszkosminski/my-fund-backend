package com.myfund.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;


@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .authorizeHttpRequests((requests) -> requests
                        .requestMatchers("/api/create-user").permitAll()
                        .requestMatchers("/").permitAll()
                        .requestMatchers("/home").permitAll()
                        .requestMatchers("/login").permitAll()
                        .anyRequest().authenticated()
                )
                .formLogin(form -> form
                        .loginPage("/login")
                        .successHandler((request, response, authentication) -> {
                            response.setStatus(200);
                        })
                        .permitAll()
                )
                .logout(logout -> logout.permitAll())
                .csrf(csrf -> csrf.disable());
        return http.build();
    }
//    @Bean
//    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
//        http
//                .authorizeHttpRequests((requests) -> requests
//                        .requestMatchers("/api/create-user").permitAll()
//                        .requestMatchers("/").permitAll()
//                        .requestMatchers("/home").permitAll()
//                        .anyRequest().authenticated()
//                )
//                .formLogin(form -> form
//                        .defaultSuccessUrl("/")
//                        .permitAll()
//                )
//                .logout(logout -> logout.permitAll())
//                .csrf(csrf -> csrf.disable());
//        return http.build();
//    }


}
package com.jsa.jobsearchapp.security;

import org.bouncycastle.jcajce.provider.asymmetric.rsa.ISOSignatureSpi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class WebSecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf((csrf) -> csrf.disable()) // REQUIRED
                .authorizeHttpRequests(
                        (authorize) ->
                                authorize
                                        .requestMatchers("/auth/register").permitAll()
                                        .anyRequest().permitAll()); //TODO change to authenticated()
//                .formLogin(
//                        (formLogin) ->
//                                formLogin
//                                        .loginPage("/login")
//                                        .loginProcessingUrl("/login")
//                                        .defaultSuccessUrl("/home", true)
//                                        .permitAll());
//                .logout(
//                        (logout) ->
//                                logout
//                                        .logoutUrl("/logout")
//                                        .logoutSuccessUrl("/login?logout")
//                                        .invalidateHttpSession(true)
//                                        .deleteCookies("JSESSIONID"));
        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}

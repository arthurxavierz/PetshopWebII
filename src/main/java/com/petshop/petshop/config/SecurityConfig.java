package com.petshop.petshop.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
@Configuration
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http, AuthenticationProvider authenticationProvider)
            throws Exception {

        http.authorizeHttpRequests(requests -> requests
                .requestMatchers("/register", "/saveUser", "/css/**", "/error").permitAll()
                // Apenas Admin pode cadastrar, editar e excluir pets (listar fica liberado para qualquer usuário logado)
                .requestMatchers("/pet/novo", "/pet/salvar", "/pet/editar/**", "/pet/atualizar/**", "/pet/excluir/**")
                .hasAuthority("Admin")
                .anyRequest().authenticated())
                .formLogin(login -> login
                        .defaultSuccessUrl("/", true))
                .logout(logout -> logout
                        .logoutUrl("/logout"))
                .exceptionHandling(handling -> handling
                        .accessDeniedPage("/accessDenied"))
                .authenticationProvider(authenticationProvider);

        return http.build();
    }

    @Bean
    public AuthenticationProvider authenticationProvider(UserDetailsService uds, BCryptPasswordEncoder encoder) {
        // No Spring Security 7 o UserDetailsService é passado no construtor
        DaoAuthenticationProvider authenticationProvider = new DaoAuthenticationProvider(uds);
        authenticationProvider.setPasswordEncoder(encoder);
        return authenticationProvider;
    }
}

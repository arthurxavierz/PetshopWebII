package com.petshop.petshop.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpMethod;
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

    // Cadeia 1: só para a API (/api/**). @Order(1) faz ela ser verificada primeiro.
    // Usa HTTP Basic (email e senha em cada requisição) e não usa CSRF.
    @Bean
    @Order(1)
    public SecurityFilterChain apiFilterChain(HttpSecurity http, AuthenticationProvider authenticationProvider)
            throws Exception {
        http
                .securityMatcher("/api/**")
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/public/**").permitAll()
                        // Mesma regra das telas: só o Admin cria, altera e exclui pets
                        .requestMatchers(HttpMethod.POST, "/api/pets/**").hasAuthority("Admin")
                        .requestMatchers(HttpMethod.PUT, "/api/pets/**").hasAuthority("Admin")
                        .requestMatchers(HttpMethod.DELETE, "/api/pets/**").hasAuthority("Admin")
                        .anyRequest().authenticated())
                // CSRF protege formulários com sessão; a API usa HTTP Basic, então fica desligado aqui
                .csrf(csrf -> csrf.disable())
                .httpBasic(basic -> {
                })
                .authenticationProvider(authenticationProvider);

        return http.build();
    }

    // Cadeia 2: o resto do sistema (telas). Login por formulário, como antes.
    @Bean
    @Order(2)
    public SecurityFilterChain webFilterChain(HttpSecurity http, AuthenticationProvider authenticationProvider)
            throws Exception {

        http.authorizeHttpRequests(requests -> requests
                .requestMatchers("/register", "/saveUser", "/css/**", "/error").permitAll()
                // Documentação da API (Swagger) liberada para abrir sem login
                .requestMatchers("/swagger-ui/**", "/swagger-ui.html", "/v3/api-docs/**").permitAll()
                // Apenas Admin pode cadastrar, editar e excluir pets (listar fica liberado para qualquer usuário logado)
                .requestMatchers("/pet/novo", "/pet/salvar", "/pet/editar/**", "/pet/atualizar/**", "/pet/excluir/**")
                .hasAuthority("Admin")
                // Adoção: o usuário padrão envia e o Admin aprova
                .requestMatchers("/adocao/nova", "/adocao/salvar").hasAuthority("Usuario")
                .requestMatchers("/adocao/aprovar/**").hasAuthority("Admin")
                .anyRequest().authenticated())
                .formLogin(login -> login
                        .loginPage("/login") // tela de login própria, em português
                        .defaultSuccessUrl("/", true)
                        .permitAll())
                .logout(logout -> logout
                        .logoutUrl("/logout"))
                .exceptionHandling(handling -> handling
                        .accessDeniedPage("/accessDenied"))
                .authenticationProvider(authenticationProvider);

        return http.build();
    }

    @Bean
    public AuthenticationProvider authenticationProvider(UserDetailsService uds, BCryptPasswordEncoder encoder) {
        // UserDetailsService passado no construtor (setUserDetailsService está deprecated)
        DaoAuthenticationProvider authenticationProvider = new DaoAuthenticationProvider(uds);
        authenticationProvider.setPasswordEncoder(encoder);
        return authenticationProvider;
    }
}

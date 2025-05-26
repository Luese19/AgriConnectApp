package org.scitechdev.finalPorject.config;

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
    
    // Enable Spring Security debug mode
    static {
        System.setProperty("spring.security.debug", "true");
        System.setProperty("logging.level.org.springframework.security", "DEBUG");
    }
    
    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            // Add debugging headers
            .headers(headers -> headers.frameOptions(frameOptions -> frameOptions.disable()))
            .authorizeHttpRequests(authorizeRequests ->
                authorizeRequests
                    .requestMatchers(new String[]{"/", "/login", "/signup", "/test-firebase", "/auth-debug", "/public/**", "/css/**", "/js/**", "/images/**", "/error", "/buyer/**", "/home", "/custom-login", "/favicon.ico", "/farmer/img/**"}).permitAll()
                    .requestMatchers(org.springframework.http.HttpMethod.POST, "/custom-login").permitAll()
                    .requestMatchers("/farmer/**").hasRole("FARMER")
                    .anyRequest().authenticated()
            )
            // Disable default Spring Security login form
            .formLogin(formLogin -> formLogin.disable())
            .logout(logout ->
                logout
                    .logoutSuccessUrl("/login?logout=true")
                    .invalidateHttpSession(true)
                    .deleteCookies("JSESSIONID")
                    .permitAll()
            )
            .exceptionHandling(exceptions -> exceptions
                .accessDeniedHandler((request, response, accessDeniedException) -> {
                    System.err.println("Access denied: " + accessDeniedException.getMessage());
                    System.err.println("Request URI: " + request.getRequestURI());
                    System.err.println("User: " + request.getUserPrincipal());
                    response.sendRedirect("/login?accessDenied=true");
                })
            )
            .csrf(csrf -> csrf.disable()); // Temporarily disable CSRF for troubleshooting

        return http.build();
    }

    @Bean
    PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}

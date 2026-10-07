package com.zoo.management.config;

import com.zoo.management.service.CustomUserDetailsService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final CustomUserDetailsService userDetailsService;

    public SecurityConfig(CustomUserDetailsService userDetailsService) {
        this.userDetailsService = userDetailsService;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder());
        return authProvider;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .headers(headers -> headers.frameOptions(frame -> frame.disable()))
                .authenticationProvider(authenticationProvider())
                .authorizeHttpRequests(auth -> auth
                        // 1. ציבורי: ממשק משתמש סטטי, תמונות, קונסולת H2, בדיקת בריאות וניהול אימות והרשמה
                        .requestMatchers("/", "/index.html", "/login.html", "/login.js", "/styles.css", "/app.js", "/favicon.ico", "/images/**").permitAll()
                        .requestMatchers("/h2-console/**").permitAll()
                        .requestMatchers("/health", "/api/health", "/error").permitAll()
                        .requestMatchers("/api/auth/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/**").permitAll()

                        // 2. מטפלים ומנהלים: האכלת חיות והעברות כלוב
                        .requestMatchers(HttpMethod.POST, "/api/animals/*/feed").hasAnyRole("KEEPER", "ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/api/animals/*/cage/*").hasAnyRole("KEEPER", "ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/api/animals/*/cage").hasAnyRole("KEEPER", "ADMIN")

                        // 3. וטרינרים ומנהלים: רישום רפואי והעברה לבידוד
                        .requestMatchers(HttpMethod.POST, "/api/animals/*/medical").hasAnyRole("VET", "ADMIN")
                        .requestMatchers(HttpMethod.POST, "/api/animals/*/quarantine").hasAnyRole("VET", "ADMIN")

                        // 3.1 משימות צוות
                        .requestMatchers(HttpMethod.POST, "/api/tasks").hasAnyRole("KEEPER", "VET", "ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/api/tasks/**").hasAnyRole("KEEPER", "VET", "ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/api/tasks/**").hasRole("ADMIN")

                        // 4. מנהלים בלבד: הוספה, עריכה ומחיקה של חיות
                        .requestMatchers(HttpMethod.POST, "/api/animals").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/api/animals/*").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/api/animals/*").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.POST, "/api/animals/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/api/animals/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/api/animals/**").hasRole("ADMIN")

                        // 5. מנהלים בלבד: ניהול מלא של כלובים, עובדים ורופאים
                        .requestMatchers("/api/cages/**").hasRole("ADMIN")
                        .requestMatchers("/api/employees/**").hasRole("ADMIN")
                        .requestMatchers("/api/veterinarians/**").hasRole("ADMIN")

                        // כל שאר הבקשות דורשות אימות
                        .anyRequest().authenticated()
                )
                .httpBasic(Customizer.withDefaults());

        return http.build();
    }
}

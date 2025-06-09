package com.example.koskeun.config;

import com.example.koskeun.security.UserDetailsServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

        @Autowired
        private UserDetailsServiceImpl userDetailsService;

        @Bean
        public PasswordEncoder passwordEncoder() {
                return new BCryptPasswordEncoder();
        }

        @Bean
        @SuppressWarnings("deprecation")
        public DaoAuthenticationProvider authenticationProvider() {
                var provider = new DaoAuthenticationProvider();
                provider.setPasswordEncoder(passwordEncoder());
                provider.setUserDetailsService(userDetailsService);
                return provider;
        }

        @Bean
        public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
                http
                                .csrf(csrf -> csrf.disable())
                                .authorizeHttpRequests(auth -> auth
                                                .requestMatchers("/", "/index", "/login", "/register", "/auth/register",
                                                                "/error",
                                                                "/css/**", "/js/**", "/images/**", "/webjars/**",
                                                                "favicon.ico")
                                                .permitAll()
                                                .requestMatchers("/kos/search", "/kos/detail/**").permitAll()
                                                .requestMatchers("/kos/my/**", "/kos/add/**", "/kos/edit/**")
                                                .hasRole("PEMILIK")
                                                .requestMatchers("/dashboard", "/profile").authenticated()
                                                .anyRequest().authenticated())
                                .formLogin(form -> form
                                                .loginPage("/login")
                                                .loginProcessingUrl("/auth/login")
                                                .defaultSuccessUrl("/dashboard")
                                                .failureUrl("/login?error=true")
                                                .permitAll())
                                .logout(logout -> logout
                                                .logoutUrl("/auth/logout")
                                                .logoutSuccessUrl("/login?logout=true")
                                                .invalidateHttpSession(true)
                                                .clearAuthentication(true)
                                                .permitAll())
                                .sessionManagement(session -> session
                                                .maximumSessions(1)
                                                .expiredUrl("/login?expired=true"));

                return http.build();
        }
}

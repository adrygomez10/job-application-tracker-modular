package dev.adriangm.tracker.configuration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfiguration {
    @Bean
    UserDetailsService userDetailsService(
        @Value("${tracker.username}") String username,
        @Value("${tracker.password-hash}") String hash
    )
    {
        if (username.isBlank() || !hash.matches("\\{bcrypt}\\$2[aby]\\$.{56}")) {
            throw new IllegalStateException(
                "Configure TRACKER_USERNAME and a {bcrypt} TRACKER_PASSWORD_HASH. See README."
            );
        }
        return new InMemoryUserDetailsManager(User.withUsername(username).password(hash).roles("OWNER").build());
    }

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception
    {
        return http
            .authorizeHttpRequests(
                requests -> requests
                    .requestMatchers("/login", "/css/**", "/fonts/**", "/actuator/health", "/error")
                    .permitAll()
                    .anyRequest()
                    .authenticated()
            )
            .formLogin(form -> form.loginPage("/login").defaultSuccessUrl("/", true).permitAll())
            .logout(logout -> logout.logoutSuccessUrl("/login?logout"))
            .headers(
                headers -> headers
                    .contentSecurityPolicy(
                        policy -> policy
                            .policyDirectives(
                                "default-src 'self'; style-src 'self'; script-src 'self';"
                                    + " img-src 'self'; font-src 'self'; frame-ancestors 'none';"
                                    + " base-uri 'self'; form-action 'self'"
                            )
                    )
            )
            .build();
    }
}

package lt.ziniumanas.testconfig;

import lt.ziniumanas.controller.ArticleController;
import lt.ziniumanas.service.security.JwtAuthenticationFilter;
import org.mockito.Mockito;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.NoOpPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@TestConfiguration
@WebMvcTest(controllers = ArticleController.class)
@Import(TestSecurityConfig.class)

public class TestSecurityConfig {
    @Bean
    public UserDetailsService userDetailsService() {
        // Mocked in-memory UserDetailsService for tests
        return new InMemoryUserDetailsManager(
                User.withUsername("testuser")
                        .password("{noop}password")
                        .roles("USER")
                        .build()
        );
    }

    @Bean
    public JwtAuthenticationFilter jwtAuthenticationFilter() {
        // Provide a mocked JwtAuthenticationFilter to avoid real instantiation
        return Mockito.mock(JwtAuthenticationFilter.class);
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth
                        .anyRequest().permitAll()
                )
                .addFilterBefore(jwtAuthenticationFilter(), UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }

}

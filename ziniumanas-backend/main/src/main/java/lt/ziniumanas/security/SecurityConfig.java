package lt.ziniumanas.security;
import lt.ziniumanas.service.security.JwtAuthenticationFilter;
import static org.springframework.security.config.Customizer.withDefaults;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthFilter;
    private final CorsConfig cors;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http
                .cors(withDefaults())
                .csrf(csrf -> csrf.disable())
                .sessionManagement(sess -> sess.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        // statika / SPA
                        .requestMatchers("/", "/index.html", "/static/**",
                                "/favicon.ico", "/favicon.png",
                                "/manifest.json", "/ziniumanas.png", "/logo192.png").permitAll()

                        // actuator
                        .requestMatchers("/actuator/**").permitAll()

                        // vieši API GET'ai (NAUDOK Ant patternus, ne {id})
                        .requestMatchers(HttpMethod.GET, "/api/search").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/articles/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/straipsnis/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/kategorija/**").permitAll()

                        // auth endpointai
                        .requestMatchers("/api/auth/**", "/api/login", "/api/register").permitAll()

                        // adminas
                        .requestMatchers("/api/admin/**").hasAuthority("ADMIN")

                        // visi kiti – su auth
                        .anyRequest().authenticated()
                )
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class)
                .build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }
}
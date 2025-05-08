package lt.ziniumanas.security;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .authorizeHttpRequests((authz) -> authz
                        .requestMatchers("/admin").authenticated() // Pridedame taisyklę konkrečiai /admin URL
                        .requestMatchers("/admin/**").authenticated() // Reikalaujama autentikacijos visiems /admin/** URL
                        .requestMatchers("/css/**", "/js/**", "/webjars/**").permitAll() // Leisti prieigą prie statinių resursų
                        .anyRequest().permitAll() // Leisti prieigą prie visų kitų URL
                )
                .formLogin((form) -> form
                        .loginPage("/login") // Nurodome savo prisijungimo puslapio URL
                        .permitAll() // Leisti visiems pasiekti prisijungimo puslapį
                        .defaultSuccessUrl("/admin/dashboard", true) // URL, į kurį nukreipti po sėkmingo prisijungimo
                        .failureUrl("/login?error") // URL, į kurį nukreipti, jei prisijungimas nepavyko
                )
                .logout((logout) -> logout
                        .permitAll() // Leisti visiems atsijungti
                        .logoutSuccessUrl("/") // URL, į kurį nukreipti po atsijungimo
                );
        return http.build();
    }

    @Bean
    public UserDetailsService userDetailsService() {
        UserDetails admin = User.withDefaultPasswordEncoder()
                .username("admin") // Pakeiskite į savo norimą vartotojo vardą
                .password("123") // Pakeiskite į saugų slaptažodį
                .roles("ADMIN")
                .build();
        return new InMemoryUserDetailsManager(admin);
    }
}

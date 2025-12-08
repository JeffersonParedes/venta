package pe.edu.promart.sistemaventas.config;

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

    // NOTA: Eliminamos el bean 'authenticationProvider'.
    // Spring Boot detectará automáticamente tu UserDetailsServiceImpl 
    // y tu PasswordEncoder y configurará la autenticación por ti.

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            // 1. Deshabilitar CSRF: Esto permite que el Logout funcione con un simple enlace (GET)
            // sin necesitar configuraciones complejas ni AntPathRequestMatcher.
            .csrf(csrf -> csrf.disable())

            // 2. Rutas Públicas vs Privadas
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/css/**", "/js/**", "/img/**", "/auth/**").permitAll()
                .anyRequest().authenticated()
            )

            // 3. Login
            .formLogin(form -> form
                .loginPage("/auth/login")
                .loginProcessingUrl("/login")
                .defaultSuccessUrl("/auth/seleccionar-caja", true)
                .permitAll()
            )

            // 4. Logout Simplificado
            .logout(logout -> logout
                .logoutUrl("/logout") // Spring intercepta esta URL
                .logoutSuccessUrl("/auth/login?logout")
                .deleteCookies("JSESSIONID")
                .invalidateHttpSession(true)
                .permitAll()
            );
        
        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
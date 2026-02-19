package Auth.service.security;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtFilter jwtFilter;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable()) // Stateless JWT ke liye CSRF disable zaroori hai
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        // 1. Signup aur Login endpoints ko allow karein
                        .requestMatchers("/auth/signup", "/auth/login").permitAll()

                        // 2. Employee management: Sirf wahi log add kar sakein jinke paas hierarchy roles hain
                        .requestMatchers("/employees/**").hasAnyAuthority("SUPER_ADMIN", "ADMIN", "MANAGER")

                        // 3. Baaki saari requests ke liye login zaroori hai
                        .anyRequest().authenticated()
                )
                // JWT Filter ko UsernamePassword filter se pehle lagana zaroori hai
                .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
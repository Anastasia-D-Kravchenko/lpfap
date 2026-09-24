package pl.edu.pjwstk.lpfap.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import pl.edu.pjwstk.lpfap.repository.UserRepository;

import java.util.List;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    private final JwtAuthFilter jwtAuthFilter;
    private final UserRepository userRepository;

    public SecurityConfig(JwtAuthFilter jwtAuthFilter, UserRepository userRepository) {
        this.jwtAuthFilter = jwtAuthFilter;
        this.userRepository = userRepository;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(12);
    }

    @Bean
    public UserDetailsService userDetailsService() {
        return email -> {
            var user = userRepository.findByEmail(email)
                    .orElseThrow(() -> new UsernameNotFoundException("User not found: " + email));
            return org.springframework.security.core.userdetails.User
                    .withUsername(user.getEmail())
                    .password(user.getPasswordHash())
                    .roles(user.getRole().name())
                    .build();
        };
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/v1/auth/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/v1/animals/all").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.GET, "/api/v1/animals", "/api/v1/animals/*").permitAll()
                        .requestMatchers(HttpMethod.POST,   "/api/v1/animals").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.POST,   "/api/v1/animals/*/photos").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PUT,    "/api/v1/animals/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PATCH,  "/api/v1/animals/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/api/v1/animals/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.GET,    "/api/v1/applications").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PATCH,  "/api/v1/applications/*/approve").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PATCH,  "/api/v1/applications/*/reject").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/api/v1/applications/**").hasRole("ADMIN")
                        .requestMatchers("/api/v1/admin/**").hasRole("ADMIN")
                        .requestMatchers("/api/v1/**").authenticated()
                        .anyRequest().permitAll()
                )
                .exceptionHandling(eh -> eh
                        .authenticationEntryPoint((req, resp, ex) -> {
                            resp.setStatus(jakarta.servlet.http.HttpServletResponse.SC_UNAUTHORIZED);
                            resp.setContentType("application/json;charset=UTF-8");
                            resp.getWriter().write(
                                "{\"status\":401,\"error\":\"Authentication required\"}");
                        })
                )
                .headers(h -> h.frameOptions(f -> f.disable()))
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOriginPatterns(List.of("*"));
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of("*"));
        config.setAllowCredentials(true);
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }
}

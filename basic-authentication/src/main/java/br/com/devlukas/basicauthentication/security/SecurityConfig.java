package br.com.devlukas.basicauthentication.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

import static org.springframework.boot.autoconfigure.security.servlet.PathRequest.toH2Console;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final CustomBasicAuthnEntryPoint customBasicAuthnEntryPoint;

    private static final String BASE_URL = "api/v1/basic-authn";

    public SecurityConfig(CustomBasicAuthnEntryPoint customBasicAuthnEntryPoint) {
        this.customBasicAuthnEntryPoint = customBasicAuthnEntryPoint;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http.csrf(csrf -> csrf.ignoringRequestMatchers(toH2Console()).disable())
                .authorizeHttpRequests(
                        auth -> auth.requestMatchers(BASE_URL + "/singup").permitAll()
                                .requestMatchers(toH2Console()).permitAll()
                                .anyRequest().authenticated()
                ).headers(headers -> headers.frameOptions(HeadersConfigurer.FrameOptionsConfig::disable));

        http.httpBasic(httpBasic -> httpBasic.authenticationEntryPoint(customBasicAuthnEntryPoint));

        return http.build();
    }

    @Bean
    public UserDetailsService userDetailsService() {
        return new CustomUserDetailsService();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}

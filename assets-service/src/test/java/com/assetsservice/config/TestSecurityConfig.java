//package com.assetsservice.config;
//
//import org.springframework.boot.test.context.TestConfiguration;
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Primary;
//import org.springframework.context.annotation.Profile;
//import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
//import org.springframework.security.config.annotation.web.builders.HttpSecurity;
//import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
//import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
//import org.springframework.security.web.SecurityFilterChain;
//import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
//
///**
// * Test security configuration that permits all requests in the test environment.
// * This is necessary because in the actual deployment, authentication will be handled
// * by the gateway-service, not by the assets-service directly.
// */
//@TestConfiguration
//@EnableWebSecurity
//@EnableMethodSecurity
//@Profile("test")
//public class TestSecurityConfig {
//
//    @Bean
//    @Primary
//    public SecurityFilterChain testSecurityFilterChain(HttpSecurity http) throws Exception {
//        // Disable security for tests since auth is handled by gateway-service in production
//        http
//            .csrf(AbstractHttpConfigurer::disable)
//            .cors(AbstractHttpConfigurer::disable)
//            .authorizeHttpRequests(authorize -> authorize
//                .requestMatchers(new AntPathRequestMatcher("/**")).permitAll()
//            )
//            .sessionManagement(AbstractHttpConfigurer::disable)
//            .formLogin(AbstractHttpConfigurer::disable)
//            .httpBasic(AbstractHttpConfigurer::disable)
//            .logout(AbstractHttpConfigurer::disable)
//            .anonymous(AbstractHttpConfigurer::disable);
//
//        return http.build();
//    }
//}

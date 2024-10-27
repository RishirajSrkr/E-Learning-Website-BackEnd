package com.rishiraj.bitbybit.config;

import com.rishiraj.bitbybit.filter.JwtFilter;
import com.rishiraj.bitbybit.implementations.UserDetailServiceImpl;
import com.rishiraj.bitbybit.utils.JwtUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
@EnableWebSecurity
@Slf4j
public class SpringSecurityConfig {

    private final UserDetailServiceImpl userDetailServiceImpl;
    private final JwtFilter jwtFilter;


    @Autowired
    private JwtUtils jwtUtils;

    public SpringSecurityConfig(UserDetailServiceImpl userDetailServiceImpl, JwtFilter jwtFilter) {
        this.userDetailServiceImpl = userDetailServiceImpl;
        this.jwtFilter = jwtFilter;
    }


    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    // AN AUTHENTICATION PROVIDER OBJECT IS NEEDED
    // SINCE AuthenticationProvider is interface, so we can't directly create the object, so we create
    //object of DaoAuthenticationProvider (a implementation class of AuthenticationProvider)
    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider daoAuthenticationProvider = new DaoAuthenticationProvider();
        daoAuthenticationProvider.setUserDetailsService(userDetailServiceImpl);
        daoAuthenticationProvider.setPasswordEncoder(passwordEncoder());
        return daoAuthenticationProvider;
    }


    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http
                .logout(logout -> logout.disable())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(request -> request
                        .requestMatchers("/course/**").authenticated()
                        /*
                        Spring Security automatically ensures that the user must be authenticated
                        before it checks their authority. You don't need to explicitly state
                        that they must be authenticated.
                         */
                        .requestMatchers("/admin/**").hasAuthority("ADMIN")
                        .anyRequest().permitAll())

                /*
                    use this if we are using custom cors configuration
                    .cors(x -> x.configurationSource(corsConfigurationSource()))
                 */
                .cors(x -> x.configurationSource(corsConfigurationSource()))

                /*
               logout
                */
//                .logout(logout -> logout
//                        .logoutUrl("/logout")
//                        .logoutSuccessHandler(((request, response, authentication) -> {
//                            /*
//                            add the token in blacklisted token table / collection
//                             */
//                            invalidateJwtToken(request);
//                            /*
//                            adding a null value to the existing 'token' cookie in cookies
//                             */
//                            response.addCookie(createCookieWithInvalidatedToken());
//                        })))

                .csrf(csrf -> csrf.disable())
                .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class)
                .build();
    }

//    private Cookie createCookieWithInvalidatedToken() {
//        Cookie cookie = new Cookie("token", null);
//        cookie.setPath("/");
//        cookie.setMaxAge(0); // Set expiration to past date to invalidate
//        cookie.setHttpOnly(true); // Ensure cookie is only accessible via HTTP requests
//        return cookie;
//    }
//
//    private LocalDateTime convertDateToLocalDateTime(String token) {
//        Date expiryDate = jwtUtils.extractExpiration(token);
//        return LocalDateTime.ofInstant(expiryDate.toInstant(), ZoneId.systemDefault());
//    }
//
//    private String getJwtTokenFromRequest(HttpServletRequest request){
//        String jwtTokenFromRequest = tokenBlacklistServices.getJwtTokenFromRequest(request);
//        log.info("jwtTokenFromRequest {} :: ", jwtTokenFromRequest);
//        return jwtTokenFromRequest;
//
//    }
//    private void invalidateJwtToken(HttpServletRequest request) {
//        String jwtTokenFromRequest = getJwtTokenFromRequest(request);
//        if(jwtTokenFromRequest != null){
//            tokenBlacklistServices.blacklistToken(jwtTokenFromRequest, convertDateToLocalDateTime(jwtTokenFromRequest));
//
//        }
//    }


    /*

    if cors issue, then use this custom cors configuration

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(List.of("http://localhost:5173")); // Allow your frontend's origin
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS")); // Allow these methods
        configuration.setAllowedHeaders(List.of("Authorization", "Content-Type")); // Allow headers
        configuration.setAllowCredentials(true); // Allow cookies or credentials

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration); // Apply CORS to all routes
        return source;
    }

     */

    @Value("${frontend}")
    private String frontend;
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(List.of(frontend, "http://localhost:5173")); // Allow your frontend's origin
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS")); // Allow these methods
        configuration.setAllowedHeaders(List.of("Authorization", "Content-Type")); // Allow headers
        configuration.setAllowCredentials(true); // Allow cookies or credentials

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration); // Apply CORS to all routes
        return source;
    }


    // AN AUTHENTICATION MANAGER OBJECT IS ALSO NEEDED
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }

}

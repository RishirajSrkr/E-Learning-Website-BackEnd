package com.rishiraj.bitbybit.controllers;

import com.rishiraj.bitbybit.entity.User;
import com.rishiraj.bitbybit.implementations.UserDetailServiceImpl;
import com.rishiraj.bitbybit.repositories.UserRepository;
import com.rishiraj.bitbybit.utils.JwtUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import java.util.Arrays;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@RestController
@RequestMapping("/auth/google")
public class GoogleAuthController {

    private static final Logger log = LoggerFactory.getLogger(GoogleAuthController.class);

    private final RestTemplate restTemplate = new RestTemplate();

    @Autowired
    private UserDetailServiceImpl userDetailService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JwtUtils jwtUtils;

    @Value("${spring.security.oauth2.client.registration.google.client-id}")
    private String clientId;
    @Value("${spring.security.oauth2.client.registration.google.client-secret}")
    private String clientSecret;

    @GetMapping("/callback")
    public ResponseEntity<?> handleGoogleCallback(@RequestParam String code) {
        try {
            // 1. exchange auth code with access token
            String tokenEndpoint = "https://oauth2.googleapis.com/token";

            MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
            body.add("code", code);
            body.add("client_id", clientId);
            body.add("client_secret", clientSecret);
            body.add("redirect_uri", "http://localhost:5173/auth/callback");
            body.add("grant_type", "authorization_code");


            log.info("body :: {} ", body);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

            HttpEntity<MultiValueMap<String, String>> httpEntity = new HttpEntity<>(body, headers);

            ResponseEntity<Map> accessTokenResponse = restTemplate.postForEntity(tokenEndpoint, httpEntity, Map.class);

            log.info("accessTokenResponse ::  {}", accessTokenResponse);

            String idToken = (String)accessTokenResponse.getBody().get("id_token");

            String userInfoUrl = "https://oauth2.googleapis.com/tokeninfo?id_token=" + idToken;

            ResponseEntity<Map> userInfoResponse = restTemplate.getForEntity(userInfoUrl, Map.class);

            log.info("userInfoResponse ::  {}", userInfoResponse);



            if (userInfoResponse.getStatusCode() == HttpStatus.OK) {
                String email = (String) userInfoResponse.getBody().get("email");
                String profileImage = (String) userInfoResponse.getBody().get("picture");

                UserDetails userDetails = userDetailService.loadUserByUsername(email);

                log.info("userDetails :: {} ", userDetails);

                if (userDetails == null) {
                    //create the user
                    User user = new User();
                    user.setEmail(email);
                    user.setName(email.split("@")[0]);
                    user.setPassword(passwordEncoder.encode(UUID.randomUUID().toString()));
                    user.setRoles(Set.of("USER"));
                    user.setProfileImageUrl(profileImage);


                    User newUser = userRepository.save(user);

                    log.info("new user :: {} ", newUser);

                    userDetails = userDetailService.loadUserByUsername(newUser.getEmail());


                    log.info("new userDetails :: {} ", userDetails);

                }


                String jwt = jwtUtils.generateToken(userDetails.getUsername());

                return new ResponseEntity<>(jwt, HttpStatus.OK);

            }

            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);

        } catch (Exception e) {
            log.error("Exception occurred while handling Google callback ", e);
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}

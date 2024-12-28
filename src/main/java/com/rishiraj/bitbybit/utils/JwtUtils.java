package com.rishiraj.bitbybit.utils;

import com.rishiraj.bitbybit.entity.BlacklistedToken;
import com.rishiraj.bitbybit.entity.User;
import com.rishiraj.bitbybit.repositories.BlacklistedTokenRepository;
import com.rishiraj.bitbybit.repositories.UserRepository;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class JwtUtils {

    private static final Logger log = LoggerFactory.getLogger(JwtUtils.class);
    @Autowired
    private UserRepository userRepository;

    @Value("${jwt.secretKey}")
    private String secretKey;

    public SecretKey getSigningKey(){
        return Keys.hmacShaKeyFor(secretKey.getBytes());
    }

    public String generateToken(String email){
        User user = userRepository.findByEmail(email).get();
        Map<String, Object> claims = new HashMap<>();
        claims.put("name", user.getName());
//        claims.put("roles", user.getRoles());
        return createToken(claims, email);
    }

    private String createToken(Map<String, Object> claims, String subject) {
        return Jwts.builder()
                .claims(claims)
                .subject(subject)
                .header().empty().add("typ", "JWT")
                .and()
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis() + 1000 * 60 * 60))
                .signWith(getSigningKey())
                .compact();
    }

    public String extractEmail(String jwt) {
        Claims claims = extractAllClaims(jwt);
        return claims.getSubject();
    }

    public List<String> extractRoles(String jwt){
        Claims claims = extractAllClaims(jwt);
        return (List<String>) claims.get("roles");
    }

    private Claims extractAllClaims(String jwt) {

        Claims payload = Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(jwt)
                .getPayload();

        return payload;


    }

    public boolean validateToken(String jwt) {
        return !isTokenExpired(jwt);
    }

    private boolean isTokenExpired(String jwt) {
        return extractExpiration(jwt).before(new Date());
    }

    public Date extractExpiration(String jwt) {
        return extractAllClaims(jwt).getExpiration();
    }







    public String getJwtFromRequest(HttpServletRequest request){
        String authorizationHeader = request.getHeader("Authorization");
        String jwt = null;
        if(authorizationHeader != null && authorizationHeader.startsWith("Bearer")){
            jwt = authorizationHeader.substring(7);
            return jwt;
        }
        return jwt;
    }

    public LocalDateTime convertDateToLocalDateTime(String token) {
        Claims claims = extractAllClaims(token);
        Date expiryDate = extractExpiration(token);
        if(expiryDate.before(new Date())){
            throw new ExpiredJwtException(null, claims, "JWT has expired");
        }
        return LocalDateTime.ofInstant(expiryDate.toInstant(), ZoneId.systemDefault());
    }

}

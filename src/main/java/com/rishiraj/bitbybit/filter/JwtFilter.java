package com.rishiraj.bitbybit.filter;

import com.rishiraj.bitbybit.implementations.UserDetailServiceImpl;
import com.rishiraj.bitbybit.utils.JwtUtils;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Slf4j
@Component
public class JwtFilter extends OncePerRequestFilter {

    @Autowired
    private JwtUtils jwtUtils;
    @Autowired
    private UserDetailServiceImpl userDetailService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException, RuntimeException {

        String jwt = jwtUtils.getJwtFromRequest(request);
        String email = null;

        if (jwt != null) {
            email = jwtUtils.extractEmail(jwt);
        } else {
            log.warn("The authorization header is missing or doesn't contain Bearer token");
        }

        /*
        checking if the jwt has expired, if so return
         */
        if(jwt != null && !jwtUtils.validateToken(jwt)){
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().write("Token has expired");
            SecurityContextHolder.clearContext();
            return;
        }


        /*
        check if incoming jwt in request header is blacklisted or not, if so return
         */
        if (jwt != null && jwtUtils.isTokenBlackListed(jwt)) {
            log.warn("The token :: {} has been blacklisted.", jwt);
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            response.getWriter().write("Token has been blacklisted");
            /*
              Clear the security context to ensure no authentication data is left
             */
            SecurityContextHolder.clearContext();
            return; //stopping further processing
        }

        if (jwt != null && jwtUtils.validateToken(jwt)) {

            UserDetails userDetails = userDetailService.loadUserByUsername(email);

            UsernamePasswordAuthenticationToken usernamePasswordAuthenticationToken = new
                    UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());

            usernamePasswordAuthenticationToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

            SecurityContextHolder.getContext().setAuthentication(usernamePasswordAuthenticationToken);

            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

//            log.info("----------------------------------------");
//            log.info("authentication {} ", authentication);
//            log.info("----------------------------------------");

        } else {
            SecurityContextHolder.clearContext();
        }

        filterChain.doFilter(request, response);
    }
}
package com.codeshare.security;

import com.codeshare.model.User;
import com.codeshare.repository.UserRepository;
import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import java.io.IOException;

@Component
public class UserSessionFilter implements Filter {

    private final UserRepository userRepository;

    @Autowired
    public UserSessionFilter(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication != null && authentication.isAuthenticated() && 
                !"anonymousUser".equals(authentication.getPrincipal())) {
            HttpSession session = httpRequest.getSession(true);
            if (session.getAttribute("user") == null) {
                Object principal = authentication.getPrincipal();
                String username = "";
                if (principal instanceof UserDetails) {
                    username = ((UserDetails) principal).getUsername();
                } else {
                    username = principal.toString();
                }
                userRepository.findByUsername(username).ifPresent(user -> {
                    session.setAttribute("user", user);
                });
            }
        }
        chain.doFilter(request, response);
    }
}

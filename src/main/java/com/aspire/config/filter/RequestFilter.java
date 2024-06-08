package com.aspire.config.filter;

import com.aspire.entities.User;
import com.aspire.enums.UserRoles;
import com.aspire.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Configuration
public class RequestFilter extends OncePerRequestFilter {

    @Autowired
    private UserService userService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        System.out.println("Request received for: " + request.getRequestURI());

        if(request.getRequestURI().contains("api")){
            String token = request.getHeader("x-user-token");
            if(token == null){
                response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "User not found");
                return;
            }
            User user = userService.getUserByToken(token);
            if(user == null){
                response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "User not found");
                return;
            }
            String url = request.getRequestURI();
            if(url.startsWith("/api/loans") && !user.getRole().equals(UserRoles.USER)){
                response.sendError(HttpServletResponse.SC_FORBIDDEN, "User not allowed to access this resource");
                return;
            }
            if(url.startsWith("/api/admin") && !user.getRole().equals(UserRoles.ADMIN)){
                response.sendError(HttpServletResponse.SC_FORBIDDEN, "User not allowed to access this resource");
                return;
            }
        }

        filterChain.doFilter(request, response);
    }
}

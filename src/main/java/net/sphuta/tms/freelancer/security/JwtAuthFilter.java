package net.sphuta.tms.freelancer.security;

import io.jsonwebtoken.Claims;
import jakarta.servlet.*;
import jakarta.servlet.http.*;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

public class JwtAuthFilter implements Filter {
    private final JwtUtil jwtUtil;
    public JwtAuthFilter(JwtUtil jwtUtil) { this.jwtUtil = jwtUtil; }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        var req = (HttpServletRequest) request;
        String header = req.getHeader("Authorization");
        if (header != null && header.startsWith("Bearer ")) {
            try {
                var jwt = header.substring(7);
                var claims = jwtUtil.parse(jwt).getBody();
                String subject = claims.getSubject();
                var roles = (List<String>) claims.get("roles");
                var authorities = roles.stream().map(r -> new org.springframework.security.core.authority.SimpleGrantedAuthority(r)).collect(Collectors.toList());
                var auth = new UsernamePasswordAuthenticationToken(subject, null, authorities);
                SecurityContextHolder.getContext().setAuthentication(auth);
            } catch (Exception e) {
                // invalid token -> ignore (will be unauthorized if protected)
            }
        }
        chain.doFilter(request, response);
    }
}

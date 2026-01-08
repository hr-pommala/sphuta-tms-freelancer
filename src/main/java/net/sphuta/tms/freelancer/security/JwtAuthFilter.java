package net.sphuta.tms.freelancer.security;

import io.jsonwebtoken.Claims;
import jakarta.servlet.*;
import jakarta.servlet.http.*;
import net.sphuta.tms.freelancer.repository.RevokedTokenRepository;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

/**
 * JWT authentication filter that also checks whether the incoming token
 * has been revoked (stored in revoked_tokens table via RevokedTokenRepository).
 *
 * Behavior preserved:
 * - If Authorization header missing or token invalid -> do nothing (request remains unauthenticated).
 * - If token is revoked -> do nothing (request remains unauthenticated).
 */
public class JwtAuthFilter implements Filter {
    private final JwtUtil jwtUtil;
    private final RevokedTokenRepository revokedRepo;

    public JwtAuthFilter(JwtUtil jwtUtil, RevokedTokenRepository revokedRepo) {
        this.jwtUtil = jwtUtil;
        this.revokedRepo = revokedRepo;
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        var req = (HttpServletRequest) request;
        String header = req.getHeader("Authorization");
        if (header != null && header.startsWith("Bearer ")) {
            try {
                var jwt = header.substring(7);

                // If token is present in revoked repository, ignore it (preserve previous behavior:
                // treat as unauthenticated). Do not set SecurityContext.
                if (revokedRepo != null && revokedRepo.findByToken(jwt).isPresent()) {
                    chain.doFilter(request, response);
                    return;
                }

                var claims = jwtUtil.parse(jwt).getBody();
                String subject = claims.getSubject();
                @SuppressWarnings("unchecked")
                var roles = (List<String>) claims.get("roles");
                // Debugging: print basic info - subject and roles (safe for dev)
                try {
                    System.out.println("[JwtAuthFilter] subject=" + subject + " roles=" + roles);
                } catch (Exception ignore) {}
                var authorities = roles.stream()
                        .map(r -> {
                            // Normalize authority names: ensure they start with ROLE_ so Spring's hasRole/hasAnyRole checks work
                            String name = (r == null) ? "" : r;
                            if (!name.startsWith("ROLE_")) {
                                name = "ROLE_" + name;
                            }
                            return new org.springframework.security.core.authority.SimpleGrantedAuthority(name);
                        })
                        .collect(Collectors.toList());
                var auth = new UsernamePasswordAuthenticationToken(subject, null, authorities);
                SecurityContextHolder.getContext().setAuthentication(auth);
            } catch (Exception e) {
                // invalid token or parsing error -> ignore (request will be unauthenticated if endpoint is protected)
            }
        }
        chain.doFilter(request, response);
    }
}

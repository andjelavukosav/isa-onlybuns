package rs.ac.uns.ftn.informatika.jpa.security.auth;

import io.jsonwebtoken.ExpiredJwtException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.web.filter.OncePerRequestFilter;
import rs.ac.uns.ftn.informatika.jpa.util.TokenUtils;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;


public class TokenAuthenticationFilter extends OncePerRequestFilter {

    private TokenUtils tokenUtils;

    private UserDetailsService userDetailsService;

    protected final Log LOGGER = LogFactory.getLog(getClass());

    public TokenAuthenticationFilter(TokenUtils tokenHelper, UserDetailsService userDetailsService) {
        this.tokenUtils = tokenHelper;
        this.userDetailsService = userDetailsService;
    }

    @Override
    public void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        System.out.println(">>> TokenAuthenticationFilter invoked for URI: " + request.getRequestURI());

        String username = null;
        String authToken = tokenUtils.getToken(request);
        System.out.println("Auth token: " + authToken);
        System.out.println("Username from token: " + username);


        try {

            if (authToken != null) {

                username = tokenUtils.getUsernameFromToken(authToken);

                if (username != null) {

                    System.out.println("Username from token: " + username);
                    UserDetails userDetails = userDetailsService.loadUserByUsername(username);
                    System.out.println("Loaded user: " + userDetails.getUsername());

                    System.out.println("Validating token...");
                    boolean valid = tokenUtils.validateToken(authToken, userDetails);
                    System.out.println("Token valid: " + valid);
                    if (tokenUtils.validateToken(authToken, userDetails)) {

                        TokenBasedAuthentication authentication = new TokenBasedAuthentication(userDetails);
                        authentication.setToken(authToken);
                        SecurityContextHolder.getContext().setAuthentication(authentication);
                        System.out.println("Authentication set with roles: " + authentication.getAuthorities());
                    }
                }
            }

        } catch (ExpiredJwtException ex) {
            LOGGER.debug("Token expired!");
        }

        chain.doFilter(request, response);
    }

}
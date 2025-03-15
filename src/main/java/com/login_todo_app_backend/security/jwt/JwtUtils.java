package com.login_todo_app_backend.security.jwt;
import java.security.Key;
import java.util.Date;
import org.springframework.stereotype.Component;
import com.login_todo_app_backend.security.services.UserDetailsImpl;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;

//Provides utility methods for creating, parsing, and validating JWT tokens, also extracting user details from a token.
@Component
public class JwtUtils {

    private final String jwtSecret;
    private final long jwtExpirationMs;
    

    public JwtUtils(String jwtSecret, long jwtExpirationMs) {
        this.jwtSecret = jwtSecret;
        this.jwtExpirationMs = jwtExpirationMs;
    }

    private Key key() {
        return Keys.hmacShaKeyFor(Decoders.BASE64.decode(jwtSecret));
    }

    // Generate JWT token for a user
    public String generateToken(UserDetailsImpl userPrincipal) {
        return Jwts.builder()
                .setSubject(userPrincipal.getUsername()) // Set username
                .setIssuedAt(new Date()) // Set current timestamp
                .setExpiration(new Date((new Date()).getTime() + jwtExpirationMs)) // Expiry
                .signWith(key(), SignatureAlgorithm.HS256) // Sign token
                .compact();
    }

    // Get username from JWT token
    public String getUserNameFromJwtToken(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(key())
                .build()
                .parseClaimsJws(token)
                .getBody()
                .getSubject();
    }

    // Validate the JWT token
    public boolean validateJwtToken(String authToken) {
        try {
            Jwts.parserBuilder().setSigningKey(key()).build().parseClaimsJws(authToken);
            return true;
        } catch (Exception e) {
            System.err.println("Invalid JWT Token: " + e.getMessage());
        }
        return false;
    }
}

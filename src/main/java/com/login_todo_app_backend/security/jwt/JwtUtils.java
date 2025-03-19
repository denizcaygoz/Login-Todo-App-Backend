package com.login_todo_app_backend.security.jwt;
import java.security.Key;
import java.util.Date;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.ExpiredJwtException;

//Provides utility methods for creating, parsing, and validating JWT tokens, also extracting user details from a token.
@Component
public class JwtUtils {

    @Value("${jwt.secret}")
    private String jwtSecret;

    @Value("${jwt.expiration-ms}")
    private long jwtExpirationMs;

    public String getJwtSecret() {
        return jwtSecret;
    }

    public long getJwtExpirationMs() {
        return jwtExpirationMs;
    }

    private Key key() {
        return Keys.hmacShaKeyFor(Decoders.BASE64.decode(jwtSecret));
    }

    //Generate JWT token for a user
    public String generateToken(String username) {
        return Jwts.builder()
        .setSubject(username)
        .setIssuedAt(new Date())
        .setExpiration(new Date(System.currentTimeMillis() + jwtExpirationMs))
        .signWith(key(), SignatureAlgorithm.HS256)
        .compact();
    }

    //Get username from JWT token
    public String getUserNameFromJwtToken(String token) {
        try {
            return Jwts.parserBuilder()
                    .setSigningKey(key())
                    .build()
                    .parseClaimsJws(token)
                    .getBody()
                    .getSubject();
        } catch (ExpiredJwtException e) {
            //If token is expired, still extract the username
            return e.getClaims().getSubject();
        } catch (Exception e) {
            throw new RuntimeException("Error parsing JWT token", e);
        }
    }

    
    public boolean isTokenExpired(String token) {
        try {
            Jwts.parserBuilder().setSigningKey(key()).build().parseClaimsJws(token);
            return false; //If no exception,then token is valid
        } catch (ExpiredJwtException e) {
            return true; //Token is expired
        } catch (Exception e) {
            throw new RuntimeException("Error checking token expiration", e);
        }
    }

    
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

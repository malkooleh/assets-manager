package com.authservice.security;

import com.authservice.model.entity.User;
import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.time.Instant;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class JwtService {

    @Value("${security.jwt.secret-key}")
    private String secretKey;

    @Value("${security.jwt.token-validity}")
    private long tokenValidity;

    @Value("${security.jwt.issuer}")
    private String issuer;

    @Value("${security.jwt.audience}")
    private String audience;

    private SecretKey key;
    private KeyPair rsaKeyPair;
    private JwtParser jwtParser;

    @PostConstruct
    public void init() {
        // Initialize the HMAC key for token signing
        key = Keys.hmacShaKeyFor(Decoders.BASE64.decode(secretKey));
        
        // Initialize the JWT parser
        jwtParser = Jwts.parser()
                .verifyWith(key)
                .requireIssuer(issuer)
                .requireAudience(audience)
                .build();
        
        // Generate RSA key pair for asymmetric signing (for JWK support)
        try {
            KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
            keyPairGenerator.initialize(2048); // Use 2048 bit keys
            rsaKeyPair = keyPairGenerator.generateKeyPair();
        } catch (NoSuchAlgorithmException e) {
            log.error("Failed to generate RSA key pair", e);
            throw new RuntimeException("Failed to initialize JWT service", e);
        }
    }

    /**
     * Generate a JWT token for a user.
     *
     * @param user the user to generate a token for
     * @return the generated JWT token
     */
    public String generateToken(User user) {
        return generateToken(user, new HashMap<>());
    }

    /**
     * Generate a JWT token for a user with additional claims.
     *
     * @param user the user to generate a token for
     * @param extraClaims additional claims to include in the token
     * @return the generated JWT token
     */
    public String generateToken(User user, Map<String, Object> extraClaims) {
        Instant now = Instant.now();
        Instant expiryDate = now.plusMillis(tokenValidity);
        
        // Extract roles from user authorities
        List<String> roles = user.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .filter(authority -> authority.startsWith("ROLE_"))
                .collect(Collectors.toList());
        
        // Extract permissions from user authorities
        List<String> permissions = user.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .filter(authority -> !authority.startsWith("ROLE_"))
                .collect(Collectors.toList());
        
        return Jwts.builder()
                .claims(extraClaims)
                .subject(user.getId().toString())
                .issuedAt(Date.from(now))
                .expiration(Date.from(expiryDate))
                .issuer(issuer)
                .audience().add(audience).and()
                .claim("username", user.getUsername())
                .claim("email", user.getEmail())
                .claim("roles", roles)
                .claim("permissions", permissions)
                .claim("preferred_username", user.getUsername())
                .signWith(key)
                .compact();
    }

    /**
     * Generate a JWT token using RSA signing for JWK support.
     *
     * @param user the user to generate a token for
     * @return the generated JWT token
     */
    public String generateRsaToken(User user) {
        Instant now = Instant.now();
        Instant expiryDate = now.plusMillis(tokenValidity);
        
        // Extract roles from user authorities
        List<String> roles = user.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .filter(authority -> authority.startsWith("ROLE_"))
                .collect(Collectors.toList());
        
        // Extract permissions from user authorities
        List<String> permissions = user.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .filter(authority -> !authority.startsWith("ROLE_"))
                .collect(Collectors.toList());
        
        return Jwts.builder()
                .subject(user.getId().toString())
                .issuedAt(Date.from(now))
                .expiration(Date.from(expiryDate))
                .issuer(issuer)
                .audience().add(audience).and()
                .claim("username", user.getUsername())
                .claim("email", user.getEmail())
                .claim("roles", roles)
                .claim("permissions", permissions)
                .claim("preferred_username", user.getUsername())
                .signWith(getRsaPrivateKey())
                .compact();
    }

    /**
     * Validate a JWT token.
     *
     * @param token the token to validate
     * @return true if the token is valid, false otherwise
     */
    public boolean validateToken(String token) {
        try {
            jwtParser.parse(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            log.error("Invalid JWT token: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Extract the subject (user ID) from a JWT token.
     *
     * @param token the token to extract from
     * @return the subject (user ID)
     */
    public String extractSubject(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    /**
     * Extract a specific claim from a JWT token.
     *
     * @param token the token to extract from
     * @param claimsResolver the function to extract the desired claim
     * @param <T> the type of the claim
     * @return the extracted claim
     */
    public <T> T extractClaim(String token, java.util.function.Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    /**
     * Extract all claims from a JWT token.
     *
     * @param token the token to extract from
     * @return all claims from the token
     */
    private Claims extractAllClaims(String token) {
        return jwtParser.parseSignedClaims(token).getPayload();
    }

    /**
     * Get the RSA public key for JWK.
     *
     * @return the RSA public key
     */
    public RSAPublicKey getRsaPublicKey() {
        return (RSAPublicKey) rsaKeyPair.getPublic();
    }

    /**
     * Get the RSA private key for signing.
     *
     * @return the RSA private key
     */
    private RSAPrivateKey getRsaPrivateKey() {
        return (RSAPrivateKey) rsaKeyPair.getPrivate();
    }
}

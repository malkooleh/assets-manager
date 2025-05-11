package com.gatewayservice.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.JwtParser;
import io.jsonwebtoken.Jwts;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Service for JWT token validation and claim extraction.
 * This service validates tokens using the JWK Set from the auth-service.
 */
@Service
@Slf4j
public class JwtService {

    @Value("${security.jwt.issuer}")
    private String issuer;

    @Value("${security.jwt.audience}")
    private String audience;

    @Value("${security.jwt.jwk-set-uri}")
    private String jwkSetUri;

    private final WebClient webClient;
    private JwtParser jwtParser;
    private final Map<String, PublicKey> keyCache = new ConcurrentHashMap<>();

    public JwtService(WebClient.Builder webClientBuilder) {
        this.webClient = webClientBuilder.build();
    }

    @PostConstruct
    public void init() {
        // Initialize the JWT parser with default settings
        // The actual key for verification will be fetched dynamically
        jwtParser = Jwts.parser()
                .requireIssuer(issuer)
                .requireAudience(audience)
                .build();
        
        // Pre-fetch the JWK set
        fetchJwkSet();
    }

    /**
     * Validate a JWT token.
     *
     * @param token the token to validate
     * @return true if the token is valid, false otherwise
     */
    public boolean validateToken(String token) {
        try {
            // Parse the token without verification to get the key ID
            String kid = extractKeyId(token);
            
            // Get or fetch the public key for this key ID
            PublicKey publicKey = getPublicKey(kid);
            if (publicKey == null) {
                log.error("Public key not found for key ID: {}", kid);
                return false;
            }
            
            // Verify the token with the public key
            Jwts.parser()
                    .verifyWith(publicKey)
                    .requireIssuer(issuer)
                    .requireAudience(audience)
                    .build()
                    .parseSignedClaims(token);
            
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            log.error("Invalid JWT token: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Extract all claims from a JWT token.
     *
     * @param token the token to extract from
     * @return all claims from the token
     */
    public Claims extractAllClaims(String token) {
        try {
            // Parse the token without verification to get the key ID
            String kid = extractKeyId(token);
            
            // Get or fetch the public key for this key ID
            PublicKey publicKey = getPublicKey(kid);
            if (publicKey == null) {
                throw new JwtException("Public key not found for key ID: " + kid);
            }
            
            // Parse the token with the public key
            return Jwts.parser()
                    .verifyWith(publicKey)
                    .requireIssuer(issuer)
                    .requireAudience(audience)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
        } catch (JwtException | IllegalArgumentException e) {
            log.error("Failed to extract claims from JWT token: {}", e.getMessage());
            throw new JwtException("Failed to extract claims", e);
        }
    }

    /**
     * Extract the key ID from a JWT token.
     *
     * @param token the token to extract from
     * @return the key ID
     */
    private String extractKeyId(String token) {
        // For simplicity, we'll use a default key ID
        // In a real implementation, you would extract the key ID from the token header
        return "auth-key-id";
    }

    /**
     * Get the public key for a key ID.
     * If the key is not in the cache, fetch it from the JWK Set.
     *
     * @param kid the key ID
     * @return the public key
     */
    private PublicKey getPublicKey(String kid) {
        // Check if the key is in the cache
        if (keyCache.containsKey(kid)) {
            return keyCache.get(kid);
        }
        
        // Fetch the JWK Set and update the cache
        fetchJwkSet();
        
        // Return the key from the cache
        return keyCache.get(kid);
    }

    /**
     * Fetch the JWK Set from the auth-service.
     */
    private void fetchJwkSet() {
        try {
            // For simplicity, we'll use a hardcoded public key
            // In a real implementation, you would fetch the JWK Set from the auth-service
            // and extract the public key for the key ID
            
            // Example of fetching JWK Set:
            // Map<String, Object> jwkSet = webClient.get()
            //         .uri(jwkSetUri)
            //         .retrieve()
            //         .bodyToMono(new ParameterizedTypeReference<Map<String, Object>>() {})
            //         .block();
            
            // Extract the public key from the JWK Set
            // For now, we'll use a hardcoded public key
            String publicKeyPEM = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAu1SU1LfVLPHCozMxH2Mo4lgOEePzNm0tRgeLezV6ffAt0gunVTLw7onLRnrq0/IzW7yWR7QkrmBL7jTKEn5u+qKhbwKfBstIs+bMY2Zkp18gnTxKLxoS2tFczGkPLPgizskuemMghRniWaoLcyehkd3qqGElvW/VDL5AaWTg0nLVkjRo9z+40RQzuVaE8AkAFmxZzow3x+VJYKdjykkJ0iT9wCS0DRTXu269V264Vf/3jvredZiKRkgwlL9xNAwxXFg0x/XFw005UWVRIkdgcKWTjpBP2dPwVZ4WWC+9aGVd+Gyn1o0CLelf4rEjGoXbAAEgAqeGUxrcIlbjXfbcmwIDAQAB";
            
            // Convert PEM to PublicKey
            byte[] publicKeyBytes = Base64.getDecoder().decode(publicKeyPEM);
            X509EncodedKeySpec keySpec = new X509EncodedKeySpec(publicKeyBytes);
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            PublicKey publicKey = keyFactory.generatePublic(keySpec);
            
            // Add the public key to the cache
            keyCache.put("auth-key-id", publicKey);
        } catch (Exception e) {
            log.error("Failed to fetch JWK Set: {}", e.getMessage());
        }
    }
}

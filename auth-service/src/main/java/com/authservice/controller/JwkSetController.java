package com.authservice.controller;

import com.authservice.security.JwtService;
import com.nimbusds.jose.jwk.JWK;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.interfaces.RSAPublicKey;
import java.util.Map;

/**
 * Controller for exposing the JWK Set endpoint.
 * This endpoint is used by resource servers to validate JWT tokens.
 */
@RestController
@RequiredArgsConstructor
public class JwkSetController {

    private final JwtService jwtService;

    /**
     * Endpoint to expose the JWK Set.
     * This is typically accessed at /.well-known/jwks.json
     *
     * @return the JWK Set as a JSON object
     */
    @GetMapping("/.well-known/jwks.json")
    public Map<String, Object> jwkSet() {
        RSAPublicKey publicKey = jwtService.getRsaPublicKey();
        
        // Create a JWK from the RSA public key
        JWK jwk = new RSAKey.Builder(publicKey)
                .keyID("auth-key-id")
                .build();
        
        // Create a JWK Set with the JWK
        JWKSet jwkSet = new JWKSet(jwk);
        
        // Return the JWK Set as a JSON object
        return jwkSet.toJSONObject();
    }
}

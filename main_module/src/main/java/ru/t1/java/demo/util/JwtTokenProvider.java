package ru.t1.java.demo.util;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.security.Key;
import java.util.Date;

@Component
public class JwtTokenProvider {

    @Value("${spring.security.token}")
    private String secretKeyString;

    private Key secretKey;

    @PostConstruct
    public void init() {
        secretKey = Keys.hmacShaKeyFor(Decoders.BASE64.decode(secretKeyString));
    }

    public String generateToken(String clientId) {
        long now = System.currentTimeMillis();
        long validity = 3600000; // 1 час

        return Jwts.builder()
                .setSubject(clientId)
                .setIssuedAt(new Date(now))
                .setExpiration(new Date(now + validity))
                .signWith(secretKey, SignatureAlgorithm.HS256)
                .compact();
    }
}

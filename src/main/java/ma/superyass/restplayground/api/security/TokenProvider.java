package ma.superyass.restplayground.api.security;

import java.util.*;
import java.util.stream.Collectors;
import javax.annotation.PostConstruct;
import javax.inject.Inject;
import io.jsonwebtoken.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.enterprise.context.ApplicationScoped;
import ma.superyass.restplayground.api.config.JwtSecurityConfig;
import ma.superyass.restplayground.security.entities.User;

@ApplicationScoped
public class TokenProvider {

    private static final String AUTHORITIES_KEY = "auth";

    private String secretKey;

    private long tokenValidityInSeconds;

    private long tokenValidityInSecondsForRememberMe;

    @Inject
    private JwtSecurityConfig securityConfig;

    @PostConstruct
    public void init() {
        this.secretKey = securityConfig.getSecret();
        this.tokenValidityInSeconds = 1000 * securityConfig.getTokenValidityInSeconds();
        this.tokenValidityInSecondsForRememberMe = 1000 * securityConfig.getTokenValidityInSecondsForRememberMe();
    }

    public String createToken(User user, Boolean rememberMe) {
        String authorities = user.getAuthorities().stream()
                .map(authority -> authority.getName())
                .collect(Collectors.joining(","));

        long now = (new Date()).getTime();
        Date validity;
        if (rememberMe) {
            validity = new Date(now + this.tokenValidityInSecondsForRememberMe);
        } else {
            validity = new Date(now + this.tokenValidityInSeconds);
        }
        
        return Jwts.builder()
                .setSubject(user.getLogin())
                .claim(AUTHORITIES_KEY, authorities)
                .signWith(SignatureAlgorithm.HS512, secretKey)
                .setExpiration(validity)
                .compact();
    }

    public UserAuthenticationToken getAuthentication(String token) {
        Claims claims = Jwts.parser()
                .setSigningKey(secretKey)
                .parseClaimsJws(token)
                .getBody();
        
        Set<String> authorities
                = Arrays.asList(claims.get(AUTHORITIES_KEY).toString().split(",")).stream()
                        .collect(Collectors.toSet());

        return new UserAuthenticationToken(claims.getSubject(), "", authorities);
    }

    public boolean validateToken(String authToken) {
        try {
            Jwts.parser().setSigningKey(secretKey).parseClaimsJws(authToken);
            return true;
        } catch (SignatureException e) {
            Logger.getLogger(TokenProvider.class.getName()).log(Level.SEVERE, e.getMessage());
            return false;
        }
    }
}

package id.co.taxpay.common.security;

import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.stereotype.Component;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.security.oauth2.jwt.JwsHeader;

import java.time.Duration;
import java.time.Instant;
import java.util.List;

@Component
public class JwtTokenService {

    private final JwtEncoder jwtEncoder;

    public JwtTokenService(JwtEncoder jwtEncoder) {
        this.jwtEncoder = jwtEncoder;
    }

    public String issue(String subject, String role, Duration ttl) {
        Instant now = Instant.now();
        List<String> scopes = switch (role == null ? "SYSTEM" : role.toUpperCase()) {
            case "MAKER" -> List.of("MAKER", "TAXPAYER");
            case "CHECKER" -> List.of("CHECKER", "TAXPAYER");
            default -> List.of("SYSTEM", "MAKER", "CHECKER", "TAXPAYER");
        };
        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer("taxpay-local")
                .subject(subject)
                .issuedAt(now)
                .expiresAt(now.plus(ttl))
                .claim("scope", String.join(" ", scopes))
                .build();
        JwsHeader header = JwsHeader.with(MacAlgorithm.HS256).build();
        return jwtEncoder.encode(JwtEncoderParameters.from(header, claims)).getTokenValue();
    }
}

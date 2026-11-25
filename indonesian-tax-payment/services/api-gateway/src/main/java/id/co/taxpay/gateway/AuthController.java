package id.co.taxpay.gateway;

import id.co.taxpay.common.security.JwtTokenService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Duration;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final JwtTokenService jwtTokenService;

    public AuthController(JwtTokenService jwtTokenService) {
        this.jwtTokenService = jwtTokenService;
    }

    @PostMapping("/token")
    public TokenResponse token(@Valid @RequestBody TokenRequest request) {
        String token = jwtTokenService.issue(request.username(), request.role(), Duration.ofHours(1));
        return new TokenResponse(token, "Bearer", 3600);
    }

    public record TokenRequest(
            @NotBlank String username,
            @NotBlank String role
    ) {
    }

    public record TokenResponse(String accessToken, String tokenType, long expiresIn) {
    }
}

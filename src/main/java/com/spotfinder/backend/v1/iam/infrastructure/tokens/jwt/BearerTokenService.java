package com.spotfinder.backend.v1.iam.infrastructure.tokens.jwt;





import com.spotfinder.backend.v1.iam.application.internal.outboundservices.tokens.TokenService;
import com.spotfinder.backend.v1.iam.infrastructure.tokens.jwt.services.TokenServiceImpl;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.core.Authentication;

/**
 * This interface is a marker interface for the JWT token service.
 * It extends the {@link TokenService} interface.
 * This interface is used to inject the JWT token service in the {@link TokenServiceImpl} class.
 */
public interface BearerTokenService extends TokenService {

    /**
     * This method is responsible for extracting the JWT token from the HTTP request.
     * @param token the HTTP request
     * @return String the JWT token
     */
    String getBearerTokenFrom(HttpServletRequest token);

    /**
     * This method is responsible for generating a JWT token from an authentication object.
     * @param authentication the authentication object
     * @return String the JWT token
     * @see Authentication
     */
    String generateToken(Authentication authentication);

    /**
     * Generates a JWT token using explicit subject and claims.
     * Subject MUST be the user id as string. Additional claims include email, roles and isEmailVerified.
     *
     * @param userId the user's id (subject)
     * @param email the user's email
     * @param roles the user's roles
     * @param isEmailVerified whether the email is verified
     * @return signed JWT token
     */
    String generateToken(Long userId, String email, java.util.List<String> roles, boolean isEmailVerified);

    /**
     * Extracts the email claim from the token.
     */
    String getEmailFromToken(String token);

    /**
     * Extracts the subject (userId) from the token.
     */
    String getUserIdFromToken(String token);
}

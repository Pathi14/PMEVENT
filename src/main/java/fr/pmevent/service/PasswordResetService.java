package fr.pmevent.service;

import fr.pmevent.entity.PasswordResetTokenEntity;
import fr.pmevent.entity.UserEntity;
import fr.pmevent.repository.PasswordResetTokenRepository;
import fr.pmevent.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.HexFormat;

@Service
@RequiredArgsConstructor
public class PasswordResetService {
    private static final SecureRandom SECURE_RANDOM = new SecureRandom();
    private static final int TOKEN_BYTES = 32;

    private final UserRepository userRepository;
    private final PasswordResetTokenRepository tokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final ObjectProvider<MailService> mailServiceProvider;

    @Value("${frontend.url:http://localhost:4200}")
    private String frontendUrl;

    @Transactional
    public void requestReset(String email) {
        userRepository.findByEmailIgnoreCase(email.trim()).ifPresent(user -> {
            tokenRepository.deleteByUser(user);

            byte[] tokenBytes = new byte[TOKEN_BYTES];
            SECURE_RANDOM.nextBytes(tokenBytes);
            String token = Base64.getUrlEncoder().withoutPadding().encodeToString(tokenBytes);

            PasswordResetTokenEntity resetToken = new PasswordResetTokenEntity();
            resetToken.setUser(user);
            resetToken.setTokenHash(hashToken(token));
            resetToken.setExpiresAt(LocalDateTime.now().plusMinutes(30));
            tokenRepository.save(resetToken);

            String resetUrl = frontendUrl.replaceAll("/+$", "")
                    + "/reinitialiser-mot-de-passe?token=" + token;
            mailServiceProvider.ifAvailable(mailService ->
                    mailService.sendPasswordResetEmail(user.getEmail(), resetUrl));
        });
    }

    @Transactional
    public void resetPassword(String token, String newPassword) {
        if (token == null || token.isBlank()) {
            throw new IllegalArgumentException("Lien de réinitialisation invalide ou expiré.");
        }

        PasswordResetTokenEntity resetToken = tokenRepository.findByTokenHash(hashToken(token))
                .filter(candidate -> candidate.getExpiresAt().isAfter(LocalDateTime.now()))
                .orElseThrow(() -> new IllegalArgumentException("Lien de réinitialisation invalide ou expiré."));

        UserEntity user = resetToken.getUser();
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
        tokenRepository.deleteByUser(user);
    }

    private String hashToken(String token) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256")
                    .digest(token.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(digest);
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException("SHA-256 is not available", exception);
        }
    }
}
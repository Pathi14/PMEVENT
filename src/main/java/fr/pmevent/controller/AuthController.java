package fr.pmevent.controller;

import fr.pmevent.common.dto.authentication.AuthResponse;
import fr.pmevent.common.dto.authentication.LoginDto;
import fr.pmevent.common.dto.authentication.PasswordResetConfirmation;
import fr.pmevent.common.dto.authentication.PasswordResetRequest;
import fr.pmevent.common.dto.authentication.RegisterDto;
import fr.pmevent.configuration.jwt.JwtUtil;
import fr.pmevent.entity.UserEntity;
import fr.pmevent.service.UserService;
import fr.pmevent.service.PasswordResetService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/auth")
@AllArgsConstructor
public class AuthController {

    private final UserService userService;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final AuthenticationManager authenticationManager;
    private final PasswordResetService passwordResetService;

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@RequestBody @Valid RegisterDto userDto) {
        UserEntity user = userService.createUser(userDto);
        String token = jwtUtil.generateToken(new org.springframework.security.core.userdetails.User(
                user.getEmail(), user.getPassword(), List.of()
        ));
        return ResponseEntity.ok(new AuthResponse(token));
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@RequestBody @Valid LoginDto request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
        );

        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        String token = jwtUtil.generateToken(userDetails);

        return ResponseEntity.ok(new AuthResponse(token));
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<Map<String, String>> requestPasswordReset(
            @RequestBody @Valid PasswordResetRequest request
    ) {
        passwordResetService.requestReset(request.email());
        return ResponseEntity.ok(Map.of(
                "message", "Si un compte correspond à cette adresse, un lien de réinitialisation a été envoyé."
        ));
    }

    @PostMapping("/reset-password")
    public ResponseEntity<Map<String, String>> resetPassword(
            @RequestBody @Valid PasswordResetConfirmation request
    ) {
        try {
            passwordResetService.resetPassword(request.token(), request.password());
            return ResponseEntity.ok(Map.of("message", "Votre mot de passe a été modifié."));
        } catch (IllegalArgumentException exception) {
            return ResponseEntity.badRequest().body(Map.of("error", exception.getMessage()));
        }
    }
}

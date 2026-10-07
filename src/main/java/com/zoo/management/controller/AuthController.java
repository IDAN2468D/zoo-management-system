package com.zoo.management.controller;

import com.zoo.management.dto.AuthResponse;
import com.zoo.management.dto.LoginRequest;
import com.zoo.management.dto.RegisterRequest;
import com.zoo.management.model.AppUser;
import com.zoo.management.model.UserRole;
import com.zoo.management.repository.AppUserRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*")
public class AuthController {

    private static final Logger log = LoggerFactory.getLogger(AuthController.class);

    private final AuthenticationManager authenticationManager;
    private final AppUserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public AuthController(AuthenticationManager authenticationManager,
                          AppUserRepository userRepository,
                          PasswordEncoder passwordEncoder) {
        this.authenticationManager = authenticationManager;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest request) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getUsername().trim(), request.getPassword())
            );

            SecurityContext context = SecurityContextHolder.createEmptyContext();
            context.setAuthentication(authentication);
            SecurityContextHolder.setContext(context);

            Optional<AppUser> userOpt = userRepository.findByUsername(request.getUsername().trim());
            AppUser user = userOpt.orElse(null);

            UserRole role = user != null ? user.getRole() : UserRole.KEEPER;
            String fullName = user != null ? user.getFullName() : request.getUsername();

            log.info("🔑 [AUTH: התחברות מוצלחת] משתמש: '{}' | תפקיד: {}", request.getUsername(), role);

            AuthResponse response = new AuthResponse(
                    true,
                    request.getUsername().trim(),
                    fullName,
                    role,
                    role.getSpringRole(),
                    "התחברת בהצלחה למערכת!"
            );

            return ResponseEntity.ok(response);
        } catch (BadCredentialsException ex) {
            log.warn("⚠️ [AUTH: שגיאת התחברות] שם משתמש או סיסמה שגויים: '{}'", request.getUsername());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new AuthResponse(false, null, null, null, null, "שם משתמש או סיסמה שגויים"));
        } catch (Exception ex) {
            log.error("❌ [AUTH: שגיאה] שגיאה באימות משתמש", ex);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new AuthResponse(false, null, null, null, null, "שגיאה בביצוע ההתחברות: " + ex.getMessage()));
        }
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody RegisterRequest request) {
        String username = request.getUsername().trim();
        if (userRepository.existsByUsername(username)) {
            return ResponseEntity.badRequest()
                    .body(new AuthResponse(false, null, null, null, null, "שם המשתמש '" + username + "' כבר תפוס במערכת"));
        }

        UserRole role = request.getRole() != null ? request.getRole() : UserRole.KEEPER;
        AppUser newUser = new AppUser(
                username,
                passwordEncoder.encode(request.getPassword()),
                request.getFullName().trim(),
                role
        );

        AppUser saved = userRepository.save(newUser);
        log.info("📝 [AUTH: הרשמת משתמש חדש] שם: '{}' | משתמש: '{}' | תפקיד: {}", saved.getFullName(), saved.getUsername(), saved.getRole());

        AuthResponse response = new AuthResponse(
                true,
                saved.getUsername(),
                saved.getFullName(),
                saved.getRole(),
                saved.getRole().getSpringRole(),
                "ההרשמה בוצעה בהצלחה! החשבון נוצר והתחברת למערכת."
        );

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/me")
    public ResponseEntity<AuthResponse> getCurrentUser(Authentication authentication) {
        if (authentication != null && authentication.isAuthenticated() && !(authentication instanceof AnonymousAuthenticationToken)) {
            String username = authentication.getName();
            Optional<AppUser> userOpt = userRepository.findByUsername(username);

            if (userOpt.isPresent()) {
                AppUser user = userOpt.get();
                return ResponseEntity.ok(new AuthResponse(
                        true,
                        user.getUsername(),
                        user.getFullName(),
                        user.getRole(),
                        user.getRole().getSpringRole(),
                        "משתמש מחובר"
                ));
            } else {
                String roleAuth = authentication.getAuthorities().stream()
                        .findFirst()
                        .map(GrantedAuthority::getAuthority)
                        .orElse("ROLE_GUEST");
                return ResponseEntity.ok(new AuthResponse(
                        true,
                        username,
                        username,
                        null,
                        roleAuth,
                        "משתמש מחובר"
                ));
            }
        }

        return ResponseEntity.ok(AuthResponse.guest());
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(HttpServletRequest request, HttpServletResponse response, Authentication authentication) {
        if (authentication != null) {
            new SecurityContextLogoutHandler().logout(request, response, authentication);
        }
        SecurityContextHolder.clearContext();
        return ResponseEntity.ok(new AuthResponse(false, null, null, null, null, "התנתקת בהצלחה מהמערכת"));
    }
}

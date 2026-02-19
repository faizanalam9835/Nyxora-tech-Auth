package Auth.service.service;

import Auth.service.dto.LoginRequest;
import Auth.service.dto.SignupRequest;
import Auth.service.models.User;
import Auth.service.repository.UserRepository;
import Auth.service.dto.LoginResponse;
import Auth.service.security.JwtProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import java.util.Map;
import java.util.HashMap;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtProvider jwtProvider;
    private final JavaMailSender mailSender;

    // 🔐 SIGNUP (ONLY SUPER ADMIN)
    public String signup(SignupRequest req) {

        if (userRepository.existsByUsername(req.getUsername()))
            throw new RuntimeException("Username already exists");

        if (userRepository.existsByEmail(req.getEmail()))
            throw new RuntimeException("Email already exists");

        if (userRepository.existsByCompanyId(req.getCompanyId()))
            throw new RuntimeException("Company already registered");

        User user = User.builder()
                .username(req.getUsername())
                .email(req.getEmail())
                .password(passwordEncoder.encode(req.getPassword()))
                .companyId(req.getCompanyId())
                .companyName(req.getCompanyName())
                .tenantId(UUID.randomUUID().toString())
                .active(true)
                .role("SUPER_ADMIN") // ✅ automatically SUPER_ADMIN
                .build();

        userRepository.save(user);

        try {
            sendCongratsMail(user);
        } catch (Exception e) {
            System.out.println("Mail failed but user created: " + e.getMessage());
        }

        return "Signup successful. Verification email sent.";
    }



    // 🔐 LOGIN (ALL USERS)
    public LoginResponse login(LoginRequest req) {

        User user = userRepository.findByEmail(req.getEmail())
                .orElseThrow(() -> new RuntimeException("Invalid credentials"));

        if (!passwordEncoder.matches(req.getPassword(), user.getPassword()))
            throw new RuntimeException("Invalid credentials");

        String token = jwtProvider.generateToken(user);

        Map<String, Object> data = new HashMap<>();
        data.put("token", token);
        data.put("userId", user.getId());
        data.put("email", user.getEmail());


        return new LoginResponse(
                true,
                "Login successful",
                data
        );
    }

    // 📧 Mail
    private void sendCongratsMail(User user) {
        SimpleMailMessage mail = new SimpleMailMessage();
        mail.setTo(user.getEmail());
        mail.setSubject("🎉 Company Registered Successfully");
        mail.setText("""
                Hi %s 👋

                Your company "%s" has been successfully registered.

                You are now the Super Admin.

                Regards,
                Auth Service
                """.formatted(user.getUsername(), user.getCompanyName()));

        mailSender.send(mail);
    }
}


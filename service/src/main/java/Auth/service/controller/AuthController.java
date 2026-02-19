package Auth.service.controller;

import Auth.service.dto.LoginResponse;
import Auth.service.dto.LoginRequest;
import Auth.service.dto.SignupRequest;
import Auth.service.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;

import java.util.Map;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserService userService;

   @PostMapping("/signup")
    public ResponseEntity<?> signup(@RequestBody SignupRequest req) {
        userService.signup(req);
        return ResponseEntity.ok("Super Admin registered successfully");
    }

    @PostMapping("/login")

    public LoginResponse login(@RequestBody LoginRequest req) {
        return userService.login(req);
    }

}

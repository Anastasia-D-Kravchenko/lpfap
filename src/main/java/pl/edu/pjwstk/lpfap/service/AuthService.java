package pl.edu.pjwstk.lpfap.service;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.edu.pjwstk.lpfap.config.JwtUtils;
import pl.edu.pjwstk.lpfap.dto.AuthResponse;
import pl.edu.pjwstk.lpfap.dto.LoginRequest;
import pl.edu.pjwstk.lpfap.dto.RegisterRequest;
import pl.edu.pjwstk.lpfap.exception.BadRequestException;
import pl.edu.pjwstk.lpfap.exception.ResourceNotFoundException;
import pl.edu.pjwstk.lpfap.model.Role;
import pl.edu.pjwstk.lpfap.model.User;
import pl.edu.pjwstk.lpfap.repository.UserRepository;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtils jwtUtils;

    public AuthService(UserRepository userRepository,
                       PasswordEncoder passwordEncoder,
                       JwtUtils jwtUtils) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtils = jwtUtils;
    }

    @Transactional
    public AuthResponse register(RegisterRequest req) {
        if (userRepository.existsByEmail(req.getEmail())) {
            throw new BadRequestException("Email already registered: " + req.getEmail());
        }
        User user = new User();
        user.setEmail(req.getEmail());
        user.setPasswordHash(passwordEncoder.encode(req.getPassword()));
        user.setFullName(req.getFullName());
        user.setPhone(req.getPhone());
        user.setRole(Role.USER);
        userRepository.save(user);
        String token = jwtUtils.generateToken(user.getEmail(), user.getRole().name());
        return new AuthResponse(token, user.getEmail(), user.getRole().name());
    }

    public AuthResponse login(LoginRequest req) {
        User user = userRepository.findByEmail(req.getEmail())
                .orElseThrow(() -> new BadRequestException("Invalid email or password"));
        if (!passwordEncoder.matches(req.getPassword(), user.getPasswordHash())) {
            throw new BadRequestException("Invalid email or password");
        }
        String token = jwtUtils.generateToken(user.getEmail(), user.getRole().name());
        return new AuthResponse(token, user.getEmail(), user.getRole().name());
    }
}

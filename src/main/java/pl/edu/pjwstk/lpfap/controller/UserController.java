package pl.edu.pjwstk.lpfap.controller;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import pl.edu.pjwstk.lpfap.dto.UpdateProfileRequest;
import pl.edu.pjwstk.lpfap.dto.UserResponse;
import pl.edu.pjwstk.lpfap.exception.ResourceNotFoundException;
import pl.edu.pjwstk.lpfap.model.User;
import pl.edu.pjwstk.lpfap.repository.UserRepository;
import pl.edu.pjwstk.lpfap.service.UploadService;

import java.io.IOException;

@RestController
@RequestMapping("/api/v1/users")
public class UserController {

    private final UserRepository userRepository;
    private final UploadService uploadService;

    public UserController(UserRepository userRepository, UploadService uploadService) {
        this.userRepository = userRepository;
        this.uploadService  = uploadService;
    }

    @GetMapping("/me")
    public ResponseEntity<UserResponse> getMe(@AuthenticationPrincipal String email) {
        User user = findUser(email);
        return ResponseEntity.ok(UserResponse.from(user));
    }

    @PatchMapping("/me")
    public ResponseEntity<UserResponse> updateMe(@AuthenticationPrincipal String email,
                                                  @Valid @RequestBody UpdateProfileRequest req) {
        User user = findUser(email);
        user.setFullName(req.getFullName());
        if (req.getPhone()     != null) user.setPhone(req.getPhone().isBlank() ? null : req.getPhone());
        if (req.getBio()       != null) user.setBio(req.getBio().isBlank() ? null : req.getBio());
        if (req.getAvatarUrl() != null) user.setAvatarUrl(req.getAvatarUrl().isBlank() ? null : req.getAvatarUrl());
        return ResponseEntity.ok(UserResponse.from(userRepository.save(user)));
    }

    @PostMapping(value = "/me/avatar", consumes = "multipart/form-data")
    public ResponseEntity<UserResponse> uploadAvatar(@AuthenticationPrincipal String email,
                                                     @RequestParam("file") MultipartFile file) throws IOException {
        User user = findUser(email);
        user.setAvatarUrl(uploadService.save(file, "avatars", "u" + user.getId()));
        return ResponseEntity.ok(UserResponse.from(userRepository.save(user)));
    }

    private User findUser(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + email));
    }
}

package pl.edu.pjwstk.lpfap.controller;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import pl.edu.pjwstk.lpfap.dto.UpdateRoleRequest;
import pl.edu.pjwstk.lpfap.dto.UserResponse;
import pl.edu.pjwstk.lpfap.service.AdminService;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/admin")
public class AdminController {

    private final AdminService adminService;

    public AdminController(AdminService adminService) {
        this.adminService = adminService;
    }

    @GetMapping("/users")
    public ResponseEntity<List<UserResponse>> listUsers() {
        return ResponseEntity.ok(adminService.listUsers());
    }

    @PatchMapping("/users/{id}/role")
    public ResponseEntity<UserResponse> updateRole(@PathVariable Long id,
                                                    @Valid @RequestBody UpdateRoleRequest req) {
        return ResponseEntity.ok(adminService.updateRole(id, req));
    }

    @GetMapping("/reports/time-to-adoption")
    public ResponseEntity<Map<String, Object>> timeToAdoption(
            @AuthenticationPrincipal String email) {
        return ResponseEntity.ok(adminService.generateTimeToAdoptionReport(email));
    }

    @GetMapping("/reports/funnel")
    public ResponseEntity<Map<String, Object>> funnel(
            @AuthenticationPrincipal String email) {
        return ResponseEntity.ok(adminService.generateFunnelReport(email));
    }

    @GetMapping("/reports/monthly")
    public ResponseEntity<Map<String, Object>> monthly(
            @AuthenticationPrincipal String email,
            @RequestParam int year,
            @RequestParam int month) {
        return ResponseEntity.ok(adminService.generateMonthlyReport(email, year, month));
    }
}

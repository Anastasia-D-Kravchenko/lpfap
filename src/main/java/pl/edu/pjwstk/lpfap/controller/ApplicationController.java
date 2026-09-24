package pl.edu.pjwstk.lpfap.controller;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import pl.edu.pjwstk.lpfap.dto.ApplicationRequest;
import pl.edu.pjwstk.lpfap.dto.ApplicationResponse;
import pl.edu.pjwstk.lpfap.service.ApplicationService;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/v1/applications")
public class ApplicationController {

    private final ApplicationService applicationService;

    public ApplicationController(ApplicationService applicationService) {
        this.applicationService = applicationService;
    }

    @PostMapping
    public ResponseEntity<ApplicationResponse> submit(
            @AuthenticationPrincipal String email,
            @Valid @RequestBody ApplicationRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(applicationService.submit(email, req));
    }

    @GetMapping("/my")
    public ResponseEntity<List<ApplicationResponse>> myApplications(
            @AuthenticationPrincipal String email) {
        return ResponseEntity.ok(applicationService.getMyApplications(email));
    }

    @PatchMapping("/{id}/withdraw")
    public ResponseEntity<ApplicationResponse> withdraw(
            @AuthenticationPrincipal String email,
            @PathVariable Long id) {
        return ResponseEntity.ok(applicationService.withdraw(email, id));
    }

    @GetMapping
    public ResponseEntity<List<ApplicationResponse>> allApplications(
            @RequestParam(required = false) pl.edu.pjwstk.lpfap.model.ApplicationStatus status,
            @RequestParam(required = false) Long animalId,
            @RequestParam(required = false)
                @org.springframework.format.annotation.DateTimeFormat(iso = org.springframework.format.annotation.DateTimeFormat.ISO.DATE)
                java.time.LocalDate from,
            @RequestParam(required = false)
                @org.springframework.format.annotation.DateTimeFormat(iso = org.springframework.format.annotation.DateTimeFormat.ISO.DATE)
                java.time.LocalDate to) {
        if (status == null && animalId == null && from == null && to == null) {
            return ResponseEntity.ok(applicationService.getAllApplications());
        }
        return ResponseEntity.ok(applicationService.search(status, animalId, from, to));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApplicationResponse> getApplication(@PathVariable Long id) {
        return ResponseEntity.ok(applicationService.getById(id));
    }

    @PatchMapping("/{id}/approve")
    public ResponseEntity<ApplicationResponse> approve(@PathVariable Long id) {
        return ResponseEntity.ok(applicationService.approve(id));
    }

    @PatchMapping("/{id}/reject")
    public ResponseEntity<ApplicationResponse> reject(@PathVariable Long id) {
        return ResponseEntity.ok(applicationService.reject(id));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        applicationService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping(value = "/{id}/photos", consumes = "multipart/form-data")
    public ResponseEntity<ApplicationResponse> addPhoto(@AuthenticationPrincipal String email,
                                                        @PathVariable Long id,
                                                        @RequestParam("file") MultipartFile file) throws IOException {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(applicationService.addPhoto(email, id, file));
    }

    @PatchMapping("/{id}/request-return")
    public ResponseEntity<ApplicationResponse> requestReturn(@AuthenticationPrincipal String email,
                                                             @PathVariable Long id) {
        return ResponseEntity.ok(applicationService.requestReturn(email, id));
    }

    @PatchMapping("/{id}/decline-return")
    public ResponseEntity<ApplicationResponse> declineReturn(@AuthenticationPrincipal String email,
                                                             @PathVariable Long id) {
        return ResponseEntity.ok(applicationService.declineReturn(email, id));
    }

    @PatchMapping("/{id}/admin-request-return")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApplicationResponse> adminRequestReturn(@PathVariable Long id) {
        return ResponseEntity.ok(applicationService.adminRequestReturn(id));
    }

    @PatchMapping("/{id}/finalize-return")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApplicationResponse> finalizeReturn(@PathVariable Long id) {
        return ResponseEntity.ok(applicationService.finalizeReturn(id));
    }
}

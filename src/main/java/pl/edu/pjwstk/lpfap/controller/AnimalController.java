package pl.edu.pjwstk.lpfap.controller;

import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import pl.edu.pjwstk.lpfap.dto.AnimalRequest;
import pl.edu.pjwstk.lpfap.dto.AnimalResponse;
import pl.edu.pjwstk.lpfap.model.Species;
import pl.edu.pjwstk.lpfap.service.AnimalService;

import java.io.IOException;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/animals")
public class AnimalController {

    private final AnimalService animalService;

    public AnimalController(AnimalService animalService) {
        this.animalService = animalService;
    }

    @GetMapping
    public ResponseEntity<Page<AnimalResponse>> listAvailable(
            @RequestParam(required = false) Species species,
            @RequestParam(required = false) String age,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(animalService.listAvailable(species, age, pageable));
    }

    @GetMapping("/all")
    public ResponseEntity<java.util.List<AnimalResponse>> listAll() {
        return ResponseEntity.ok(animalService.listAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<AnimalResponse> getAnimal(@PathVariable Long id) {
        return ResponseEntity.ok(animalService.getById(id));
    }

    @PostMapping
    public ResponseEntity<AnimalResponse> createAnimal(@Valid @RequestBody AnimalRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED).body(animalService.create(req));
    }

    @PutMapping("/{id}")
    public ResponseEntity<AnimalResponse> updateAnimal(@PathVariable Long id,
                                                        @Valid @RequestBody AnimalRequest req) {
        return ResponseEntity.ok(animalService.update(id, req));
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<AnimalResponse> patchStatus(@PathVariable Long id,
                                                       @RequestBody Map<String, String> body) {
        String status = body.get("status");
        return ResponseEntity.ok(animalService.patchStatus(id, status));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> archiveAnimal(@PathVariable Long id) {
        animalService.archive(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping(value = "/{id}/photos", consumes = "multipart/form-data")
    public ResponseEntity<AnimalResponse> addPhoto(@PathVariable Long id,
                                                   @RequestParam("file") MultipartFile file) throws IOException {
        return ResponseEntity.status(HttpStatus.CREATED).body(animalService.addPhoto(id, file));
    }

    @DeleteMapping("/{id}/photos/{photoId}")
    public ResponseEntity<AnimalResponse> deletePhoto(@PathVariable Long id, @PathVariable Long photoId) {
        return ResponseEntity.ok(animalService.removePhoto(id, photoId));
    }
}

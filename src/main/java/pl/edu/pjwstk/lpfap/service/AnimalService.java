package pl.edu.pjwstk.lpfap.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import pl.edu.pjwstk.lpfap.dto.AnimalRequest;
import pl.edu.pjwstk.lpfap.dto.AnimalResponse;
import pl.edu.pjwstk.lpfap.exception.BadRequestException;
import pl.edu.pjwstk.lpfap.exception.ResourceNotFoundException;
import pl.edu.pjwstk.lpfap.model.Animal;
import pl.edu.pjwstk.lpfap.model.AnimalPhoto;
import pl.edu.pjwstk.lpfap.model.AnimalStatus;
import pl.edu.pjwstk.lpfap.model.Shelter;
import pl.edu.pjwstk.lpfap.model.Species;
import pl.edu.pjwstk.lpfap.repository.AnimalPhotoRepository;
import pl.edu.pjwstk.lpfap.repository.AnimalRepository;
import pl.edu.pjwstk.lpfap.repository.ShelterRepository;

import java.io.IOException;
import java.util.List;

@Service
@Transactional(readOnly = true)
public class AnimalService {

    private final AnimalRepository animalRepository;
    private final ShelterRepository shelterRepository;
    private final AnimalPhotoRepository photoRepository;
    private final UploadService uploadService;

    public AnimalService(AnimalRepository animalRepository,
                         ShelterRepository shelterRepository,
                         AnimalPhotoRepository photoRepository,
                         UploadService uploadService) {
        this.animalRepository = animalRepository;
        this.shelterRepository = shelterRepository;
        this.photoRepository = photoRepository;
        this.uploadService = uploadService;
    }

    public Page<AnimalResponse> listAvailable(Species species, String age, Pageable pageable) {
        Integer minAge = null;
        Integer maxAge = null;
        if (age != null && !age.isBlank()) {
            switch (age.toLowerCase()) {
                case "young"  -> { minAge = 0;  maxAge = 11; }
                case "adult"  -> { minAge = 12; maxAge = 59; }
                case "senior" -> { minAge = 60; }
                default -> throw new BadRequestException(
                        "Invalid age filter: " + age + " (allowed: young, adult, senior)");
            }
        }
        Page<Animal> page = animalRepository.search(
                AnimalStatus.AVAILABLE, species, minAge, maxAge, pageable);
        return page.map(a -> AnimalResponse.from(a, galleryUrls(a.getId())));
    }

    public List<AnimalResponse> listAll() {
        return animalRepository.findAllWithShelter().stream()
                .map(a -> AnimalResponse.from(a, galleryUrls(a.getId())))
                .toList();
    }

    public AnimalResponse getById(Long id) {
        Animal animal = animalRepository.findByIdWithShelter(id)
                .orElseThrow(() -> new ResourceNotFoundException("Animal not found: " + id));
        return AnimalResponse.from(animal, galleryUrls(id));
    }

    @Transactional
    public AnimalResponse create(AnimalRequest req) {
        Animal animal = new Animal();
        applyRequest(animal, req);
        Animal saved = animalRepository.save(animal);
        return AnimalResponse.from(
                animalRepository.findByIdWithShelter(saved.getId()).orElse(saved),
                galleryUrls(saved.getId()));
    }

    @Transactional
    public AnimalResponse update(Long id, AnimalRequest req) {
        Animal animal = findAnimal(id);
        applyRequest(animal, req);
        Animal saved = animalRepository.save(animal);
        return AnimalResponse.from(
                animalRepository.findByIdWithShelter(saved.getId()).orElse(saved),
                galleryUrls(saved.getId()));
    }

    @Transactional
    public AnimalResponse patchStatus(Long id, String status) {
        Animal animal = findAnimal(id);
        AnimalStatus current = animal.getStatus();
        AnimalStatus next;
        try {
            next = AnimalStatus.valueOf(status.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new BadRequestException("Invalid status: " + status);
        }
        if ((current == AnimalStatus.ADOPTED || current == AnimalStatus.ARCHIVED)
                && next == AnimalStatus.AVAILABLE) {
            throw new BadRequestException("Cannot transition from " + current + " back to AVAILABLE");
        }
        animal.setStatus(next);
        return AnimalResponse.from(animalRepository.save(animal), galleryUrls(animal.getId()));
    }

    @Transactional
    public void archive(Long id) {
        Animal animal = findAnimal(id);
        animal.archive();
        animalRepository.save(animal);
    }

    @Transactional
    public AnimalResponse addPhoto(Long animalId, MultipartFile file) throws IOException {
        Animal animal = findAnimal(animalId);
        String url = uploadService.save(file, "animals", "a" + animalId);
        int next = photoRepository.findByAnimalIdOrderBySortOrderAscIdAsc(animalId).size();
        photoRepository.save(new AnimalPhoto(animal, url, next));
        if (animal.getPhotoUrl() == null || animal.getPhotoUrl().isBlank()) {
            animal.setPhotoUrl(url);
            animalRepository.save(animal);
        }
        return AnimalResponse.from(
                animalRepository.findByIdWithShelter(animalId).orElse(animal),
                galleryUrls(animalId));
    }

    @Transactional
    public AnimalResponse removePhoto(Long animalId, Long photoId) {
        AnimalPhoto p = photoRepository.findById(photoId)
                .orElseThrow(() -> new ResourceNotFoundException("Photo not found: " + photoId));
        if (!p.getAnimal().getId().equals(animalId)) {
            throw new BadRequestException("Photo does not belong to this animal");
        }
        photoRepository.delete(p);
        return getById(animalId);
    }

    private void applyRequest(Animal animal, AnimalRequest req) {
        animal.setName(req.getName());
        animal.setSpecies(req.getSpecies());
        animal.setBreed(req.getBreed());
        animal.setAge(req.getAge());
        animal.setSex(req.getSex());
        animal.setDescription(req.getDescription());
        animal.setMedicalStatus(req.getMedicalStatus());
        animal.setPhotoUrl(req.getPhotoUrl());
        if (req.getShelterId() != null) {
            Shelter shelter = shelterRepository.findById(req.getShelterId())
                    .orElseThrow(() -> new ResourceNotFoundException("Shelter not found: " + req.getShelterId()));
            animal.setShelter(shelter);
        }
    }

    Animal findAnimal(Long id) {
        return animalRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Animal not found: " + id));
    }

    private List<String> galleryUrls(Long animalId) {
        return photoRepository.findByAnimalIdOrderBySortOrderAscIdAsc(animalId)
                .stream().map(AnimalPhoto::getUrl).toList();
    }
}

package pl.edu.pjwstk.lpfap.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import pl.edu.pjwstk.lpfap.dto.ApplicationRequest;
import pl.edu.pjwstk.lpfap.dto.ApplicationResponse;
import pl.edu.pjwstk.lpfap.exception.BadRequestException;
import pl.edu.pjwstk.lpfap.exception.ResourceNotFoundException;
import pl.edu.pjwstk.lpfap.model.*;
import pl.edu.pjwstk.lpfap.repository.AnimalRepository;
import pl.edu.pjwstk.lpfap.repository.ApplicationPhotoRepository;
import pl.edu.pjwstk.lpfap.repository.ApplicationRepository;
import pl.edu.pjwstk.lpfap.repository.UserRepository;

import java.io.IOException;
import java.util.List;

@Service
@Transactional(readOnly = true)
public class ApplicationService {

    private final ApplicationRepository applicationRepository;
    private final UserRepository userRepository;
    private final AnimalRepository animalRepository;
    private final AnimalService animalService;
    private final ApplicationPhotoRepository photoRepository;
    private final UploadService uploadService;

    public ApplicationService(ApplicationRepository applicationRepository,
                               UserRepository userRepository,
                               AnimalRepository animalRepository,
                               AnimalService animalService,
                               ApplicationPhotoRepository photoRepository,
                               UploadService uploadService) {
        this.applicationRepository = applicationRepository;
        this.userRepository = userRepository;
        this.animalRepository = animalRepository;
        this.animalService = animalService;
        this.photoRepository = photoRepository;
        this.uploadService = uploadService;
    }

    @Transactional
    public ApplicationResponse submit(String email, ApplicationRequest req) {
        User user = findUser(email);
        Animal animal = animalService.findAnimal(req.getAnimalId());
        if (animal.getStatus() != AnimalStatus.AVAILABLE) {
            throw new BadRequestException("Animal is not available for applications");
        }
        Application app = new Application();
        app.setType(req.getType());
        app.setMotivationText(req.getMotivationText());
        app.setApplicant(user);
        app.setAnimal(animal);
        Application saved = applicationRepository.save(app);
        return toResponse(applicationRepository.findByIdWithDetails(saved.getId()).orElse(saved));
    }

    public List<ApplicationResponse> getMyApplications(String email) {
        User user = findUser(email);
        return applicationRepository.findByApplicantId(user.getId())
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional
    public ApplicationResponse withdraw(String email, Long applicationId) {
        User user = findUser(email);
        Application app = findApplicationWithDetails(applicationId);
        if (!app.getApplicant().getId().equals(user.getId())) {
            throw new BadRequestException("This application does not belong to you");
        }
        if (app.getStatus() != ApplicationStatus.PENDING) {
            throw new BadRequestException("Only PENDING applications can be withdrawn");
        }
        app.withdraw();
        return toResponse(applicationRepository.save(app));
    }

    public List<ApplicationResponse> getAllApplications() {
        return applicationRepository.findAllWithDetails()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    public List<ApplicationResponse> search(ApplicationStatus status,
                                            Long animalId,
                                            java.time.LocalDate from,
                                            java.time.LocalDate to) {
        java.time.LocalDateTime fromDt = from != null ? from.atStartOfDay() : null;
        java.time.LocalDateTime toDt   = to   != null ? to.plusDays(1).atStartOfDay() : null;
        return applicationRepository.search(status, animalId, fromDt, toDt)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    public ApplicationResponse getById(Long id) {
        return toResponse(findApplicationWithDetails(id));
    }

    @Transactional
    public ApplicationResponse approve(Long id) {
        Application app = findApplicationWithDetails(id);
        if (app.getStatus() != ApplicationStatus.PENDING) {
            throw new BadRequestException("Only PENDING applications can be approved");
        }
        Animal animal = app.getAnimal();
        if (animal == null) throw new BadRequestException("Application has no associated animal");
        if (animal.getStatus() != AnimalStatus.AVAILABLE) {
            throw new BadRequestException(
                    "Cannot approve: animal '" + animal.getName() + "' is already " + animal.getStatus());
        }
        app.approve();
        if (app.getType() == ApplicationType.ADOPTION) animal.markAdopted();
        else                                            animal.markFostered();

        applicationRepository.findByAnimalId(animal.getId()).stream()
                .filter(other -> !other.getId().equals(app.getId()))
                .filter(other -> other.getStatus() == ApplicationStatus.PENDING)
                .forEach(other -> { other.reject(); applicationRepository.save(other); });

        return toResponse(applicationRepository.save(app));
    }

    @Transactional
    public ApplicationResponse reject(Long id) {
        Application app = findApplicationWithDetails(id);
        if (app.getStatus() != ApplicationStatus.PENDING) {
            throw new BadRequestException("Only PENDING applications can be rejected");
        }
        app.reject();
        return toResponse(applicationRepository.save(app));
    }

    @Transactional
    public void delete(Long id) {
        if (!applicationRepository.existsById(id)) {
            throw new ResourceNotFoundException("Application not found: " + id);
        }
        applicationRepository.deleteById(id);
    }

    @Transactional
    public ApplicationResponse requestReturn(String email, Long applicationId) {
        User user = findUser(email);
        Application app = findApplicationWithDetails(applicationId);
        if (!app.getApplicant().getId().equals(user.getId())) {
            throw new BadRequestException("This application does not belong to you");
        }
        return requestReturnInternal(app);
    }

    @Transactional
    public ApplicationResponse adminRequestReturn(Long applicationId) {
        return requestReturnInternal(findApplicationWithDetails(applicationId));
    }

    private ApplicationResponse requestReturnInternal(Application app) {
        if (app.getType() != ApplicationType.FOSTER) {
            throw new BadRequestException("Only FOSTER applications can request a return");
        }
        if (app.getStatus() != ApplicationStatus.APPROVED) {
            throw new BadRequestException("Only APPROVED foster applications can request a return");
        }
        app.setStatus(ApplicationStatus.RETURN_REQUESTED);
        return toResponse(applicationRepository.save(app));
    }

    @Transactional
    public ApplicationResponse declineReturn(String email, Long applicationId) {
        User user = findUser(email);
        Application app = findApplicationWithDetails(applicationId);
        if (!app.getApplicant().getId().equals(user.getId()))
            throw new BadRequestException("This application does not belong to you");
        if (app.getStatus() != ApplicationStatus.RETURN_REQUESTED)
            throw new BadRequestException("Only RETURN_REQUESTED applications can be declined");
        app.setStatus(ApplicationStatus.APPROVED);
        return toResponse(applicationRepository.save(app));
    }

    @Transactional
    public ApplicationResponse finalizeReturn(Long applicationId) {
        Application app = findApplicationWithDetails(applicationId);
        if (app.getStatus() != ApplicationStatus.RETURN_REQUESTED) {
            throw new BadRequestException("Only RETURN_REQUESTED applications can be finalised");
        }
        app.setStatus(ApplicationStatus.RETURNED);
        Animal animal = app.getAnimal();
        if (animal != null && animal.getStatus() == AnimalStatus.FOSTERED) {
            animal.setStatus(AnimalStatus.AVAILABLE);
            animalRepository.save(animal);
        }
        return toResponse(applicationRepository.save(app));
    }


    @Transactional
    public ApplicationResponse addPhoto(String email, Long applicationId, MultipartFile file) throws IOException {
        User user = findUser(email);
        Application app = findApplicationWithDetails(applicationId);
        if (!app.getApplicant().getId().equals(user.getId())) {
            throw new BadRequestException("This application does not belong to you");
        }
        String url = uploadService.save(file, "applications", "app" + applicationId);
        int next = photoRepository.findByApplicationIdOrderBySortOrderAscIdAsc(applicationId).size();
        photoRepository.save(new ApplicationPhoto(app, url, null, next));
        return toResponse(app);
    }


    private ApplicationResponse toResponse(Application a) {
        List<String> urls = photoRepository.findByApplicationIdOrderBySortOrderAscIdAsc(a.getId())
                .stream().map(ApplicationPhoto::getUrl).toList();
        return ApplicationResponse.from(a, urls);
    }

    private Application findApplicationWithDetails(Long id) {
        return applicationRepository.findByIdWithDetails(id)
                .orElseThrow(() -> new ResourceNotFoundException("Application not found: " + id));
    }

    private User findUser(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + email));
    }
}

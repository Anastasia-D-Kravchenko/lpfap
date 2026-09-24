package pl.edu.pjwstk.lpfap.repository;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.ListCrudRepository;
import pl.edu.pjwstk.lpfap.model.Application;
import pl.edu.pjwstk.lpfap.model.ApplicationStatus;
import java.util.List;
import java.util.Optional;

public interface ApplicationRepository extends ListCrudRepository<Application, Long> {

    @Query("SELECT a FROM Application a LEFT JOIN FETCH a.applicant LEFT JOIN FETCH a.animal WHERE a.applicant.id = :applicantId ORDER BY a.submissionDate DESC")
    List<Application> findByApplicantId(Long applicantId);

    @Query("SELECT a FROM Application a LEFT JOIN FETCH a.applicant LEFT JOIN FETCH a.animal WHERE a.animal.id = :animalId ORDER BY a.submissionDate DESC")
    List<Application> findByAnimalId(Long animalId);

    @Query("SELECT a FROM Application a LEFT JOIN FETCH a.applicant LEFT JOIN FETCH a.animal WHERE a.status = :status ORDER BY a.submissionDate DESC")
    List<Application> findByStatus(ApplicationStatus status);

    @Query("SELECT a FROM Application a LEFT JOIN FETCH a.applicant LEFT JOIN FETCH a.animal ORDER BY a.submissionDate DESC")
    List<Application> findAllWithDetails();

    @Query("SELECT a FROM Application a LEFT JOIN FETCH a.applicant LEFT JOIN FETCH a.animal "
            + "WHERE (:status IS NULL OR a.status = :status) "
            + "AND (:animalId IS NULL OR a.animal.id = :animalId) "
            + "AND (:from IS NULL OR a.submissionDate >= :from) "
            + "AND (:to   IS NULL OR a.submissionDate <= :to) "
            + "ORDER BY a.submissionDate DESC")
    List<Application> search(ApplicationStatus status,
                             Long animalId,
                             java.time.LocalDateTime from,
                             java.time.LocalDateTime to);

    @Query("SELECT a FROM Application a LEFT JOIN FETCH a.applicant LEFT JOIN FETCH a.animal WHERE a.id = :id")
    Optional<Application> findByIdWithDetails(Long id);

    long countByStatus(ApplicationStatus status);
}

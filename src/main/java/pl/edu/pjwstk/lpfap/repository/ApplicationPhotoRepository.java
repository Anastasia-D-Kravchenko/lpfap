package pl.edu.pjwstk.lpfap.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import pl.edu.pjwstk.lpfap.model.ApplicationPhoto;

import java.util.List;

public interface ApplicationPhotoRepository extends JpaRepository<ApplicationPhoto, Long> {
    List<ApplicationPhoto> findByApplicationIdOrderBySortOrderAscIdAsc(Long applicationId);
}

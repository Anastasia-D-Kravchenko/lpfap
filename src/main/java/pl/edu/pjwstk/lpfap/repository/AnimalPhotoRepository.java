package pl.edu.pjwstk.lpfap.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import pl.edu.pjwstk.lpfap.model.AnimalPhoto;

import java.util.List;

public interface AnimalPhotoRepository extends JpaRepository<AnimalPhoto, Long> {
    List<AnimalPhoto> findByAnimalIdOrderBySortOrderAscIdAsc(Long animalId);
}

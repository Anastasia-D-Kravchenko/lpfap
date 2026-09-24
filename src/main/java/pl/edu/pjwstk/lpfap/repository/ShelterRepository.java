package pl.edu.pjwstk.lpfap.repository;

import org.springframework.data.repository.ListCrudRepository;
import pl.edu.pjwstk.lpfap.model.Shelter;

public interface ShelterRepository extends ListCrudRepository<Shelter, Long> {
}

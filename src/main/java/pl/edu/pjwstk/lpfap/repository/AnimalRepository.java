package pl.edu.pjwstk.lpfap.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.ListCrudRepository;
import pl.edu.pjwstk.lpfap.model.Animal;
import pl.edu.pjwstk.lpfap.model.AnimalStatus;
import pl.edu.pjwstk.lpfap.model.Species;

public interface AnimalRepository extends ListCrudRepository<Animal, Long> {

    @Query("SELECT a FROM Animal a LEFT JOIN FETCH a.shelter WHERE a.status = :status")
    Page<Animal> findByStatus(AnimalStatus status, Pageable pageable);

    @Query("SELECT a FROM Animal a LEFT JOIN FETCH a.shelter WHERE a.status = :status AND a.species = :species")
    Page<Animal> findByStatusAndSpecies(AnimalStatus status, Species species, Pageable pageable);

    @Query("SELECT a FROM Animal a LEFT JOIN FETCH a.shelter "
            + "WHERE a.status = :status "
            + "AND (:species IS NULL OR a.species = :species) "
            + "AND (:minAge IS NULL OR a.age >= :minAge) "
            + "AND (:maxAge IS NULL OR a.age <= :maxAge)")
    Page<Animal> search(AnimalStatus status, Species species, Integer minAge, Integer maxAge, Pageable pageable);

    @Query("SELECT a FROM Animal a LEFT JOIN FETCH a.shelter WHERE a.id = :id")
    java.util.Optional<Animal> findByIdWithShelter(Long id);

    @Query("SELECT a FROM Animal a LEFT JOIN FETCH a.shelter ORDER BY a.id DESC")
    java.util.List<Animal> findAllWithShelter();
}

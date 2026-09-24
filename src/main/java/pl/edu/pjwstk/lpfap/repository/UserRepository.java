package pl.edu.pjwstk.lpfap.repository;

import org.springframework.data.repository.ListCrudRepository;
import pl.edu.pjwstk.lpfap.model.User;
import java.util.Optional;

public interface UserRepository extends ListCrudRepository<User, Long> {
    Optional<User> findByEmail(String email);
    boolean existsByEmail(String email);
}

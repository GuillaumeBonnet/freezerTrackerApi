package api.repository;

import org.springframework.data.repository.CrudRepository;

import api.model.User;
import api.model.VerificationToken;

public interface VerificationTokenRepository extends CrudRepository<VerificationToken, Long> {

    VerificationToken findByUser(User user);
}

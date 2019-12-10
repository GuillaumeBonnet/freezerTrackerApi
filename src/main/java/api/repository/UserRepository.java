package api.repository;

import java.util.List;

import org.springframework.data.repository.CrudRepository;

import api.model.User;

public interface UserRepository extends CrudRepository<User, Long> {
    List<User> findByEmail(String email);
	List<User> findByUsername(String username);
}

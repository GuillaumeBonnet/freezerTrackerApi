package api.repository;

import org.springframework.data.repository.CrudRepository;

import api.model.User;

public interface UserRepository extends CrudRepository<User, Long> {
}

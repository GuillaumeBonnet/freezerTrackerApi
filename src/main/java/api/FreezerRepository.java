package api;

import org.springframework.data.repository.CrudRepository;

public interface FreezerRepository extends CrudRepository<Freezer, Long> {
	Freezer findByName(String firstName);
}

package api.repository;

import java.util.Set;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import api.model.Freezer;

public interface FreezerRepository extends CrudRepository<Freezer, Long> {

	@Query("select distinct f from Freezer f left join fetch f.content c WHERE f.creationTimestamp != null ORDER BY f.updateTimestamp DESC")
	Set<Freezer> findAllWithAliments();

	@Query("select distinct f from Freezer f left join fetch f.content c WHERE f.id = :Id")
	Freezer findByIdWithContent(@Param("Id")Long Id);
}

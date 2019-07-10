package api;

import java.util.List;
import java.util.Set;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import api.Aliment;

public interface AlimentRepository extends CrudRepository<Aliment, Long> {

	@Query("select distinct al from Aliment al WHERE al.creationTimestamp != null AND al.freezer.id = :freezerId ORDER BY al.updateTimestamp DESC")
	Set<Aliment> findFreezerContent(@Param("freezerId")Long freezerId);
}
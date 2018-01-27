package api;
import java.util.List;
import org.springframework.data.repository.CrudRepository;
import api.Aliment;

public interface AlimentRepository extends CrudRepository<Aliment, Long> {
	Aliment findByName(String firstName);
	List<Aliment> findByCategory(String Category);
}

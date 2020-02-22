package api.repository;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.EntityGraph.EntityGraphType;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import api.model.User;

public interface UserRepository extends CrudRepository<User, Long> {
    User findByEmail(String email);
    User findByUsername(String username);

    @EntityGraph(value = "user.freezers", type = EntityGraphType.LOAD)
    @Query("SELECT u FROM users u WHERE u.email = :email ORDER BY u.updateTimestamp DESC")
    User findByEmail_WithFreezers(@Param("email") String email);

    @EntityGraph(value = "user.freezers.content", type = EntityGraphType.LOAD)
    @Query("SELECT u FROM users u WHERE u.email = :email ORDER BY u.updateTimestamp DESC")
    User findByEmail_WithFreezersAndContent(@Param("email") String email);
    
    //TODO dead code
    // @Query("select distinct u from user u left join fetch f.content c WHERE f.creationTimestamp != null ORDER BY f.updateTimestamp DESC")
    // User findWithUser();
    
    // @Query("select distinct f from Freezer f left join fetch f.content c WHERE f.creationTimestamp != null ORDER BY f.updateTimestamp DESC")
}

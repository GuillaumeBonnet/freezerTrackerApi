package api;
import java.util.List;

import org.springframework.data.repository.CrudRepository;

import api.Employee;

public interface EmployeeRepository extends CrudRepository<Employee, Long> {

  Employee findByFirstName(String firstName);
  List<Employee> findByLastName(String lastName);
}
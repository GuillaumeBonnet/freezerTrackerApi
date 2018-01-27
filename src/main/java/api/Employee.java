package api; 


import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;


@Entity
public class Employee {

  private @Id @GeneratedValue Long id;
  private String firstName, lastName, description;

  private Employee() {}

  public Employee(String firstName, String lastName, String description) {
    this.firstName = firstName;
    this.lastName = lastName;
    this.description = description;
  }

/* (non-Javadoc)
 * @see java.lang.Object#toString()
 */
@Override
public String toString() {
	StringBuilder builder = new StringBuilder();
	builder.append("Employee [id=");
	builder.append(id);
	builder.append(", firstName=");
	builder.append(firstName);
	builder.append(", lastName=");
	builder.append(lastName);
	builder.append(", description=");
	builder.append(description);
	builder.append("]");
	return builder.toString();
}
}
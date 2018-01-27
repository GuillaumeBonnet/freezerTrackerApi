package api;

import java.sql.Date;
import java.util.List;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToMany;

@Entity
public class Freezer {
	private @Id @GeneratedValue Long id;
	String name;
	@ManyToMany
	List<Aliment> content;
	
	private Freezer() { }

	public Freezer(String name, List<Aliment> content) {
		super();
		this.name = name;
		this.content = content;
	}









	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("Freezer [id=");
		builder.append(id);
		builder.append(", name=");
		builder.append(name);
		builder.append(", content=");
		builder.append(content);
		builder.append("]");
		return builder.toString();
	}
	
	
}

package api;

import java.util.List;
import javax.persistence.ManyToMany;
import javax.persistence.Entity;

@Entity
public class Freezer extends EntityRoot {
	protected String name;
	@ManyToMany
	protected List<Aliment> content;
	
	private Freezer() { }

	public Freezer(String name, List<Aliment> content) {
		super();
		this.name = name;
		this.content = content;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public List<Aliment> getContent() {
		return content;
	}

	public void setContent(List<Aliment> content) {
		this.content = content;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("Freezer [id=");
		builder.append(this.id);
		builder.append(", name=");
		builder.append(name);
		builder.append(", content=");
		builder.append(content);
		builder.append("]");
		return builder.toString();
	}
	
	
}

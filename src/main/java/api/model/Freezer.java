package api.model;

import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.OneToMany;

@Entity
public class Freezer extends EntityRoot {

	@Column(name="name")
	private String name;

	@OneToMany(mappedBy = "freezer", cascade= CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
	private Set<Aliment> content;
	
	public Freezer() { }

	public Freezer(String name, Set<Aliment> content) {
		super();
		this.name = name;
		this.content = content;
	}

	public String getName() {
		return this.name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Set<Aliment> getContent() {
		return content;
	}

	public void setContent(Set<Aliment> content) {
		this.content = content;
	}

	@Override
	public String toString() {
		return 
		"Freezer [ "+ super.toString() 
		+ ", content=" + content 
		+ ", name=" + name
		+ "]";
	}


	
	
}

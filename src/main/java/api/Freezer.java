package api;

import java.util.HashSet;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.OneToMany;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.fasterxml.jackson.annotation.JsonView;

import org.springframework.context.annotation.Lazy;

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

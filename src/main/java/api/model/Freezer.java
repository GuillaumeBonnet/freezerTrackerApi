package api.model;

import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonView;

@Entity
public class Freezer extends EntityRoot implements Cloneable{

	/* -------------------------------------------------------------------------- */
	/*                                   Fields                                   */
	/* -------------------------------------------------------------------------- */

	@JsonView(JsonViews.Summary.class)
	@Column(name="name")
	private String name;

	@JsonView(JsonViews.Details.class)
	@OneToMany(mappedBy = "freezer", cascade= CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
	private Set<Aliment> content;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name="user_id", referencedColumnName="id")
	@JsonIgnore
	private User user;

	/* -------------------------------------------------------------------------- */
	/*                                Constructors                                */
	/* -------------------------------------------------------------------------- */
	
	public Freezer() { }

	public Freezer(String name, Set<Aliment> content) {
		super();
		this.name = name;
		this.content = content;
	}

	/* -------------------------------------------------------------------------- */
	/*                                   Methods                                  */
	/* -------------------------------------------------------------------------- */
	

	@Override
	public String toString() {
		return "Freezer [content=" + content + ", name=" + name + ", user=" + user + "]";
	}

	@Override
	public Freezer clone() throws CloneNotSupportedException {
		return (Freezer)super.clone();
	}

	/* -------------------------------------------------------------------------- */
	/*                              Getters & Setters                             */
	/* -------------------------------------------------------------------------- */

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

	public User getUser() {
		return user;
	}

	public void setUser(User user) {
		this.user = user;
	}
}

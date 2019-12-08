package api.model;

import javax.persistence.Column;
import javax.persistence.Entity;

@Entity(name = "users") // "user" is a restriced keywords in postgreql
public class User extends EntityRoot {

	@Column(name="username")
	private String username;

	@Column(name="password")
	private String password;

	@Column(name="email")
	private String email;

	@Column(name="is_enabled")
	private Boolean isEnabled;

	/* -------------------------------------------------------------------------- */
	/*                                Constructors                                */
	/* -------------------------------------------------------------------------- */

	public User(String username, String password, String email, Boolean isEnabled) {
		this.username = username;
		this.password = password;
		this.email = email;
		this.isEnabled = isEnabled;
	}

	public User() {
	}

	/* -------------------------------------------------------------------------- */
	/*                              Getters & Setters                             */
	/* -------------------------------------------------------------------------- */

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public Boolean getIsEnabled() {
		return isEnabled;
	}

	public void setIsEnabled(Boolean isEnabled) {
		this.isEnabled = isEnabled;
	}
}

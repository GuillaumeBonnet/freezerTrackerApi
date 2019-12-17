package api.model;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

import com.fasterxml.jackson.databind.ObjectMapper;

import org.springframework.security.core.GrantedAuthority;


@Entity(name = "user_roles") // "user" is a restriced keywords in postgreql
public class UserRole extends EntityRoot implements GrantedAuthority {
	
	public final static List<String> ROLES_VALUES = Arrays.asList("ROLE_USER", "ROLE_ADMINISTRATOR");

	@ManyToOne
	@JoinColumn(name="user_id", referencedColumnName="id")
	private User user;

	@Column(name="role")
	private String role;

	
	/* -------------------------------------------------------------------------- */
	/*                                Constructors                                */
	/* -------------------------------------------------------------------------- */
	public UserRole(User user, String role) {
		this.user = user;
		this.role = role;
		checkIfRoleExists(role);
	}

	public UserRole(User user) {
		this.user = user;
		this.role = "ROLE_USER";
		checkIfRoleExists(role);
	}

	public UserRole() {
	}

	
	/* -------------------------------------------------------------------------- */
	/*                              Getters & Setters                             */
	/* -------------------------------------------------------------------------- */
	public User getUser() {
		return user;
	}

	public void setUser(User user) {
		this.user = user;
	}

	public String getRole() {
		return role;
	}

	public void setRole(String role) {
		this.role = role;
		checkIfRoleExists(role);
	}

	/* -------------------------------------------------------------------------- */
	/*                                Util methods                                */
	/* -------------------------------------------------------------------------- */
	public void checkIfRoleExists(String role) {
		if ( ! ROLES_VALUES.contains(role) ) {
			throw new RuntimeException("The Role *" + role + "* is not one of the available roles : [" + String.join(", ", ROLES_VALUES) + "].");
		}
	}

	/* -------------------------------------------------------------------------- */
	/*                       GrantedAuthority implementation                      */
	/* -------------------------------------------------------------------------- */

	@Override
	public String getAuthority() {
		return this.role;
	}


}

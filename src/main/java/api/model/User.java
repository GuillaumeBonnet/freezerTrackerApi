package api.model;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.NamedAttributeNode;
import javax.persistence.NamedEntityGraph;
import javax.persistence.NamedSubgraph;
import javax.persistence.OneToMany;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

@NamedEntityGraph(
	name="user.freezers"
	, attributeNodes = {
		@NamedAttributeNode("freezers")
	}
)
@NamedEntityGraph(
	name="user.freezers.content"
	, attributeNodes = {
		@NamedAttributeNode(value = "freezers", subgraph = "content-subgraph"),
	}
	, subgraphs = {
		@NamedSubgraph(
			name = "content-subgraph",
			attributeNodes = {
				@NamedAttributeNode("content"),
			}
		)
	}
)
@Entity(name = "users") // "user" is a restriced keywords in postgreql
public class User extends EntityRoot implements UserDetails {

	/**
	 *
	 */
	private static final long serialVersionUID = 1L;

	@Column(name = "username")
	private String username;

	@Column(name="password")
	private String password;

	@Column(name="email")
	private String email;

	@Column(name="is_enabled")
	private Boolean isEnabled;

	@OneToMany(mappedBy = "user", cascade= CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
	private Set<UserRole> roles;

	@OneToMany(mappedBy = "user", cascade= CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
	private Set<Freezer> freezers;

	/* -------------------------------------------------------------------------- */
	/*                                   Methods                                  */
	/* -------------------------------------------------------------------------- */

	/* -------------------------------------------------------------------------- */
	/*                                Constructors                                */
	/* -------------------------------------------------------------------------- */
	
	
		public User(String username, String password, String email, Boolean isEnabled, Set<UserRole> roles) {
			this.username = username;
			this.password = password;
			this.email = email;
			this.isEnabled = isEnabled;
			for(UserRole role : roles) {
				role.setUser(this);
			}
			this.roles = roles;
			this.freezers = new HashSet<Freezer>();
		}

		public User(String username, String password, String email, Boolean isEnabled, String role) {
			this.username = username;
			this.password = password;
			this.email = email;
			this.isEnabled = isEnabled;
			Set<UserRole> roles = new HashSet<UserRole>();
			roles.add(new UserRole(this, role));
			this.roles = roles;
			this.freezers = new HashSet<Freezer>();
		}
		
		public User(String username, String password, String email, Boolean isEnabled, Collection<String> roles) {
			this.username = username;
			this.password = password;
			this.email = email;
			this.isEnabled = isEnabled;				
			this.roles = roles.stream()
				.map((role) -> new UserRole(this, role))
				.collect(Collectors.toSet())
			;
			this.freezers = new HashSet<Freezer>();
		}

		public User() {
			this.freezers = new HashSet<Freezer>();
		}

	/* -------------------------------------------------------------------------- */
	/*                         UserDetails implementation                         */
	/* -------------------------------------------------------------------------- */
	@Override
	public Collection<? extends GrantedAuthority> getAuthorities() {
		return this.roles;
	}

	@Override
	public boolean isEnabled() {
		return this.isEnabled;
	}
	
	@Override
	public boolean isAccountNonLocked() {
		// TODO! Auto-generated method stub
		return true;
	}
	
	@Override
	public boolean isAccountNonExpired() {
		// TODO! Auto-generated method stub
		return true;
	}

	@Override
	public boolean isCredentialsNonExpired() {
		// TODO! Auto-generated method stub
		return true;
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

	public Set<UserRole> getRoles() {
		return roles;
	}

	public void setRoles(Set<UserRole> roles) {
		this.roles = roles;
	}

	public Set<Freezer> getFreezers() {
		return freezers;
	}

	public void setFreezers(Set<Freezer> freezers) {
		this.freezers = freezers;
	}
}

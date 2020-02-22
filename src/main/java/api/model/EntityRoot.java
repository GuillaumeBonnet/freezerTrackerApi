package api.model;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.MappedSuperclass;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import com.fasterxml.jackson.annotation.JsonView;

import org.hibernate.annotations.Generated;
import org.hibernate.annotations.GenerationTime;

@MappedSuperclass  
@Inheritance(strategy = InheritanceType.TABLE_PER_CLASS) 
public abstract class EntityRoot {
	
	@JsonView(JsonViews.Summary.class)
	@Id
	@GeneratedValue
	@Column(name="id")
	protected Long id;
	
	@JsonView(JsonViews.Summary.class)
	@Generated(value = GenerationTime.INSERT)
    @Temporal(value=TemporalType.TIMESTAMP)
    @Column(name="creation_timestamp")
    protected Date creationTimestamp;

	@JsonView(JsonViews.Summary.class)
	@Generated(value = GenerationTime.ALWAYS)
	@Temporal(value=TemporalType.TIMESTAMP)
	@Column(name="update_timestamp")
    protected Date updateTimestamp;

    public Date getCreationTimestamp() {
        return this.creationTimestamp;
    }

    public Date getUpdateTimestamp() {
        return this.updateTimestamp;
    }	
	
	/** Getters for id
	 * @return the id
	 */
	public Long getId() {
		return id;
	}
	/**
	 * @param id the id to set
	 */
	public void setId(Long id) {
		this.id = id;
	}

	//used in toString() of sub classes
	@Override
	public String toString() {
		return "id=" + id + ", creationTimestamp=" + creationTimestamp + ", updateTimestamp="
				+ updateTimestamp;
	}

}

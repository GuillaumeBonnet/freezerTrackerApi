package api;

import java.sql.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import com.fasterxml.jackson.annotation.JsonIgnore;


@Entity
public class Aliment extends EntityRoot {
	
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name="freezer_id", referencedColumnName="id")
	@JsonIgnore
	private Freezer freezer;

	@Column(name="name")
	private String name;

	@Column(name="category")
	private String category;

	@Column(name="iconic_font_name")
	private String iconicFontName;

	@Column(name="quantity")
	private Double quantity;

	@Column(name="quantity_unit")
	private String quantityUnit;

	@Column(name="stored_date")
	private Date storedDate;

	@Column(name="expiration_date")
	private Date expirationDate;
	
	public Aliment() {}
	
	public Aliment(Long id) {
		super();
		this.id = id;
	}
	
	public Aliment(Freezer freezer, String name, String category, String iconicFontName, Double quantity,
			String quantityUnit, Date storedDate, Date expirationDate) {
		super();
		this.freezer = freezer;
		this.name = name;
		this.category = category;
		this.iconicFontName = iconicFontName;
		this.quantity = quantity;
		this.quantityUnit = quantityUnit;
		this.storedDate = storedDate;
		this.expirationDate = expirationDate;
	}	

	/** Getters for name
	 * @return the name
	 */
	public String getName() {
		return name;
	}

	/**
	 * @return the freezer
	 */
	public Freezer getFreezer() {
		return freezer;
	}

	/**
	 * @param freezer the freezer to set
	 */
	public void setFreezer(Freezer freezer) {
		this.freezer = freezer;
	}

	/** Getters for category
	 * @return the category
	 */
	public String getCategory() {
		return category;
	}

	/** Getters for iconicFontName
	 * @return the iconicFontName
	 */
	public String getIconicFontName() {
		return iconicFontName;
	}

	/** Getters for quantity
	 * @return the quantity
	 */
	public Double getQuantity() {
		return quantity;
	}

	/** Getters for quantityUnit
	 * @return the quantityUnit
	 */
	public String getQuantityUnit() {
		return quantityUnit;
	}

	/** Getters for storedDate
	 * @return the storedDate
	 */
	public Date getStoredDate() {
		return storedDate;
	}

	/** Getters for expirationDate
	 * @return the expirationDate
	 */
	public Date getExpirationDate() {
		return expirationDate;
	}

	/**
	 * @param name the name to set
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * @param category the category to set
	 */
	public void setCategory(String category) {
		this.category = category;
	}

	/**
	 * @param iconicFontName the iconicFontName to set
	 */
	public void setIconicFontName(String iconicFontName) {
		this.iconicFontName = iconicFontName;
	}

	/**
	 * @param quantity the quantity to set
	 */
	public void setQuantity(Double quantity) {
		this.quantity = quantity;
	}

	/**
	 * @param quantityUnit the quantityUnit to set
	 */
	public void setQuantityUnit(String quantityUnit) {
		this.quantityUnit = quantityUnit;
	}

	/**
	 * @param storedDate the storedDate to set
	 */
	public void setStoredDate(Date storedDate) {
		this.storedDate = storedDate;
	}

	/**
	 * @param expirationDate the expirationDate to set
	 */
	public void setExpirationDate(Date expirationDate) {
		this.expirationDate = expirationDate;
	}

	@Override
	public String toString() {
		return 
		"Aliment [ "
		+ super.toString()
		+ ", category=" + category 
		+ ", expirationDate=" + expirationDate 
		+ ", freezer=" + ( this.freezer == null ? "null" : this.freezer.hashCode() )				
		+ ", iconicFontName=" + iconicFontName 
		+ ", name=" + name 
		+ ", quantity=" + quantity 
		+ ", quantityUnit=" + quantityUnit 
		+ ", storedDate=" + storedDate 
		+ "]";
	}





}

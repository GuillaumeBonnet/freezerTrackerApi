package api;


import java.sql.Date;
import javax.persistence.Entity;

@Entity
public class Aliment extends EntityRoot {

	private String name;
	private String category;
	private String iconicFontName;
	private Double quantity;
	private String quantityUnit;
	private Date storedDate;
	private Date expirationDate;

	public Aliment() {}

	public Aliment(Long id) {
		super();
		this.id = id;
	}
	
	public Aliment(String name, String category, String iconicFontName, Double quantity, String quantityUnit,
			Date storedDate, Date expirationDate) {
		super();
		this.name = name;
		this.category = category;
		this.iconicFontName = iconicFontName;
		this.quantity = quantity;
		this.quantityUnit = quantityUnit;
		this.storedDate = storedDate;
		this.expirationDate = expirationDate;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("Aliment [id=");
		builder.append(id);
		builder.append(", name=");
		builder.append(name);
		builder.append(", category=");
		builder.append(category);
		builder.append(", iconicFontName=");
		builder.append(iconicFontName);
		builder.append(", quantity=");
		builder.append(quantity);
		builder.append(", quantityUnit=");
		builder.append(quantityUnit);
		builder.append(", storedDate=");
		builder.append(storedDate);
		builder.append(", expirationDate=");
		builder.append(expirationDate);
		builder.append("]");
		builder.append('\n');
		return builder.toString();
	}

	/** Getters for name
	 * @return the name
	 */
	public String getName() {
		return name;
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




}

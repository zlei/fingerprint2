package com.lighthousesignal.lsslib;

/**
 * Represents an offer for a certain location, building, at a certain time.
 */
public class Offer {
	
	private int offerId;
	private int propertyId;
	private String retailerId;
	private String title;
	private String description;
	
	/**
	 * @return The id of the offer.
	 */
	public int getOfferId() {
		return offerId;
	}
	
	public void setOfferId(int offer_id) {
		this.offerId = offer_id;
	}
	
	/**
	 * @return The offer's property identifier.
	 */
	public int getPropertyId() {
		return propertyId;
	}
	
	public void setPropertyId(int property_id) {
		this.propertyId = property_id;
	}
	
	/**
	 * @return The offer's retailer identifer.
	 */
	public String getRetailerId() {
		return retailerId;
	}
	
	public void setRetailerId(String retailer_id) {
		this.retailerId = retailer_id;
	}
	
	/**
	 * @return The title of the offer.
	 */
	public String getTitle() {
		return title;
	}
	
	public void setTitle(String title) {
		this.title = title;
	}
	
	/**
	 * @return A description of the offer.
	 */
	public String getDescription() {
		return description;
	}
	
	public void setDescription(String description) {
		this.description = description;
	}
}

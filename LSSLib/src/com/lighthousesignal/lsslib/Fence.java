package com.lighthousesignal.lsslib;

/**
 * A geofence
 */
public class Fence {

	private String geofenceId;
	private String retailerId;
	private String propertyId;
	
	public Fence(){
	}
	
	public Fence(String geo_id, String ret_id, String prop_id){
		geofenceId = geo_id;
		retailerId = ret_id;
		propertyId = prop_id;
	}

	/**
	 * @return The id of the geofence.
	 */
	public String getGeofenceId() {
		return geofenceId;
	}

	public void setGeofenceId(String geofenceId) {
		this.geofenceId = geofenceId;
	}

	/**
	 * @return The retailer identifier.
	 */
	public String getRetailerId() {
		return retailerId;
	}

	public void setRetailerId(String retailerId) {
		this.retailerId = retailerId;
	}
	
	/**
	 * @return The property identifier.
	 */
	public String getPropertyId() {
		return propertyId;
	}

	public void setPropertyId(String propertyId) {
		this.propertyId = propertyId;
	}
}

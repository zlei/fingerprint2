package com.lighthousesignal.lsslib;

import android.graphics.Bitmap;

/**
 * Represents a floor of a building.
 */
public class Floor {
	
	private int id;
	private String name;
	private String buildingName;
	private String description;
	private Bitmap mapImage;
	private String imagePath;
	
	public Floor(int id){
		this(id, "", "");
	}
	
	public Floor(int id, String name){
		this(id, name, "");
	}
	
	public Floor(int id, String name, String description){
		this.id = id;
		this.name = name;
		this.description = description;
		this.mapImage = null;
	}
	
	/**
	 * @return The id number of the floor.
	 */
	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	/**
	 * @return The name of the floor.
	 */
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	/**
	 * @return A description of the floor.
	 */
	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}
	
	/**
	 * @return The bitmap image of the floor map.
	 */
	public Bitmap getMapImage(){
		return mapImage;
	}
	
	public void setMapImage(Bitmap mapImage){
		this.mapImage = mapImage;
	}
	
	/**
	 * @return The url of the bitmap image.
	 */
	public String getImagePath(){
		return imagePath;
	}
	
	public void setImagePath(String imagePath){
		this.imagePath = imagePath;
	}
	
	public void setBuildingName(String buildingName){
		this.buildingName = buildingName;
	}

	/**
	 * @return The name of the building this floor belongs to.
	 */
	public String getBuildingName() {
		return buildingName;
	}
}

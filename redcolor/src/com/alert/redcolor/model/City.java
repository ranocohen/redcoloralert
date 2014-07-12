package com.alert.redcolor.model;

import android.location.Location;

public class City {
	
	private String hebName;
	private String engName;
	private long id;
	private double lat,lng;
	private long areaId;
	
	public Location getLocation()
	{
		Location loc = new Location("");
		loc.setLatitude(lat);
		loc.setLongitude(lng);
		return loc;
	}
	public double metersTo(Location other) {
		return getLocation().distanceTo(other);
	}
	public String getHebName() {
		return hebName;
	}
	public void setHebName(String hebName) {
		this.hebName = hebName;
	}
	public String getEngName() {
		return engName;
	}
	public void setEngName(String engName) {
		this.engName = engName;
	}
	public long getId() {
		return id;
	}
	public void setId(long id) {
		this.id = id;
	}
	public double getLat() {
		return lat;
	}
	public void setLat(double lat) {
		this.lat = lat;
	}
	public double getLng() {
		return lng;
	}
	public void setLng(double lng) {
		this.lng = lng;
	}
	public long getAreaId() {
		return areaId;
	}
	public void setAreaId(long areaId) {
		this.areaId = areaId;
	}
	
	
}

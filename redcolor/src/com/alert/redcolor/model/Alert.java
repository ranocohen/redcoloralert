package com.alert.redcolor.model;

import org.joda.time.DateTime;
import org.json.JSONArray;
import org.json.JSONException;

import com.alert.redcolor.db.RedColordb;

import android.database.Cursor;

public class Alert {

	private long id;
	private String location;
	private double lng,lat;
	private DateTime time;
	public Alert(long id, String loc , double lng, double lat , DateTime time) {
		this.id = id;
		this.location = loc;
		this.lat = lat;
		this.lng = lng;
		this.time = time;
	}
	public Alert(Cursor cursor) {
		this.id = cursor.getLong(cursor.getColumnIndex(RedColordb.AlertColumns.ID));
		this.location = cursor.getString(cursor.getColumnIndex(RedColordb.AlertColumns.location));
		this.lat = cursor.getDouble(cursor.getColumnIndex(RedColordb.AlertColumns.lat));
		this.lng = cursor.getDouble(cursor.getColumnIndex(RedColordb.AlertColumns.lng));
		this.time = new DateTime(cursor.getString(cursor.getColumnIndex(RedColordb.AlertColumns.time)));
		
	}
	public Alert(JSONArray obj , DateTime time) {
		this.time = time;
		
		try {
			this.location = obj.getString(0);
			lat = obj.getDouble(1);
			lng = obj.getDouble(2);
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	public long getId() {
		return id;
	}
	public void setId(long id) {
		this.id = id;
	}
	public String getLocation() {
		return location;
	}
	public void setLocation(String location) {
		this.location = location;
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
	public DateTime getTime() {
		return this.time;
	}
	public void setTime(DateTime time) {
		this.time = time;
	}
	
}

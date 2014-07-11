package com.alert.redcolor.model;

import org.joda.time.DateTime;
import org.json.JSONArray;
import org.json.JSONException;

import com.alert.redcolor.db.RedColordb;

import android.database.Cursor;

public class Alert {

	private long id;
	private String location;
	private double x,y;
	private DateTime time;
	public Alert(long id, String loc , double x, double y , DateTime time) {
		this.id = id;
		this.location = loc;
		this.x = x;
		this.y = y;
		this.time = time;
	}
	public Alert(Cursor cursor) {
		this.id = cursor.getLong(cursor.getColumnIndex(RedColordb.Columns.ID));
		this.location = cursor.getString(cursor.getColumnIndex(RedColordb.Columns.location));
		this.x = cursor.getDouble(cursor.getColumnIndex(RedColordb.Columns.xCord));
		this.y = cursor.getDouble(cursor.getColumnIndex(RedColordb.Columns.yCord));
		this.time = new DateTime(cursor.getLong(cursor.getColumnIndex(RedColordb.Columns.yCord)));
		
	}
	public Alert(JSONArray obj , DateTime time) {
		this.time = time;
		
		try {
			this.location = obj.getString(0);
			x = obj.getDouble(1);
			y = obj.getDouble(2);
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
	public double getX() {
		return x;
	}
	public void setX(double x) {
		this.x = x;
	}
	public double getY() {
		return y;
	}
	public void setY(double y) {
		this.y = y;
	}
	public DateTime getTime() {
		return this.time;
	}
	public void setTime(DateTime time) {
		this.time = time;
	}
	
}

package com.alert.redcolor.model;

import org.joda.time.DateTime;

import com.alert.redcolor.db.RedColordb;

import android.database.Cursor;

public class Alert {

	private long id;
	private String location;
	private long x,y;
	DateTime time;
	
	public Alert(Cursor cursor) {
		this.id = cursor.getLong(cursor.getColumnIndex(RedColordb.Columns.ID));
		this.location = cursor.getString(cursor.getColumnIndex(RedColordb.Columns.location));
		this.x = cursor.getLong(cursor.getColumnIndex(RedColordb.Columns.xCord));
		this.y = cursor.getLong(cursor.getColumnIndex(RedColordb.Columns.yCord));
		this.time = new DateTime(cursor.getLong(cursor.getColumnIndex(RedColordb.Columns.yCord)));
		
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
	public long getX() {
		return x;
	}
	public void setX(long x) {
		this.x = x;
	}
	public long getY() {
		return x;
	}
	public void setY(long y) {
		this.y = y;
	}
	public DateTime getTime() {
		return this.time;
	}
	public void setTime(DateTime time) {
		this.time = time;
	}
	
}

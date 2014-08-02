package com.alert.redcolor.model;

import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.content.Context;
import android.database.Cursor;
import android.location.Location;

import com.alert.redcolor.db.RedColordb.CitiesColumns;

public class City {
	
	private String hebName;
	private String engName;
	private long id;
	private double lat,lng;
	private String time;
	private long areaId;
	public City(Cursor cursor) {
		this.id = cursor.getLong(cursor.getColumnIndex(CitiesColumns.ID));
		this.areaId = cursor.getLong(cursor.getColumnIndex(CitiesColumns.oref_id));
		this.time = cursor.getString(cursor.getColumnIndex(CitiesColumns.time));
		this.hebName = cursor.getString(cursor.getColumnIndex(CitiesColumns.name_he));
		this.engName = cursor.getString(cursor.getColumnIndex(CitiesColumns.name_en));
		this.lat = cursor.getDouble(cursor.getColumnIndex(CitiesColumns.lat));
		this.lng = cursor.getDouble(cursor.getColumnIndex(CitiesColumns.lng));
		
	}
	public Location getLocation()
	{
		Location loc = new Location("");
		loc.setLatitude(lat);
		loc.setLongitude(lng);
		return loc;
	}
	public double distanceTo(Location other) {
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
	public String getName(Context context) {
		String locale =Locale.getDefault().getLanguage().toString();
		boolean hebLocale = false;
		String regex = "^\"?(.[^\",]*)";
		Pattern p = Pattern.compile(regex);
		String[] projection = new String[] { CitiesColumns.name_en };
		if (locale.equals("iw") || locale.equals("he")) {
			hebLocale = true;
		}
		if(hebLocale) 
			return getHebName();
		else
		{
			String tmp ="";
			Matcher m = p.matcher(getEngName());
			if (m.find()) 
			    tmp = m.group(1);
			return tmp;
		}
	}
	
	
}

package com.alert.redcolor.model;

import org.joda.time.DateTime;

import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;

import com.alert.redcolor.db.AlertProvider;
import com.alert.redcolor.db.RedColordb;

public class Alert {

	private long id;
	private long areaId;
	private DateTime time;
	private boolean painted;
	public Alert(long areaId , DateTime time) {
		this.areaId = areaId;
		this.time = time;
	}
	public Alert(Cursor cursor) {
		this.id = cursor.getLong(cursor.getColumnIndex(RedColordb.AlertColumns.ID));
		this.areaId = cursor.getLong(cursor.getColumnIndex(RedColordb.AlertColumns.AreaId));
		this.time = new DateTime(cursor.getString(cursor.getColumnIndex(RedColordb.AlertColumns.time)));
		this.painted = ((cursor.getInt(cursor.getColumnIndex(RedColordb.AlertColumns.painted))) > 0);
	}
	
	public long getId() {
		return id;
	}
	public void setId(long id) {
		this.id = id;
	}
	public long getAreaId() {
		return areaId;
	}
	public DateTime getTime() {
		return this.time;
	}
	public void setTime(DateTime time) {
		this.time = time;
	}
	public Area getArea(Context con) {
		Uri uri = ContentUris.withAppendedId(AlertProvider.OREF_CONTENT_URI, areaId);
		Cursor c = con.getContentResolver().query(uri, null, null, null,null);
		return new Area(c);
	}
	public boolean isPainted() {
		return painted;
	}
}

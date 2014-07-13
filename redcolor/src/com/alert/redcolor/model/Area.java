package com.alert.redcolor.model;

import android.database.Cursor;

import com.alert.redcolor.db.RedColordb.OrefColumns;

public class Area {
	private long id;
	private String name;
	private int areaNum;
	
	public Area(Cursor cursor) {
		this.id = cursor.getLong(cursor.getColumnIndex(OrefColumns.ID));
		this.name = cursor.getString(cursor.getColumnIndex(OrefColumns.name));
		this.areaNum = cursor.getInt(cursor.getColumnIndex(OrefColumns.index));

	}

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public int getAreaNum() {
		return areaNum;
	}

	public void setAreaNum(int areaNum) {
		this.areaNum = areaNum;
	}
	
}

package com.alert.redcolor.db;

import java.util.ArrayList;
import java.util.HashMap;

import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;

import com.alert.redcolor.db.RedColordb.CitiesColumns;
import com.alert.redcolor.model.Area;
import com.alert.redcolor.model.City;

public class ProviderQueries {
	Context mCon;

	public ProviderQueries(Context context) {
		this.mCon = context;
	}

	public Area areaById(long id) {
		Area ans = null;
		Cursor c = null;
		try {
			c = mCon.getContentResolver().query(
					ContentUris.withAppendedId(AlertProvider.OREF_CONTENT_URI,
							id), null, null, null, null);
			if (c.moveToFirst())
				ans = new Area(c);
		} finally {
			if (c != null)
				c.close();
		}
		return ans;
	}

	public String[] getCitiesNames(long id) {
		String[] ans;
		Cursor c = null;
		try {
			c = mCon.getContentResolver().query(
					AlertProvider.CITIES_CONTENT_URI,
					new String[] { CitiesColumns.name_he },
					CitiesColumns.oref_id + " = " + id, null, null);
			ans = new String[c.getCount()];
			int i = 0;
			while (c.moveToNext()) {
				ans[i] = c.getString(0);
				i++;
			}
		} finally {
			if (c != null)
				c.close();
		}
		return ans;
	}
	
	/**
	 * 
	 * @return HashMap of all cities in database , key = id , value = city name(heb)
	 */
	public HashMap<Long,String> getCitiesMap() {
		HashMap<Long,String> ans = new HashMap<Long, String>();
		Cursor c = null;
		try {
			c = mCon.getContentResolver().query(
					AlertProvider.CITIES_CONTENT_URI,
					new String[] { CitiesColumns.ID,CitiesColumns.name_he },
					null, null, null);
			while (c.moveToNext()) {
				Long id = c.getLong(0);
				String val = c.getString(1);
				ans.put(id,val);
			}
		} finally {
			if (c != null)
				c.close();
		}
		return ans;
	}
	public ArrayList<City> getCities(long id) {
		ArrayList<City> ans;
		Cursor c = null;
		try {
			c = mCon.getContentResolver().query(
					AlertProvider.CITIES_CONTENT_URI,
					null,
					CitiesColumns.oref_id + " = " + id, null, null);
			ans = new ArrayList<City>();
			
			while (c.moveToNext()) {
				ans.add(new City(c));
			}
		} finally {
			if (c != null)
				c.close();
		}
		return ans;
	}
}
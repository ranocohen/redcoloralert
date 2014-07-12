package com.alert.redcolor.db;



import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;

import com.alert.redcolor.db.RedColordb.CitiesColumns;
import com.alert.redcolor.model.Area;



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
					ContentUris.withAppendedId(
							AlertProvider.OREF_CONTENT_URI, id), null,
					null, null, null);
			if (c.moveToFirst())
				ans = new Area(c);
		} finally {
			if (c != null)
				c.close();
		}
		return ans;
	}
	
	public String[] getCities (long id) {
		String []ans;
		Cursor c = null;
		try {
			c = mCon.getContentResolver().query(
					AlertProvider.CITIES_CONTENT_URI,  new String[] { CitiesColumns.name_he},
					CitiesColumns.oref_id +" = "+id, null, null);
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
	}
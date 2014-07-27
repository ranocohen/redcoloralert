package com.alert.redcolor.db;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;

import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;

import com.alert.redcolor.db.RedColordb.AlertColumns;
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
		String locale =Locale.getDefault().getLanguage().toString();
		boolean hebLocale = false;
		String regex = "^\"?(.[^\",]*)";
		Pattern p = Pattern.compile(regex);
		String[] projection = new String[] { CitiesColumns.name_en };
		if (locale.equals("iw") || locale.equals("he")) {
			hebLocale = true;
			projection = new String[] { CitiesColumns.name_he };
		}

		try {
			c = mCon.getContentResolver().query(
					AlertProvider.CITIES_CONTENT_URI,
					projection,
					CitiesColumns.oref_id + " = " + id, null, null);
			ans = new String[c.getCount()];
			int i = 0;
			while (c.moveToNext()) {
				String tmp = c.getString(0);
				if(!hebLocale)
				{
					Matcher m = p.matcher(tmp);
					if (m.find()) 
					    tmp = m.group(1);
				}
				
				ans[i] = tmp;
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
	 * @return HashMap of all cities in database , key = id , value = city
	 *         name(heb)
	 */
	public HashMap<Long, String> getCitiesMap() {
		HashMap<Long, String> ans = new HashMap<Long, String>();
		Cursor c = null;
		try {
			c = mCon.getContentResolver().query(
					AlertProvider.CITIES_CONTENT_URI,
					new String[] { CitiesColumns.ID, CitiesColumns.name_he },
					null, null, null);
			while (c.moveToNext()) {
				Long id = c.getLong(0);
				String val = c.getString(1);
				ans.put(id, val);
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
					AlertProvider.CITIES_CONTENT_URI, null,
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

	/* returns unix time_stamp of latest alert in db */
	public long getLastestAlertTime() {
		long ans = -1;
		Cursor c = null;
		try {
			c = mCon.getContentResolver().query(
					AlertProvider.ALERTS_CONTENT_URI,
					new String[] { AlertColumns.time }, null, null,
					AlertColumns.time + " DESC LIMIT 1");

			while (c.moveToNext()) {
				String dtStr = c.getString(0);
				DateTime dt = new DateTime(dtStr);
				dt = dt.toDateTime(DateTimeZone.UTC);
				ans = dt.getMillis() / 1000;
			}
		} finally {
			if (c != null)
				c.close();
		}

		return ans;
	}

	/* returns unix time_stamp of latest alert in db */
	public ArrayList<Long> latestAlerts() {
		ArrayList<Long> ans = new ArrayList<Long>();
		Cursor c = null;
		try {
			c = mCon.getContentResolver()
					.query(AlertProvider.ALERTS_CONTENT_URI,
							new String[] { AlertColumns.ID },
							"strftime('%Y-%m-%d %H:%M:%S',time) > datetime('now', '-10 minutes')",
							null, null);

			while (c.moveToNext()) {
				ans.add(c.getLong(0));
			}
		} finally {
			if (c != null)
				c.close();
		}

		return ans;
	}
	
	/* Return the time to get into shelter (in mills) */
	public long getTime(long cityId) {
		long ans = -1;
		Cursor c = null;
		try {
			c = mCon.getContentResolver().query(
					ContentUris.withAppendedId(AlertProvider.CITIES_CONTENT_URI, cityId),
					new String[] { CitiesColumns.time }, null, null,null);

			while (c.moveToNext()) {
				String dtStr = c.getString(0);
				if(dtStr.equals("דקה וחצי"))
					ans = 90*1000;
				else if(dtStr.equals("דקה"))
					ans = 60*1000;
				else if(dtStr.contains("45"))
					ans = 45*1000;
				else if(dtStr.contains("30"))
					ans = 30*1000;
				else if(dtStr.contains("15"))
					ans = 15*1000;
				else if(dtStr.equals("3 דקות"))
					ans = 3*60*1000;
			}
		} finally {
			if (c != null)
				c.close();
		}

		return ans;
	}
}
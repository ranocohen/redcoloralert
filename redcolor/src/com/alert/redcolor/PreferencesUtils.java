package com.alert.redcolor;

import java.util.HashSet;
import java.util.Set;

import org.joda.time.DateTime;
import org.joda.time.Interval;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.preference.PreferenceManager;

public class PreferencesUtils {

	public static final String ALERTS_TYPE_KEY = "pref_alertsType";
	public static final String VIBRATE_KEY = "vibrate";
	public static final String NOTIFY_KEY = "pref_enable_alerts";
	public static final String NIGHTMODE_KEY = "pref_enable_nightmode";
	public static final String NIGHTMODE_START_KEY = "pref_night_start";
	public static final String NIGHTMODE_END_KEY = "pref_night_end";
	public static final String PREF_ALL_ALERTS_VALUE = "all";
	public static final String PREF_LOCAL_ALERTS_VALUE = "local";
	public static final String PREF_CUSTOM_ALERT_VALUE = "custom";
	public static final String ALERTS_TOWNS_SELECT = "pref_towns_select";

	public static String getAlertsType(Context con) {
		SharedPreferences sharedPref = PreferenceManager
				.getDefaultSharedPreferences(con);
		String alertsType = sharedPref.getString(ALERTS_TYPE_KEY, con.getString(R.string.pref_alert_type_default));
		return alertsType;

	}

	public static Uri getRingtone(Context con) {
		String ringtonePreference;
		// Get the xml/preferences.xml preferences
		SharedPreferences sharedPref = PreferenceManager
				.getDefaultSharedPreferences(con);
		ringtonePreference = sharedPref.getString("ringtonePref",
				"DEFAULT_RINGTONE_URI");
		Uri uri = Uri.parse(ringtonePreference);
		return uri;
	}
	public static boolean toVibrate(Context con) {
		SharedPreferences sharedPref = PreferenceManager
				.getDefaultSharedPreferences(con);
		boolean vib = sharedPref.getBoolean(VIBRATE_KEY, true);

		return vib;
	}
	public static boolean isNotifyEnabled(Context con) {
		SharedPreferences sharedPref = PreferenceManager
				.getDefaultSharedPreferences(con);
		boolean notify = sharedPref.getBoolean(NOTIFY_KEY, true);

		return notify;
	}
	public static boolean toNotify(Context con,DateTime alertTime) {
		
		boolean enabled = PreferencesUtils.isNotifyEnabled(con);
		if(!enabled)
			return false;
		
		String alertType = PreferencesUtils.getAlertsType(con);
		if(alertType.equals(PreferencesUtils.PREF_LOCAL_ALERTS_VALUE))
			return true;
		
		boolean inNightMode = PreferencesUtils.inNightMode(con);
		if(!inNightMode)
			return true;
		

		DateTime start = PreferencesUtils.nightModeStart(con);
		DateTime end = PreferencesUtils.nightModeEnd(con);
		
		
		
		start = alertTime.withTime(start.getHourOfDay(),start.getMinuteOfHour(), 0, 0);
		end = alertTime.withTime(end.getHourOfDay(),end.getMinuteOfHour(), 0, 0);
		if(start.isAfter(end))
			start = start.minusDays(1);
		
		
		Interval inter = new Interval(start,end);
		return (!inter.contains(alertTime));
		

	}	
	public static long[] getSelectedTownsIds(Context con) {
		
	
		SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(con);
		Set<String> selections = sharedPrefs.getStringSet(PreferencesUtils.ALERTS_TOWNS_SELECT, new HashSet<String>());
		long[] ids = new long[selections.size()];
		int i = 0;
		for(String s  :selections) {
			ids[i] = Long.parseLong(s);
			i++;
		}
		return ids;
		
	}
	public static boolean inNightMode(Context con) {
		SharedPreferences sharedPref = PreferenceManager
				.getDefaultSharedPreferences(con);
		boolean nightMode= sharedPref.getBoolean(NIGHTMODE_KEY, true);

		return nightMode;
	}
	public static DateTime nightModeStart(Context con) {
		SharedPreferences sharedPref = PreferenceManager
				.getDefaultSharedPreferences(con);
		long start = sharedPref.getLong(NIGHTMODE_START_KEY, DateTime.now().getMillis());
		return new DateTime(start);
		
	}
	public static DateTime nightModeEnd(Context con) {
		SharedPreferences sharedPref = PreferenceManager
				.getDefaultSharedPreferences(con);
		long end = sharedPref.getLong(NIGHTMODE_END_KEY, DateTime.now().getMillis());
		return new DateTime(end);
		
	}
}

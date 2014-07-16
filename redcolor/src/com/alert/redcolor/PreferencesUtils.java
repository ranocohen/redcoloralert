package com.alert.redcolor;

import java.util.HashSet;
import java.util.Set;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.preference.PreferenceManager;

public class PreferencesUtils {

	public static final String ALERTS_TYPE_KEY = "pref_alertsType";
	public static final String VIBRATE_KEY = "vibrate";
	public static final String NOTIFY_KEY = "pref_enable_alerts";
	public static final String PREF_ALL_ALERTS_VALUE = "all";
	public static final String PREF_LOCAL_ALERTS_VALUE = "local";
	public static final String PREF_CUSTOM_ALERT_VALUE = "custom";
	public static final String ALERTS_TOWNS_SELECT = "pref_towns_select";

	public static String getAlertsType(Context con) {
		SharedPreferences sharedPref = PreferenceManager
				.getDefaultSharedPreferences(con);
		String alertsType = sharedPref.getString(ALERTS_TYPE_KEY, "local");
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
	public static boolean toNotify(Context con) {
		SharedPreferences sharedPref = PreferenceManager
				.getDefaultSharedPreferences(con);
		boolean notify = sharedPref.getBoolean(NOTIFY_KEY, true);

		return notify;
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
}

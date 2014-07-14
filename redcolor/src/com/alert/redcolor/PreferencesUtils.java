package com.alert.redcolor;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;


public class PreferencesUtils {

	     
	    
	    public static final String ALERTS_TYPE_KEY = "pref_alertsType";
	    public static final String ALERTS_TOWNS_SELECT = "pref_towns_select";
	    public static final String PREF_ALL_ALERTS = "all";
	    public static final String PREF_LOCAL_ALERTS = "local";
	    public static String getAlertsType(Context con) {
	    	SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(con);
	    	String alertsType = sharedPref.getString(ALERTS_TYPE_KEY, "local");
	    	return alertsType;
	    	
	    }
}

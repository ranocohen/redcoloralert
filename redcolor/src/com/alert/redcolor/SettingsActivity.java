package com.alert.redcolor;

import java.io.IOException;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import com.alert.redcolor.ui.TownListPreference;
import com.google.android.gms.gcm.GoogleCloudMessaging;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.MultiSelectListPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;

public class SettingsActivity extends Activity {
	
	Context con;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		con = getApplicationContext();
		// Display the fragment as the main content.
		getFragmentManager().beginTransaction()
				.replace(android.R.id.content, new SettingsFragment()).commit();
	}

	public static class SettingsFragment extends PreferenceFragment {
		private TownListPreference townListPref;
		@Override
		public void onCreate(Bundle savedInstanceState) {
			super.onCreate(savedInstanceState);

			// Load the preferences from an XML resource
			addPreferencesFromResource(R.xml.preferences);
			PreferenceManager.setDefaultValues(getActivity(),
					R.xml.preferences, false);
			

			ListPreference alertsPref = (ListPreference) findPreference(PreferencesUtils.ALERTS_TYPE_KEY);
			townListPref = (TownListPreference) findPreference(PreferencesUtils.ALERTS_TOWNS_SELECT);

			
			String currValue = alertsPref.getValue();
			if(currValue.equals(PreferencesUtils.PREF_CUSTOM_ALERT_VALUE))
				townListPref.setEnabled(true);
			else 
				townListPref.setEnabled(false);
			
			alertsPref.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {

						@Override
						public boolean onPreferenceChange(
								Preference preference, Object newValue) {
							String val = (String) newValue;
							if (val.equals(PreferencesUtils.PREF_CUSTOM_ALERT_VALUE))
								townListPref.setEnabled(true);
							else
								townListPref.setEnabled(false);
							return true;
						}
					});
			
			Preference button = (Preference)findPreference("button");
			button.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
			                @Override
			                public boolean onPreferenceClick(Preference arg0) { 
			                	
			                	sendAliveTest();
			                	return true;
			                }
			            });

		}
		
		
		private void sendTestIdToBackend(String regId) {
			String serverUrl = Utils.SERVER+"/android_test";
			Map<String, Object> params = new HashMap<String, Object>();
			params.put("regid", regId);

			try {
				ServerUtils.post(serverUrl, params);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		private void sendAliveTest() {
			new AsyncTask<Void, Void, String>() {

				protected String doInBackground(Void... params) {
					String msg = "";
					SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
					// then you use
					String regid = prefs.getString(MainActivity.PROPERTY_REG_ID, null);
					sendTestIdToBackend(regid);
					return msg;
				}

				@Override
				protected void onPostExecute(String msg) {
					/*
					 * Toast.makeText(getApplicationContext(), msg,
					 * Toast.LENGTH_LONG) .show();
					 */
				}
			}.execute(null, null, null);

		}
	}

	/* Helper class to sort Map by value */
	public static class MapUtil {
		public static <K, V extends Comparable<? super V>> Map<K, V> sortByValue(
				Map<K, V> map) {
			List<Map.Entry<K, V>> list = new LinkedList<Map.Entry<K, V>>(
					map.entrySet());
			Collections.sort(list, new Comparator<Map.Entry<K, V>>() {
				public int compare(Map.Entry<K, V> o1, Map.Entry<K, V> o2) {
					return (o1.getValue()).compareTo(o2.getValue());
				}
			});

			Map<K, V> result = new LinkedHashMap<K, V>();
			for (Map.Entry<K, V> entry : list) {
				result.put(entry.getKey(), entry.getValue());
			}
			return result;
		}
	}
	
	
}
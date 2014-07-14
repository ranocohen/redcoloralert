package com.alert.redcolor;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.MultiSelectListPreference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.widget.Toast;

import com.alert.redcolor.db.ProviderQueries;

public class SettingsActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// Display the fragment as the main content.
		getFragmentManager().beginTransaction()
				.replace(android.R.id.content, new SettingsFragment()).commit();
	}

	public static class SettingsFragment extends PreferenceFragment {
		private MultiSelectListPreference mslp;

		@Override
		public void onCreate(Bundle savedInstanceState) {
			super.onCreate(savedInstanceState);

			// Load the preferences from an XML resource
			addPreferencesFromResource(R.xml.preferences);
			PreferenceManager.setDefaultValues(getActivity(),
					R.xml.preferences, false);

			
			populateTownsPreferenceList();
			SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
			Set<String> selections = sharedPrefs.getStringSet(PreferencesUtils.ALERTS_TOWNS_SELECT, null);
			String[] selected = selections.toArray(new String[selections.size()]);
			if(selected.length > 0)
			Toast.makeText(getActivity(), selected[0], Toast.LENGTH_LONG).show();
		}

		public void populateTownsPreferenceList() {
			mslp = (MultiSelectListPreference) findPreference(PreferencesUtils.ALERTS_TOWNS_SELECT);
			ProviderQueries pq = new ProviderQueries(getActivity());
			HashMap<Long, String> cities = pq.getCitiesMap();
			List<String> enteries = new ArrayList<String>();
			;
			HashSet<String> values = new HashSet<String>();

			cities = (HashMap<Long, String>) MapUtil.sortByValue(cities);

			for (Long l : cities.keySet())
				values.add(l.toString());

			for (String s : cities.values())
				enteries.add(s);

			mslp.setEntries(enteries.toArray(new String[enteries.size()]));
			mslp.setEntryValues(values.toArray(new String[values.size()]));
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
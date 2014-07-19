package com.alert.redcolor;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.pm.PackageManager.NameNotFoundException;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.preference.RingtonePreference;
import android.provider.MediaStore;
import android.util.Log;

import com.alert.redcolor.ui.TownListPreference;

public class SettingsActivity extends Activity {

	Context con;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
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
		
			SharedPreferences preferences = PreferenceManager
					.getDefaultSharedPreferences(getActivity());
			// ringinite is true if red ringtone was not set as default before
			boolean firstInit = preferences.getBoolean("ringInit", false);
			if (!firstInit) {
				
				
				SharedPreferences.Editor editor = preferences.edit();
				editor.putBoolean("ringInit", true);
				editor.apply();
				final RingtonePreference ringPref = (RingtonePreference) findPreference("ringtonePref");
				File file = new File(			Environment.getExternalStorageDirectory().getPath()+
						"/media/audio/notifications","RED.mp3");
						
				if (file.exists()) {
					/* In order the RingtoneManager to recgonize our uri we must add it to the system media
					 * content provider */
					 
					ContentValues values = new ContentValues();
					values.put(MediaStore.MediaColumns.DATA, file.getAbsolutePath());
					values.put(MediaStore.MediaColumns.TITLE, "Red notification");
					values.put(MediaStore.MediaColumns.SIZE, file.length());
					values.put(MediaStore.MediaColumns.MIME_TYPE, "audio/mp3");
					values.put(MediaStore.Audio.Media.ARTIST, "RED");
					values.put(MediaStore.Audio.Media.DURATION, 1);
					values.put(MediaStore.Audio.Media.IS_RINGTONE, false);
					values.put(MediaStore.Audio.Media.IS_NOTIFICATION, true);
					values.put(MediaStore.Audio.Media.IS_ALARM, false);
					values.put(MediaStore.Audio.Media.IS_MUSIC, false);
					
					/* If its exist we delete it */
					Uri uri = MediaStore.Audio.Media.getContentUriForPath(file.getAbsolutePath());
					getActivity().getContentResolver().delete(uri, MediaStore.MediaColumns.DATA + "=\"" + file.getAbsolutePath() + "\"", null);
					Uri newUri =getActivity().getContentResolver().insert(uri, values);
					 
					/* Adding to the db and getting the REAL uri */
					//Insert it into the database 
					Uri uri2 = MediaStore.Audio.Media.getContentUriForPath(file.getAbsolutePath());
					Uri newUri2 = getActivity().getContentResolver().insert(uri2, values);
					 
					RingtoneManager.setActualDefaultRingtoneUri(
					  getActivity(), 
					  RingtoneManager.TYPE_NOTIFICATION,
					  android.provider.Settings.System.DEFAULT_RINGTONE_URI
					); 
					
					/* Finally set to default */
					PreferenceManager.getDefaultSharedPreferences(getActivity()).edit().putString
					("ringtonePref", newUri.toString()).commit();
					//ringPref.setDefaultValue(ringtoneUri.toString());
				} else {
					Uri ringtoneUri = Uri
							.parse("content://settings/system/notification_sound");
				}
					//ringPref.setDefaultValue(ringtoneUri.toString());
			}
				
			// Do something else
				//I read my ringtone setting ( I read the value from my ringtone_uri key ) 
				//than if it is not set I set the value with the default value from the phone 
				Uri defaultstr = Uri.parse(PreferenceManager.getDefaultSharedPreferences
						(getActivity()).getString("ringtonePref",
								android.provider.Settings.System.DEFAULT_RINGTONE_URI.toString()));
				//than I do this, I save the default ringtone to my setting 
				 if(defaultstr.equals(android.provider.Settings.System.DEFAULT_RINGTONE_URI)){
				PreferenceManager.getDefaultSharedPreferences(getActivity()).edit().putString
				("ringtonePref", android.provider.Settings.System.DEFAULT_RINGTONE_URI.toString()).commit();
				                } 
			final ListPreference alertsPref = (ListPreference) findPreference(PreferencesUtils.ALERTS_TYPE_KEY);
			townListPref = (TownListPreference) findPreference(PreferencesUtils.ALERTS_TOWNS_SELECT);

			String currValue = alertsPref.getValue();
			if (currValue.equals(PreferencesUtils.PREF_CUSTOM_ALERT_VALUE))
				townListPref.setEnabled(true);
			else
				townListPref.setEnabled(false);

			alertsPref.setSummary(alertsPref.getEntry());

			alertsPref
					.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {

						@Override
						public boolean onPreferenceChange(
								Preference preference, Object newValue) {
							String val = (String) newValue;
							// custom places
							if (val.equals(PreferencesUtils.PREF_CUSTOM_ALERT_VALUE))
								townListPref.setEnabled(true);
							else
								townListPref.setEnabled(false);

							if (val.equals(PreferencesUtils.PREF_LOCAL_ALERTS_VALUE))
								if (showReminderMessage())
									locationReminderDialog();

							CharSequence[] currText = alertsPref.getEntries();

							alertsPref.setSummary(currText[alertsPref
									.findIndexOfValue(val)]);

							return true;
						}
					});

			Preference ver = (Preference) findPreference("version");
			String versionName;
			try {
				versionName = getActivity().getPackageManager().getPackageInfo(
						getActivity().getPackageName(), 0).versionName;
				ver.setSummary(versionName);
			} catch (NameNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			Preference button = (Preference) findPreference("button");
			button.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
				@Override
				public boolean onPreferenceClick(Preference arg0) {

					sendAliveTest();
					return true;
				}
			});

		}

		/*
		 * returns true if locationReminderDialog need to be shown
		 */
		public boolean showReminderMessage() {

			boolean setting = true;
			SharedPreferences preferences = PreferenceManager
					.getDefaultSharedPreferences(getActivity());
			setting = preferences.getBoolean("alerttypelocationdialog", true);

			return setting;
		}

		public void locationReminderDialog() {
			new AlertDialog.Builder(getActivity())
					.setTitle(R.string.location_reminder_title)
					.setMessage(R.string.location_reminder_message)
					.setPositiveButton(android.R.string.ok,
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog,
										int which) {
									// continue with delete
								}
							})
					.setNegativeButton(R.string.never_remind,
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog,
										int which) {
									// don't show this message again
									SharedPreferences preferences = PreferenceManager
											.getDefaultSharedPreferences(getActivity());
									SharedPreferences.Editor editor = preferences
											.edit();
									editor.putBoolean(
											"alerttypelocationdialog", false);
									editor.apply();
								}
							}).setIcon(android.R.drawable.ic_dialog_alert)
					.show();
		}

		private void sendTestIdToBackend(String regId) {
			String serverUrl = Utils.SERVER + "/android_test";
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

					String regid = getRegistrationId(getActivity());
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

		private String getRegistrationId(Context context) {
			final SharedPreferences prefs = context.getSharedPreferences(
					MainActivity.class.getSimpleName(), Context.MODE_PRIVATE);
			String registrationId = prefs.getString(
					MainActivity.PROPERTY_REG_ID, "");

			if (registrationId.isEmpty()) {
				Log.i(Utils.TAG, "Registration not found.");
				return "";
			}
			return registrationId;
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

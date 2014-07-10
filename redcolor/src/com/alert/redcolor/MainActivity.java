/*
 * Copyright 2012 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.alert.redcolor;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import android.app.ActionBar;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.location.Location;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient.ConnectionCallbacks;
import com.google.android.gms.common.GooglePlayServicesClient.OnConnectionFailedListener;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.google.android.gms.location.LocationClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.maps.MapView;

public class MainActivity extends FragmentActivity implements
		ActionBar.TabListener, ConnectionCallbacks, OnConnectionFailedListener,
		LocationListener {
	private final static int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;
	public static final String EXTRA_MESSAGE = "message";
	public static final String PROPERTY_REG_ID = "registration_id";
	private static final String PROPERTY_APP_VERSION = "appVersion";
	    
	private String SENDER_ID = "295544852061";
	//map testing
	public static MapView map;
	 
	 GoogleCloudMessaging gcm;
	 AtomicInteger msgId = new AtomicInteger();
	SharedPreferences prefs;
	Context context;

	    String regid;
	// Location related variables
	LocationRequest locationRequest;
	LocationClient locationClient;
	boolean locationEnabled;
	/**
	 * The {@link android.support.v4.view.PagerAdapter} that will provide
	 * fragments for each of the three primary sections of the app. We use a
	 * {@link android.support.v4.app.FragmentPagerAdapter} derivative, which
	 * will keep every loaded fragment in memory. If this becomes too memory
	 * intensive, it may be best to switch to a
	 * {@link android.support.v4.app.FragmentStatePagerAdapter}.
	 */
	AppSectionsPagerAdapter mAppSectionsPagerAdapter;

	/**
	 * The {@link ViewPager} that will display the three primary sections of the
	 * app, one at a time.
	 */
	ViewPager mViewPager;

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		context = getApplicationContext();
		setContentView(R.layout.activity_main);
		   // Check device for Play Services APK.
	    if (checkPlayServices()) {
	        gcm = GoogleCloudMessaging.getInstance(this);
            regid = getRegistrationId(context);

            if (regid.isEmpty()) {
                registerInBackground();
            }
            else
            	registerInBackground(regid);
        } else {
            Log.i(Utils.TAG, "No valid Google Play Services APK found.");
        }
	  
		LocationManager manager = (LocationManager) getApplication().getSystemService(Context.LOCATION_SERVICE);
		if (!manager.isProviderEnabled(LocationManager.GPS_PROVIDER) && !manager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
		    locationEnabled = false;
		    Toast.makeText(getApplication(), "Enable location services for accurate data", Toast.LENGTH_SHORT).show();
		}
		else locationEnabled = true;

		locationClient = new LocationClient(this, this, this);
		locationRequest = new LocationRequest();
		// Use high accuracy
		locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
		// Set the update interval to 5 seconds
		locationRequest.setInterval(5000);
		// Set the fastest update interval to 1 second
		locationRequest.setFastestInterval(1000);

		// Create the adapter that will return a fragment for each of the three
		// primary sections
		// of the app.
		mAppSectionsPagerAdapter = new AppSectionsPagerAdapter(
				getSupportFragmentManager());

		// Set up the action bar.
		final ActionBar actionBar = getActionBar();

		// Specify that the Home/Up button should not be enabled, since there is
		// no hierarchical
		// parent.
		actionBar.setHomeButtonEnabled(false);

		// Specify that we will be displaying tabs in the action bar.
		actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);

		// Set up the ViewPager, attaching the adapter and setting up a listener
		// for when the
		// user swipes between sections.
		mViewPager = (ViewPager) findViewById(R.id.pager);
		mViewPager.setAdapter(mAppSectionsPagerAdapter);
		mViewPager
				.setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
					@Override
					public void onPageSelected(int position) {
						// When swiping between different app sections, select
						// the corresponding tab.
						// We can also use ActionBar.Tab#select() to do this if
						// we have a reference to the
						// Tab.
						actionBar.setSelectedNavigationItem(position);
					}
				});

/*		// For each of the sections in the app, add a tab to the action bar.
		for (int i = 0; i < mAppSectionsPagerAdapter.getCount(); i++) {
			// Create a tab with text corresponding to the page title defined by
			// the adapter.
			// Also specify this Activity object, which implements the
			// TabListener interface, as the
			// listener for when this tab is selected.
			actionBar.addTab(actionBar.newTab()
					.setText(mAppSectionsPagerAdapter.getPageTitle(i))
					.setTabListener(this));
		}*/
		//TODO add stings :)
		actionBar.addTab(actionBar.newTab()
				.setText("מפה")
				.setTabListener(this));
		
		actionBar.addTab(actionBar.newTab()
				.setText("רשימת התראות")
				.setTabListener(this));
	    
	}
	/**
	 * Check the device to make sure it has the Google Play Services APK. If
	 * it doesn't, display a dialog that allows users to download the APK from
	 * the Google Play Store or enable it in the device's system settings.
	 */
	private boolean checkPlayServices() {
	    int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);
	    if (resultCode != ConnectionResult.SUCCESS) {
	        if (GooglePlayServicesUtil.isUserRecoverableError(resultCode)) {
	            GooglePlayServicesUtil.getErrorDialog(resultCode, this,
	                    PLAY_SERVICES_RESOLUTION_REQUEST).show();
	        } else {
	            Log.i(Utils.TAG, "This device is not supported.");
	            finish();
	        }
	        return false;
	    }
	    return true;
	}
	@Override
	public void onTabUnselected(ActionBar.Tab tab,
			FragmentTransaction fragmentTransaction) {
	}

	@Override
	public void onTabSelected(ActionBar.Tab tab,
			FragmentTransaction fragmentTransaction) {
		// When the given tab is selected, switch to the corresponding page in
		// the ViewPager.
		mViewPager.setCurrentItem(tab.getPosition());
	}

	@Override
	public void onTabReselected(ActionBar.Tab tab,
			FragmentTransaction fragmentTransaction) {
	}

	/**
	 * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
	 * one of the primary sections of the app.
	 */
	public static class AppSectionsPagerAdapter extends FragmentPagerAdapter {

		public AppSectionsPagerAdapter(FragmentManager fm) {
			super(fm);
		}

		@Override
		public Fragment getItem(int i) {
			switch (i) {
			case 0:
				// The first section of the app is the most interesting -- it
				// offers
				// a launchpad into the other demonstrations in this example
				// application.
				// return new LaunchpadSectionFragment();
				return new CustomMapFragment();
			case 1:
				return new AlertsListFragment();
				
				//TODO need to make listview within fragment and not to use listfragmet
				// (or find a solution for this ^ )
				// update: found one 

			default:
				// The other sections of the app are dummy placeholders.
				Fragment fragment = new DummySectionFragment();
				Bundle args = new Bundle();
				args.putInt(DummySectionFragment.ARG_SECTION_NUMBER, i + 1);
				fragment.setArguments(args);
				return fragment;
			}
		}

		@Override
		public int getCount() {
			return 2;
		}

		@Override
		public CharSequence getPageTitle(int position) {
			return "Section " + (position + 1);
		}
	}

	/**
	 * A dummy fragment representing a section of the app, but that simply
	 * displays dummy text.
	 */
	public static class DummySectionFragment extends Fragment {

		public static final String ARG_SECTION_NUMBER = "section_number";

		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container,
				Bundle savedInstanceState) {
			View rootView = inflater.inflate(R.layout.fragment_section_dummy,
					container, false);
			Bundle args = getArguments();
			((TextView) rootView.findViewById(android.R.id.text1))
					.setText(getString(R.string.dummy_section_text,
							args.getInt(ARG_SECTION_NUMBER)));
			return rootView;
		}
	}

	@Override
	public void onLocationChanged(Location location) {
		locationClient.removeLocationUpdates(this);

	}

	@Override
	public void onConnectionFailed(ConnectionResult result) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onConnected(Bundle connectionHint) {
		Location location = locationClient.getLastLocation();
		if (location == null)
			locationClient.requestLocationUpdates(locationRequest, this);
		
		if (location != null) {
		    Toast.makeText(getApplication(), "Location: " + location.getLatitude() + ", " + location.getLongitude(), Toast.LENGTH_SHORT).show();
		}
		else if (location == null && locationEnabled) {
		    locationClient.requestLocationUpdates(locationRequest, this);
		}
		// else
		/*
		 * Toast.makeText( getActivity(), "Location: " + location.getLatitude()
		 * + ", " + location.getLongitude(), Toast.LENGTH_SHORT) .show();
		 */

	}

	@Override
	public void onDisconnected() {
		// TODO Auto-generated method stub

	}
	
	// You need to do the Play Services APK check here too.
	@Override
	protected void onResume() {
	    super.onResume();
	    checkPlayServices();
	}
	/**
	 * Gets the current registration ID for application on GCM service.
	 * <p>
	 * If result is empty, the app needs to register.
	 *
	 * @return registration ID, or empty string if there is no existing
	 *         registration ID.
	 */
	private String getRegistrationId(Context context) {
	    final SharedPreferences prefs = getGCMPreferences(context);
	    String registrationId = prefs.getString(PROPERTY_REG_ID, "");
	    if (registrationId.isEmpty()) {
	        Log.i(Utils.TAG, "Registration not found.");
	        return "";
	    }
	    // Check if app was updated; if so, it must clear the registration ID
	    // since the existing regID is not guaranteed to work with the new
	    // app version.
	    int registeredVersion = prefs.getInt(PROPERTY_APP_VERSION, Integer.MIN_VALUE);
	    int currentVersion = getAppVersion(context);
	    if (registeredVersion != currentVersion) {
	        Log.i(Utils.TAG, "App version changed.");
	        return "";
	    }
	    return registrationId;
	}
	/**
	 * @return Application's {@code SharedPreferences}.
	 */
	private SharedPreferences getGCMPreferences(Context context) {
	    // This sample app persists the registration ID in shared preferences, but
	    // how you store the regID in your app is up to you.
	    return getSharedPreferences(MainActivity.class.getSimpleName(),
	            Context.MODE_PRIVATE);
	}
	/**
	 * @return Application's version code from the {@code PackageManager}.
	 */
	private static int getAppVersion(Context context) {
	    try {
	        PackageInfo packageInfo = context.getPackageManager()
	                .getPackageInfo(context.getPackageName(), 0);
	        return packageInfo.versionCode;
	    } catch (NameNotFoundException e) {
	        // should never happen
	        throw new RuntimeException("Could not get package name: " + e);
	    }
	}
	/**
	 * Registers the application with GCM servers asynchronously.
	 * <p>
	 * Stores the registration ID and app versionCode in the application's
	 * shared preferences.
	 */
	private void registerInBackground() {

		new AsyncTask<Void, Void, String>() {
			 
            protected String doInBackground(Void... params) {
            	 String msg = "";
                 try {
                     if (gcm == null) {
                         gcm = GoogleCloudMessaging.getInstance(context);
                     }
                     regid = gcm.register(SENDER_ID);
                     msg = "Device registered, registration ID=" + regid;

                     // You should send the registration ID to your server over HTTP,
                     // so it can use GCM/HTTP or CCS to send messages to your app.
                     // The request to your server should be authenticated if your app
                     // is using accounts.
                     sendRegistrationIdToBackend(regid);

                     // For this demo: we don't need to send it because the device
                     // will send upstream messages to a server that echo back the
                     // message using the 'from' address in the message.

                     // Persist the regID - no need to register again.
                     storeRegistrationId(context, regid);
                 } catch (IOException ex) {
                     msg = "Error :" + ex.getMessage();
                     // If there is an error, don't just keep trying to register.
                     // Require the user to click a button again, or perform
                     // exponential back-off.
                 }
                 return msg;
             }

             @Override
             protected void onPostExecute(String msg) {
                 Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_LONG).show();
             }
         }.execute(null, null, null);
	    
		
	}
	/**
	 * Registers the application with GCM servers asynchronously.
	 * <p>
	 * Stores the registration ID and app versionCode in the application's
	 * shared preferences.
	 */
	private void registerInBackground(final String regid2) {

		new AsyncTask<Void, Void, String>() {
			 
            protected String doInBackground(Void... params) {
            	 String msg = "";
                 try {
                     if (gcm == null) {
                         gcm = GoogleCloudMessaging.getInstance(context);
                     }
                     regid = gcm.register(SENDER_ID);
                     msg = "Device registered, registration ID=" + regid;

                     // You should send the registration ID to your server over HTTP,
                     // so it can use GCM/HTTP or CCS to send messages to your app.
                     // The request to your server should be authenticated if your app
                     // is using accounts.
                     sendRegistrationIdToBackend(regid2);

                     // For this demo: we don't need to send it because the device
                     // will send upstream messages to a server that echo back the
                     // message using the 'from' address in the message.

                     // Persist the regID - no need to register again.
                     storeRegistrationId(context, regid2);
                 } catch (IOException ex) {
                     msg = "Error :" + ex.getMessage();
                     // If there is an error, don't just keep trying to register.
                     // Require the user to click a button again, or perform
                     // exponential back-off.
                 }
                 return msg;
             }

             @Override
             protected void onPostExecute(String msg) {
                 Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_LONG).show();
             }
         }.execute(null, null, null);
	    
		
	}
	/**
	 * Stores the registration ID and app versionCode in the application's
	 * {@code SharedPreferences}.
	 *
	 * @param context application's context.
	 * @param regId registration ID
	 */
	private void storeRegistrationId(Context context, String regId) {
	    final SharedPreferences prefs = getGCMPreferences(context);
	    int appVersion = getAppVersion(context);
	    Log.i(Utils.TAG, "Saving regId on app version " + appVersion);
	    SharedPreferences.Editor editor = prefs.edit();
	    editor.putString(PROPERTY_REG_ID, regId);
	    editor.putInt(PROPERTY_APP_VERSION, appVersion);
	    editor.commit();
	}
	
	/**
	 * Sends the registration ID to your server over HTTP, so it can use GCM/HTTP
	 * or CCS to send messages to your app. Not needed for this demo since the
	 * device sends upstream messages to a server that echoes back the message
	 * using the 'from' address in the message.
	 */
	private void sendRegistrationIdToBackend(String regId) {
	      String serverUrl = Utils.SERVER_URL;
	        Map<String, String> params = new HashMap<String, String>();
	        params.put("_id", regId);
	        params.put("name", "idan");

	   try {
		ServerUtils.post(serverUrl, params);
	} catch (IOException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	}
	}
}

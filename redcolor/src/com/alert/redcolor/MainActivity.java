package com.alert.redcolor;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.joda.time.DateTime;
import org.joda.time.Period;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import android.app.ActionBar;
import android.app.FragmentTransaction;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.alert.redcolor.AlertsListFragment.OnRedSelectListener;
import com.alert.redcolor.GoogleMapFragment.OnGoogleMapFragmentListener;
import com.alert.redcolor.db.ProviderQueries;
import com.alert.redcolor.db.RedColordb;
import com.alert.redcolor.db.RedColordb.CitiesColumns;
import com.alert.redcolor.db.RedColordb.OrefColumns;
import com.alert.redcolor.db.RedColordb.Tables;
import com.alert.redcolor.volley.JsonRequest;
import com.crashlytics.android.Crashlytics;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient.ConnectionCallbacks;
import com.google.android.gms.common.GooglePlayServicesClient.OnConnectionFailedListener;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.google.android.gms.location.LocationClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.CancelableCallback;
import com.google.android.gms.maps.GoogleMap.SnapshotReadyCallback;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

public class MainActivity extends FragmentActivity implements
		ActionBar.TabListener, ConnectionCallbacks, OnConnectionFailedListener,
		LocationListener, OnGoogleMapFragmentListener, OnRedSelectListener {
	private final static int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;
	public static final String EXTRA_MESSAGE = "message";
	public static final String PROPERTY_REG_ID = "registration_id";
	private static final String PROPERTY_APP_VERSION = "appVersion";
	private static final int STARTING_ALPHA = 90; // hot zone starting color

	private String SENDER_ID = "295544852061";
	public static MapView map;

	GoogleCloudMessaging gcm;
	AtomicInteger msgId = new AtomicInteger();
	SharedPreferences prefs;
	Context context;

	String regid;
	private GoogleMap mUIGoogleMap;
	// HashMap<LatLng, Circle> circles = new HashMap<LatLng, Circle>();
	ArrayList<Circle> circles = new ArrayList<Circle>();

	// Location related variables
	LocationRequest locationRequest;
	LocationClient locationClient;
	boolean locationEnabled = false;
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
		requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);

		// Utils.backup(getApplicationContext());

		Crashlytics.start(this);
		// run location service
		// Intent intent = new Intent(this, BackgroundLocationService.class);
		// startService(intent);

		context = getApplicationContext();
		SharedPreferences.Editor editor = PreferenceManager
				.getDefaultSharedPreferences(getApplicationContext()).edit();
		editor.putInt("page", 0);
		editor.apply();
		setContentView(R.layout.activity_main);
		ConnectionDetector cd = new ConnectionDetector(getApplicationContext());
		if (!cd.isConnectingToInternet())
			showNoConnectionError();
		initFirstData();
		// Check device for Play Services APK.
		if (checkPlayServices()) {
			gcm = GoogleCloudMessaging.getInstance(this);
			regid = getRegistrationId(context);

			if (regid.isEmpty()) {
				registerInBackground();
			}
			Crashlytics.setUserIdentifier(regid);
		} else {
			Log.i(Utils.TAG, "No valid Google Play Services APK found.");
		}

		/*
		 * JsonRequest jr = new JsonRequest();
		 * jr.pushWithParams("http://213.57.173.69:4567/android_test",
		 * getRegistrationId(context));
		 */

		// check if location service is on
		LocationManager manager = (LocationManager) getApplication()
				.getSystemService(Context.LOCATION_SERVICE);
		if (!manager.isProviderEnabled(LocationManager.GPS_PROVIDER)
				&& !manager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
			locationEnabled = false;
			Toast.makeText(getApplication(),
					"Enable location services for accurate data",
					Toast.LENGTH_SHORT).show();
		} else
			locationEnabled = true;

		locationClient = new LocationClient(this, this, this);

		locationClient.connect();

		locationRequest = LocationRequest.create();
		// Use high accuracy

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

		/*
		 * // For each of the sections in the app, add a tab to the action bar.
		 * for (int i = 0; i < mAppSectionsPagerAdapter.getCount(); i++) { //
		 * Create a tab with text corresponding to the page title defined by //
		 * the adapter. // Also specify this Activity object, which implements
		 * the // TabListener interface, as the // listener for when this tab is
		 * selected. actionBar.addTab(actionBar.newTab()
		 * .setText(mAppSectionsPagerAdapter.getPageTitle(i))
		 * .setTabListener(this)); }
		 */
		// TODO add stings :)
		actionBar.addTab(actionBar.newTab().setText(getString(R.string.map))
				.setTabListener(this));

		actionBar.addTab(actionBar.newTab()
				.setText(getString(R.string.latest_alerts))
				.setTabListener(this));

	}

	private void showNoConnectionError() {
		FrameLayout fl = (FrameLayout) findViewById(R.id.main_content);
		LayoutInflater vi = (LayoutInflater) getApplicationContext()
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View v = vi.inflate(R.layout.error, null);

		fl.addView(v, 0, new ViewGroup.LayoutParams(
				ViewGroup.LayoutParams.MATCH_PARENT,
				ViewGroup.LayoutParams.WRAP_CONTENT));
	}

	/**
	 * Check the device to make sure it has the Google Play Services APK. If it
	 * doesn't, display a dialog that allows users to download the APK from the
	 * Google Play Store or enable it in the device's system settings.
	 */
	private boolean checkPlayServices() {
		int resultCode = GooglePlayServicesUtil
				.isGooglePlayServicesAvailable(this);
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
				return new GoogleMapFragment();
			case 1:
				return new AlertsListFragment();

				// TODO need to make listview within fragment and not to use
				// listfragmet
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
		Toast.makeText(getApplicationContext(),
				"Please make sure you have connection enabled",
				Toast.LENGTH_LONG).show();
		finish();
	}

	// check if the client already has the last location
	@Override
	public void onConnected(Bundle connectionHint) {
		Location location = locationClient.getLastLocation();
		/*
		 * if (location == null) locationClient.connect();
		 */
		locationClient.requestLocationUpdates(locationRequest, this);

		if (location != null) {
			// animate to last location
			if (mUIGoogleMap != null) {

				/*
				 * LatLng latLng = new LatLng(location.getLatitude(),
				 * location.getLongitude()); CameraUpdate cameraUpdate =
				 * CameraUpdateFactory.newLatLngZoom( latLng, 11);
				 * mUIGoogleMap.animateCamera(cameraUpdate);
				 */
				// drawAlertHotzone(latLng);

				// else
				/*
				 * Toast.makeText( getActivity(), "Location: " +
				 * location.getLatitude() + ", " + location.getLongitude(),
				 * Toast.LENGTH_SHORT) .show();
				 */
			}
		} else if (location == null && locationEnabled) {
			locationClient.requestLocationUpdates(locationRequest, this);
		}

	}

	// maps circle "hotzone" overlay
	/*
	 * private void updateMarkerWithCircle(LatLng position) {
	 * mCircle.setCenter(position); mMarker.setPosition(position); }
	 */

	/**
	 * gets location for code red alert and mark the area as a 'hot zone' which
	 * would slowly faded after 10 minutes according to home front command
	 * guidance
	 * 
	 * @param position
	 *            - where the code red alert was 'fired'
	 */
	public void drawAlertHotzone(final LatLng position, final String color,
			long timePassed) {

		final Marker mMarker;
		double radiusInMeters = 10000.0;
		// default values
		int fillColor = Color.argb(STARTING_ALPHA, 255, 255, 255);
		int strokeColor = Color.argb(STARTING_ALPHA, 255, 255, 255);

		// timepassed in minutes
		int timePassedMin = (int) (timePassed / 60000);

		int fillScale = STARTING_ALPHA;
		if (timePassed <= 10 && timePassed > 0)
			fillScale = (timePassedMin / 10) * STARTING_ALPHA;

		if (color.equals("red")) {
			fillColor = Color.argb(fillScale, 255, 0, 00);
			strokeColor = Color.argb(200, 255, 0, 0);
		} else if (color.equals("blue")) {
			fillColor = Color.argb(fillScale, 0, 0, 255);
			strokeColor = Color.argb(200, 0, 0, 255);
		}

		Location l = new Location("");
		l.setLatitude(position.latitude);
		l.setLatitude(position.longitude);

		CircleOptions circleOptions = new CircleOptions().center(position)
				.radius(radiusInMeters).fillColor(fillColor)
				.strokeColor(strokeColor).strokeWidth(8);

		final Circle circleZone;

		circleZone = mUIGoogleMap.addCircle(circleOptions);
		// add to hashmap as well
		circles.add(circleZone);

		MarkerOptions markerOptions = new MarkerOptions().position(position);
		mMarker = mUIGoogleMap.addMarker(markerOptions);

		long cooldownTimeDef = 10 * 60 * 1000; // 10 minutes
		if (timePassedMin < 10) {
			cooldownTimeDef = (10 - timePassedMin) * 60 * 1000;
		}

		final long cooldownTime = cooldownTimeDef;
		// final long cooldownTime = 1 * 10 * 1000; // 10 seconds
		final long intervalTime = 60 * 1000; // 1 minute interval
		final int coolTime = 10;

		// TODO change DEBUG Values
		/*
		 * final long cooldownTime = 10*60*1000; //10 minutes final long
		 * intervalTime = 1*60*1000; //1 minute interval final int coolTime =
		 * 10;
		 */

		new CountDownTimer(cooldownTime, intervalTime) {

			// int fillInterval = (int) (150 / (cooldownTime/1000)); //divide by
			// time in seconds int strockInterval = (int) (240 /
			// //(cooldownTime/2/1000));

			int fillInterval = STARTING_ALPHA / coolTime;

			public void onTick(long millisUntilFinished) {

				// filling alpha reduction
				// int currFillColor = circles.get(positionc).getFillColor();
				int p = circles.lastIndexOf(circleZone);

				Log.d("TEST", circles.size() + " /" + p);
				try {
					int currFillColor = circles.get(p).getFillColor();
					int a = Color.alpha(currFillColor);
					a = a - fillInterval;

					Log.d("TICK", circles.get(p).toString() + "/" + a);

					if (color.equals("red")) {
						circles.get(p).setFillColor(Color.argb(a, 200, 0, 0));
					} else if (color.equals("blue")) {
						circles.get(p).setFillColor(Color.argb(a, 0, 0, 200));
					}

				} catch (IndexOutOfBoundsException e) {
				}

				// mCircle.setFillColor(Color.argb(a, 255, 0, 0));

			}

			public void onFinish() {
				try {
					int p = circles.lastIndexOf(circleZone);
					circles.get(p).remove();
					mMarker.remove();
					circles.remove(p);
				} catch (IndexOutOfBoundsException e) {
				}

			} // mTextField.setText("done!"); }
		}.start();
	}

	public void stayInSafePlaceTimer() {

		final long cooldownTime = 1 * 10 * 1000; // 10 seconds
		final long intervalTime = 1 * 1000; // 1 second interval
		final int coolTime = 10;

		// TODO change DEBUG Values
		/*
		 * final long cooldownTime = 10*60*1000; //10 minutes final long
		 * intervalTime = 1*60*1000; //1 minute interval final int coolTime =
		 * 10;
		 */
		new CountDownTimer(cooldownTime, intervalTime) {

			@Override
			public void onTick(long millisUntilFinished) {
				// inform the user how much time left to stay in safe place
				int timeLeft = (int) (millisUntilFinished / 1000);
				String msgFormat = getResources().getString(
						R.string.safe_place_timer);
				String strMsg = String.format(msgFormat, timeLeft);

				Toast.makeText(getApplicationContext(), strMsg,
						Toast.LENGTH_SHORT).show();
			}

			@Override
			public void onFinish() {

				Toast.makeText(getApplicationContext(), "???? ???? ????? ????",
						Toast.LENGTH_SHORT).show();
			}
		};

	}

	private Circle mCircle;

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
		int registeredVersion = prefs.getInt(PROPERTY_APP_VERSION,
				Integer.MIN_VALUE);
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
		// This sample app persists the registration ID in shared preferences,
		// but
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

					// You should send the registration ID to your server over
					// HTTP,
					// so it can use GCM/HTTP or CCS to send messages to your
					// app.
					// The request to your server should be authenticated if
					// your app
					// is using accounts.
					sendRegistrationIdToBackend(regid);

					// For this demo: we don't need to send it because the
					// device
					// will send upstream messages to a server that echo back
					// the
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
				/*
				 * Toast.makeText(getApplicationContext(), msg,
				 * Toast.LENGTH_LONG) .show();
				 */
			}
		}.execute(null, null, null);

	}

	/**
	 * Stores the registration ID and app versionCode in the application's
	 * {@code SharedPreferences}.
	 * 
	 * @param context
	 *            application's context.
	 * @param regId
	 *            registration ID
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
	 * Sends the registration ID to your server over HTTP, so it can use
	 * GCM/HTTP or CCS to send messages to your app. Not needed for this demo
	 * since the device sends upstream messages to a server that echoes back the
	 * message using the 'from' address in the message.
	 */
	private void sendRegistrationIdToBackend(String regId) {
		String serverUrl = Utils.SERVER_URL;
		Map<String, Object> params = new HashMap<String, Object>();
		params.put("regid", regId);

		try {
			String versionName = getPackageManager().getPackageInfo(
					getPackageName(), 0).versionName;
			params.put("version", versionName);
		} catch (NameNotFoundException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

		try {
			ServerUtils.post(serverUrl, params);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Override
	public void onMapReady(GoogleMap map) {
		mUIGoogleMap = map;

		mUIGoogleMap.setMyLocationEnabled(true);
		mUIGoogleMap.getUiSettings().setMyLocationButtonEnabled(true);
		mUIGoogleMap.getUiSettings().setZoomControlsEnabled(true);

		// lastLatLng = mUIGoogleMap.getCameraPosition().target;

		// Animate map to ideal location
		// Tel aviv location
		double lat = 32.055168;
		double lng = 34.799744;

		Location location = new Location("");
		location.setLatitude(lat);
		location.setLongitude(lng);
		LatLng latLng = new LatLng(location.getLatitude(),
				location.getLongitude());
		CameraUpdate cameraUpdate = CameraUpdateFactory
				.newLatLngZoom(latLng, 8);
		mUIGoogleMap.animateCamera(cameraUpdate);

	}

	@Override
	protected void onStop() {
		// TODO Auto-generated method stub
		super.onStop();

	}

	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		if (mUIGoogleMap != null)
			mUIGoogleMap.clear();
		if (circles != null) {
			if (circles.size() != 0)
				circles.clear();
		}
	}

	@Override
	protected void onPause() {

		RedColordb.getInstance(this).cleanDatabase();
		super.onPause();
	}

	/* Inserting the data from csv to database only in the first launch */
	private void initFirstData() {

		SharedPreferences preferences = PreferenceManager
				.getDefaultSharedPreferences(this);
		boolean firstInit = preferences.getBoolean("firstInit2", false);
		if (!firstInit) {
			setProgressBarIndeterminateVisibility(true);
			setProgressBarVisibility(true);
			initData init = new initData(this);
			init.execute();
			return;
		} else
			queryServer();

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle item selection
		switch (item.getItemId()) {
		case R.id.action_settings:
			startActivity(new Intent(this, SettingsActivity.class));
			return true;
		case R.id.share:
			// dick shit fuck face thing
			captureMapScreen();
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}
	
	public void captureMapScreen() {
        SnapshotReadyCallback callback = new SnapshotReadyCallback() {

            @Override
            public void onSnapshotReady(Bitmap snapshot) {
                try {
                	//check status bar height 
                	Rect rectangle= new Rect();
                	Window window= getWindow();
                	window.getDecorView().getWindowVisibleDisplayFrame(rectangle);
                	int statusBarHeight= rectangle.top;
                	int contentViewTop= 
                	    window.findViewById(Window.ID_ANDROID_CONTENT).getTop();
                	int titleBarHeight= contentViewTop - statusBarHeight;

                	   Log.i("*** Jorgesys :: ", "StatusBar Height= " + statusBarHeight + " , TitleBar Height = " + titleBarHeight);
                	   
                	// Calculate ActionBar height
                	   
                	   int actionBarHeight = 0;
/*                	   TypedValue tv = new TypedValue();
                	   if (getTheme().resolveAttribute(android.R.attr.actionBarSize, tv, true))
                	   {
                		   actionBarHeight = TypedValue.complexToDimensionPixelSize(tv.data,getResources().getDisplayMetrics());
                	   }*/
                	   actionBarHeight = getActionBar().getHeight();
                	//end checking
                	
            		View mView = findViewById(android.R.id.content).getRootView();
                    mView.setDrawingCacheEnabled(true);
                    Bitmap backBitmap = mView.getDrawingCache();
                    Bitmap bmOverlay = Bitmap.createBitmap(
                            backBitmap.getWidth(), backBitmap.getHeight(),
                            backBitmap.getConfig());
                    Canvas canvas = new Canvas(bmOverlay);
                    
                    
                    canvas.drawBitmap(backBitmap, 0, 0, null);
                    canvas.drawBitmap(snapshot, 0,statusBarHeight+actionBarHeight, null);
                    
                    
                    String path = Environment.getExternalStorageDirectory()
                            + "/MapScreenShot"
                            + System.currentTimeMillis() + ".png";
                    File file = new File(path);
                    FileOutputStream out = new FileOutputStream(file);

                    bmOverlay.compress(Bitmap.CompressFormat.PNG, 90, out);
                    
            		try {
            			out.flush();
            			out.close();
            		} catch (IOException e1) {
            			// TODO Auto-generated catch block
            			e1.printStackTrace();
            		}
            		
            		Intent share = new Intent(Intent.ACTION_SEND);

            		// If you want to share a png image only, you can do:

            		// setType("image/png"); OR for jpeg: setType("image/jpeg");
            		share.setType("image/*");

            		// Make sure you put example png image named myImage.png in your
            		// directory

            		Uri uri = Uri.fromFile(file);
            		share.putExtra(Intent.EXTRA_STREAM, uri);

            		startActivity(Intent.createChooser(share, "Share Image!"));
                    
                    
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        };

        mUIGoogleMap.snapshot(callback);

    }

	private void shareImage() {
		Intent share = new Intent(Intent.ACTION_SEND);

		View screen = getWindow().getDecorView().findViewById(
				android.R.id.content);
		screen.setDrawingCacheEnabled(true);
		Bitmap bm = screen.getDrawingCache();
		
		String path = Environment.getExternalStorageDirectory().toString();
		OutputStream fOut = null;
		File file = new File(path, "screenshot.jpg");
		try {
			fOut = new FileOutputStream(file);
		} catch (FileNotFoundException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

		bm.compress(Bitmap.CompressFormat.JPEG, 85, fOut);
		try {
			fOut.flush();
			fOut.close();
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		

		// If you want to share a png image only, you can do:

		// setType("image/png"); OR for jpeg: setType("image/jpeg");
		share.setType("image/*");

		// Make sure you put example png image named myImage.png in your
		// directory

		Uri uri = Uri.fromFile(file);
		share.putExtra(Intent.EXTRA_STREAM, uri);

		startActivity(Intent.createChooser(share, "Share Image!"));
	}

	@Override
	public void OnRedSelectedListener(long id, DateTime time) {
		DateTime now = new DateTime();
		Period period = new Period(time, now);
		int i = period.getMinutes();

		ProviderQueries pq = new ProviderQueries(getApplicationContext());
		Location location = pq.getCities(id).get(0).getLocation();
		setFocus(location, i);
		mViewPager.setCurrentItem(0);

		DateTimeFormatter parser2 = DateTimeFormat
				.forPattern("yyyy-MM-dd HH:mm");

		String strFormat = getResources().getString(R.string.time_fired);
		String strTimeMessage = String
				.format(strFormat, time.toString(parser2));

		Toast.makeText(getApplicationContext(), strTimeMessage,
				Toast.LENGTH_SHORT).show();

	}

	Marker testMarker;

	private void setFocus(Location location, int timeToShow) {

		if (timeToShow > 10) {
			timeToShow = 10;
		}

		final long timeToShowMiliSec = 10 * 60 * 1000;
		final LatLng latlng = new LatLng(location.getLatitude(),
				location.getLongitude());

		CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(latlng,
				10);
		mUIGoogleMap.animateCamera(cameraUpdate, new CancelableCallback() {

			@Override
			public void onFinish() {
				MarkerOptions markerOptions = new MarkerOptions()
						.position(latlng);
				final Marker tempMarker = mUIGoogleMap.addMarker(markerOptions);
				tempMarker.setIcon(BitmapDescriptorFactory
						.defaultMarker(BitmapDescriptorFactory.HUE_BLUE));
				int i = 0;

				if (timeToShowMiliSec != 0) {
					drawAlertHotzone(latlng, "blue", timeToShowMiliSec);
				}

				new CountDownTimer(10000, 1000) {
					public void onTick(long millisUntilFinished) {
						// mTextField.setText("Seconds remaining: " +
						// millisUntilFinished / 1000);
					}

					public void onFinish() {
						tempMarker.remove();
					}
				}.start();
			}

			@Override
			public void onCancel() {
				// _googleMap.getUiSettings().setAllGesturesEnabled(true);

			}
		});
	}

	private class initData extends AsyncTask<Void, Void, Void> {
		private Context context;

		public initData(Context context) { // can take other params if needed
			this.context = context;
		}

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			deleteDatabase(RedColordb.DATABASE_NAME);
		}

		@SuppressWarnings("deprecation")
		@Override
		protected Void doInBackground(Void... params) {
			InputStreamReader isr = new InputStreamReader(getResources()
					.openRawResource(R.raw.redalert_en));
			BufferedReader reader = new BufferedReader(isr);
			HashMap<Long, String> orefMap = new HashMap<Long, String>();
			SQLiteDatabase db = RedColordb.getInstance(getApplicationContext())
					.getWritableDatabase();

			DatabaseUtils.InsertHelper inserter = new DatabaseUtils.InsertHelper(
					db, Tables.CITIES);

			DatabaseUtils.InsertHelper inserter2 = new DatabaseUtils.InsertHelper(
					db, Tables.OREF_LOCATIONS);
			int latCol = inserter.getColumnIndex(CitiesColumns.lat);
			int lngCol = inserter.getColumnIndex(CitiesColumns.lng);
			int nameHeCol = inserter.getColumnIndex(CitiesColumns.name_he);
			int nameEnCol = inserter.getColumnIndex(CitiesColumns.name_en);
			int orefIdCol = inserter.getColumnIndex(CitiesColumns.oref_id);
			int timeCol = inserter.getColumnIndex(CitiesColumns.time);

			try {
				db.beginTransaction();
				String line;
				while ((line = reader.readLine()) != null) {
					inserter.prepareForInsert();
					// EOF is ="-1" , just a temp check
					if (line.length() <= 3)
						break;

					/*
					 * Split line with , delimeter (ignoring commas in quotes)
					 */
					String[] data = line
							.split(",(?=([^\"]*\"[^\"]*\")*[^\"]*$)");
					long oref_id = Long.parseLong(data[0]);
					String he_name = data[1];
					String en_name = data[2];

					String oref_loc_str = data[3];

					String time = data[4];
					Double lat = Double.parseDouble(data[5]);
					Double lng = Double.parseDouble(data[6]);

					inserter.bind(latCol, lat);
					inserter.bind(lngCol, lng);
					inserter.bind(nameHeCol, he_name);
					inserter.bind(nameEnCol, en_name);
					inserter.bind(timeCol, time);
					inserter.bind(orefIdCol, oref_id);
					inserter.execute();
					/*
					 * getContentResolver().insert(
					 * AlertProvider.CITIES_CONTENT_URI, cityCv);
					 */
					// Avoid duplicates of pikud areas
					orefMap.put(Long.valueOf(oref_id), oref_loc_str);

				}

				int indexCol = inserter2.getColumnIndex(OrefColumns.index);
				int nameCol = inserter2.getColumnIndex(OrefColumns.name);
				int idCol = inserter2.getColumnIndex(OrefColumns.ID);

				for (Entry<Long, String> e : orefMap.entrySet()) {
					inserter2.prepareForInsert();
					Long key = e.getKey();
					String value = e.getValue();

					Pattern pattern = Pattern
							.compile("^(.*)\\s(\\d*)(\\s(.*))?$");
					Matcher matcher = pattern.matcher(value.trim());

					String area = "";
					String num = "";

					while (matcher.find()) {
						area = matcher.group(1);
						num = matcher.group(2);
					}

					inserter2.bind(idCol, key);
					inserter2.bind(nameCol, area);
					inserter2.bind(indexCol, num);
					inserter2.execute();

				}
				db.setTransactionSuccessful();
			} catch (IOException ex) {
				// handle exception
			} finally {
				try {
					db.endTransaction();
					inserter.close();
					inserter2.close();
					reader.close();
					isr.close();
				} catch (IOException e) {
					// handle exception
				}
			}

			return null;
		}

		@Override
		protected void onPostExecute(Void result) {

			setProgressBarIndeterminateVisibility(false);
			SharedPreferences preferences = PreferenceManager
					.getDefaultSharedPreferences(getApplicationContext());
			SharedPreferences.Editor editor = preferences.edit();
			editor.putBoolean("firstInit2", true);
			editor.apply();

			// TODO IDAN FORGOT TO CHANGE TO PRODUCTOIN?!??!?!?!?!
			// :OOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOO
			queryServer();

		}

	}

	public void queryServer() {
		JsonRequest jr = new JsonRequest();
		ProviderQueries pq = new ProviderQueries(this);
		long latest = pq.getLastestAlertTime();
		if (latest != -1)
			jr.requestJsonObject(Utils.SERVER_ALERTS + "0/25/?timestamp="
					+ latest, getApplicationContext());
		else
			jr.requestJsonObject(Utils.SERVER_ALERTS + "0/25",
					getApplicationContext());

	}
}

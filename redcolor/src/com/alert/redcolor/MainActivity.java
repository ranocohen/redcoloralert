package com.alert.redcolor;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.joda.time.DateTime;
import org.joda.time.Period;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.ActionBar;
import android.app.FragmentTransaction;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.animation.Interpolator;
import android.view.animation.LinearInterpolator;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.alert.redcolor.AlertsListFragment.OnRedSelectListener;
import com.alert.redcolor.GoogleMapFragment.OnGoogleMapFragmentListener;
import com.alert.redcolor.analytics.AnalyticsApp;
import com.alert.redcolor.db.ProviderQueries;
import com.alert.redcolor.db.RedColordb;
import com.alert.redcolor.db.RedColordb.CitiesColumns;
import com.alert.redcolor.db.RedColordb.OrefColumns;
import com.alert.redcolor.db.RedColordb.Tables;
import com.alert.redcolor.model.Alert;
import com.alert.redcolor.services.LocationReceiver;
import com.alert.redcolor.services.LocationService;
import com.alert.redcolor.ui.RateThisApp;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
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
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

public class MainActivity extends FragmentActivity implements
		ActionBar.TabListener, ConnectionCallbacks, OnConnectionFailedListener,
		LocationListener, OnGoogleMapFragmentListener, OnRedSelectListener {
	private final static int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;
	public static final String EXTRA_MESSAGE = "message";
	public static final String PROPERTY_REG_ID = "registration_id";
	private static final String PROPERTY_APP_VERSION = "appVersion";
	private static final int STARTING_ALPHA = 90; // hot zone starting color
	private ArrayList<Alert> alerts;
	// fused fuck shit
	private LocationClient locationclient;
	private LocationRequest locationrequest;
	private Intent mIntentService;
	private PendingIntent mPendingIntent;
	// =====
	private String SENDER_ID = "295544852061";
	public static MapView map;
	private BroadcastReceiver mBroadcast;
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

	private List<Marker> markers = new ArrayList<Marker>(); // TODO MOVE UP

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);

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

		// fused test fuck shitו
		int resp = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);
		if (resp == ConnectionResult.SUCCESS) {
			locationclient = new LocationClient(this, this, this);
			locationclient.connect();
		} else {
			Toast.makeText(this, "Google Play Service Error " + resp,
					Toast.LENGTH_LONG).show();

		}

		// fused fuck shit
		mIntentService = new Intent(this, LocationService.class);
		mPendingIntent = PendingIntent.getService(this, 3, mIntentService, 0);
		// =====

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

		mBroadcast = new BroadcastReceiver() {
			@Override
			public void onReceive(Context context, Intent intent) {
				if(intent != null)
				{
					long id = intent.getLongExtra("ID", -1);
					DateTime dt = DateTime.parse(intent.getStringExtra("TIME"));
					if(id!=-1)
					{
						Alert a = new Alert(id,dt);
						AlertsListFragment fragment = getAlertsListFragment();
						if(fragment != null)
							fragment.addAlert(a);
						
						GoogleMapFragment mapFragment = getMapFragment();
						if(mapFragment != null)
							mapFragment.addAlert(a);
					}
						
				}
				

			}
		};
	

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

//		actionBar.addTab(actionBar.newTab().setText(getString(R.string.stats))
//				.setTabListener(this));
		
		queryServer(0);
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
				// Log.i(Utils.TAG, "This device is not supported.");
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
				return new GoogleMapFragment();
			case 1:
				return AlertsListFragment.newInstance();
/*			case 2:
				return new StatsFragment();*/

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
		if (locationClient.isConnected())
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
		locationrequest = LocationRequest.create();
		locationrequest.setInterval(5 * 60 * 1000);
		// locationclient.requestLocationUpdates(locationrequest,
		// mPendingIntent);
		Intent intent = new Intent(this, LocationReceiver.class);
		PendingIntent locationIntent = PendingIntent.getBroadcast(
				getApplicationContext(), 14872, intent,
				PendingIntent.FLAG_CANCEL_CURRENT);
		locationclient.requestLocationUpdates(locationrequest, locationIntent);

		/*
		 * Location location = locationClient.getLastLocation();
		 * 
		 * if (location != null) { // animate to last location if (mUIGoogleMap
		 * != null) {
		 * 
		 * 
		 * LatLng latLng = new LatLng(location.getLatitude(),
		 * location.getLongitude()); CameraUpdate cameraUpdate =
		 * CameraUpdateFactory.newLatLngZoom( latLng, 11);
		 * mUIGoogleMap.animateCamera(cameraUpdate);
		 * 
		 * // drawAlertHotzone(latLng);
		 * 
		 * // else
		 * 
		 * Toast.makeText( getActivity(), "Location: " + location.getLatitude()
		 * + ", " + location.getLongitude(), Toast.LENGTH_SHORT) .show();
		 * 
		 * } } else if (location == null && locationEnabled &&
		 * locationClient.isConnected()) {
		 * locationClient.requestLocationUpdates(locationRequest, this); }
		 */

	}

	// maps circle "hotzone" overlay
	/*
	 * private void updateMarkerWithCircle(LatLng position) {
	 * mCircle.setCenter(position); mMarker.setPosition(position); }
	 */

	// gaza

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
				.strokeColor(strokeColor).strokeWidth(6);

		final Circle circleZone;

		circleZone = mUIGoogleMap.addCircle(circleOptions);

		/*
		 * // add to hashmap as well circles.add(circleZone);
		 * 
		 * new CountDownTimer(1100, 10) {
		 * 
		 * @Override public void onTick(long millisUntilFinished) {
		 * if(circleZone.getRadius()!=11000.0)
		 * circleZone.setRadius(circleZone.getRadius()+100);
		 * 
		 * }
		 * 
		 * @Override public void onFinish() { // TODO Auto-generated method stub
		 * 
		 * } }.start();
		 */

		MarkerOptions markerOptions = new MarkerOptions().position(position);
		if (color.equals("blue"))
			markerOptions.icon(BitmapDescriptorFactory
					.defaultMarker(BitmapDescriptorFactory.HUE_BLUE));
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

				// Log.d("TEST", circles.size() + " /" + p);
				try {
					int currFillColor = circles.get(p).getFillColor();
					int a = Color.alpha(currFillColor);
					a = a - fillInterval;

					// Log.d("TICK", circles.get(p).toString() + "/" + a);

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
		Toast.makeText(this, "Disconnected. Please re-connect.",
				Toast.LENGTH_SHORT).show();
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
			// Log.i(Utils.TAG, "Registration not found.");
			return "";
		}
		// Check if app was updated; if so, it must clear the registration ID
		// since the existing regID is not guaranteed to work with the new
		// app version.
		int registeredVersion = prefs.getInt(PROPERTY_APP_VERSION,
				Integer.MIN_VALUE);
		int currentVersion = getAppVersion(context);
		if (registeredVersion != currentVersion) {
			// Log.i(Utils.TAG, "App version changed.");
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
		// Log.i(Utils.TAG, "Saving regId on app version " + appVersion);
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

	public void drawMissilePath(long time, double lat, double lon, long sec) {
		final Handler mHandler = new Handler();
		Animator animator = new Animator((int) time, mHandler, lat, lon, sec);

		mHandler.postDelayed(animator, 1000);
		animator.startAnimation(true);
	}

	@Override
	public void onMapReady(GoogleMap map) {
		try {
			mUIGoogleMap = map;
			if (map == null)
				return;
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
			CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(
					latLng, 8);
			mUIGoogleMap.animateCamera(cameraUpdate);
		} catch (Exception e) {
			Toast.makeText(getApplicationContext(), "Error", Toast.LENGTH_SHORT)
					.show();
		}

	}

	@Override
	protected void onStop() {
		LocalBroadcastManager.getInstance(this).unregisterReceiver(mBroadcast);
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
		if (locationClient != null)
			locationClient.disconnect();
	}

	/* Inserting the data from csv to database only in the first launch */
	private void initFirstData() {

		SharedPreferences preferences = PreferenceManager
				.getDefaultSharedPreferences(this);
		boolean firstInit = preferences.getBoolean("firstInit" + Utils.initVer,
				false);
		if (!firstInit) {
			setProgressBarIndeterminateVisibility(true);
			setProgressBarVisibility(true);
			initData init = new initData(this);
			init.execute();
			saveRingtone();

			return;
		}

	}

	public void redRingtone() {

		byte[] buffer = null;
		InputStream fIn = getBaseContext().getResources().openRawResource(
				R.raw.short_alert);
		int size = 0;

		try {
			size = fIn.available();
			buffer = new byte[size];
			fIn.read(buffer);
			fIn.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			// return false;
		}

		// TODO CHANGE NOT TO HARDCODED PATH!
		String path = "/sdcard/media/audio/notifications/";
		String filename = "RED" + ".mp3";

		boolean exists = (new File(path)).exists();
		if (!exists) {
			new File(path).mkdirs();
		}

		FileOutputStream save;
		try {
			save = new FileOutputStream(path + filename);
			save.write(buffer);
			save.flush();
			save.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			// return false;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			// return false;
		}

		sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE,
				Uri.parse("file://" + path + filename)));

	}

	private void saveRingtone() {

		new AsyncTask<Void, Void, Void>() {

			protected Void doInBackground(Void... params) {

				redRingtone();
				return null;
			}

			@Override
			protected void onPostExecute(Void msg) {
			}
		}.execute(null, null, null);

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

		default:
			return super.onOptionsItemSelected(item);
		}
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
		
		final int timeHolder = timeToShow;
		
		final long timeToShowMiliSec = 10 * 60 * 1000;
		final LatLng latlng = new LatLng(location.getLatitude(),
				location.getLongitude());

		CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(latlng,
				10);
		mUIGoogleMap.animateCamera(cameraUpdate, new CancelableCallback() {

			@Override
			public void onFinish() {
				if(timeHolder<10)
					return;
				
				if (timeToShowMiliSec != 0) {
					drawAlertHotzone(latlng, "blue", timeToShowMiliSec);
				}
				MarkerOptions markerOptions = new MarkerOptions()
						.position(latlng);
				Marker tempMarker = mUIGoogleMap.addMarker(markerOptions);
				tempMarker.setIcon(BitmapDescriptorFactory
						.defaultMarker(BitmapDescriptorFactory.HUE_BLUE));
				int i = 0;

				final Marker finalMarker = tempMarker;

				new CountDownTimer(10000, 1000) {
					public void onTick(long millisUntilFinished) {
						// mTextField.setText("Seconds remaining: " +
						// millisUntilFinished / 1000);
					}

					public void onFinish() {
						finalMarker.remove();
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
			editor.putBoolean("firstInit" + Utils.initVer, true);
			editor.apply();

			// TODO IDAN FORGOT TO CHANGE TO PRODUCTOIN?!??!?!?!?!
			// :OOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOO

		}

	}

	public void queryServer(final int page) {
		//Log.i("endless", "loadingMore Activity " + page);
		setProgressBarIndeterminateVisibility(true);
		String url = Utils.SERVER_ALERTS + (page*Utils.MAX_ENTRIES)+"/"+Utils.MAX_ENTRIES;
		JsonObjectRequest jr = new JsonObjectRequest(Request.Method.GET, url,
				null, new Response.Listener<JSONObject>() {
					@Override
					public void onResponse(JSONObject response) {
						alerts = new ArrayList<Alert>();
						try {

							JSONArray data = response.getJSONArray("data");
							// iterates on each alert
							for (int i = 0; i < data.length(); i++) {
								JSONObject alert = data.getJSONObject(i);
								JSONArray areas = alert.getJSONArray("areas");
								String time = alert.getString("time");

								DateTime dt = Utils.parseDateTime(time);

								for (int j = 0; j < areas.length(); j++) {
									JSONObject area = areas.getJSONObject(j);
									int area_id = area.getInt("area_id");
									Alert a = new Alert(area_id, dt);
									alerts.add(a);
								}

							}
							AlertsListFragment listFragment = getAlertsListFragment();
							if (listFragment != null)
								listFragment.addAlerts(alerts);
							
							GoogleMapFragment mapFragment = getMapFragment();
							if (mapFragment != null)
								mapFragment.setAlerts(alerts);
							setProgressBarIndeterminateVisibility(false);
						} catch (JSONException e) {
							e.printStackTrace();
						}
					}
				}, new Response.ErrorListener() {
					@Override
					public void onErrorResponse(VolleyError error) {

					}
				});
		jr.setShouldCache(false);
		((AnalyticsApp) getApplication()).addToRequestQueue(jr);
	}

	public class Animator implements Runnable {

		private int ANIMATE_SPEEED = 5000;
		private static final int ANIMATE_SPEEED_TURN = 1000;
		private static final int BEARING_OFFSET = 20;
		final Handler handler;
		long sec;

		Random rnd = new Random();
		int color = Color.argb(255, 255, rnd.nextInt(256), rnd.nextInt(256));

		private final Interpolator interpolator = new LinearInterpolator();

		public Animator(final int time, Handler mHandler, double lat,
				double lon, long sec) {
		//	Log.i("TIME", "" + time);
			this.ANIMATE_SPEEED = time;
			this.handler = mHandler;
			this.sec = sec;
			endLatLng = new LatLng(lat, lon);
			beginLatLng = new LatLng(31.522561, 34.453593);

		}

		int currentIndex = 0;

		float tilt = 90;
		float zoom = 15.5f;
		boolean upward = true;

		long start = SystemClock.uptimeMillis();

		LatLng endLatLng = null;
		LatLng beginLatLng = null;

		boolean showPolyline = false;

		private Marker trackingMarker;

		public void reset() {
			start = SystemClock.uptimeMillis();
			currentIndex = 0;
			/*
			 * endLatLng = getEndLatLng(); beginLatLng = getBeginLatLng();
			 */
		}

		public void stop() {
			trackingMarker.remove();
			handler.removeCallbacks(this);

		}

		public void initialize(boolean showPolyLine) {
			reset();
			this.showPolyline = showPolyLine;

			// highLightMarker(0);

			if (showPolyLine) {
				polyLine = initializePolyLine();
			}

		}

		private Polyline polyLine;
		private PolylineOptions rectOptions = new PolylineOptions();

		private Polyline initializePolyLine() {
			// polyLinePoints = new ArrayList<LatLng>();
			rectOptions.add(new LatLng(31.522561, 34.453593)).color(color);
			return mUIGoogleMap.addPolyline(rectOptions);
		}

		/**
		 * Add the marker to the polyline.
		 */
		private void updatePolyLine(LatLng latLng) {
			List<LatLng> points = polyLine.getPoints();
			points.add(latLng);
			polyLine.setPoints(points);
		}

		public void stopAnimation() {
			this.stop();
		}

		public void startAnimation(boolean showPolyLine) {
			this.initialize(showPolyLine);
		}

		@Override
		public void run() {

			long elapsed = SystemClock.uptimeMillis() - start;
			double t = interpolator.getInterpolation((float) elapsed / sec);

			// LatLng endLatLng = getEndLatLng();
			// LatLng beginLatLng = getBeginLatLng();

			double lat = t * endLatLng.latitude + (1 - t)
					* beginLatLng.latitude;
			double lng = t * endLatLng.longitude + (1 - t)
					* beginLatLng.longitude;
			LatLng newPosition = new LatLng(lat, lng);

			// trackingMarker.setPosition(newPosition);

			if (showPolyline) {
				updatePolyLine(newPosition);
			}

			// It's not possible to move the marker + center it through a
			// cameraposition update while another camerapostioning was already
			// happening.
			// navigateToPoint(newPosition,tilt,bearing,currentZoom,false);
			// navigateToPoint(newPosition,false);

			if (t < 1) {
				handler.postDelayed(this, 16);
			} else {
				drawAlertHotzone(endLatLng, "red", sec);
				polyLine.remove();
			}
		}

		private LatLng getEndLatLng() {
			return markers.get(currentIndex + 1).getPosition();
		}

		private LatLng getBeginLatLng() {
			return markers.get(currentIndex).getPosition();
		}

	};

	private Location convertLatLngToLocation(LatLng latLng) {
		Location loc = new Location("someLoc");
		loc.setLatitude(latLng.latitude);
		loc.setLongitude(latLng.longitude);
		return loc;
	}

	private float bearingBetweenLatLngs(LatLng begin, LatLng end) {
		Location beginL = convertLatLngToLocation(begin);
		Location endL = convertLatLngToLocation(end);

		return beginL.bearingTo(endL);
	}

	@Override
	protected void onStart() {
		super.onStart();
		// Monitor launch times and interval from installation
		RateThisApp.onStart(this);
		// If the criteria is satisfied, "Rate this app" dialog will be shown
		RateThisApp.showRateDialogIfNeeded(this);
		LocalBroadcastManager.getInstance(this).registerReceiver((mBroadcast),
				new IntentFilter(GcmIntentService.NEW_PUSH));
	}
	private static String makeFragmentName(int viewId, int index)
	{ 
	     return "android:switcher:" + viewId + ":" + index;
	}
	public AlertsListFragment getAlertsListFragment() {
		AlertsListFragment fragment = (AlertsListFragment) getSupportFragmentManager()
				.findFragmentByTag(makeFragmentName(R.id.pager,1));	
		return fragment;
	}
	public GoogleMapFragment getMapFragment() {
		GoogleMapFragment fragment = (GoogleMapFragment) getSupportFragmentManager()
				.findFragmentByTag(makeFragmentName(R.id.pager,0));	
		return fragment;
	}
	
}

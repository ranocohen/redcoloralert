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

import com.alert.redcolor.GoogleMapFragment.OnGoogleMapFragmentListener;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient.ConnectionCallbacks;
import com.google.android.gms.common.GooglePlayServicesClient.OnConnectionFailedListener;
import com.google.android.gms.location.LocationClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import android.app.ActionBar;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends FragmentActivity implements
		ActionBar.TabListener, ConnectionCallbacks, OnConnectionFailedListener,
		LocationListener, OnGoogleMapFragmentListener {

	// map testing
	public static MapView map;
	private GoogleMap mUIGoogleMap;

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
		setContentView(R.layout.activity_main);

		//check if location service is on
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
		actionBar
				.addTab(actionBar.newTab().setText("מפה").setTabListener(this));

		actionBar.addTab(actionBar.newTab().setText("רשימת התראות")
				.setTabListener(this));
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
		// TODO Auto-generated method stub

	}

	//check if the client already has the last location
	@Override
	public void onConnected(Bundle connectionHint) {
		Location location = locationClient.getLastLocation();
		if (location == null)
			locationClient.connect();
			locationClient.requestLocationUpdates(locationRequest, this);

		if (location != null) {
			//animate to last location
			if (mUIGoogleMap != null) {

				LatLng latLng = new LatLng(location.getLatitude(),
						location.getLongitude());
				CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(
						latLng, 10);
				mUIGoogleMap.animateCamera(cameraUpdate);
				
				drawMarkerWithCircle(latLng);
				
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
	
	//maps circle "hotzone" overlay
/*	private void updateMarkerWithCircle(LatLng position) {
	    mCircle.setCenter(position);
	    mMarker.setPosition(position);
	}*/

	private void drawMarkerWithCircle(LatLng position){
	    double radiusInMeters = 100.0;
	    int strokeColor = 0xffff0000; //red outline
	    int shadeColor = 0x44ff0000; //opaque red fill

	    CircleOptions circleOptions = new CircleOptions().center(position).radius(radiusInMeters).fillColor(shadeColor).strokeColor(strokeColor).strokeWidth(8);
	    mCircle = mUIGoogleMap.addCircle(circleOptions);

	    MarkerOptions markerOptions = new MarkerOptions().position(position);
	    mMarker = mUIGoogleMap.addMarker(markerOptions);
	    
	    long cooldownTime=30000;
	    new CountDownTimer(cooldownTime, 1000) {

	        public void onTick(long millisUntilFinished) {
	        	mUIGoogleMap.clear();
	        	
	            //mTextField.setText("seconds remaining: " + millisUntilFinished / 1000);
	        }

	        public void onFinish() {
	            //mTextField.setText("done!");
	        }
	     }.start();
	}
	
	private Circle mCircle;
	private Marker mMarker;

	@Override
	public void onDisconnected() {
		// TODO Auto-generated method stub

	}

	@Override
	public void onMapReady(GoogleMap map) {
		mUIGoogleMap = map;

	}

}

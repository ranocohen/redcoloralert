package com.alert.redcolor;

import org.joda.time.DateTime;
import org.joda.time.Minutes;

import android.app.Activity;
import android.content.ContentUris;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.alert.redcolor.analytics.AnalyticsApp;
import com.alert.redcolor.analytics.AnalyticsApp.TrackerName;
import com.alert.redcolor.db.AlertProvider;
import com.alert.redcolor.db.ProviderQueries;
import com.alert.redcolor.db.RedColordb.AlertColumns;
import com.alert.redcolor.model.Alert;
import com.alert.redcolor.model.City;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMapOptions;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;

public class GoogleMapFragment extends SupportMapFragment implements LoaderCallbacks<Cursor>{
	
    private static final String SUPPORT_MAP_BUNDLE_KEY = "MapOptions";
    public String TAG = "Map";
    public static interface OnGoogleMapFragmentListener {
        void onMapReady(GoogleMap map);
    }

    public static GoogleMapFragment newInstance() {
        return new GoogleMapFragment();
    }

    public static GoogleMapFragment newInstance(GoogleMapOptions options) {
        Bundle arguments = new Bundle();
        arguments.putParcelable(SUPPORT_MAP_BUNDLE_KEY, options);

        GoogleMapFragment fragment = new GoogleMapFragment();
        fragment.setArguments(arguments);
        

        //fragment.getMap().getUiSettings().setMyLocationButtonEnabled(true);
        fragment.getMap().getUiSettings().setCompassEnabled(true);
        fragment.getMap().getUiSettings().setZoomControlsEnabled(false);
                
        return fragment;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mCallback = (OnGoogleMapFragmentListener) getActivity();
        } catch (ClassCastException e) {
            throw new ClassCastException(getActivity().getClass().getName() + " must implement OnGoogleMapFragmentListener");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = super.onCreateView(inflater, container, savedInstanceState);
        if (mCallback != null) {
            mCallback.onMapReady(getMap());
        }
        return view;
    }

    private OnGoogleMapFragmentListener mCallback;

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		  // Get tracker.
        Tracker t = ((AnalyticsApp) getActivity().getApplication()).getTracker(
            TrackerName.APP_TRACKER);

        // Set screen name.
        // Where path is a String representing the screen name.
        t.setScreenName(TAG);

        // Send a screen view.
        t.send(new HitBuilders.AppViewBuilder().build());
    	// Prepare the loader. Either re-connect with an existing one,
		// or start a new one.
		getLoaderManager().initLoader(0, null, this);

		super.onActivityCreated(savedInstanceState);
	}
	@Override
	public Loader<Cursor> onCreateLoader(int id, Bundle args) {

		CursorLoader cursorLoader = new CursorLoader(getActivity(),
				AlertProvider.ALERTS_CONTENT_URI, null, null, null, "datetime("+AlertColumns.time+") DESC");
		return cursorLoader;
	}

	@Override
	public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
		ProviderQueries pq = new ProviderQueries(getActivity());
		DateTime now = DateTime.now();
		while(data.moveToNext())
		{
			Alert a = new Alert(data);
			Minutes diff = Minutes.minutesBetween(a.getTime(), now);
			int minutes = diff.getMinutes();
			if(minutes <= 1 && !a.isPainted())
			{
				
				//get random city in this area
				City city = pq.getCities(a.getAreaId()).get(0);
				
				//Update on db that this area is painted
				setPainted(a.getId());			
				
				//paint on map
				MainActivity activity = (MainActivity)getActivity();
				activity.drawAlertHotzone(new LatLng(city.getLat(), city.getLng()));
				
				
				
			}

		}
	}
	/**
	 * 
	 * @param id the alert id
	 * Sets on database painted column to 1
	 */
	private void setPainted(long id) {
		ContentValues updatedValues = new ContentValues();
		updatedValues.put(AlertColumns.painted, 1);
		getActivity().getContentResolver().update(
				ContentUris.withAppendedId(AlertProvider.ALERTS_CONTENT_URI, id),   // the user dictionary content URI
			    updatedValues ,                      // the columns to update
			    null,         // the column to select on
			    null // the value to compare to
			);

		
	}

	@Override
	public void onLoaderReset(Loader<Cursor> loader) {

	}
}
package com.alert.redcolor;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import org.joda.time.DateTime;
import org.joda.time.Minutes;
import org.joda.time.Seconds;

import android.app.Activity;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.TextView;

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
import com.google.android.gms.maps.GoogleMap.SnapshotReadyCallback;
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
			if(minutes <= 10 && !a.isPainted())
			{

				//get random city in this area
				City city = pq.getCities(a.getAreaId()).get(0);
				
				//Update on db that this area is painted
				setPainted(a.getId());			
				Seconds seconds = Seconds.secondsBetween(a.getTime(), now);
				//paint on map
				MainActivity activity = (MainActivity)getActivity();
				//activity.drawAlertHotzone(new LatLng(city.getLat(), city.getLng()),"red",seconds.toPeriod().getMillis());
				activity.drawMissilePath(pq.getTime(city.getId()),city.getLat(),city.getLng(),seconds.toPeriod().getMillis());
				//activity.stayInSafePlaceTimer();
				//Log.i("Paint","Painting city - "+city.getHebName()+ " "+seconds.toString());

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
	@Override
	public void onCreate(Bundle savedInstanceState) {
	
		super.onCreate(savedInstanceState);
		setHasOptionsMenu(true);
	}
	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		super.onCreateOptionsMenu(menu, inflater);
		inflater.inflate(R.menu.map_menu, menu);
		
	}
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle item selection
				switch (item.getItemId()) {
				
				case R.id.share:
					// dick shit fuck face thing
					shareScreenShotTask screenTask = new shareScreenShotTask();
					screenTask.execute();
					return true;
				default:
					return super.onOptionsItemSelected(item);
				}
		
	}
	 class shareScreenShotTask extends AsyncTask<Void, Void, Void>    {
    	 
	        TextView tv;
	 
	        shareScreenShotTask()    {
	                     
	        }
	 
	        // Executed on the UI thread before the
	        // time taking task begins
	        @Override
	        protected void onPreExecute() {
	            super.onPreExecute();
	        }
	 
	        // Executed on a special thread and all your
	        // time taking tasks should be inside this method
	        @Override
	        protected Void doInBackground(Void... arg0) {
	        	captureMapScreen();
	            return null;
	        }
	        
	        // Executed on the UI thread after the
	        // time taking process is completed
	        @Override
	        protected void onPostExecute(Void result) {
	            super.onPostExecute(result);
	        }
	    }   
		
		public void captureMapScreen() {
	        SnapshotReadyCallback callback = new SnapshotReadyCallback() {

	            @Override
	            public void onSnapshotReady(Bitmap snapshot) {
	                try {
	                	//check status bar height 
	                	Rect rectangle= new Rect();
	                	Window window= getActivity().getWindow();
	                	window.getDecorView().getWindowVisibleDisplayFrame(rectangle);
	                	int statusBarHeight= rectangle.top;
	                	int contentViewTop= 
	                	    window.findViewById(Window.ID_ANDROID_CONTENT).getTop();
	                	int titleBarHeight= contentViewTop - statusBarHeight;

	                	 //  Log.i("*** Jorgesys :: ", "StatusBar Height= " + statusBarHeight + " , TitleBar Height = " + titleBarHeight);
	                	   
	                	// Calculate ActionBar height
	                	   
	                	   int actionBarHeight = 0;
	                	   actionBarHeight = getActivity().getActionBar().getHeight();
	                	//end checking
	                	
	            		View mView = getActivity().findViewById(android.R.id.content).getRootView();
	                    mView.setDrawingCacheEnabled(true);
	                    Bitmap backBitmap = mView.getDrawingCache();
	                    Bitmap bmOverlay = Bitmap.createBitmap(
	                            backBitmap.getWidth(), backBitmap.getHeight(),
	                            backBitmap.getConfig());
	                    Canvas canvas = new Canvas(bmOverlay);
	                    canvas.drawBitmap(backBitmap, 0, 0, null);
	                    canvas.drawBitmap(snapshot, 0,statusBarHeight+actionBarHeight, null);
	                    
	                    Bitmap bm = Bitmap.createBitmap(
	                            backBitmap.getWidth(), backBitmap.getHeight()-statusBarHeight,
	                            backBitmap.getConfig());
	                    
	                    bm = Bitmap.createBitmap(bmOverlay, 0, statusBarHeight, backBitmap.getWidth(), backBitmap.getHeight()-statusBarHeight);
	                    
	                    
	                    String path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES) 
	                    		+ File.separator + "Screenshots";
	                           
	                    File dir = new File( path);
	                    dir.mkdirs();
	                    File file = new File(path+"/"+"red.png");
	                    
	                    file.setReadable(true, false);
	                    FileOutputStream out = new FileOutputStream(file);

	                    //bmOverlay.compress(Bitmap.CompressFormat.PNG, 90, out);
	                    bm.compress(Bitmap.CompressFormat.PNG, 90, out);
	                    
	            		Intent share = new Intent(Intent.ACTION_SEND);

	            		// If you want to share a png image only, you can do:

	            		// setType("image/png"); OR for jpeg: setType("image/jpeg");
	            		share.setType("image/*");

	            		// Make sure you put example png image named myImage.png in your
	            		// directory

	            		Uri uri = Uri.fromFile(file);
	            		share.putExtra(Intent.EXTRA_STREAM, uri);
	            		
	            		try {
	            			out.flush();
	            			out.close();
	            		} catch (IOException e1) {
	            			// TODO Auto-generated catch block
	            			e1.printStackTrace();
	            		}

	            		startActivity(Intent.createChooser(share, "Share Image!"));
	                    
	                    
	                } catch (Exception e) {
	                    e.printStackTrace();
	                }
	            }
	        };

	        if(getMap()!=null)
	        	getMap().snapshot(callback);

	    }

		private void shareImage() {
			Intent share = new Intent(Intent.ACTION_SEND);

			View screen = getActivity().getWindow().getDecorView().findViewById(
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
}

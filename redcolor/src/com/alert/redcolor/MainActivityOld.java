/*package com.alert.redcolor;

import org.joda.time.DateTime;

import com.alert.redcolor.db.AlertProvider;
import com.alert.redcolor.db.RedColordb;
import com.alert.redcolor.model.Alert;

import android.app.ActionBar;
import android.app.ActionBar.Tab;
import android.app.ActionBar.TabListener;
import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.ContentValues;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

public class MainActivityOld extends Activity   {


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		   // Notice that setContentView() is not used, because we use the root
	    // android.R.id.content as the container for each fragment

		
	
		//addAlert(new Alert(1,"ASHKELON",2000,1000,new DateTime()));
		//addAlert(new Alert(2,"BATYAM",3000,1000,new DateTime()));
		
		
		
		
		
		
		
	    // setup action bar for tabs
	    ActionBar actionBar = getActionBar();
	    actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
	    actionBar.setDisplayShowTitleEnabled(false);

	    Tab tab = actionBar.newTab()
	                       .setText("Tab1")
	                       .setTabListener(new TabListener<PlaceholderFragment>(
	                               this, "tab1", PlaceholderFragment.class));
	    actionBar.addTab(tab);

	    tab = actionBar.newTab()
	                   .setText("Tab2")
	                   .setTabListener(new TabListener<AlertsListFragment>(
	                           this, "tab2", AlertsListFragment.class));
	    actionBar.addTab(tab);
	}

	private void addAlert(Alert alert) {
		ContentValues cv = new ContentValues();
		cv.put(RedColordb.Columns.ID , alert.getId());
		cv.put(RedColordb.Columns.xCord , alert.getX());
		cv.put(RedColordb.Columns.yCord , alert.getY());
		cv.put(RedColordb.Columns.location , alert.getLocation());
		cv.put(RedColordb.Columns.time , alert.getTime().toString());
		
		getContentResolver().insert(
				AlertProvider.ALERTS_CONTENT_URI, cv);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	*//**
	 * A placeholder fragment containing a simple view.
	 *//*
	public static class PlaceholderFragment extends Fragment {

		public PlaceholderFragment() {
		}

		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container,
				Bundle savedInstanceState) {
			View rootView = inflater.inflate(R.layout.fragment_main, container,
					false);
			return rootView;
		}
	}
	
	public static class TabListener<T extends Fragment> implements ActionBar.TabListener {
	    private Fragment mFragment;
	    private final Activity mActivity;
	    private final String mTag;
	    private final Class<T> mClass;

	    *//** Constructor used each time a new tab is created.
	      * @param activity  The host Activity, used to instantiate the fragment
	      * @param tag  The identifier tag for the fragment
	      * @param clz  The fragment's Class, used to instantiate the fragment
	      *//*
	    public TabListener(Activity activity, String tag, Class<T> clz) {
	        mActivity = activity;
	        mTag = tag;
	        mClass = clz;
	    }

	     The following are each of the ActionBar.TabListener callbacks 

	    public void onTabSelected(Tab tab, FragmentTransaction ft) {
	        // Check if the fragment is already initialized
	        if (mFragment == null) {
	            // If not, instantiate and add it to the activity
	            mFragment = Fragment.instantiate(mActivity, mClass.getName());
	            ft.add(android.R.id.content, mFragment, mTag);
	        } else {
	            // If it exists, simply attach it in order to show it
	            ft.attach(mFragment);
	        }
	    }

	    public void onTabUnselected(Tab tab, FragmentTransaction ft) {
	        if (mFragment != null) {
	            // Detach the fragment, because another one is being attached
	            ft.detach(mFragment);
	        }
	    }

	    public void onTabReselected(Tab tab, FragmentTransaction ft) {
	        // User selected the already selected tab. Usually do nothing.
	    }
	}
}
*/
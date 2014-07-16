package com.alert.redcolor;

import org.joda.time.DateTime;

import android.app.Activity;
import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.CursorAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.alert.redcolor.analytics.AnalyticsApp;
import com.alert.redcolor.analytics.AnalyticsApp.TrackerName;
import com.alert.redcolor.db.AlertProvider;
import com.alert.redcolor.db.ProviderQueries;
import com.alert.redcolor.db.RedColordb.AlertColumns;
import com.alert.redcolor.model.Alert;
import com.alert.redcolor.model.Area;
import com.alert.redcolor.volley.JsonRequest;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;

public class AlertsListFragment extends ListFragment implements
		LoaderCallbacks<Cursor> , OnScrollListener  {

	public final static String TAG = "AlertsList";
	OnRedSelectListener mCallback;
	private AlertsAdapter mAdapter;
	private int visibleThreshold = 5;
	private int currentPage = 0;
	private int previousTotal = 0;
	private boolean loading = true;

	public static AlertsListFragment newInstance() {
		AlertsListFragment fragment = new AlertsListFragment();
		Bundle args = new Bundle();
		args.putString("tag", TAG);
		fragment.setArguments(args);
		return fragment;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {

		setEmptyText(getString(R.string.no_alerts));

		// Start out with a progress indicator.
		setListShown(false);

		getListView().setDivider(
				getResources().getDrawable(R.drawable.fade_divider));
		getListView().setDividerHeight(1);
		getListView().setVerticalScrollBarEnabled(false);
		getListView().setOnScrollListener(this);

		mAdapter = new AlertsAdapter(getActivity(), null, 0);
		setListAdapter(mAdapter);

		// Prepare the loader. Either re-connect with an existing one,
		// or start a new one.
		getLoaderManager().initLoader(0, null, this);

		// analytics
		// Get tracker.
		Tracker t = ((AnalyticsApp) getActivity().getApplication())
				.getTracker(TrackerName.APP_TRACKER);

		// Set screen name.
		// Where path is a String representing the screen name.
		t.setScreenName(TAG);

		// Send a screen view.
		t.send(new HitBuilders.AppViewBuilder().build());

		super.onCreate(savedInstanceState);
		super.onActivityCreated(savedInstanceState);
	}

	@Override
	public Loader<Cursor> onCreateLoader(int id, Bundle args) {

		CursorLoader cursorLoader = new CursorLoader(getActivity(),
				AlertProvider.ALERTS_CONTENT_URI, null, null, null, "datetime("
						+ AlertColumns.time + ") DESC");
		return cursorLoader;
	}

	@Override
	public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
		mAdapter.swapCursor(data);
		currentPage = Math.round(data.getCount()/25);
		// The list should now be shown.
		if (isResumed()) {
			setListShown(true);
		} else {
			setListShownNoAnimation(true);
		}
	}

	@Override
	public void onLoaderReset(Loader<Cursor> loader) {
		mAdapter.swapCursor(null);

	}

	public class AlertsAdapter extends CursorAdapter {
		LayoutInflater layoutInflater;

		public AlertsAdapter(Context context, Cursor c, int flags) {
			super(context, c, flags);
			layoutInflater = LayoutInflater.from(context);

		}

		@Override
		public void bindView(View view, final Context context, Cursor cursor) {
			if (cursor == null)
				return;

			ViewHolder holder = (ViewHolder) view.getTag();
			Alert alert = new Alert(cursor);

			holder.time.setText(alert.getTime().toString("HH:mm:ss"));
			holder.date.setText(alert.getTime().toString("dd-MM-yy"));
			ProviderQueries pq = new ProviderQueries(getActivity());
			Area a = pq.areaById(alert.getAreaId());
			holder.name.setText(a.getName() + " " + a.getAreaNum());

			String[] cities = pq.getCitiesNames(a.getId());
			StringBuilder builder = new StringBuilder();
			for (int i = 0; i < cities.length; i++) {
				builder.append(cities[i]);
				if (i != cities.length - 1)
					builder.append(", ");
			}
			holder.cities.setText(builder.toString());

		}

		@Override
		public View newView(Context context, Cursor cursor, ViewGroup parent) {
			View view = layoutInflater.inflate(R.layout.list_item_alert, null);

			ViewHolder holder = new ViewHolder();
			holder.name = (TextView) view.findViewById(R.id.location);
			holder.time = (TextView) view.findViewById(R.id.time);
			holder.date = (TextView) view.findViewById(R.id.date);
			holder.cities = (TextView) view.findViewById(R.id.cities);
			view.setTag(holder);
			return view;
		}
	}
	
    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        
        super.onListItemClick(l, v, position, id);
        Cursor c = ((CursorAdapter)l.getAdapter()).getCursor();
        c.moveToPosition(position);
        Alert a = new Alert(c);
        mCallback.OnRedSelectedListener(a.getAreaId(),a.getTime());
    }

	// static class for holding references to views optimizing listview recycles
	private static class ViewHolder {
		TextView name;
		TextView time;
		TextView date;
		TextView cities;

	}

    // MainActivity Activity must implement this interface
    public interface OnRedSelectListener {
    	/**
    	 * 
    	 * @param id the database id of the RED(Alert)
    	 * @param dateTime 
    	 */
        public void OnRedSelectedListener(long id, DateTime dateTime);
    }
    @Override
    public void onAttach(Activity activity) {
    	super.onAttach(activity);
        try {
            mCallback = (OnRedSelectListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement OnHeadlineSelectedListener");
        }
    	
    }

	@Override
	public void onScrollStateChanged(AbsListView view, int scrollState) {
		
	}

	@Override
	public void onScroll(AbsListView view, int firstVisibleItem,
			int visibleItemCount, int totalItemCount) {
	        if (loading) {
	            if (totalItemCount > previousTotal) {
	                loading = false;
	                previousTotal = totalItemCount;
	                currentPage++;
	            }
	        }
	        if (!loading && (totalItemCount - visibleItemCount) <= (firstVisibleItem + visibleThreshold)) {
	            // I load the next page of gigs using a background task,
	            // but you can call any function here.
	           
	            JsonRequest jr = new JsonRequest();
				//jr.requestJsonObject("http://213.57.173.69:4567/alerts/"+offset+"/25",getActivity());
	            Toast.makeText(getActivity(), "Loading "+totalItemCount +" - "+(totalItemCount+25), Toast.LENGTH_SHORT).show();
	            jr.requestJsonObject("http://redalert-il.herokuapp.com/alerts/"+totalItemCount+"/25",getActivity());
	            loading = true;
	        }
	}
}

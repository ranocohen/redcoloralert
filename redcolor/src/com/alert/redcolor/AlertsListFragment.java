package com.alert.redcolor;

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
import android.widget.CursorAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.alert.redcolor.analytics.AnalyticsApp;
import com.alert.redcolor.analytics.AnalyticsApp.TrackerName;
import com.alert.redcolor.db.AlertProvider;
import com.alert.redcolor.db.ProviderQueries;
import com.alert.redcolor.db.RedColordb.AlertColumns;
import com.alert.redcolor.model.Alert;
import com.alert.redcolor.model.Area;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;

public class AlertsListFragment extends ListFragment implements
		LoaderCallbacks<Cursor> {

	public final static String TAG = "AlertsList";
	OnRedSelectListener mCallback;
	private AlertsAdapter mAdapter;

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
        mCallback.OnRedSelectedListener(a.getAreaId());
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
    	 */
        public void OnRedSelectedListener(long id);
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
}

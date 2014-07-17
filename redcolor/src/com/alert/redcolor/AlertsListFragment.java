package com.alert.redcolor;

import java.util.ArrayList;

import org.joda.time.DateTime;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.ListFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.BaseAdapter;
import android.widget.CursorAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.alert.redcolor.analytics.AnalyticsApp;
import com.alert.redcolor.analytics.AnalyticsApp.TrackerName;
import com.alert.redcolor.db.ProviderQueries;
import com.alert.redcolor.model.Alert;
import com.alert.redcolor.model.Area;
import com.alert.redcolor.volley.JsonRequest;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;

public class AlertsListFragment extends ListFragment implements
		OnScrollListener {

	public final static String TAG = "AlertsList";
	OnRedSelectListener mCallback;
	private AlertsAdapter mAdapter;
	private int visibleThreshold = 5;
	// private int currentPage = 0;
	private int previousTotal = 0;
	private boolean loading = true;
	private ArrayList<Alert> alerts;

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
		alerts = new ArrayList<Alert>();
		setEmptyText(getString(R.string.no_alerts));

		// Start out with a progress indicator.
		setListShown(false);

		getListView().setDivider(
				getResources().getDrawable(R.drawable.fade_divider));
		getListView().setDividerHeight(1);
		getListView().setVerticalScrollBarEnabled(false);
		getListView().setOnScrollListener(this);

		mAdapter = new AlertsAdapter(getActivity());
		setListAdapter(mAdapter);
		JsonRequest jr = new JsonRequest();
		jr.requestJsonObject2(Utils.SERVER_ALERTS + 0 + "/25",
				getActivity(),mAdapter);
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

	public class AlertsAdapter extends BaseAdapter {
		LayoutInflater layoutInflater;

		public AlertsAdapter(Context con) {
			layoutInflater = LayoutInflater.from(con);
		}

		@Override
		public int getCount() {
			return alerts.size();
		}

		@Override
		public Object getItem(int position) {
			return alerts.get(position);
		}

		@Override
		public long getItemId(int position) {
			return alerts.get(position).getId();
		}

		@Override
		public View getView(int position, View view, ViewGroup parent) {
			ViewHolder holder;
			if (view == null) {
				view = layoutInflater.inflate(R.layout.list_item_alert, null);
				holder = new ViewHolder();
				holder.name = (TextView) view.findViewById(R.id.location);
				holder.time = (TextView) view.findViewById(R.id.time);
				holder.date = (TextView) view.findViewById(R.id.date);
				holder.cities = (TextView) view.findViewById(R.id.cities);
				view.setTag(holder);
			}
			else
			{
				holder = (ViewHolder) view.getTag();
			}
			
				
				Alert alert = alerts.get(position);

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

			return view;
		}

		public void addAlerts(ArrayList<Alert> newData) {
			alerts.addAll(newData);
			notifyDataSetChanged();
			
		}

	}

	@Override
	public void onListItemClick(ListView l, View v, int position, long id) {

		super.onListItemClick(l, v, position, id);
		
		
		Alert a = alerts.get(position);
		mCallback.OnRedSelectedListener(a.getAreaId(), a.getTime());
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
		 * @param id
		 *            the database id of the RED(Alert)
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
				int currentPage = PreferenceManager
						.getDefaultSharedPreferences(getActivity()).getInt(
								"page", 0);
				currentPage++;
				SharedPreferences.Editor editor = PreferenceManager
						.getDefaultSharedPreferences(getActivity()).edit();
				editor.putInt("page", currentPage);
				editor.apply();

			}
		}
		if (!loading
				&& (totalItemCount - visibleItemCount) <= (firstVisibleItem + visibleThreshold)) {
			// I load the next page of gigs using a background task,
			// but you can call any function here.
			int currentPage = PreferenceManager.getDefaultSharedPreferences(
					getActivity()).getInt("page", 0);
			JsonRequest jr = new JsonRequest();
			// jr.requestJsonObject("http://213.57.173.69:4567/alerts/"+offset+"/25",getActivity());
			int offset = currentPage * 25;
			jr.requestJsonObject2(Utils.SERVER_ALERTS + offset + "/25",
					getActivity(),mAdapter);
			loading = true;
		}
	}

    
}

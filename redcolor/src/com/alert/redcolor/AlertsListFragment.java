package com.alert.redcolor;

import java.util.ArrayList;
import java.util.List;

import org.joda.time.DateTime;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v4.widget.SwipeRefreshLayout.OnRefreshListener;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.alert.redcolor.db.ProviderQueries;
import com.alert.redcolor.model.Alert;
import com.alert.redcolor.model.Area;
import com.google.android.gms.actions.ReserveIntents;

public class AlertsListFragment extends ListFragment implements
		OnScrollListener, OnRefreshListener {

	public final static String TAG = "AlertsList";
	OnRedSelectListener mCallback;
	private AlertsAdapter mAdapter;
	private SwipeRefreshLayout mSwipe;

	public static AlertsListFragment newInstance() {
		AlertsListFragment fragment = new AlertsListFragment();
		Bundle args = new Bundle();
		args.putString("tag", TAG);
		fragment.setArguments(args);
		return fragment;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {

		getListView().setDivider(
				getResources().getDrawable(R.drawable.fade_divider));
		getListView().setDividerHeight(1);
		getListView().setVerticalScrollBarEnabled(true);
		getListView().setOnScrollListener(this);
		mAdapter = new AlertsAdapter(getActivity(), R.id.location,
				new ArrayList<Alert>());

		setListAdapter(mAdapter);
		

		super.onCreate(savedInstanceState);
		super.onActivityCreated(savedInstanceState);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View v = inflater.inflate(R.layout.fragmnet_alerts_list, container,
				false);
		mSwipe = (SwipeRefreshLayout) v.findViewById(R.id.swipe_container);
		mSwipe.setOnRefreshListener(this);
		mSwipe.setColorScheme(android.R.color.holo_red_dark,
				android.R.color.holo_red_light, android.R.color.holo_red_dark,
				android.R.color.holo_red_light);

		return v;
	}

	public class AlertsAdapter extends ArrayAdapter<Alert> {
		private int page;
		private boolean isLoading;
		List<Alert> alerts;

		public AlertsAdapter(Context context, int resource, List<Alert> alerts) {
			super(context, resource, alerts);
			page = 1;
			isLoading = false;
			this.alerts = alerts;
		}

		public void resetPage() {
			this.page = 1;
		}

		public void increasePage() {

			page++;
		}

		public int getPage() {
			return this.page;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			ViewHolder holder;
			if (convertView == null) {

				convertView = LayoutInflater.from(getContext()).inflate(
						R.layout.list_item_alert, null);
				holder = new ViewHolder();
				holder.name = (TextView) convertView
						.findViewById(R.id.location);
				holder.time = (TextView) convertView.findViewById(R.id.time);
				holder.date = (TextView) convertView.findViewById(R.id.date);
				holder.cities = (TextView) convertView
						.findViewById(R.id.cities);
				convertView.setTag(holder);
			} else {

				holder = (ViewHolder) convertView.getTag();

			}

			if (getItem(position) != null) {
				holder = (ViewHolder) convertView.getTag();
				Alert alert = getItem(position);
				holder.time.setText(alert.getTime().toString("HH:mm:ss"));
				holder.date.setText(alert.getTime().toString("dd/MM/yy"));
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
			return convertView;
		}

		public boolean isLoading() {

			// Log.i("Adapter", "is loading " + isLoading);
			return isLoading;
		}

		public void setLoading(boolean loading) {
			this.isLoading = loading;
		}

		public void loadMore() {
			setLoading(true);
			((MainActivity)getActivity()).queryServer(getPage());
			increasePage();
		}

		@Override
		public Alert getItem(int position) {
			return alerts.get(position);
		}
	}

	@Override
	public void onListItemClick(ListView l, View v, int position, long id) {


		super.onListItemClick(l, v, position, id);
		Alert a = mAdapter.getItem(position);
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
					+ " must implement OnRedSelectedListener");
		}
	

	}

	@Override
	public void onScrollStateChanged(AbsListView view, int scrollState) {

	}

	@Override
	public void onScroll(AbsListView view, int firstVisible, int visibleCount,
			int totalCount) {

		boolean loadMore;

		if (mAdapter == null)
			return;

		loadMore = (0 != totalCount)
				&& ((firstVisible + visibleCount) >= (totalCount));

		if (false == mAdapter.isLoading && true == loadMore) {
			Log.i("endless", "loadingMore " + mAdapter.getPage());
			mAdapter.loadMore();
			mAdapter.isLoading = true;

		}

	}

	@Override
	public void onRefresh() {
		mAdapter.clear();
		mAdapter.resetPage();
		((MainActivity)getActivity()).queryServer(0);
	}
	public void addAlerts(ArrayList<Alert> alerts) {
		mAdapter.addAll(alerts);
		mAdapter.notifyDataSetChanged();
		mAdapter.setLoading(false);
		mSwipe.setRefreshing(false);
		
	}
	public void addAlert(Alert alert) {
		mAdapter.alerts.add(0, alert);
		mAdapter.notifyDataSetChanged();
		mSwipe.setRefreshing(false);
	}
	
	
}

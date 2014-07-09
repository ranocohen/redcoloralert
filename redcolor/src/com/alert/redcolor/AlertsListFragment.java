package com.alert.redcolor;

import com.alert.redcolor.db.AlertProvider;
import com.alert.redcolor.model.Alert;

import android.app.AlertDialog;
import android.content.ContentUris;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Checkable;
import android.widget.CursorAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;


public class AlertsListFragment extends ListFragment implements
		LoaderCallbacks<Cursor> {

	
	public final static String TAG = "Alerts";
	

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

		getListView().setDivider(getResources().getDrawable(R.drawable.fade_divider));
		getListView().setDividerHeight(1);
		getListView().setVerticalScrollBarEnabled(false);

		
		mAdapter = new AlertsAdapter(getActivity(), null, 0);
		setListAdapter(mAdapter);
		
		// Prepare the loader. Either re-connect with an existing one,
		// or start a new one.
		getLoaderManager().initLoader(0, null, this);

		super.onCreate(savedInstanceState);
		super.onActivityCreated(savedInstanceState);
	}

	

	
	@Override
	public Loader<Cursor> onCreateLoader(int id, Bundle args) {

		CursorLoader cursorLoader = new CursorLoader(getActivity(),
				AlertProvider.ALERTS_CONTENT_URI, null, null, null, null);
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
			holder.location.setText(alert.getLocation());
			holder.time.setText(alert.getTime().toString());
			


		}

		@Override
		public View newView(Context context, Cursor cursor, ViewGroup parent) {
			View view = layoutInflater.inflate(R.layout.list_item_alert, null);

			ViewHolder holder = new ViewHolder();
			holder.location = (TextView) view.findViewById(R.id.location);
			holder.time = (TextView) view.findViewById(R.id.time);
			view.setTag(holder);
			return view;
		}
	}

	// static class for holding references to views optimizing listview recycles
	private static class ViewHolder {
		TextView location;
		TextView time;
		

	}



}

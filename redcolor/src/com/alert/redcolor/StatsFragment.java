package com.alert.redcolor;

import java.util.ArrayList;

import org.joda.time.DateTime;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.alert.redcolor.adapters.TopAlert;
import com.alert.redcolor.adapters.TopAlertsAdapter;
import com.alert.redcolor.analytics.AnalyticsApp;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.data.ChartData;
import com.github.mikephil.charting.data.DataSet;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.github.mikephil.charting.utils.Legend.LegendForm;

public class StatsFragment extends Fragment {

	public final static String TAG = "Stats";
	private BarChart mBarChart;
	private ListView mList;
	private TopAlertsAdapter adapter;
	public static StatsFragment newInstance() {
		StatsFragment fragment = new StatsFragment();
		Bundle args = new Bundle();
		args.putString("tag", TAG);
		fragment.setArguments(args);
		return fragment;
	}

	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View v = inflater.inflate(R.layout.stats, container, false);
		mList = (ListView) v.findViewById(R.id.list);
		adapter = new TopAlertsAdapter(getActivity(), R.id.location,new ArrayList<TopAlert>());
		mList.setAdapter(adapter);
		queryData();

		return v;
	}

	private void queryData() {
		DateTime nowDt = DateTime.now();
		long now = (long) (nowDt.getMillis()/1000.0);
		long lastWeek = (long) (nowDt.minusDays(7).getMillis()/1000.0);
		
		String url = Utils.SERVER_STATS + String.valueOf(lastWeek)+"/"+String.valueOf(now)+"?top=5"; 
		JsonObjectRequest jr = new JsonObjectRequest(Request.Method.GET,
				url, null,
				new Response.Listener<JSONObject>() {
					@Override
					public void onResponse(JSONObject response) {
						try {
							
							JSONArray data = response.getJSONArray("data");
							
							for (int i = 0; i < data.length(); i++) {
								JSONObject stats = data.getJSONObject(i);
								int count = stats.getInt("total");
								String id = stats.getString("_id");
								
								adapter.add(new TopAlert(id, count));	
							}
							
							adapter.notifyDataSetChanged();
							/*
							JSONArray data = response.getJSONArray("data");
							ArrayList<Entry> entries = new ArrayList<Entry>();
							ArrayList<String> xVals = new ArrayList<String>();
							for (int i = 0; i < data.length(); i++) {
								JSONObject stats = data.getJSONObject(i);
								int count = stats.getInt("total");
								xVals.add("R" + i);
								entries.add(new Entry(count, i));
							}
							
							ChartData chartData = new ChartData(xVals,
									new DataSet(entries, "RED"));
							ColorTemplate ct = new ColorTemplate();
							ct.addColorsForDataSets(new int[] { R.color.red,
									R.color.green , R.color.green, R.color.red 
									,R.color.green , R.color.green, R.color.red 
									,R.color.green , R.color.green, R.color.red }, getActivity());
							mBarChart.setColorTemplate(ct);
							mBarChart.setData(chartData);
							mBarChart.set3DEnabled(false);
							mBarChart.setDescription("");
							mBarChart.setYLabelCount(5);
							mBarChart.setValueDigits(3);
							mBarChart.setDrawYValues(false);
							mBarChart.setDrawGridBackground(false);
							mBarChart.getLegend().setForm(LegendForm.CIRCLE);
							mBarChart.getLegend().setLegendLabels(new String[] { "AAA", "BBB", "CCC","DDD"
									,"AAA", "BBB", "CCC","DDD","AAA", "BBB"
								
							});
							mBarChart.getLegend().setFormSize(10f);
							mBarChart.invalidate();
					*/	} catch (JSONException e) {
						e.printStackTrace();
						}
						;
					}
				}, new Response.ErrorListener() {
					@Override
					public void onErrorResponse(VolleyError error) {
						Log.i(TAG, error.getMessage());
					}
				});
		((AnalyticsApp) getActivity().getApplication()).addToRequestQueue(jr);

	};

}

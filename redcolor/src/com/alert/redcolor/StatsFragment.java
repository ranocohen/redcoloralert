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

		mBarChart = (BarChart) v.findViewById(R.id.chart);

		queryData();

		return v;
	}

	private void queryData() {
		long now = (long) (DateTime.now().getMillis()/1000.0);
		JsonObjectRequest jr = new JsonObjectRequest(Request.Method.GET,
				Utils.SERVER_STATS + "1406129233/"+String.valueOf(now)+"?top=10", null,
				new Response.Listener<JSONObject>() {
					@Override
					public void onResponse(JSONObject response) {
						try {
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
									R.color.green }, getActivity());
							mBarChart.setColorTemplate(ct);
							mBarChart.setData(chartData);
							mBarChart.set3DEnabled(false);
							mBarChart.setDescription("");
							mBarChart.setYLabelCount(5);
							mBarChart.setValueDigits(3);
							mBarChart.setDrawYValues(false);
							mBarChart.setDrawGridBackground(false);
							mBarChart.getLegend().setForm(LegendForm.CIRCLE);
							mBarChart.getLegend().setFormSize(10f);
							mBarChart.invalidate();
						} catch (JSONException e) {
							// TODO Auto-generated catch block
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

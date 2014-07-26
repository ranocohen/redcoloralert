package com.alert.redcolor;

import org.eazegraph.lib.charts.BarChart;
import org.eazegraph.lib.models.BarModel;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.graphics.Color;
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
import com.google.android.gms.internal.jr;



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
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View v = inflater.inflate(R.layout.stats, container,false);
	
		
	
		
		mBarChart = (BarChart) v.findViewById(R.id.barchart);
		
		queryData();
		mBarChart.startAnimation();
		
		return v;
	}
	private void queryData() {
        JsonObjectRequest jr = new JsonObjectRequest(Request.Method.GET,
        		Utils.SERVER_STATS + "1406129233/1406349233?top=15",null,new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
            	try {
					JSONArray data = response.getJSONArray("data");
					for (int i = 0; i < data.length(); i++) {
						JSONObject stats = data.getJSONObject(i);
						int count = stats.getInt("total");
						mBarChart.addBar(new BarModel("H"+count,count, Color.RED));
						mBarChart.setBarWidth(10f);
						
					}
				} catch (JSONException e) {
					// TODO Auto-generated catch block
				}
;            }
        },new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.i(TAG,error.getMessage());
            }
        });
        ((AnalyticsApp)getActivity().getApplication()).addToRequestQueue(jr);

		
	};
	
	
}

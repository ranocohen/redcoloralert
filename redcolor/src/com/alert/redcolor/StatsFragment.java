package com.alert.redcolor;

import org.eazegraph.lib.charts.BarChart;
import org.eazegraph.lib.models.BarModel;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;



public class StatsFragment extends Fragment {

	public final static String TAG = "Stats";

	public static StatsFragment newInstance() {
		StatsFragment fragment = new StatsFragment();
		Bundle args = new Bundle();
		args.putString("tag", TAG);
		fragment.setArguments(args);
		return fragment;
	}
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View v = inflater.inflate(R.layout.stats, container,false);
		
		
		BarChart mBarChart = (BarChart) v.findViewById(R.id.barchart);

		mBarChart.addBar(new BarModel(2.3f, 0xFF123456));
		mBarChart.addBar(new BarModel(2.f,  0xFF343456));
		mBarChart.addBar(new BarModel(3.3f, 0xFF563456));
		mBarChart.addBar(new BarModel(1.1f, 0xFF873F56));
		mBarChart.addBar(new BarModel(2.7f, 0xFF56B7F1));
		mBarChart.addBar(new BarModel(2.f,  0xFF343456));
		mBarChart.addBar(new BarModel(0.4f, 0xFF1FF4AC));
		mBarChart.addBar(new BarModel(4.f,  0xFF1BA4E6));

		mBarChart.startAnimation();
		
		return v;
	};
}

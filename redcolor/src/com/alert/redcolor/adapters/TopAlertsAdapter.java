package com.alert.redcolor.adapters;

import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextSwitcher;
import android.widget.TextView;

import com.alert.redcolor.R;

public class TopAlertsAdapter extends ArrayAdapter<TopAlert> {
	List<TopAlert> alerts;
	public TopAlertsAdapter(Context context, int resource,
			List<TopAlert> objects) {
		super(context, resource, objects);
		this.alerts = objects;
	}
	@Override
	public TopAlert getItem(int position) {
		return alerts.get(position);
	}
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		   ViewHolder holder;             
		 
		   if (convertView == null) {                 
		 
		    convertView = LayoutInflater.from(getContext()).inflate(R.layout.list_item_stats, null);    
		    holder = new ViewHolder();                 
		    holder.id = (TextView) convertView.findViewById(R.id.location);                
		    holder.count = (TextView) convertView.findViewById(R.id.count);                 
		    convertView.setTag(holder);             
		   } else {                 
		 
		    holder = (ViewHolder) convertView.getTag(); 
		 
		   }             
		 
		   if(getItem(position) != null)
		   {
			   TopAlert alert = getItem(position);
			   
			   holder.id.setText(alert.getArea());  
			   holder.count.setText(""+alert.getCount());   
		   }
		    
		 
		   return convertView; 
	}
	private static class ViewHolder {
		TextView id;
		TextView count;

	}
}

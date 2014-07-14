package com.alert.redcolor.ui;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import android.content.Context;
import android.preference.MultiSelectListPreference;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.view.View;
import android.widget.EditText;
import android.widget.ListView;

import com.alert.redcolor.R;
import com.alert.redcolor.SettingsActivity.MapUtil;
import com.alert.redcolor.db.ProviderQueries;

public class TownListPicker extends MultiSelectListPreference {
	public TownListPicker(Context context, AttributeSet attrs) {
		super(context, attrs);
		setPositiveButtonText(android.R.string.ok);
		setNegativeButtonText(android.R.string.cancel);
		setDialogLayoutResource(R.layout.town_picker_layout);
		setDialogIcon(null);

		ProviderQueries pq = new ProviderQueries(context);
		HashMap<Long, String> cities = pq.getCitiesMap();
		List<String> enteries = new ArrayList<String>();
		HashSet<String> values = new HashSet<String>();

		cities = (HashMap<Long, String>) MapUtil.sortByValue(cities);

		for (Long l : cities.keySet())
			values.add(l.toString());

		for (String s : cities.values())
			enteries.add(s);

		setEntries(enteries.toArray(new String[enteries.size()]));
		setEntryValues(values.toArray(new String[values.size()]));

	}

	@Override
	protected void onBindDialogView(View view) {
		final ListView lv = (ListView) view.findViewById(R.id.list);
		EditText search = (EditText) view.findViewById(R.id.txt_searchContact);
		search.addTextChangedListener(new TextWatcher() {
		     
		    @Override
		    public void onTextChanged(CharSequence cs, int arg1, int arg2, int arg3) {
		       ((TownsAdapter)lv.getAdapter()).getFilter().filter(cs);

		    }
		     
		    @Override
		    public void beforeTextChanged(CharSequence arg0, int arg1, int arg2,
		            int arg3) {
		        // TODO Auto-generated method stub
		         
		    }
		     
		    @Override
		    public void afterTextChanged(Editable arg0) {
		        // TODO Auto-generated method stub                          
		    }
		});
		
		ArrayList<String> values = new ArrayList<String>();
		for(int i =0;i<getEntries().length;i++)
			values.add(getEntries()[i].toString());
		
		lv.setAdapter(new TownsAdapter(getContext(),values));
		
		super.onBindDialogView(view);
	}

	


}
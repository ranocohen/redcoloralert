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
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.Filter;
import android.widget.Filterable;
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
		        // When user changed the Text

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
		lv.setAdapter(new ContactListAdapter(getContext()));
		
		super.onBindDialogView(view);
	}

	class ContactListAdapter extends BaseAdapter  {
		Context context;
	    //Two data sources, the original data and filtered data
	    private ArrayList<HashMap<String, String>> originalData;
	    private ArrayList<HashMap<String, String>> filteredData;
		public ContactListAdapter(Context context) {
			super();
			this.context = context;

		}

		/* Custom View Generation(You may modify this to include other Views) */
		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			LayoutInflater inflater = (LayoutInflater) context
					.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

			View view_row = inflater.inflate(R.layout.town_picker_list_item, parent,
					false);

			CheckBox chk_contact = (CheckBox) view_row
					.findViewById(R.id.checkBox1);
			chk_contact.setText(getItem(position));
			// Code to get Selected Contacts.
			chk_contact
					.setOnCheckedChangeListener(new OnCheckedChangeListener() {

						@Override
						public void onCheckedChanged(CompoundButton arg0,
								boolean arg1) {

						}

					});
			return view_row;
		}

		public boolean alreadySelected() {
			boolean ret = false;

			ret = true;

			return ret;
		}

		@Override
		public int getCount() {
			return getEntries().length;
		}

		@Override
		public String getItem(int arg0) {
			return getEntries()[arg0].toString();
		}

		@Override
		public long getItemId(int arg0) {
			return Long.parseLong(getEntryValues()[arg0].toString());
		}

	}

}
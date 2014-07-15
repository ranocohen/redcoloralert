package com.alert.redcolor.ui;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;

import com.alert.redcolor.R;

public class TownsAdapter extends BaseAdapter implements Filterable,
		OnCheckedChangeListener {
	private boolean[] checkedList;
	private List<TownItem> originalData = null;
	private List<TownItem> filteredData = null;
	private LayoutInflater mInflater;
	private ItemFilter mFilter = new ItemFilter();

	public TownsAdapter(Context context, List<TownItem> data) {
		this.filteredData = data;
		this.originalData = data;
		mInflater = LayoutInflater.from(context);
	}

	public int getCount() {
		return filteredData.size();
	}

	public Object getItem(int position) {
		return filteredData.get(position);
	}

	public long getItemId(int position) {
		return position;
	}

	public void setCheckedItems(boolean[] checked) {
		checkedList = checked;
	}

	public boolean[] getCheckedItems() {
		return checkedList;
	}

	public View getView(final int position, View convertView, ViewGroup parent) {
		// A ViewHolder keeps references to children views to avoid unnecessary
		// calls
		// to findViewById() on each row.
		ViewHolder holder;

		// When convertView is not null, we can reuse it directly, there is no
		// need
		// to reinflate it. We only inflate a new View when the convertView
		// supplied
		// by ListView is null.
		if (convertView == null) {
			convertView = mInflater.inflate(R.layout.town_picker_list_item,
					null);
			
			// Creates a ViewHolder and store references to the two children
			// views
			// we want to bind data to.
			holder = new ViewHolder();
			holder.text = (TextView) convertView.findViewById(R.id.name);
			holder.cb = (CheckBox) convertView.findViewById(R.id.check);

			// Bind the data efficiently with the holder.

			convertView.setTag(holder);
		} else {
			// Get the ViewHolder back to get fast access to the TextView
			// and the ImageView.
			holder = (ViewHolder) convertView.getTag();

		}

		// If weren't re-ordering this you could rely on what you set last time
		holder.cb.setTag(filteredData.get(position));
		holder.text.setText(filteredData.get(position).getName());
		holder.cb.setChecked(filteredData.get(position).isChecked());
		holder.cb.setOnCheckedChangeListener(this);
		return convertView;
	}

	static class ViewHolder {
		TextView text;
		CheckBox cb;
	}

	public Filter getFilter() {
		return mFilter;
	}

	private class ItemFilter extends Filter {
		@Override
		protected FilterResults performFiltering(CharSequence constraint) {

			String filterString = constraint.toString().toLowerCase();
			FilterResults results = new FilterResults();

			final List<TownItem> list = originalData;
			int count = list.size();
			final ArrayList<TownItem> nlist = new ArrayList<TownItem>(count);
			String filterableString;

			for (int i = 0; i < count; i++) {
				filterableString = list.get(i).getName();
				if (filterableString.toLowerCase().contains(filterString)) {
					nlist.add(list.get(i));
					
				}
			}

			results.values = nlist;
			results.count = nlist.size();

			return results;
		}

		@SuppressWarnings("unchecked")
		@Override
		protected void publishResults(CharSequence constraint,
				FilterResults results) {
			filteredData = (ArrayList<TownItem>) results.values;
			notifyDataSetChanged();
		}

	}

	@Override
	public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
		TownItem ti = (TownItem) buttonView.getTag();
		ti.setChecked(isChecked);
		checkedList[ti.getPosition()] = isChecked;
	}
	public static class TownItem {
		private long id;
		private String name;
		private boolean checked;
		private int position;
		
		public TownItem(long id, String name, boolean checked,int position) {
			this.id = id;
			this.name = name;
			this.checked = checked;
			this.position = position;
		}
		private long getId() { return this.id;}
		private boolean isChecked() { return this.checked;}
		private String getName() { return this.name;}
		private int getPosition() {return this.position;}
		private void setChecked(boolean c) { this.checked =c;}
	}
}
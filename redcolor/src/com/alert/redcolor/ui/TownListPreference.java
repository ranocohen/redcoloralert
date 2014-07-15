package com.alert.redcolor.ui;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.TypedArray;
import android.os.Parcel;
import android.os.Parcelable;
import android.preference.DialogPreference;
import android.preference.PreferenceManager;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.view.View;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import com.alert.redcolor.PreferencesUtils;
import com.alert.redcolor.R;
import com.alert.redcolor.SettingsActivity.MapUtil;
import com.alert.redcolor.db.ProviderQueries;
import com.alert.redcolor.ui.TownsAdapter.TownItem;

public class TownListPreference extends DialogPreference {

	private CharSequence[] mEntries;
	private CharSequence[] mEntryValues;
	private Set<String> mValues = new HashSet<String>();
	private Set<String> mNewValues = new HashSet<String>();
	private ListView lv;

	public TownListPreference(Context context, AttributeSet attrs) {
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
		lv = (ListView) view.findViewById(R.id.list);
		EditText search = (EditText) view.findViewById(R.id.txt_searchContact);
		search.addTextChangedListener(new TextWatcher() {

			@Override
			public void onTextChanged(CharSequence cs, int arg1, int arg2,
					int arg3) {
				((TownsAdapter) lv.getAdapter()).getFilter().filter(cs);

			}

			@Override
			public void beforeTextChanged(CharSequence arg0, int arg1,
					int arg2, int arg3) {

			}

			@Override
			public void afterTextChanged(Editable arg0) {
			}
		});
		boolean [] checkedItems = getSelectedItems();
		ArrayList<TownItem> values = new ArrayList<TownItem>();
		for (int i = 0; i < getEntries().length; i++)
		{
			
			long id = Long.parseLong(getEntryValues()[i].toString());
			String name = getEntries()[i].toString();
			boolean isChecked = checkedItems[i];
			
			values.add(new TownItem(id,name,isChecked,i));
		}
			
		
		lv.setAdapter(new TownsAdapter(getContext(),values ));
		super.onBindDialogView(view);
	}

    
    /**
     * Sets the human-readable entries to be shown in the list. This will be
     * shown in subsequent dialogs.
     * <p>
     * Each entry must have a corresponding index in
     * {@link #setEntryValues(CharSequence[])}.
     * 
     * @param entries The entries.
     * @see #setEntryValues(CharSequence[])
     */
    public void setEntries(CharSequence[] entries) {
        mEntries = entries;
    }
    
    /**
     * @see #setEntries(CharSequence[])
     * @param entriesResId The entries array as a resource.
     */
    public void setEntries(int entriesResId) {
        setEntries(getContext().getResources().getTextArray(entriesResId));
    }
    
    /**
     * The list of entries to be shown in the list in subsequent dialogs.
     * 
     * @return The list as an array.
     */
    public CharSequence[] getEntries() {
        return mEntries;
    }
    
    /**
     * The array to find the value to save for a preference when an entry from
     * entries is selected. If a user clicks on the second item in entries, the
     * second item in this array will be saved to the preference.
     * 
     * @param entryValues The array to be used as values to save for the preference.
     */
    public void setEntryValues(CharSequence[] entryValues) {
        mEntryValues = entryValues;
    }

    /**
     * @see #setEntryValues(CharSequence[])
     * @param entryValuesResId The entry values array as a resource.
     */
    public void setEntryValues(int entryValuesResId) {
        setEntryValues(getContext().getResources().getTextArray(entryValuesResId));
    }
    
    /**
     * Returns the array of values to be saved for the preference.
     * 
     * @return The array of values.
     */
    public CharSequence[] getEntryValues() {
        return mEntryValues;
    }
    
    /**
     * Sets the value of the key. This should contain entries in
     * {@link #getEntryValues()}.
     * 
     * @param values The values to set for the key.
     */
    public void setValues(Set<String> values) {
    	
        mValues.clear();
        mValues.addAll(values);        
        
        Set<String> s = PreferenceManager.getDefaultSharedPreferences(getContext())
        	.getStringSet(getKey(), new HashSet<String>());
        s = new HashSet<String>(s);
        s.clear();
        s.addAll(values);
        
        SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(getContext()).edit();
        editor.putStringSet(getKey(), s);
        editor.commit();
       
    }
    
    /**
     * Retrieves the current value of the key.
     */
    public Set<String> getValues() {
        return mValues;
    }
    
    /**
     * Returns the index of the given value (in the entry values array).
     * 
     * @param value The value whose index should be returned.
     * @return The index of the value, or -1 if not found.
     */
    public int findIndexOfValue(String value) {
        if (value != null && mEntryValues != null) {
            for (int i = mEntryValues.length - 1; i >= 0; i--) {
                if (mEntryValues[i].equals(value)) {
                    return i;
                }
            }
        }
        return -1;
    }
    
    
    private boolean[] getSelectedItems() {
        final CharSequence[] entries = mEntryValues;
        final int entryCount = entries.length;
        final Set<String> values = mValues;
        boolean[] result = new boolean[entryCount];
        
        for (int i = 0; i < entryCount; i++) {
            result[i] = values.contains(entries[i].toString());
        }
        
        return result;
    }
    
    
    @Override
    protected void onDialogClosed(boolean positiveResult) {
        super.onDialogClosed(positiveResult);
        boolean [] checkedItems = ((TownsAdapter)lv.getAdapter()).getCheckedItems();
        
        if (positiveResult) {
            final Set<String> values = mNewValues;
            if (callChangeListener(values)) {
            	values.clear();
            	for(int i =0;i<checkedItems.length;i++)
                {
            		if(checkedItems[i])
            			values.add(getEntryValues()[i].toString());
                }
                setValues(values);
            }
        }
    }
    
    @Override
    protected Object onGetDefaultValue(TypedArray a, int index) {
        final CharSequence[] defaultValues = a.getTextArray(index);
        final int valueCount = defaultValues.length;
        final Set<String> result = new HashSet<String>();
        
        for (int i = 0; i < valueCount; i++) {
            result.add(defaultValues[i].toString());
        }
        
        return result;
    }
    
    @Override
    protected void onSetInitialValue(boolean restoreValue, Object defaultValue) {
    	SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(getContext());
		Set<String> selections = sharedPrefs.getStringSet(PreferencesUtils.ALERTS_TOWNS_SELECT, new HashSet<String>());
		Set<String> selectionsCopy = new HashSet<String>(selections);
		setValues(selectionsCopy);
    }
    
    
    @Override
    protected void onPrepareDialogBuilder(Builder builder) {
        super.onPrepareDialogBuilder(builder);
       ((TownsAdapter)lv.getAdapter()).setCheckedItems(getSelectedItems());
        
    }

    
    @Override
    protected Parcelable onSaveInstanceState() {
        final Parcelable superState = super.onSaveInstanceState();
        if (isPersistent()) {
            // No need to save instance state
            return superState;
        }
        
        final SavedState myState = new SavedState(superState);
        myState.values = getValues();
        return myState;
    }
    
    private static class SavedState extends BaseSavedState {
        Set<String> values;
        
        public SavedState(Parcel source) {
            super(source);
            values = new HashSet<String>();
            String[] strings = source.createStringArray();
            
            final int stringCount = strings.length;
            for (int i = 0; i < stringCount; i++) {
                values.add(strings[i]);
            }
        }
        
        public SavedState(Parcelable superState) {
            super(superState);
        }
        
        @Override
        public void writeToParcel(Parcel dest, int flags) {
            super.writeToParcel(dest, flags);
            dest.writeStringArray(values.toArray(new String[0]));
        }
        
        public static final Parcelable.Creator<SavedState> CREATOR =
                new Parcelable.Creator<SavedState>() {
            public SavedState createFromParcel(Parcel in) {
                return new SavedState(in);
            }
            
            public SavedState[] newArray(int size) {
                return new SavedState[size];
            }
        };
    }


}
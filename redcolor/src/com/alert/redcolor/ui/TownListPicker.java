package com.alert.redcolor.ui;

import android.content.Context;
import android.preference.MultiSelectListPreference;
import android.util.AttributeSet;

import com.alert.redcolor.R;


public class TownListPicker extends MultiSelectListPreference  {
    public TownListPicker(Context context, AttributeSet attrs) {
        super(context, attrs);
        setPositiveButtonText(android.R.string.ok);
        setNegativeButtonText(android.R.string.cancel);
        setDialogLayoutResource(R.layout.town_picker_layout);
        setDialogIcon(null);
    }

}
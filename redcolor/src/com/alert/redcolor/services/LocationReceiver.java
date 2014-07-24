package com.alert.redcolor.services;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.util.Log;

import com.google.android.gms.location.LocationClient;

public class LocationReceiver extends BroadcastReceiver {
	 
    @Override
    public void onReceive(Context context, Intent intent) {
 
        Location location = (Location) intent.getExtras().get(LocationClient.KEY_LOCATION_CHANGED);
        if(location != null)
        	Log.i("GOT LOCATION", ""+location.toString());
        else
        	Log.i("GOT LOCATION", "GOT NULL");
        return;
    }
}
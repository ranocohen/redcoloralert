package com.alert.redcolor;

import org.joda.time.DateTime;
import org.json.JSONArray;
import org.json.JSONException;

import android.app.IntentService;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.IBinder;
import android.os.SystemClock;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.widget.Toast;

import com.alert.redcolor.db.AlertProvider;
import com.alert.redcolor.db.RedColordb;
import com.alert.redcolor.model.Alert;
import com.alert.redcolor.services.BackgroundLocationService;
import com.alert.redcolor.services.BackgroundLocationService.LocalBinder;
import com.google.android.gms.gcm.GoogleCloudMessaging;

public class GcmIntentService extends IntentService
 {
    public static final int NOTIFICATION_ID = 1;
    private NotificationManager mNotificationManager;
    private NotificationCompat.Builder builder;
    
    boolean mBound = false;

    BackgroundLocationService mService;
    private ServiceConnection mConnection = new ServiceConnection() {
        // Called when the connection with the service is established
        public void onServiceConnected(ComponentName className, IBinder service) {
            // Because we have bound to an explicit
            // service that is running in our own process, we can
            // cast its IBinder to a concrete class and directly access it.
            LocalBinder binder = (LocalBinder) service;
            
            binder.getServerInstance().getLastKnownLocation();
            mBound = true;
        }

        // Called when the connection with the service disconnects unexpectedly
        public void onServiceDisconnected(ComponentName className) {
            Log.e("GCM disconnected from location", "onServiceDisconnected");
            mBound = false;
        }
    };

    public GcmIntentService() {
        super("GcmIntentService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
    	  Bundle extras = intent.getExtras();
          GoogleCloudMessaging gcm = GoogleCloudMessaging.getInstance(this);
          // The getMessageType() intent parameter must be the intent you received
          // in your BroadcastReceiver.
          String messageType = gcm.getMessageType(intent);

          if (!extras.isEmpty()) {  // has effect of unparcelling Bundle
              /*
               * Filter messages based on message type. Since it is likely that GCM
               * will be extended in the future with new message types, just ignore
               * any message types you're not interested in, or that you don't
               * recognize.
               */
              if (GoogleCloudMessaging.
                      MESSAGE_TYPE_SEND_ERROR.equals(messageType)) {
                  sendNotification("Send error: " + extras.toString());
              } else if (GoogleCloudMessaging.
                      MESSAGE_TYPE_DELETED.equals(messageType)) {
                  sendNotification("Deleted messages on server: " +
                          extras.toString());
              // If it's a regular GCM message, do some work.
              } else if (GoogleCloudMessaging.
                      MESSAGE_TYPE_MESSAGE.equals(messageType)) {
                  // This loop represents the service doing some work.
              	String jsonStr = extras.getString("alerts");
              	String time = extras.getString("timestamp");
              	Long l = Long.parseLong(time);
              	DateTime dt = new DateTime(l.longValue()*1000);
              	String locationProvider = LocationManager.NETWORK_PROVIDER;
					// Or use LocationManager.GPS_PROVIDER
					LocationManager locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
					Location lastKnownLocation = locationManager.getLastKnownLocation(locationProvider);
					
              	try {
  					JSONArray json = new JSONArray(jsonStr);
  					for(int i =0;i<json.length();i++) {
  						JSONArray obj = json.getJSONArray(i);
  						Alert alert = new Alert(obj , dt);
  						
  						
  						ContentValues cv = new ContentValues();
  						cv.put(RedColordb.AlertColumns.lat, alert.getLat());
  						cv.put(RedColordb.AlertColumns.lng , alert.getLng());
  						cv.put(RedColordb.AlertColumns.location , alert.getLocation());
  						cv.put(RedColordb.AlertColumns.time , alert.getTime().toString());
  						
  						getContentResolver().insert(
  								AlertProvider.ALERTS_CONTENT_URI, cv);
  						
  					
  					
  					
  						
  						
  						
  					}
  				} catch (JSONException e) {
  					// TODO Auto-generated catch block
  					e.printStackTrace();
  				}
              	  sendNotification("Received: " + extras.toString());
              	  
              	Intent intent1 = new Intent(this, BackgroundLocationService.class);
              	bindService(intent1, mConnection, Context.BIND_AUTO_CREATE);
              	  
                  Log.i(Utils.TAG, "Completed work @ " + SystemClock.elapsedRealtime());
                  // Post notification of received message.
                 
                  Log.i(Utils.TAG, "Received: " + extras.toString());
                  
              }
          }
          // Release the wake lock provided by the WakefulBroadcastReceiver.
          GcmBroadcastReceiver.completeWakefulIntent(intent);
    }

    // Put the message into a notification and post it.
    // This is just one simple example of what you might choose to do with
    // a GCM message.
    private void sendNotification(String msg) {
        mNotificationManager = (NotificationManager)
                this.getSystemService(Context.NOTIFICATION_SERVICE);

        PendingIntent contentIntent = PendingIntent.getActivity(this, 0,
                new Intent(this, MainActivity.class), 0);

        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(this)
        .setContentTitle("GCM Notification")
        .setSmallIcon(R.drawable.ic_launcher)
        .setStyle(new NotificationCompat.BigTextStyle()
        .bigText(msg))
        .setContentText(msg);

        mBuilder.setContentIntent(contentIntent);
        mNotificationManager.notify(NOTIFICATION_ID, mBuilder.build());
    }


	
}
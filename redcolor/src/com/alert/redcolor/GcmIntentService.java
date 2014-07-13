package com.alert.redcolor;

import java.util.ArrayList;

import org.joda.time.DateTime;
import org.json.JSONArray;
import org.json.JSONException;

import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.alert.redcolor.db.AlertProvider;
import com.alert.redcolor.db.ProviderQueries;
import com.alert.redcolor.db.RedColordb.AlertColumns;
import com.alert.redcolor.model.Area;
import com.alert.redcolor.model.City;
import com.google.android.gms.gcm.GoogleCloudMessaging;

public class GcmIntentService extends IntentService {
	public static final int NOTIFICATION_ID = 1;
	private NotificationManager mNotificationManager;
	private NotificationCompat.Builder builder;
	// Distance to get notification is 5km
	private int radiusDistance = 5 * 1000;
	private int notificationsNums = 0;

	/*
	 * boolean mBound = false;
	 * 
	 * BackgroundLocationService mService; private ServiceConnection mConnection
	 * = new ServiceConnection() { // Called when the connection with the
	 * service is established public void onServiceConnected(ComponentName
	 * className, IBinder service) { // Because we have bound to an explicit //
	 * service that is running in our own process, we can // cast its IBinder to
	 * a concrete class and directly access it. LocalBinder binder =
	 * (LocalBinder) service;
	 * 
	 * binder.getServerInstance().getLastKnownLocation(); mBound = true; }
	 * 
	 * // Called when the connection with the service disconnects unexpectedly
	 * public void onServiceDisconnected(ComponentName className) {
	 * Log.e("GCM disconnected from location", "onServiceDisconnected"); mBound
	 * = false; } };
	 */

	public GcmIntentService() {
		super("GcmIntentService");
	}

	@Override
	protected void onHandleIntent(Intent intent) {
		
		
		/* Make sure database is ready */
		if(!doneFirstInit())
		{
			return;
		}
		Bundle extras = intent.getExtras();
		GoogleCloudMessaging gcm = GoogleCloudMessaging.getInstance(this);
		// The getMessageType() intent parameter must be the intent you received
		// in your BroadcastReceiver.
		String messageType = gcm.getMessageType(intent);

		if (!extras.isEmpty()) { // has effect of unparcelling Bundle
			/*
			 * Filter messages based on message type. Since it is likely that
			 * GCM will be extended in the future with new message types, just
			 * ignore any message types you're not interested in, or that you
			 * don't recognize.
			 */
			if (GoogleCloudMessaging.MESSAGE_TYPE_SEND_ERROR
					.equals(messageType)) {

			} else if (GoogleCloudMessaging.MESSAGE_TYPE_DELETED
					.equals(messageType)) {

				// If it's a regular GCM message, do some work.
			} else if (GoogleCloudMessaging.MESSAGE_TYPE_MESSAGE
					.equals(messageType)) {
				// This loop represents the service doing some work.
				String jsonStr = extras.getString("alerts");
				String time = extras.getString("timestamp");
				int type = Integer.parseInt(extras.getString("type"));
				if (type == 1) {

					Long l = Long.parseLong(time);
					DateTime dt = new DateTime(l.longValue() * 1000);
					String locationProvider = LocationManager.NETWORK_PROVIDER;
					// Or use LocationManager.GPS_PROVIDER
					LocationManager locationManager = (LocationManager) this
							.getSystemService(Context.LOCATION_SERVICE);
					Location lastKnownLocation = locationManager
							.getLastKnownLocation(locationProvider);

					StringBuilder titleBuilder = new StringBuilder();
					StringBuilder contentBuilder = new StringBuilder();
					ArrayList<City> cities = new ArrayList<City>();
					ProviderQueries pq = new ProviderQueries(
							getApplicationContext());

					try {
						JSONArray json = new JSONArray(jsonStr);
						for (int i = 0; i < json.length(); i++) {

							long id = json.getLong(i);

							/* Adding the alert to db */
							ContentValues cv = new ContentValues();
							cv.put(AlertColumns.AreaId, id);
							cv.put(AlertColumns.time, dt.toString());
							cv.put(AlertColumns.painted, 0);

							getContentResolver().insert(
									AlertProvider.ALERTS_CONTENT_URI, cv);

							/* Check location */

							Area a = pq.areaById(id);
							
							titleBuilder.append(a.getName()).append(" ")
									.append(a.getAreaNum());
							if (i != json.length() - 1)
								titleBuilder.append(", ");

							cities.addAll(pq.getCities(id));

							notificationsNums++;

						}
					} catch (JSONException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}

					boolean toNotify = false;

					String alertType = PreferencesUtils
							.getAlertsType(getApplicationContext());
					if (alertType.equals(PreferencesUtils.PREF_ALL_ALERTS))
						toNotify = true;

					loop: for (int j = 0; j < cities.size(); j++) {
						contentBuilder.append(cities.get(j).getHebName());
						if (j != cities.size() - 1)
							contentBuilder.append(", ");

						if (lastKnownLocation != null
								&& alertType
										.equals(PreferencesUtils.PREF_LOCAL_ALERTS)) {
							double distance = cities.get(j).distanceTo(
									lastKnownLocation);
							if (distance <= radiusDistance) {
								toNotify = true;
								break loop;
							}
						}
					}

					if (toNotify)
						sendNotification(titleBuilder.toString(),
								contentBuilder.toString(), notificationsNums);

					/*
					 * Intent intent1 = new Intent(this,
					 * BackgroundLocationService.class); bindService(intent1,
					 * mConnection, Context.BIND_AUTO_CREATE);
					 */

				}
			}
		}
		// Release the wake lock provided by the WakefulBroadcastReceiver.
		GcmBroadcastReceiver.completeWakefulIntent(intent);
	}

	private boolean doneFirstInit() {
		SharedPreferences preferences = PreferenceManager
				.getDefaultSharedPreferences(this);
		boolean firstInit = preferences.getBoolean("firstInit", false);
		
		return firstInit;
	}

	// Put the message into a notification and post it.
	// This is just one simple example of what you might choose to do with
	// a GCM message.
	private void sendNotification(String title, String content, int counter) {

		mNotificationManager = (NotificationManager) this
				.getSystemService(Context.NOTIFICATION_SERVICE);

		PendingIntent contentIntent = PendingIntent.getActivity(this, 0,
				new Intent(this, MainActivity.class), 0);

		NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(
				this)
				.setSmallIcon(R.drawable.ic_launcher)
				.setContentTitle(title)
				.setNumber(counter)
				.setDefaults(
						Notification.DEFAULT_LIGHTS
								| Notification.DEFAULT_VIBRATE)
				.setSound(
						Uri.parse("android.resource://" + getPackageName()
								+ "/" + R.raw.short_alert))
				.setContentText(content)
				.setStyle(
						new NotificationCompat.BigTextStyle().bigText(content));

		mBuilder.setContentIntent(contentIntent);
		mNotificationManager.notify(NOTIFICATION_ID, mBuilder.build());
	}

}
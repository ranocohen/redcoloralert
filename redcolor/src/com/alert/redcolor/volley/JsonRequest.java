package com.alert.redcolor.volley;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.format.DateTimeFormat;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.util.Log;

import com.alert.redcolor.AlertsListFragment.AlertsAdapter;
import com.alert.redcolor.analytics.AnalyticsApp;
import com.alert.redcolor.db.AlertProvider;
import com.alert.redcolor.db.RedColordb.AlertColumns;
import com.alert.redcolor.model.Alert;
import com.android.volley.Request.Method;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;

public class JsonRequest {

	public JsonRequest() {
	}

	public void pushWithParams(String url, final String regid) {
		// Tag used to cancel the request
		String tag_json_obj = "json_obj_req";

		JsonObjectRequest jsonObjReq = new JsonObjectRequest(Method.POST, url,
				null, new Response.Listener<JSONObject>() {

					@Override
					public void onResponse(JSONObject response) {
						Log.d("onResponse", response.toString());
					}
				}, new Response.ErrorListener() {

					@Override
					public void onErrorResponse(VolleyError error) {
						VolleyLog.d("onErrorResponse",
								"Error: " + error.getMessage());
					}
				}) {

			@Override
			protected Map<String, String> getParams() {
				Map<String, String> params = new HashMap<String, String>();
				params.put("regid", regid.toString());

				return params;
			}

		};

		// Adding request to request queue
		AnalyticsApp.getInstance().addToRequestQueue(jsonObjReq, tag_json_obj);

	}

	public void requestJsonArray(String url, Context con) {
		// Tag used to cancel the request
		String tag_json_arry = "json_array_req";

		/*
		 * final ProgressDialog pDialog = new ProgressDialog(con);
		 * pDialog.setMessage("Loading..."); pDialog.show();
		 */

		JsonArrayRequest req = new JsonArrayRequest(url,
				new Response.Listener<JSONArray>() {
					@Override
					public void onResponse(JSONArray response) {
						Log.d("VolleyJsonArrayOnResponse: ",
								response.toString());
						// pDialog.hide();
					}
				}, new Response.ErrorListener() {
					@Override
					public void onErrorResponse(VolleyError error) {
						VolleyLog.d("VolleyJsonArrayError",
								"Error: " + error.getMessage());
						// pDialog.hide();
					}
				});

		// Adding request to request queue
		AnalyticsApp.getInstance().addToRequestQueue(req, tag_json_arry);
	}

	public void analyzeAlertJson(JSONObject response, Context context,
			AlertsAdapter adapter) {
		if(adapter == null)
			return;
		int count = 0;
		try {
			JSONArray data = response.getJSONArray("data");
			// iterates on each alert
			for (int i = 0; i < data.length(); i++) {
				JSONObject alert = data.getJSONObject(i);
				JSONArray areas = alert.getJSONArray("areas");
				String time = alert.getString("time");
				// TODO call your method below if needed in datetime format (for
				// db)
				DateTime dt = parseDateTime(time);

				// area's shit:
				for (int j = 0; j < areas.length(); j++) {
					JSONObject area = areas.getJSONObject(j);
					String area_name = area.getString("area_name"); // TODO IDAN
																	// DB
					int area_id = area.getInt("area_id");// TODO IDAN DB
					/* Adding the alert to db */
					
					Alert a = new Alert(area_id,dt);
					adapter.add(a);
					count++;
					// no need for this but we'll keep it just in case

					/*
					 * for (int k = 0; k < locations.length(); k++) { JSONObject
					 * location = locations.getJSONObject(k); String
					 * location_name = location.getString("name_he"); double lat
					 * = location.getDouble("lat"); double lng =
					 * location.getDouble("lng"); //TODO WHATEVER YOU WANT TO
					 * (DB) }
					 */
				}
				adapter.notifyDataSetChanged();
			}
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		if (adapter != null) {
			{
				adapter.setLoading(false);
				adapter.increasePage();
			}
		

		}
		Log.i("endless", " added " + count);
	}

	private boolean isDuplciate(long area_id, DateTime dt, Context context) {
		Cursor c = context.getContentResolver().query(
				AlertProvider.ALERTS_CONTENT_URI,
				null,
				AlertColumns.AreaId + " = " + area_id + " AND "
						+ AlertColumns.time + " = '" + dt.toString() + "'",
				null, null);
		if (c.getCount() > 0) {
			c.close();
			return true;
		}
		c.close();
		return false;

	}

	public ArrayList<Alert> analyzeAlertJson2(JSONObject response,
			Context context) {
		
		try {
			JSONArray data = response.getJSONArray("data");
			// iterates on each alert

			for (int i = 0; i < data.length(); i++) {
				JSONObject alert = data.getJSONObject(i);
				JSONArray areas = alert.getJSONArray("areas");
				String time = alert.getString("time");
				// TODO call your method below if needed in datetime format (for
				// db)
				DateTime dt = parseDateTime(time);

				// area's shit:
				for (int j = 0; j < areas.length(); j++) {
					JSONObject area = areas.getJSONObject(j);
					String area_name = area.getString("area_name"); // TODO IDAN
																	// DB
					int area_id = area.getInt("area_id");// TODO IDAN DB
					/* Adding the alert to db */

					Alert a = new Alert(area_id, dt);
					

					JSONArray locations = area.getJSONArray("locations");

					// no need for this but we'll keep it just in case

					/*
					 * for (int k = 0; k < locations.length(); k++) { JSONObject
					 * location = locations.getJSONObject(k); String
					 * location_name = location.getString("name_he"); double lat
					 * = location.getDouble("lat"); double lng =
					 * location.getDouble("lng"); //TODO WHATEVER YOU WANT TO
					 * (DB) }
					 */
				}
			}
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}

	private static DateTime parseDateTime(String input) {
		String pattern = "yyyy-MM-dd HH:mm:ss 'UTC";
		DateTime utc = DateTime.parse(input, DateTimeFormat.forPattern(pattern)
				.withZoneUTC());
		DateTime dateTime = utc.withZone(DateTimeZone.getDefault());
		return dateTime;
	}

	public void requestJsonObject(String url, final Context con) {
		// Tag used to cancel the request
		String tag_json_obj = "json_obj_req";
		Log.i("endless", url);
		JsonObjectRequest jsonObjReq = new JsonObjectRequest(Method.GET, url,
				null, new Response.Listener<JSONObject>() {

					@Override
					public void onResponse(JSONObject response) {
						// Log.d("VolleyJsonObjectOnResponse",
						// response.toString());
						analyzeAlertJson(response, con, null);
					}
				}, new Response.ErrorListener() {

					@Override
					public void onErrorResponse(VolleyError error) {
						// VolleyLog.d("VolleyJsonObjectError",
						// "Error: " + error.getMessage());
					}
				});

		// Adding request to request queue
		AnalyticsApp.getInstance().addToRequestQueue(jsonObjReq, tag_json_obj);
	}

	public void requestJsonObject(String url, final Context con,
			final AlertsAdapter adapter) {
		// Tag used to cancel the request
		String tag_json_obj = "json_obj_req";
		Log.i("endless", url);
		JsonObjectRequest jsonObjReq = new JsonObjectRequest(Method.GET, url,
				null, new Response.Listener<JSONObject>() {

					@Override
					public void onResponse(JSONObject response) {
						// Log.d("VolleyJsonObjectOnResponse",
						// response.toString());
						analyzeAlertJson(response, con, adapter);
					}
				}, new Response.ErrorListener() {

					@Override
					public void onErrorResponse(VolleyError error) {
						// VolleyLog.d("VolleyJsonObjectError",
						// "Error: " + error.getMessage());
					}
				});

		// Adding request to request queue
		AnalyticsApp.getInstance().addToRequestQueue(jsonObjReq, tag_json_obj);
	}

}

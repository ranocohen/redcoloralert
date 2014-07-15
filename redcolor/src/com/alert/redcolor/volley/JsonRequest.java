package com.alert.redcolor.volley;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Context;
import android.util.Log;

import com.alert.redcolor.analytics.AnalyticsApp;
import com.alert.redcolor.db.AlertProvider;
import com.alert.redcolor.db.RedColordb.AlertColumns;
import com.android.volley.Request.Method;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;

public class JsonRequest {

	public JsonRequest() {

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
	
	public void analyzeAlertJson(JSONObject response,Context context) {
		try {
			JSONArray data = response.getJSONArray("data");
			//iterates on each alert
			for (int i = 0; i < data.length(); i++) {
				JSONObject alert = data.getJSONObject(i);
				JSONArray areas = alert.getJSONArray("areas");
				String time = alert.getString("time");
				//TODO call your method below if needed in datetime format (for db)
				DateTime dt = parseDateTime(time);
				
				//area's shit:
				for (int j = 0; j < areas.length(); j++) {
					JSONObject area = areas.getJSONObject(j);
					String area_name = area.getString("area_name"); //TODO IDAN DB
					int area_id = area.getInt("area_id");//TODO IDAN DB
					/* Adding the alert to db */
					ContentValues cv = new ContentValues();
					cv.put(AlertColumns.AreaId, area_id);
					cv.put(AlertColumns.time, dt.toString());
					cv.put(AlertColumns.painted, 0);

					context.getContentResolver().insert(
							AlertProvider.ALERTS_CONTENT_URI, cv);

					JSONArray locations = area.getJSONArray("locations");
					
					//no need for this but we'll keep it just in case
					
/*					for (int k = 0; k < locations.length(); k++) {
						JSONObject location = locations.getJSONObject(k);
						String location_name = location.getString("name_he");
						double lat = location.getDouble("lat");
						double lng = location.getDouble("lng");
						//TODO WHATEVER YOU WANT TO (DB)
					}*/
				}
			}
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private static DateTime parseDateTime(String input){
	     String pattern = "yyyy-MM-dd HH:mm:ss 'UTC";
	     DateTime dateTime  = DateTime.parse(input, DateTimeFormat.forPattern(pattern));
	     return dateTime;
	}

	public void requestJsonObject(String url, final Context con) {
		// Tag used to cancel the request
		String tag_json_obj = "json_obj_req";

		final ProgressDialog pDialog = new ProgressDialog(con);
		pDialog.setMessage("Loading...");
		pDialog.show();

		JsonObjectRequest jsonObjReq = new JsonObjectRequest(Method.GET, url,
				null, new Response.Listener<JSONObject>() {

					@Override
					public void onResponse(JSONObject response) {
						Log.d("VolleyJsonObjectOnResponse", response.toString());
						analyzeAlertJson(response,con);
						pDialog.hide();
					}
				}, new Response.ErrorListener() {

					@Override
					public void onErrorResponse(VolleyError error) {
						VolleyLog.d("VolleyJsonObjectError",
								"Error: " + error.getMessage());
						// hide the progress dialog
						pDialog.hide();
					}
				});

		// Adding request to request queue
		AnalyticsApp.getInstance().addToRequestQueue(jsonObjReq, tag_json_obj);
	}

}

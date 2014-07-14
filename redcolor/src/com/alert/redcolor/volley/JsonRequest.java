package com.alert.redcolor.volley;

import org.json.JSONArray;
import org.json.JSONObject;

import com.android.volley.Request.Method;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;

import android.app.ProgressDialog;
import android.content.Context;
import android.util.Log;

public class JsonRequest {
	
	public JsonRequest() {
		
	}
	
	public void requestJsonArray(String url,Context con) {
		// Tag used to cancel the request
		String tag_json_arry = "json_array_req";
		 
		final ProgressDialog pDialog = new ProgressDialog(con);
		pDialog.setMessage("Loading...");
		pDialog.show();     
		         
		JsonArrayRequest req = new JsonArrayRequest(url,
		                new Response.Listener<JSONArray>() {
		                    @Override
		                    public void onResponse(JSONArray response) {
		                        Log.d("VolleyJsonArrayOnResponse: ", response.toString());        
		                        pDialog.hide();             
		                    }
		                }, new Response.ErrorListener() {
		                    @Override
		                    public void onErrorResponse(VolleyError error) {
		                        VolleyLog.d("VolleyJsonArrayError", "Error: " + error.getMessage());
		                        pDialog.hide();
		                    }
		                });
		 
		// Adding request to request queue
		AppController.getInstance().addToRequestQueue(req, tag_json_arry);
	}
	
	public void requestJsonObject(String url,Context con) {
		// Tag used to cancel the request
		String tag_json_obj = "json_obj_req";
		 
		final ProgressDialog pDialog = new ProgressDialog(con);
		pDialog.setMessage("Loading...");
		pDialog.show();     
		         
		        JsonObjectRequest jsonObjReq = new JsonObjectRequest(Method.GET,
		                url, null,
		                new Response.Listener<JSONObject>() {
		 
		                    @Override
		                    public void onResponse(JSONObject response) {
		                        Log.d("VolleyJsonObjectOnResponse", response.toString());
		                        pDialog.hide();
		                    }
		                }, new Response.ErrorListener() {
		 
		                    @Override
		                    public void onErrorResponse(VolleyError error) {
		                        VolleyLog.d("VolleyJsonObjectError", "Error: " + error.getMessage());
		                        // hide the progress dialog
		                        pDialog.hide();
		                    }
		                });
		 
		// Adding request to request queue
		AppController.getInstance().addToRequestQueue(jsonObjReq, tag_json_obj);
	}

}

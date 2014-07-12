package com.alert.redcolor;

import java.io.IOException;
import java.io.InputStream;

import android.content.Context;
import android.content.Intent;

public class Utils {

	     
	    // give your server registration url here
	    //static final String SERVER_URL = "http://redalert-il.herokuapp.com/android_register_v1"; 
	
		//DEBUG VALUES
	    static final String SERVER_URL = "http://213.57.173.69:4567/android_register_v1"; 

	 
	    // Google project id
	    static final String SENDER_ID = "903913289319"; 
	 
	    /**
	     * Tag used on log messages.
	     */
	    static final String TAG = "RED COLOR";
	 
	    static final String DISPLAY_MESSAGE_ACTION =
	            "com.alert.redcolor.DISPLAY_MESSAGE";
	 
	    static final String EXTRA_MESSAGE = "message";
	 
	    /**
	     * Notifies UI to display a message.
	     * <p>
	     * This method is defined in the common helper because it's used both by
	     * the UI and the background service.
	     *
	     * @param context application's context.
	     * @param message message to be displayed.
	     */
	    static void displayMessage(Context context, String message) {
	        Intent intent = new Intent(DISPLAY_MESSAGE_ACTION);
	        intent.putExtra(EXTRA_MESSAGE, message);
	        context.sendBroadcast(intent);
	    }
	    
	    /* Parsing the raw file to string */
	    public static String parseFile(int rawId, Context context) {
	    	String json = null;
		    try {

		        InputStream is = context.getResources().openRawResource(rawId);
		        int size = is.available();
		        byte[] buffer = new byte[size];
		        is.read(buffer);
		        is.close();
		        json = new String(buffer, "UTF-8");


		    } catch (IOException ex) {
		        ex.printStackTrace();
		        return null;
		    }
		    return json;	
	    }
	
}

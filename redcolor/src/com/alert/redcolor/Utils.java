package com.alert.redcolor;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.channels.FileChannel;

import android.content.Context;
import android.content.Intent;
import android.os.Environment;

import com.alert.redcolor.db.RedColordb;

public class Utils {

	     
	    // give your server registration url here
	    //static final String SERVER_URL = "http://redalert-il.herokuapp.com/android_register_v1"; 
	
		//DEBUG VALUES
	    static final String SERVER_URL = "http://213.57.173.69:4567/android_register_v1";
	    
	    //static final String SERVER = "http://213.57.173.69:4567";
	    static final String SERVER = "http://redalert-il.herokuapp.com";
	    

	 
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
	    public static void backup(Context con) {
			File sd = Environment.getExternalStorageDirectory();

			if (sd.canWrite()) {
				File backupFile = null;
				File backupDir = new File(sd, "RED//backup//");
				if (!backupDir.exists())
					backupDir.mkdirs();

				String dbPath = con.getDatabasePath(RedColordb.DATABASE_NAME)
						.toString();
				String backupPath = "RED//backup//alerts.db";
				File dbFile = new File(dbPath);
				backupFile = new File(sd, backupPath);

				try {
					FileChannel src = new FileInputStream(dbFile).getChannel();
					FileChannel dst = new FileOutputStream(backupFile).getChannel();
					dst.transferFrom(src, 0, src.size());
					src.close();
					dst.close();
				} catch (Exception e) {

				}
			}
		}
}

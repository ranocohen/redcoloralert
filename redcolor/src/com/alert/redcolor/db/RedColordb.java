package com.alert.redcolor.db;



import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.alert.redcolor.R;



public class RedColordb extends SQLiteOpenHelper {
	// DATABASE Name
	public static final String DATABASE_NAME = "alerts.db";


	
	public interface Tables {

		public static final String ALERTS = "alerts";
		public static final String OREF_LOCATIONS = "oref";
		public static final String CITIES = "cities";

	}
	// Current Version
	private static final int DATABASE_VERSION = 1;

	/**
	 * private instance for the singleton pattern , See
	 * {@link #getInstance(android.content.Context ctx) } method.
	 */
	private static RedColordb mInstance = null;
	private Context mCon;


	public interface AlertColumns {
		public static final String ID = "_id";
		public static final String lat = "lat";
		public static final String lng = "lng";
		public static final String location = "location";
		public static final String time = "time";
	}
	public interface OrefColumns {
		public static final String ID = "_id";
		public static final String name = "name";
		public static final String index = "idx";
		
	}
	public interface CitiesColumns {
		public static final String ID = "_id";
		public static final String oref_id = "oref_id";
		public static final String name_he = "name_he";
		public static final String name_en = "name_en";
		public static final String lat = "lat";
		public static final String lng = "lng";
		public static final String time = "time";
		
	}
	/**
	 * Returning instance of helper , using the singleton pattern and
	 * application context to avoid memory leaks
	 */
	public static RedColordb getInstance(Context ctx) {

		// don't accidentally leak an Activity's context.
		// See this article for more information: http://bit.ly/6LRzfx
		if (mInstance == null) {
			mInstance = new RedColordb(ctx.getApplicationContext());
			mInstance.mCon = ctx;
		}
		return mInstance;
	}

	/** Create a helper object for the Work database */
	private RedColordb(Context ctx) {
		super(ctx, DATABASE_NAME, null, DATABASE_VERSION);

	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		
		/* Creating tables */
		db.execSQL("CREATE TABLE " + Tables.ALERTS + " ("
				+ AlertColumns.ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
				+ AlertColumns.lat + " REAL, "
				+ AlertColumns.lng + " REAL, "
				+ AlertColumns.location + " TEXT, "
				+ AlertColumns.time + " TEXT);");
		
		
		db.execSQL("CREATE TABLE " + Tables.OREF_LOCATIONS + " ("
				+ OrefColumns.ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
				+ OrefColumns.index + " INTEGER, "				
				+ OrefColumns.name + " TEXT);");
		
		
		db.execSQL("CREATE TABLE " + Tables.CITIES + " ("
				+ CitiesColumns.ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
				+ CitiesColumns.lat + " REAL, "
				+ CitiesColumns.lng + " REAL, "
				+ CitiesColumns.name_en + " TEXT, "
				+ CitiesColumns.name_he + " TEXT, "
				+ CitiesColumns.oref_id + " INTEGER, "
				+ CitiesColumns.time + " TEXT);");
		
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		// TODO Auto-generated method stub
		
	}
	
	public static void initData(Context mCon) {
/* Inserting data to oref and cities tables from lidan's json file*/
		
		InputStreamReader isr = new InputStreamReader(
					mCon.getResources().openRawResource(R.raw.redalert_en));
		BufferedReader reader = new BufferedReader(isr);
		SortedSet<String> oref_loc = new TreeSet<String>();
		    try {
		        String line;
		        while ((line = reader.readLine()) != null ) {
		        	
		        	if(line.length()<=3)
		        		break;
		        		
		             String[] data = line.split(",(?=([^\"]*\"[^\"]*\")*[^\"]*$)");
		             
		             String he_name = data[1];
		             String en_name = data[2];
		          
		           
		             String oref_loc_str = data[3];
		             Pattern pattern = Pattern.compile("^(.*)\\s(\\d*)(\\s(.*))?$");
		             
			         Matcher matcher = pattern.matcher(oref_loc_str.trim());
			         
			            
			            String num = "";
			           
			            while (matcher.find()) 
			                num = matcher.group(2);
			         int oref_idx=0;   
			         
			         if(num.equals(""))   
			        	 oref_idx = 1;
			         else
			         oref_idx = Integer.parseInt(num);
		             String time = data[4];
		             Double lat = Double.parseDouble(data[5]);
		             Double lng = Double.parseDouble(data[6]);
		             
		             ContentValues cityCv = new ContentValues();
		             cityCv.put(CitiesColumns.lat, lat);
		             cityCv.put(CitiesColumns.lng , lng);
		             cityCv.put(CitiesColumns.name_he, he_name);
		             cityCv.put(CitiesColumns.name_en , en_name);
		             cityCv.put(CitiesColumns.oref_id, oref_idx);
		             cityCv.put(CitiesColumns.time, time);
		            
						
					mCon.getContentResolver().insert(
								AlertProvider.CITIES_CONTENT_URI, cityCv);
					
					//Avoid duplicates of pikud areas   
					oref_loc.add(oref_loc_str);
						
		            
		        }
		        for(String curr : oref_loc) {
		        	
		        	ContentValues orefCv = new ContentValues();
		        	Pattern pattern = Pattern.compile("^(.*)\\s(\\d*)(\\s(.*))?$");
		            Matcher matcher = pattern.matcher(curr.trim());

		            String area = "";
		            String num = "";

		            while (matcher.find()) {
		            	area = matcher.group(1);
			            num = matcher.group(2);
		            }
		            

		             int oref_idx= Integer.parseInt(num);
		             
		        	orefCv.put(OrefColumns.index, oref_idx);
		        	orefCv.put(OrefColumns.name, area);
		        	
		        	mCon.getContentResolver().insert(
							AlertProvider.OREF_CONTENT_URI, orefCv);
		        	   
		        }
		    }
		    catch (IOException ex) {
		        // handle exception
		    }
		    finally {
		        try {
		           reader.close();
		           isr.close();
		        }
		        catch (IOException e) {
		            // handle exception
		        }
		    }
	}
}
package com.alert.redcolor.db;



import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;



public class RedColordb extends SQLiteOpenHelper {
	// DATABASE Name
	public static final String DATABASE_NAME = "alerts.db";

	
	// Table name
	public static final String TABLE_NAME = "alerts";
	
	
	// Context
	private Context con;

	// Current Version
	private static final int DATABASE_VERSION = 1;

	/**
	 * private instance for the singleton pattern , See
	 * {@link #getInstance(android.content.Context ctx) } method.
	 */
	private static RedColordb mInstance = null;



	public interface Columns {
		public static final String ID = "_id";
		public static final String xCord = "x";
		public static final String yCord = "y";
		public static final String location = "location";
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
			mInstance.con = ctx;
		}
		return mInstance;
	}

	/** Create a helper object for the Work database */
	private RedColordb(Context ctx) {
		super(ctx, DATABASE_NAME, null, DATABASE_VERSION);

	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		db.execSQL("CREATE TABLE " + TABLE_NAME + " ("
				+ Columns.ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
				+ Columns.xCord + " INTEGER, "
				+ Columns.yCord + " INTEGER, "
				+ Columns.location + " TEXT, "
				+ Columns.time + " TEXT);");
				
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		// TODO Auto-generated method stub
		
	}
	
	
}
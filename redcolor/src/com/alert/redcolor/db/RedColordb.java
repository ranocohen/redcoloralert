package com.alert.redcolor.db;



import java.util.ArrayList;
import java.util.Locale;



import android.app.ActionBar.Tab;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.graphics.Color;
import android.preference.PreferenceManager;
import android.text.TextUtils;



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
		public static final String xCord = "work_id";
		public static final String yCord = "date";
		public static final String time = "start";
		
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
				+ Columns.yCord + " TEXT, "
				+ Columns.time + " TEXT);");
				
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		// TODO Auto-generated method stub
		
	}

	
}
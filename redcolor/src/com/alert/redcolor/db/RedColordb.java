package com.alert.redcolor.db;

import java.util.ArrayList;

import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class RedColordb extends SQLiteOpenHelper {
	// DATABASE Name
	public static final String DATABASE_NAME = "alerts.db";

	public interface Tables {

		public static final String ALERTS = "alerts";
		public static final String OREF_LOCATIONS = "oref";
		public static final String CITIES = "cities";

	}

	// Current Version
	private static final int DATABASE_VERSION = 2;

	/**
	 * private instance for the singleton pattern , See
	 * {@link #getInstance(android.content.Context ctx) } method.
	 */
	private static RedColordb mInstance = null;
	private Context mCon;

	public interface AlertColumns {
		public static final String ID = "_id";
		public static final String AreaId = "area_id";
		public static final String time = "time";
		public static final String painted = "painted";
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
		db.execSQL("CREATE TABLE " + Tables.ALERTS + " (" + AlertColumns.ID
				+ " INTEGER PRIMARY KEY AUTOINCREMENT, " + AlertColumns.AreaId
				+ " INTEGER, " + AlertColumns.painted + " INTEGER, "
				+ AlertColumns.time + " TEXT);");

		db.execSQL("CREATE TABLE " + Tables.OREF_LOCATIONS + " ("
				+ OrefColumns.ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
				+ OrefColumns.index + " INTEGER, " + OrefColumns.name
				+ " TEXT);");

		db.execSQL("CREATE TABLE " + Tables.CITIES + " (" + CitiesColumns.ID
				+ " INTEGER PRIMARY KEY AUTOINCREMENT, " + CitiesColumns.lat
				+ " REAL, " + CitiesColumns.lng + " REAL, "
				+ CitiesColumns.name_en + " TEXT, " + CitiesColumns.name_he
				+ " TEXT, " + CitiesColumns.oref_id + " INTEGER, "
				+ CitiesColumns.time + " TEXT);");

	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		// If you need to add a new column
		if (oldVersion < 2) {
			db.execSQL("ALTER TABLE " + Tables.ALERTS + " ADD COLUMN "
					+ AlertColumns.painted + " INTEGER");
			db.execSQL("UPDATE " + Tables.ALERTS + " SET "
					+ AlertColumns.painted + " = 1");
		}

	}
	/* Keeping only 50 entries in db */
	public void cleanDatabase() {
		Cursor c = null;
		try {

			SQLiteDatabase db = getWritableDatabase();

			c = db.rawQuery("select * from alerts where _id not in "
					+ " (select _id from alerts order by time desc limit 50)",
					null);
			Log.i("DB CLEAN", "DELETED "+c.getCount() +" ENTRIES");
			while (c.moveToNext()) {
				long id = c.getLong(0);
				mCon.getContentResolver().delete(
						ContentUris.withAppendedId(
								AlertProvider.ALERTS_CONTENT_URI, id), null,
						null);
			}

		} catch (SQLException e) {

		} finally {
			if (c != null)
				c.close();
		}
		
	}
	/* Keeping only 50 entries in db */
	public void updatePainted() {
		
		ProviderQueries pq = new ProviderQueries(mCon);
		ArrayList<Long> alerts = pq.latestAlerts();
		
		for(Long id : alerts) {
			ContentValues updatedValues = new ContentValues();
			updatedValues.put(AlertColumns.painted, 0);
			mCon.getContentResolver().update(
					ContentUris.withAppendedId(AlertProvider.ALERTS_CONTENT_URI, id),   // the user dictionary content URI
				    updatedValues ,                      // the columns to update
				    null,         // the column to select on
				    null // the value to compare to
				);
	
		}
		
		
	}
	
}
package com.alert.redcolor.db;

import com.alert.redcolor.db.RedColordb.AlertColumns;
import com.alert.redcolor.db.RedColordb.CitiesColumns;
import com.alert.redcolor.db.RedColordb.OrefColumns;
import com.alert.redcolor.db.RedColordb.Tables;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.provider.SyncStateContract.Columns;
import android.text.TextUtils;



public class AlertProvider extends ContentProvider {
    private RedColordb dbHelper;

    // unique symbolic name of the provider
    private static final String AUTHORITY = "com.alert.redcolor.provider";

    // Content URI's

    //Alerts table Content URI
    public static final Uri ALERTS_CONTENT_URI =
            Uri.parse("content://" + AUTHORITY + "/alerts");
    
    //Alerts table Content URI
    public static final Uri OREF_CONTENT_URI =
            Uri.parse("content://" + AUTHORITY + "/orefs");
    
    //Alerts table Content URI
    public static final Uri CITIES_CONTENT_URI =
            Uri.parse("content://" + AUTHORITY + "/cities");

    //Alerts table
    private static final int ALERTS = 1;
    private static final int SINGLE_ALERT = 2;
    private static final int CITIES = 3;
    private static final int SINGLE_CITY = 4;
    private static final int OREFS = 5;
    private static final int SINGLE_OREF = 6;
    // system calls onCreate() when it starts up the provider.
    @Override
    public boolean onCreate() {
        // get access to the database helper
        dbHelper = RedColordb.getInstance(getContext());
        return false;
    }

    // a content URI pattern matches content URIs using wildcard characters:
    // *: Matches a string of any valid characters of any length.
    // #: Matches a string of numeric characters of any length.
    private static final UriMatcher uriMatcher;

    static {
        uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
        uriMatcher.addURI(AUTHORITY, "alerts", ALERTS);
        uriMatcher.addURI(AUTHORITY, "alerts/#", SINGLE_ALERT);
        uriMatcher.addURI(AUTHORITY, "cities", CITIES);
        uriMatcher.addURI(AUTHORITY, "cities/#", SINGLE_CITY);
        uriMatcher.addURI(AUTHORITY, "orefs", OREFS);
        uriMatcher.addURI(AUTHORITY, "orefs/#", SINGLE_OREF);
        
    }

    //Return the MIME type corresponding to a content URI
    @Override
    public String getType(Uri uri) {

        switch (uriMatcher.match(uri)) {
            case ALERTS:
                return "vnd.android.cursor.dir/vnd.com.alert.redcolor.provider.alerts";
            case SINGLE_ALERT:
                return "vnd.android.cursor.item/vnd.com.alert.redcolor.provider.alerts";
            case CITIES:
                return "vnd.android.cursor.dir/vnd.com.alert.redcolor.provider.cities";
            case SINGLE_CITY:
                return "vnd.android.cursor.item/vnd.com.alert.redcolor.provider.cities";
            case OREFS:
                return "vnd.android.cursor.dir/vnd.com.alert.redcolor.provider.orefs";
            case SINGLE_OREF:
                return "vnd.android.cursor.item/vnd.com.alert.redcolor.provider.orefs";
                        default:
                throw new IllegalArgumentException("Unsupported URI: " + uri);
        }
    }

    // The insert() method adds a new row to the appropriate table, using the values
    // in the ContentValues argument. If a column name is not in the ContentValues argument,
    // you may want to provide a default value for it either in your provider code or in
    // your database schema.
    @Override
    public Uri insert(Uri uri, ContentValues values) {
        long id;

        switch (uriMatcher.match(uri)) {
            case ALERTS:
                id = insertAlert(values);
                break;
            case CITIES:
                id = insertCity(values);
                break;
            case OREFS:
                id = insertOref(values);
                break;
            default:
                throw new IllegalArgumentException("Unsupported URI: " + uri);
        }

        getContext().getContentResolver().notifyChange(uri, null);

        if (id < 0)
            return null;

        return ContentUris.withAppendedId(uri, id);
    }

    
    private long insertAlert(ContentValues values) {
        long id;
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        id = db.insert(Tables.ALERTS, null,values);
        return id;
    }
    private long insertCity(ContentValues values) {
        long id;
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        id = db.insert(Tables.CITIES, null,values);
        return id;
    }
    private long insertOref(ContentValues values) {
        long id;
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        id = db.insert(Tables.OREF_LOCATIONS, null,values);
        return id;
    }


   
    // The query() method must return a Cursor object, or if it fails,
    // throw an Exception. If you are using an SQLite database as your data storage,
    // you can simply return the Cursor returned by one of the query() methods of the
    // SQLiteDatabase class. If the query does not match any rows, you should return a
    // Cursor instance whose getCount() method returns 0. You should return null only
    // if an internal error occurred during the query process.
    @Override
    public Cursor query(Uri uri, String[] projection, String selection,
                        String[] selectionArgs, String sortOrder) {

        SQLiteDatabase db = dbHelper.getWritableDatabase();
        SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
        String id;
        switch (uriMatcher.match(uri)) {

            
            case ALERTS:
                qb.setTables(Tables.ALERTS);
                break;
            case SINGLE_ALERT:
                qb.setTables(Tables.ALERTS);
                id = uri.getLastPathSegment();
                qb.appendWhere(AlertColumns.ID + "=" + id);
                break;
            case CITIES:
                qb.setTables(Tables.CITIES);
                break;
            case SINGLE_CITY:
                qb.setTables(Tables.CITIES);
                id = uri.getLastPathSegment();
                qb.appendWhere(CitiesColumns.ID + "=" + id);
                break;
            case OREFS:
                qb.setTables(Tables.OREF_LOCATIONS);
                break;
            case SINGLE_OREF:
                qb.setTables(Tables.OREF_LOCATIONS);
                id = uri.getLastPathSegment();
                qb.appendWhere(OrefColumns.ID + "=" + id);
                break;
                default:
                throw new IllegalArgumentException("Unsupported URI: " + uri);
        }

        Cursor cursor = qb.query(db, projection, selection,
                selectionArgs, null, null, sortOrder);
        
        cursor.setNotificationUri(getContext().getContentResolver(), uri);
        
     
        return cursor;

    }

    // The delete() method deletes rows based on the seletion or if an id is
    // provided then it deleted a single row. The methods returns the numbers
    // of records delete from the database. If you choose not to delete the data
    // physically then just update a flag here.
    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        String id;
        String select;
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        int deleteCount = 0;
        switch (uriMatcher.match(uri)) {

            case ALERTS:
                deleteCount = db.delete(Tables.ALERTS, selection, selectionArgs);
                break;
            case SINGLE_ALERT:
                id = uri.getPathSegments().get(1);
                selection = AlertColumns.ID+ "=" + id
                        + (!TextUtils.isEmpty(selection) ?
                        " AND (" + selection + ')' : "");
                deleteCount = db.delete(Tables.ALERTS, selection, selectionArgs);
                
        		
                break;
           
            default:
                throw new IllegalArgumentException("Unsupported URI: " + uri);


        }


        getContext().getContentResolver().notifyChange(uri, null);
        
 
        return deleteCount;
    }

    // The update method() is same as delete() which updates multiple rows
    // based on the selection or a single row if the row id is provided. The
    // update method returns the number of updated rows.
    @Override
    public int update(Uri uri, ContentValues values, String selection,
                      String[] selectionArgs) {
        String id;
        int updateCount = 0;
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        switch (uriMatcher.match(uri)) {
            case ALERTS:
                //do nothing
                break;
            case SINGLE_ALERT:
                id = uri.getPathSegments().get(1);
                selection = RedColordb.AlertColumns.ID+ "=" + id
                        + (!TextUtils.isEmpty(selection) ?
                        " AND (" + selection + ')' : "");
                updateCount = db.update(Tables.ALERTS, values, selection, selectionArgs);
                
                break;
                        default:
                throw new IllegalArgumentException("Unsupported URI: " + uri);
        }


        getContext().getContentResolver().notifyChange(uri, null);
  
        return updateCount;
    }



}

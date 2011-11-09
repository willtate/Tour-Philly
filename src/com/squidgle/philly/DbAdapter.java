package com.squidgle.philly;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class DbAdapter {
	
	public static final String KEY_ROWID = "_id";
	public static final String KEY_LOC = "location";
	public static final String KEY_DESC = "description";
	public static final String KEY_LAT = "latitude";
	public static final String KEY_LONG = "longitude";
	
	public static final String TAG = "Squidgle.Philly";
	
    private static final String DATABASE_NAME = "data";
    private static final String DATABASE_TABLE = "locations";
    private static final int DATABASE_VERSION = 1;
    
    private static final String DATABASE_CREATE =
            "create table notes (_id integer primary key autoincrement, "
            + "location text not null, "
            + "description text not null, "
            + "latitude real not null, "
            + "longitude real not null";
        
        private static final String DATABASE_UPGRADE =
        	"notes (_id integer primary key autoincrement, "
        	+ "location text not null, "
        	+ "description text not null, "
        	+ "latitude real not null, "
        	+ "longitude real not null";
	
	private DatabaseHelper mDbHelper;
	private SQLiteDatabase mDb;
	private final Context mCtx;
	
	private static class DatabaseHelper extends SQLiteOpenHelper {

        DatabaseHelper(Context context) {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {

            db.execSQL(DATABASE_CREATE);
        }
        
        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            Log.w(TAG, "Upgrading database from version " + oldVersion + " to " + newVersion);
            
            db.beginTransaction();
            try {
            	db.execSQL("CREATE TABLE IF NOT EXISTS " + DATABASE_UPGRADE);
            	List<String> columns = GetColumns(db, DATABASE_TABLE);
            	db.execSQL("ALTER table " + DATABASE_TABLE + " RENAME TO 'temp_" + DATABASE_TABLE + "'");
            	db.execSQL("create table " + DATABASE_UPGRADE);
            	columns.retainAll(GetColumns(db, DATABASE_TABLE));
            	String cols = join(columns, ","); 
            	db.execSQL(String.format( "INSERT INTO %s (%s) SELECT %s from temp_%s", DATABASE_TABLE, cols, cols, DATABASE_TABLE));
            	db.execSQL("DROP table 'temp_" + DATABASE_TABLE + "'");
            	db.setTransactionSuccessful();
            } finally {
            	db.endTransaction();
            }
        }
		
	}
	
	public static List<String> GetColumns(SQLiteDatabase db, String tableName) {
        List<String> ar = null;
        Cursor c = null;
        try {
            c = db.rawQuery("select * from " + tableName + " limit 1", null);
            if (c != null) {
                ar = new ArrayList<String>(Arrays.asList(c.getColumnNames()));
            }
        } catch (Exception e) {
            Log.v(tableName, e.getMessage(), e);
            e.printStackTrace();
        } finally {
            if (c != null)
                c.close();
        }
        return ar;
    }

    public static String join(List<String> list, String delim) {
        StringBuilder buf = new StringBuilder();
        int num = list.size();
        for (int i = 0; i < num; i++) {
            if (i != 0)
                buf.append(delim);
            buf.append((String) list.get(i));
        }
        return buf.toString();
    }
    
    /**
     * Constructor - takes the context to allow the database to be
     * opened/created
     * 
     * @param ctx the Context within which to work
     */
    public DbAdapter(Context ctx) {
        this.mCtx = ctx;
    }
    
    /**
     * Open the location database. If it cannot be opened, try to create a new
     * instance of the database. If it cannot be created, throw an exception to
     * signal the failure
     * 
     * @return this (self reference, allowing this to be chained in an
     *         initialization call)
     * @throws SQLException if the database could be neither opened or created
     */
    public DbAdapter open() throws SQLException {
        mDbHelper = new DatabaseHelper(mCtx);
        mDb = mDbHelper.getWritableDatabase();
        return this;
    }
    
    /**
     * Close the location database.
     */

    public void close() {
        mDbHelper.close();
    }
    
    /**
     * Return a Cursor over the list of all locations in the database
     * 
     * @return Cursor over all locations
     */
    public Cursor fetchAllItems(String orderBy) {
        return mDb.query(DATABASE_TABLE, new String[] {
        		KEY_ROWID, KEY_LOC, KEY_DESC, KEY_LAT, KEY_LONG}, 
                null, null, null, null, orderBy);
    }

    /**
     * Return a Cursor positioned at the location that matches the given rowId
     * 
     * @param rowId id of location to retrieve
     * @return Cursor positioned to matching note, if found
     * @throws SQLException if note could not be found/retrieved
     */
    public Cursor fetchItem(long rowId) throws SQLException {

        Cursor cursor = mDb.query(true, DATABASE_TABLE, new String[] {
        		KEY_ROWID, KEY_LOC, KEY_DESC, KEY_LAT, KEY_LONG},
                KEY_ROWID + "=" + rowId, 
                null, null, null, null, null);
        if (cursor != null) {
            cursor.moveToFirst();
        }
        return cursor;
    }
}

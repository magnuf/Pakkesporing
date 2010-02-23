package com.krashk.pakketracker;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class PackagesDbAdapter {

	public static final String KEY_NUMBER = "trackingnumber";
	public static final String KEY_STATUS = "status";
	public static final String KEY_ID = "_id";
	public static final String KEY_CHANGED = "changed";

	private static final String TAG = "PackagesDbAdapter";
	private DatabaseHelper dbHelper;
	private SQLiteDatabase db;

	/**
	 * Database creation sql statement
	 */
	private static final String DATABASE_CREATE =
		"create table packages (_id integer primary key autoincrement, "
		+ "trackingnumber text not null, status text, changed integer);";

	private static final String DATABASE_NAME = "pakketracker";
	private static final String DATABASE_TABLE = "packages";
	private static final int DATABASE_VERSION = 2;

	private final Context ctx;

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
			Log.w(TAG, "Upgrading database from version " + oldVersion + " to "
					+ newVersion + ", which will destroy all old data");
			db.execSQL("DROP TABLE IF EXISTS packages");
			onCreate(db);
		}
	}

	/**
	 * Constructor - takes the context to allow the database to be
	 * opened/created
	 * 
	 * @param ctx the Context within which to work
	 */
	public PackagesDbAdapter(Context ctx) {
		this.ctx = ctx;
	}

	/**
	 * Open the packages database. If it cannot be opened, try to create a new
	 * instance of the database. If it cannot be created, throw an exception to
	 * signal the failure
	 * 
	 * @return this (self reference, allowing this to be chained in an
	 *         initialization call)
	 * @throws SQLException if the database could be neither opened or created
	 */
	public PackagesDbAdapter open() throws SQLException {
		dbHelper = new DatabaseHelper(ctx);
		db = dbHelper.getWritableDatabase();
		return this;
	}

	public void close() {
		dbHelper.close();
	}


	/**
	 * Create a new package using the trackingnumber provided. If the package is
	 * successfully created return the new id for that package, otherwise return
	 * a -1 to indicate failure.
	 * 
	 * @param trackingnumber the trackingnumber of the package
	 * @return id or -1 if failed
	 */
	public long createPackage(String trackingnumber) {
		ContentValues initialValues = new ContentValues();
		initialValues.put(KEY_NUMBER, trackingnumber);

		return db.insert(DATABASE_TABLE, null, initialValues);
	}

	/**
	 * Delete the package with the given id
	 * 
	 * @param id id of package to delete
	 * @return true if deleted, false otherwise
	 */
	public boolean deletePackage(long id) {

		return db.delete(DATABASE_TABLE, KEY_ID + "=" + id, null) > 0;
	}

	/**
	 * Return a Cursor over the list of all packages in the database
	 * 
	 * @return Cursor over all packages
	 */
	public Cursor fetchAllPackages() {

		return db.query(DATABASE_TABLE, new String[] {KEY_ID, KEY_NUMBER,
				KEY_STATUS, KEY_CHANGED}, null, null, null, null, KEY_ID+ " ASC");
	}
	/**
	 * Returns the number of packages in the db
	 * 
	 * @return Number of packages
	 */
	public int getNumPackages() {
		return db.query(DATABASE_TABLE, new String[] {KEY_ID, KEY_NUMBER,
				KEY_STATUS, KEY_CHANGED}, null, null, null, null, null).getCount();
	}

	/**
	 * Return a Cursor positioned at the package that matches the given id
	 * 
	 * @param id id of package to retrieve
	 * @return Cursor positioned to matching package, if found
	 * @throws SQLException if package could not be found/retrieved
	 */
	public Cursor fetchPackage(long id) throws SQLException {

		Cursor mCursor =

			db.query(true, DATABASE_TABLE, new String[] {KEY_ID,
					KEY_NUMBER, KEY_STATUS, KEY_CHANGED}, KEY_ID + "=" + id, null,
					null, null, null, null);
		if (mCursor != null) {
			mCursor.moveToFirst();
		}
		return mCursor;

	}

	/**
	 * Update the package using the details provided. The package to be updated is
	 * specified using the id, and it is altered to use the status and changed
	 * values passed in
	 * 
	 * @param id id of package to update
	 * @param packagenumber value to set packagenumber to
	 * @param status value to set package status to
	 * @param changed Says if there is a new status, and what kind of status (new location, broken package etc)
	 * @return true if the package was successfully updated, false otherwise
	 */
	public boolean updatePackage(long id, String status, int changed) {
		ContentValues args = new ContentValues();
		args.put(KEY_CHANGED, changed);
		args.put(KEY_STATUS, status);

		return db.update(DATABASE_TABLE, args, KEY_ID + "=" + id, null) > 0;
	}
}


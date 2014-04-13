package com.exercise.cc.dbHandling;


import java.util.Map;
import java.util.Vector;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;


/**
 * Sqlite Database handling and maintaining class, 
 * for storing and retreaving currencies
 * @author Matti
 *
 */
public class DbHandler extends SQLiteOpenHelper{

    public static final String KEY_NAME = "name";
    public static final String KEY_SHORT_NAME = "shortname";
    public static final String KEY_RATE = "rate";
    public static final String KEY_FAVOURITE = "favourite";
    public static final String KEY_TIME = "unixtime";
    public static final String KEY_ROWID = "_id";
	
	public SQLiteDatabase mDb;
	private static final String DATABASE_NAME = "rates";
    private static final String DATABASE_TABLE = "currency";
    private static final int DATABASE_VERSION = 4;
    private static final String DATABASE_CREATE =
    	"create table currency (_id integer primary key autoincrement, "
    	+ "shortname text not null, "
    	+ "name text not null, "
    	+ "rate float not null, "
    	+ "favourite integer, "
    	+" unixtime integer not null);";
    
    /**
     * Singleton instance for db handler 
     */
    private static DbHandler mSingletonDB = null;
    
    /**
     * Method for retreaving singleton instance
     * @param context
     * @return
     */
    public static DbHandler getInstance(Context context){
    	if(mSingletonDB==null){
    		mSingletonDB = new DbHandler(context);
    	}
    	return mSingletonDB;
    }
    
    /**
     * Private constructor (for making the class singleton)
     * @param context
     */
    private DbHandler(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		db.execSQL(DATABASE_CREATE);
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		db.execSQL("DROP TABLE IF EXISTS currency");
		onCreate(db);
	}

	/**
	 * Opening the database
	 * @return
	 * @throws SQLException
	 */
    public DbHandler open() throws SQLException {
        mDb = this.getWritableDatabase();
        return this;
    }

    /**
     * Method for adding currency to db
     * @param c
     * @return
     */
	public long addCurrency(Currency c) {
		ContentValues values = currencyToContentValues(c, false);        
		return mDb.insert(DATABASE_TABLE, null, values);
	}
	
	/**
	 * Removing currency
	 * @param shortName
	 * @return
	 */
	public long removeCurrencyWithShortName(String shortName) {
		return mDb.delete(DATABASE_TABLE, KEY_SHORT_NAME + "='" + shortName + "'", null);
	}
	
	/**
	 * For updating currency
	 * @param c
	 * @return
	 */
	public long updateCurrency(Currency c) {
		ContentValues values = currencyToContentValues(c, true);
		return mDb.update(DATABASE_TABLE, values, KEY_SHORT_NAME + "='" + c.mShortName + "'", null);
	}
	
	/**
	 * Retrieving currency with ISO 4217 standard abbreviation
	 * @param shortName
	 * @return
	 */
	public Currency getCurrencyWithShortName(String shortName){
		Cursor cr = mDb.query(DATABASE_TABLE, 
				new String[] { KEY_NAME, KEY_SHORT_NAME, KEY_RATE, KEY_TIME, KEY_FAVOURITE}, 
				KEY_SHORT_NAME + "='" + shortName + "'", null, null, null, null);
		cr.moveToFirst();
		Currency c = currentCursorToCurrency(cr);
		cr.close();
		return c;
	}

	private Currency currentCursorToCurrency(Cursor cr)
	{
		Currency c = new Currency();
		c.mName = getStringColumnValue(cr, KEY_NAME);
		c.mShortName = getStringColumnValue(cr, KEY_SHORT_NAME);
		c.mRate = getFloatColumnValue(cr, KEY_RATE);
		c.mUnixTime = getIntColumnValue(cr, KEY_TIME);
		if(getIntColumnValue(cr, KEY_FAVOURITE)==1) {c.mFavourite=true;} else {c.mFavourite=false;}
		return c;
	}
	
	private String getStringColumnValue(Cursor cr, String cname){
		return cr.getString(cr.getColumnIndex(cname));
	}
	private long getLongColumnValue(Cursor cr, String cname){
		return cr.getLong(cr.getColumnIndex(cname));
	}
	private float getFloatColumnValue(Cursor cr, String cname){
		return cr.getFloat(cr.getColumnIndex(cname));		
	}
	private long getIntColumnValue(Cursor cr, String cname){	
		return cr.getInt(cr.getColumnIndex(cname));
	}
	
	/**
	 * Helper method for converting currency to ContentValues format
	 * @param c
	 * @param update
	 * @return
	 */
	public ContentValues currencyToContentValues(Currency c, boolean update){
        ContentValues initialValues = new ContentValues();
        initialValues.put(KEY_NAME, c.mName);
        initialValues.put(KEY_SHORT_NAME, c.mShortName);
        initialValues.put(KEY_RATE, c.mRate);
        if(!update)// do not override favourites
        	initialValues.put(KEY_FAVOURITE, c.mFavourite);  
        initialValues.put(KEY_TIME, c.mUnixTime);
        return initialValues;
	}
	
	/**
	 *  Return currency short names 
	 */
	public Vector<String> getCurrencyShortNames(Boolean favourites) {
		Cursor cr = null;
		if(!mDb.isOpen()){
			this.open();
		}
		try {
			String selection = null;
			if(favourites==true) selection = "favourite=1";
			cr = mDb.query(DATABASE_TABLE, 
					new String[] { KEY_SHORT_NAME }, 
					selection, null, null, null, KEY_SHORT_NAME);
		} catch (Exception e) {
			Log.w("CC", e.getMessage());
		}
		Vector<String> list = new Vector<String>();
		int ind = cr.getColumnIndex(KEY_SHORT_NAME);
		cr.moveToPosition(-1);
		while( cr.moveToNext()!=false){
			list.add(cr.getString(ind));
		}
		cr.close();
		return list;
	}

	/**
	 * Clears the db
	 */
	public void clearDb() {
		mDb.delete(DATABASE_TABLE, null, null);
	}

	/**
	 * Adding list of currencies to db, creates currency if it does not
	 * exist, updates otherwise
	 * @param rates
	 */
	public void addCurrencies(Map<String, Currency> rates) {
		for (String isoCode : rates.keySet()) {
			try {
				Currency c = rates.get(isoCode);
				c.mUnixTime = System.currentTimeMillis()/1000;
				if(c.mName==null){
					Log.w("CC","no name for " + isoCode);
					c.mName = "noname";
				}
				// check if currency exist
				if(checkIfExists(isoCode)){ // update
					ContentValues cv = currencyToContentValues(c, true);
					mDb.update(DATABASE_TABLE, cv, "shortname='" + isoCode + "'", null);
				} else { // insert
					ContentValues cv = currencyToContentValues(c, false);
					mDb.insert(DATABASE_TABLE, null, cv);
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
	
	/**
	 * Check if specified currency exists
	 * @param isoCode ISO 4217 standard 
	 * @return
	 */
	public Boolean checkIfExists(String isoCode){
		Cursor cr = mDb.query(DATABASE_TABLE, new String[] { KEY_SHORT_NAME }, "shortname='"+isoCode+"'", 
							  null, null, null, null, null);
		if(cr.getCount()!=0) { cr.close(); return true;}
		else { cr.close(); return false;}
	}
	
	/**
	 * Storing favourite accoding curerncy specified by ISO 4217 standard
	 * @param isoCode
	 * @param isFaw
	 */
	public void setFavourite(String isoCode, Boolean isFaw){
		ContentValues values = new ContentValues();
		if(isFaw){
			values.put(KEY_FAVOURITE, 1);
		} else {
			values.put(KEY_FAVOURITE, 0);
		}
		mDb.update(DATABASE_TABLE, values, "shortname='" + isoCode+ "'" , null);
	}

	/**
	 * Return all currencies
	 * @return
	 */
	public Vector<Currency> getCurrencies() {
		Vector<Currency> currencies = new Vector<Currency>();
		Cursor cr = mDb.query(DATABASE_TABLE, new String[] { KEY_NAME, KEY_SHORT_NAME, KEY_RATE, KEY_TIME, KEY_FAVOURITE}, null, null, null, null, KEY_SHORT_NAME);
		cr.moveToPosition(-1);
		while (cr.moveToNext())
		{
			currencies.add(currentCursorToCurrency(cr));
		}
		cr.close();
		return currencies;
	}
	
	/**
	 * Return all favourite currencies
	 * @return
	 */
	public Vector<Currency> getFavCurrencies(){
		Vector<Currency> currencies = new Vector<Currency>();
		Cursor cr = mDb.query(DATABASE_TABLE, new String[] { KEY_NAME, KEY_SHORT_NAME, KEY_RATE, KEY_TIME, KEY_FAVOURITE}, "favourite=1", null, null, null, KEY_SHORT_NAME);
		cr.moveToPosition(-1);
		while (cr.moveToNext())
		{
			currencies.add(currentCursorToCurrency(cr));
		}
		cr.close();
		return currencies;		
	}
	
	/**
	 * Set currency, update if exist, add new one otherwise
	 * @param c
	 * @return
	 */
	public Boolean setCurrency(Currency c){
		if(checkIfExists(c.mShortName)){
			this.updateCurrency(c);
		} else {
			this.addCurrency(c);
		}
		return true;
	}

	/**
	 * Store currency rate
	 * @param isoCode
	 * @param rate
	 */
	public void storeRate(String isoCode, float rate) {
		long utime = System.currentTimeMillis()/1000;
		if(checkIfExists(isoCode)){ // update rate and time
			 ContentValues cv = new ContentValues();
			 cv.put(KEY_RATE, rate);
			 cv.put(KEY_TIME, utime);
			 mDb.update(DATABASE_TABLE, cv, KEY_SHORT_NAME + "='" + isoCode + "'", null);
		} else { // should never happen
			Currency c = new Currency();
			c.mFavourite = false;
			c.mName = "noname";
			c.mShortName = isoCode;
			c.mRate = rate;
			c.mUnixTime = utime;
			mDb.insert(DATABASE_TABLE, null, currencyToContentValues(c, true));
		}
	}
}
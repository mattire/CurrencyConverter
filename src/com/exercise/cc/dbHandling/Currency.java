package com.exercise.cc.dbHandling;

/**
 * Currency helper class for handling currencies
 * @author Matti
 *
 */
public class Currency {

	/**
	 * Abbreviation for currency according to ISO 4217 standard
	 */
	public String mShortName;
	
	/**
	 * Long name for currency
	 */
	public String mName;
	
	/**
	 *  rate in dollars 
	 */
	public float mRate;
	
	/**
	 * For marking users favourites 
	 */
	public Boolean mFavourite = false;
	
	/**
	 * Currency update time (not used)
	 */
	public long mUnixTime;
}

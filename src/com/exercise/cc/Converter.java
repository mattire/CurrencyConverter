package com.exercise.cc;

import android.content.Context;
import android.content.SharedPreferences;

import com.exercise.cc.dbHandling.Currency;
import com.exercise.cc.dbHandling.DbHandler;
import com.exercise.cc.webHandling.UpdateCurrenciesTask;
import com.exercise.cc.webHandling.WebLogic;

/**
 * Class that does conversion or passes conversion task to passes
 * conversion task to UpdateCurrenciesTask
 * @author Matti
 *
 */
public class Converter 
{	
	DbHandler mDbHandler; 
	WebLogic mWebLogic;
	private IUpdateResultHandler mUpdateHandler;
	private ConverterApplication mCApp;
	private Context mCtx;
	
	
	/**
	 * Constructor
	 * @param dh database instance
	 * @param wl weblogic instance
	 * @param urh handler for conversion result
	 */
	public Converter(DbHandler dh, WebLogic wl, IUpdateResultHandler urh, ConverterApplication capp, Context ctx){
		mDbHandler = dh;
		mWebLogic = wl;
		mUpdateHandler = urh;
		mCApp = capp;
		mCtx = ctx;
	}
	
	/**
	 * Method for doing the conversion
	 * @param amount 
	 * @param currencyFrom
	 * @param currencyTo
	 * @param settings application settings
	 */
	public void convert(float amount, String currencyFrom, String currencyTo, SharedPreferences settings){
		float fFrom = 0.0F;
		float fTo = 0.0F;
		float rate = 0.0F;
		if(settings.getInt(SettingsActivity.PREFS_MODE, SettingsActivity.PREFS_MODE_FETCH_ON_UPDATE_USE_JS)
				!=SettingsActivity.PREFS_MODE_FETCH_ON_CONVERT)
		{
			Currency CFrom = mDbHandler.getCurrencyWithShortName(currencyFrom);
			Currency CTo = mDbHandler.getCurrencyWithShortName(currencyTo);
			fFrom = CFrom.mRate;
			fTo = CTo.mRate;
			rate = fFrom/fTo;
			mUpdateHandler.handleConversionResult(rate * amount);
		} else {
			UpdateCurrenciesTask task = 
				new UpdateCurrenciesTask(mWebLogic, 
										 mUpdateHandler, 
										 mCtx, 
										 mCApp);
			task.execute("getWebRate", currencyFrom, currencyTo, Float.toString(amount));
		}
	}
	
}

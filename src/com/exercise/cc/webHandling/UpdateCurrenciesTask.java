package com.exercise.cc.webHandling;

import java.util.Map;

import android.content.Context;
import android.os.AsyncTask;

import com.exercise.cc.ConverterApplication;
import com.exercise.cc.IUpdateResultHandler;
import com.exercise.cc.SettingsActivity;
import com.exercise.cc.dbHandling.Currency;
import com.exercise.cc.dbHandling.DbHandler;

/**
 * Class for handling update task acynchronously, so that heavy processes
 * dont jam the UI 
 * @author Matti
 *
 */
public class UpdateCurrenciesTask extends
		AsyncTask<String, Void, String> {

	WebLogic mWebLogic;
	private DbHandler mDbHandler;
	private ConverterApplication mCApplication;
	private IUpdateResultHandler mUHandler;
	
	public UpdateCurrenciesTask(WebLogic wl, 
								IUpdateResultHandler handler, 
								Context context,
								ConverterApplication capp){
		mWebLogic = wl;
		
		mDbHandler = DbHandler.getInstance(context);
		mCApplication = capp;
		mUHandler = handler;
	}
	
	@Override
	protected String doInBackground(String... args) {
		if(args[0]=="getDollarRates"){
			Map<String, Currency> rates = mWebLogic.getDollarRates();
			String err = mWebLogic.getErrors();
			mCApplication.setStringPreferenceSetting(SettingsActivity.PREFS_ERRORS, err);
			
			if(!mDbHandler.mDb.isOpen()){
				mDbHandler.open();
			}
			mDbHandler.addCurrencies(rates);
			return "getDollarRates";
		} else if(args[0]=="getWebRate") {
			String currencyFrom;
			String currencyTo;
			String amount;
			if(args[1]!="2"){
				currencyFrom = args[1];
				currencyTo = args[2];
				amount = args[3];
				Float rate = mWebLogic.getWebRate(currencyFrom, currencyTo);
				float result = rate * Float.parseFloat(amount);
				return "getWebRate|" + Float.toString(result);
			} else {
				currencyFrom = args[2];
				currencyTo = args[3];
				amount = args[4];
				float rate1 = currencyFrom=="USD" ? 1.00F : mWebLogic.getWebRate(currencyFrom, "USD");
				float rate2 = currencyTo=="USD" ? 1.00F : mWebLogic.getWebRate(currencyTo, "USD");
		    	if(!mDbHandler.mDb.isOpen()){
		    		mDbHandler.open();
		    	}
		    	mDbHandler.storeRate(currencyFrom, rate1);
		    	mDbHandler.storeRate(currencyTo, rate2);
				float result = rate1/rate2 * Float.parseFloat(amount);
				return "getWebRate|" + Float.toString(result);
			}
		}
		return null;
	}
	
	@Override
    protected void onPostExecute(String result) {
		if(result.startsWith("getDollarRates")){
			if(mUHandler!=null)
				mUHandler.handleUpdateResult(result);
		}
		if(result.startsWith("getWebRate")){
			String rate = result.substring(result.indexOf("|")+1);
			if(mUHandler!=null)
				mUHandler.handleConversionResult(Float.parseFloat(rate));
		}
    }

	public class Test{
		
	}
}

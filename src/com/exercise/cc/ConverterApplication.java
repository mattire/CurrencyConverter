package com.exercise.cc;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;

import android.app.Application;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.res.Resources;

/**
 * Using Application class as utilities class for some common functions
 * @author Matti
 *
 */
public class ConverterApplication extends Application {
	
	private SharedPreferences mSettings;
	public Resources mResources;
	public HashMap<String, String> m_SupportedCurrencies = new HashMap<String, String>(); // holds currency names 
	private String mUpdateTime;
		
	@Override
	public void onCreate() {
		mResources = getResources();
		mSettings = getSharedPreferences(SettingsActivity.PREFS_NAME, 0);
		setupSupportedCurrencies();
	}
	
	/**
	 * Reads currency names from resources and sets up HashMap for retreaving currency name when you
	 * know three letter abbreviation according to ISO 4217 standard   
	 */
	void setupSupportedCurrencies(){
		String[] currencies = mResources.getStringArray(R.array.currencies);
		for (String currency : currencies) {
			m_SupportedCurrencies.put(currency.substring(0, 3),currency.substring(4)); // put ISO code and currency name
		}
	}
	
	/**
	 * Gets current time
	 * @return current time in string format
	 */
    public String currentTime(){
    	Calendar cal = Calendar.getInstance();
    	SimpleDateFormat sdf = new SimpleDateFormat(("yyyy-MM-dd HH:mm:ss"));
    	String updateTime = sdf.format(cal.getTime());
    	return updateTime;    	
    }

    /**
     * Sets string value with key to application preferences
     * @param key 
     * @param val
     */
    public void setStringPreferenceSetting(String key, String val){
		Editor e = mSettings.edit();
		e.putString(key, val);
		e.commit();    	
    }
    
    /**
     * Gets string value with key from application preferences
     * @param key
     * @return
     */
    public String getStringPreferenceSetting(String key){
    	return mSettings.getString(key, "0");
    }

    /**
     * Gets integer value with key from application preferences
     * @param key
     * @return
     */
	public int getIntPreferenceSetting(String key) {
		return mSettings.getInt(key, -1);
	}

	/**
	 * sets up update time to preferences
	 * @param updateTime
	 */
	synchronized public void setUpdateTime(String updateTime) {
		this.mUpdateTime = updateTime;
    	setStringPreferenceSetting(SettingsActivity.PREFS_UPDATE_TIME, updateTime);
	}

	/**
	 * Get current DB update time
	 * @return
	 */
	synchronized public String getUpdateTime() {
		return mUpdateTime;
	}

}

package com.exercise.cc.webHandling;

import java.util.HashMap;
import java.util.Map;

import com.exercise.cc.ConverterApplication;
import com.exercise.cc.dbHandling.Currency;

/**
 * Kind of facade class that is supposed to hide web handling complexities 
 * undernead it
 * @author Matti
 *
 */
public class WebLogic 
{	
	// Web data provider used
	IProviderParser m_Provider;
	// All providers
	Map<String, IProviderParser> mHandlers = new HashMap<String, IProviderParser>();
	// Reference to application data
	ConverterApplication mCA;

	// Singleton instance
	static WebLogic mInstance = null;
	
	private String errors;
	public synchronized String getErrors() { return errors; }
	
	// Retreaving singleton instance
	static public WebLogic getInstance(ConverterApplication ca){
		if(mInstance==null){
			mInstance = new WebLogic(ca);
		}
		return mInstance;
	}
	
	
	private WebLogic(ConverterApplication ca) {
		mCA = ca;
		mHandlers.put("CoinMill", new CoinMillParser());
		m_Provider = mHandlers.get("CoinMill");
	}
	
	/**
	 * Provider setter class
	 * @param provider
	 * @return
	 */
	public Boolean setProvider(String provider){
		m_Provider = mHandlers.get(provider);
		return true;
	} 
		
	/**
	 * Function for retreaving rate between currencies from web
	 * @param currency1
	 * @param currency2
	 * @return
	 */
	public Float getWebRate(String currency1, String currency2){
		return m_Provider.fetchRate(currency1, currency2);
	}
	
	/**
	 * Fetch all currency rates to update database
	 * @return
	 */
	public Map<String, Currency> getDollarRates(){
		
		Map<String, Currency> currencies = new HashMap<String, Currency>();
		// add dollar
		Currency d = new Currency();
		d.mShortName = "USD";
		d.mRate = 1.00F;
		d.mName = mCA.m_SupportedCurrencies.get("USD");
		currencies.put("USD", d);
		
		HashMap<String, Float> rates = m_Provider.fetchRates();
		
		errors = m_Provider.getErrors();
		
		for (String key : rates.keySet()) {
			Currency c = new Currency();
			c.mShortName = key;
			c.mRate = rates.get(key);
			c.mName = mCA.m_SupportedCurrencies.get(key);
			currencies.put(key, c);
		}
return currencies;
	}
	
}

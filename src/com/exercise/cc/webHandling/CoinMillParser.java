package com.exercise.cc.webHandling;

import java.io.IOException;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.HashMap;
import java.util.Locale;

import org.apache.http.client.ClientProtocolException;

import android.util.Log;
import android.util.Pair;

/**
 * Class for parsing currency data from http://coinmill.com/
 * @author Matti
 *
 */
public class CoinMillParser implements IProviderParser{
	private static String ITEM = "item";
	private static String DESCRIPTION = "description";
	private static String TAG1000 = "</td></tr><tr><td>1,000";
	private static String TAG1000START = "</td><td>";
	private static String TAG1000END = "</";
	private static String CONVERSION_TABLE = "<table class=conversionchart>";
	
	// Specify if fetching currency data from http://coinmill.com/chart/ (more accurate than rss feed)
	private static boolean useChart = true;
	
	
	// Old address things for direct currency fetching (was not accurate enough)
	/* 
	private static boolean useChart = false;
	private static String CURRENCIES_BASE_ADDR = "http://coinmill.com/rss/";
	private static String CURRENCIES_END_ADDR = ".xml";
	/*/
	// New address things for direct currency fetching
	private static String CURRENCIES_BASE_ADDR = "http://coinmill.com/chart/";
	private static String CURRENCIES_END_ADDR = ".html";
	//*/
	
	// Address for fetching all the rates at once
	private static String CURRENCIES_ADDR = "http://coinmill.com/currency.js";
	
	private static String errors = "0";

	WebHandler mWH = new WebHandler();
	
	@Override
	public float parseRate(String data, String currency1, String currency2) {
		String rateString = simpleStringParsing(data, currency1, currency2);
		return Float.parseFloat(rateString);
	}
	

	@Override
	public float fetchRate(String currency1, String currency2) {
		float rate = 0.0F;
		try {
			String data = mWH.fetchData(formWebAddress(currency1, currency2));
			if (!useChart) {rate = parseRate(data, currency1, currency2);}
			else {rate = this.stringParseRateFromChart1000(data);}
		} catch (ClientProtocolException e) {
			handleError(e);		
		} catch (IOException e) {
			handleError(e);
		}
		errors = "0";
		return rate;
	}	
	
	private void handleError(Exception e){
		String m = e.getMessage();
		Log.w("CC", m);
		errors = m;
		e.printStackTrace();		
	}

	/**
	 * Form web address for currency 
	 * @param currency1
	 * @param currency2
	 * @return
	 */
	private String formWebAddress(String currency1, String currency2){
		//"http://coinmill.com/rss/GBP_USD.xml"		
		//return "http://coinmill.com/rss/" + currency1 + "_" + currency2 + ".xml";
		return CURRENCIES_BASE_ADDR + currency1 + "_" + currency2 + CURRENCIES_END_ADDR;
	}
	
	/**
	 * Old rss parsing method not used
	 * @param data
	 * @param cur1
	 * @param cur2
	 * @return
	 */
	private String simpleStringParsing(String data, String cur1, String cur2)
	{
		int itemstart = data.indexOf(ITEM);
		int start = data.indexOf(DESCRIPTION, itemstart);
		int end = data.indexOf(DESCRIPTION, start + 11);
		String description = data.substring(start + 11, end);
		String findString1 = "1.00 " + cur1 + " = "; 
		//String findString2 = "1.00 " + cur2 + " = ";
		String endTag1 = " " + cur2 + "<br/>";
		//String endTag2 = " " + cur1 + "<br/>";
		int rateBegin = description.indexOf(findString1);
		int rateEnd = description.indexOf(endTag1);
 		String rateString = description.substring(rateBegin + 11, rateEnd);
		//if(rateString.startsWith("0.00")){ // need to read earlier value & calculate from there
		if(rateString.startsWith("0.")){ // need to read earlier value & calculate from there
			String findString2 = "1.00 " + cur2 + " = ";
			String endTag2 = " " + cur1 + "<br/>";;
			String rateString2 = description.substring(
					description.indexOf(findString2) + findString2.length(), 
						description.indexOf(endTag2));
			Float rate = 1/Float.parseFloat(rateString2);
			rateString = Float.toString(rate);
		}
		Log.d("CurrencyConverter", "Rate string: " + rateString);
		
		return rateString;
	}
	
	/**
	 * Parsing rates from js page
	 */
	@Override
	public HashMap<String, Float> fetchRates(){
		HashMap<String, Float> rates = new HashMap<String, Float>();
		try {
			//String data = mWH.fetchData("http://coinmill.com/currency.js");
			String data = mWH.fetchData(CURRENCIES_ADDR);
			rates = parseRates(data);
			errors = "0";
		} catch (ClientProtocolException e) {
			handleError(e);
		} catch (IOException e) {
			handleError(e);
		}
		return rates;
	}
	
	private HashMap<String, Float> parseRates(String data) {
		HashMap<String, Float> rates = new HashMap<String, Float>();
		//String[] lines = data.split("\n");
		int start = data.indexOf("'") + 1;
		int end = data.indexOf("'", start+1);
		String currencyData = data.substring(start, end);
		String[] dataUnits = currencyData.split("\\|");
		for (String dataUnit : dataUnits) {
			Pair<String, Float> pair = parseCurrencyJsRatesDataUnit(dataUnit);
			rates.put(pair.first, pair.second);
		}
		
//		String[] lines = data.split("',");
//		for (String string : lines) {
//			if(string.startsWith("'');"))
//				break;
//			else if(string.startsWith("'")){
//				Pair<String, Float> pair = parseCurrencyJsRatesLine(string);
//				rates.put(pair.first, pair.second);
//			}
//		}
		return rates;
	}
	
	private Pair<String, Float> parseCurrencyJsRatesDataUnit(String dat){
		String isoCode = null;
		float rate = 0;
		String[] pieces = dat.split(",");
		isoCode = pieces[0];
		try {
			rate = Float.parseFloat(pieces[1]);
		} catch (Exception e) {
			Log.w("CC", e.getMessage());
		}		
		return new Pair<String, Float>(isoCode, rate);
	}

	private Pair<String, Float> parseCurrencyJsRatesLine(String line){
		String isoCode = null;
		float rate = 0;
		try {
			int sep1 = line.indexOf('|');
			int sep2 = line.indexOf('|', sep1+1);
			isoCode = line.substring(1, sep1);
			String rateStr = line.substring(sep1+1, sep2);
			rate = Float.parseFloat(rateStr);
		} catch (Exception e) {
			//e.printStackTrace();
			Log.w("CC", e.getMessage());
		} 
		return new Pair<String, Float>(isoCode, rate);
	}

	/**
	 * Parses data from chart page, replaces the rss method above 
	 * @param data
	 * @return
	 */
	private Float stringParseRateFromChart1000(String data){
		int tablestart = data.indexOf(CONVERSION_TABLE);
		int rateBegin1 = data.indexOf(TAG1000, tablestart) + TAG1000.length();
		int rateBegin2 = data.indexOf(TAG1000START, rateBegin1) + TAG1000START.length();
		int rateEnd = data.indexOf(TAG1000END, rateBegin2);
		NumberFormat nf = java.text.DecimalFormat.getInstance(Locale.US);
		try {
			Number n = nf.parse(data.substring(rateBegin2,rateEnd));
			return n.floatValue()/1000.0F;
		} catch (ParseException e) {
			String m = e.getMessage();
			errors = m;
			Log.w("CC", m);
			return 0.0F;
		}
	}
	
	/**
	 * Method for getting errors
	 */
	@Override
	public String getErrors() {
		return errors;
	}
}


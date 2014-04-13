package com.exercise.cc.webHandling;


//import org.apache.http.client.HttpClient;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import android.util.Log;


/**
 * Class doing the actual data fetching from the web
 * @author Matti
 *
 */
public class WebHandler {
	
	public String fetchData(String address) throws ClientProtocolException, IOException
	{
		DefaultHttpClient dhc = new DefaultHttpClient();
		HttpResponse response = dhc.execute(new HttpGet(address));
		InputStream contentStream = null;
		contentStream = response.getEntity().getContent();
		
		BufferedReader br = new BufferedReader(new InputStreamReader(contentStream,"euc-kr"));
		StringBuffer sb = new StringBuffer();
	    try{
	    	String line = null;
	    	while ((line = br.readLine())!=null){
	    		sb.append(line);
	    	}
	    	Log.w("CurrencyConverter", sb.toString());
	    }catch (IOException e){
	    	//e.getStackTrace()
	    	Log.w("CurrencyConverter", e.getMessage());
	    }
	    return sb.toString();
	}


}

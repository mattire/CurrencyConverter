package com.exercise.cc;

import android.widget.TextView;
import android.app.Activity;

/**
 * Class for catching and displaying errors during setting content view and
 * initializing activities
 * @author Matti
 *
 */
public class ContentViewHandler {
	/**
	 * Method for catching error during activity initialization, if exceptions
	 * occur they are caught and displayed, (howevers if exceptions getMessage method 
	 * returns null then the method itself will crash) 
	 * @param resource
	 * @param activity
	 * @return true if initialization was successful, false if it was not
	 */
	static boolean trySetContentView(int resource, Activity activity)
	{
        try{
        	activity.setContentView(resource);
        	InitializationInterface ii = (InitializationInterface) activity;
        	ii.initialize();
        	return true;
        } catch (Exception ex)
        {
    		String message = ex.getMessage();
        	activity.setContentView(R.layout.error);
        	TextView tv = (TextView) activity.findViewById(R.id.textView2);
        	tv.setText(message);
        }
        
		return false;
	}
}

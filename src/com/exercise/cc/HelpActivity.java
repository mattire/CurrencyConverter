package com.exercise.cc;

import android.app.Activity;
import android.os.Bundle;

/**
 * Help activity for displaying help view
 * @author Matti
 *
 */
public class HelpActivity extends Activity implements InitializationInterface{
    @Override
	public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.help);
    }

    public void initialize()
    {
    	
    }
}

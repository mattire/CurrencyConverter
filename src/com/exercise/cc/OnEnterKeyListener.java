package com.exercise.cc;

import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnKeyListener;

/**
 * Event listener for listening enter key press
 * @author Matti
 *
 */
public class OnEnterKeyListener implements OnKeyListener {

	ConvertActivity mCA;
	int mComponentId;
	public OnEnterKeyListener(ConvertActivity ca, int ci){
		mCA = ca;
		mComponentId = ci;
	}
	
	@Override
	public boolean onKey(View arg0, int arg1, KeyEvent arg2) {
		if( arg1 == KeyEvent.KEYCODE_ENTER){
			mCA.returnPressedCallback(mComponentId);
			return true;
		}	
		return false;
	}

}

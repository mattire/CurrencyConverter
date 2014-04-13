package com.exercise.cc;

import android.text.Editable;
import android.text.method.KeyListener;
import android.view.KeyEvent;
import android.view.View;

/**
 * Listens for enter key event in ConverterActivity and 
 * calls returnPressedCallback initiating currency conversion.
 * @author Matti
 *
 */
public class EnterKeyListener implements KeyListener {
	
	ConvertActivity mCA;
	int mComponentId;
	View mCallback;
	
	/**
	 * Constructor
	 * @param ca
	 * @param id
	 * @param callback
	 */
	public EnterKeyListener(ConvertActivity ca, int id, View callback){
		mCA = ca;
		mComponentId = id;
		mCallback = callback;
	}
	
	@Override
	public void clearMetaKeyState(View view, Editable content, int states) {
	}

	@Override
	public int getInputType() {
		return 0;
	}

	@Override
	public boolean onKeyDown(View view, Editable text, int keyCode,
			KeyEvent event) {
		if( keyCode == KeyEvent.KEYCODE_ENTER){
			mCA.returnPressedCallback(mComponentId);
			return true;
		}	
		return false;
	}

	@Override
	public boolean onKeyOther(View view, Editable text, KeyEvent event) {
		return false;
	}

	@Override
	public boolean onKeyUp(View view, Editable text, int keyCode, KeyEvent event) {
		return false;
	}

}

package com.exercise.cc;

import java.util.ArrayList;
import java.util.Vector;

import android.app.ListActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnKeyListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CheckedTextView;
import android.widget.ListView;

import com.exercise.cc.dbHandling.Currency;
import com.exercise.cc.dbHandling.DbHandler;

/**
 * Activity for displaying currency favourite settings
 * @author Matti
 *
 */
public class FavouritesActivity extends ListActivity implements InitializationInterface, OnKeyListener, OnItemClickListener 
{
	DbHandler mDH = null;
	ArrayList<String> listItems=new ArrayList<String>(); 
	ArrayAdapter<String> mAdapter;
	ListView mList = null;
	Vector<Currency> currencies = null;
	
    @Override
	public void onCreate(Bundle savedInstanceState) {
    	super.onCreate(savedInstanceState);
    	if(ContentViewHandler.trySetContentView(R.layout.favourites, this)==true){    		
    	}
    }

	@Override
	public void initialize() {		
//		LinearLayout ll = (LinearLayout) findViewById(R.id.LinearLayout01);
//		ll.setOnKeyListener(this);
		mDH = DbHandler.getInstance(getApplicationContext());
		mDH.open();
		
		currencies = mDH.getCurrencies();
		mDH.close();
		for (Currency currency : currencies) {
			listItems.add(currency.mShortName + "-" +currency.mName);
		}
    	mAdapter=new ArrayAdapter<String>(this,
    		android.R.layout.simple_list_item_multiple_choice,
		    listItems);
    	
    	mList=getListView();
    	mList.setOnKeyListener(this);
    	mList.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);    	
    	setListAdapter(mAdapter);

		mList.setOnItemClickListener(this);
//		mList.setOnItemClickListener(new OnItemClickListener(){
//
//			@Override
//			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
//					long arg3) {
//				String name = arg1.getClass().getName();
//				Log.w("CC", name);
//				try {
//					Boolean checked = ((CheckedTextView) arg1).isChecked();
//					storeFavourite(arg2, !checked);
//					
//				} catch (Exception e) {
//					Log.w("CC", e.getMessage());
//				}				
//				
//			}
//			
//		});    	
	}

	/**
	 * 
	 * @param f
	 * @param faw
	 */
	public void storeFavourite(int f, Boolean faw){
		mDH.setFavourite(currencies.elementAt(f).mShortName, faw);
	}
	
	@Override
	public void onResume(){
		mDH.open();
		super.onResume();
		int pos = mList.getFirstVisiblePosition();
		mList.setItemChecked(pos, true);
		int i = 0;
		for (Currency c : currencies) {
			mList.setItemChecked(i, c.mFavourite);
			i++;
		}
	}

	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		super.onListItemClick(l, v, position, id);		
		try {
			Currency c = currencies.elementAt(position);
			c.mFavourite = ((CheckBox) v).isChecked();
			mDH.updateCurrency(c);
			//mDH.setCurrency(c);
		} catch (Exception e) {
			Log.w("CC", e.getMessage());
		}
	}

	@Override
	public void onPause(){
		super.onPause();
		mDH.close();
	}

	/**
	 * Gets pressed key, if it's an alphabet calls setCharacterPosition
	 * for scrolling to currency which has ISO abbreviation that starts with
	 * that character
	 */
	@Override
	public boolean onKey(View arg0, int arg1, KeyEvent arg2) {
		// TODO Auto-generated method stub
		int key = arg2.getKeyCode();
		if(key >= KeyEvent.KEYCODE_A && key <= KeyEvent.KEYCODE_Z)
		{
			char c = (char)arg2.getUnicodeChar();
			char[] array = {c};
			String s = new String(array);
			//Log.d("CC", s);
			s = s.toUpperCase();
			setCharacterPosition(s);
			return true;
		}
		return false;
	}	
	
	/**
	 * Scrolls list to string position defined by s
	 * @param s
	 * @return
	 */
	private int setCharacterPosition(String s){
		for(int i=0; i<mAdapter.getCount();i++)
		{
			if(mAdapter.getItem(i).startsWith(s)){
				mList.setSelection(i);
			}
		}
		return 0;
	}

	/**
	 * Stores favourite setting for clicked item
	 */
	@Override
	public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
		String name = arg1.getClass().getName();
		Log.w("CC", name);
		try {
			Boolean checked = ((CheckedTextView) arg1).isChecked();
			storeFavourite(arg2, !checked);
		} catch (Exception e) {
			Log.w("CC", e.getMessage());
		}						
	}
}

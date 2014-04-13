package com.exercise.cc;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.ToggleButton;


/**
 * Activity for displaying and editing application settings
 * @author Matti
 *
 */
public class SettingsActivity extends Activity implements InitializationInterface, OnItemSelectedListener, OnCheckedChangeListener 
{
	// preference identifiers
	public static final String PREFS_NAME = "CC_preferences"; // Preference identifier for CurrencyConverter application
	public static final String PREFS_MODE = "mode"; // Name for mode that specifies if currency rates are fetched directly form web or from db
	public static final String PREFS_FAVOURITES = "favourites"; // Identifier for favourite setting preferences 
																// specifies if only favourites are shown or all currencies
	public static final String PREFS_UPDATE_MODE = "update mode"; // specifier for update mode
																  // stores amount of minutes between
																  // updates, if equals -1 periodic 
																  // update is off
	public static final String PREFS_FIRST_RUN = "first run";	  // specifier fo first run
	
	public static final int PREFS_MODE_FETCH_ON_CONVERT = 0; 			// always use net connection if possible, store fetched value to db, if no net read db
	public static final int PREFS_MODE_FETCH_ON_UPDATE_USE_JS = 2;      // same as above, but fetch all data at once from currency.js file
	public static final String PREFS_UPDATE_TIME = "update time";	// time when database was updated
	public static final String PREFS_ERRORS = "errors";				// preference for storing error during db update
	
	ArrayAdapter<CharSequence> adapter;
	
	public SettingsActivity mInstance; // Reference to settings activity instance
	
	private static final int ACTIVITY_FAVOURITES=0; // Identifier for starting favourites activity for result. 
	
	CheckBox mChkFavourites=null; // Checkbox for selecting show only favourites mode
	
	RadioButton mRadioFetchOnConv=null; 	// Radio button for selecting fetching currency rates directly from web mode
	RadioButton mRadioFetchOnUpdJs=null; 	// Radio button for using db for conversion rates
	RadioGroup mRGroup=null; 				// RadioGroup for above
	Boolean mFavouritesEdited=false; 		// Boolean specifying if favourites were edited during settings editing
	boolean mUpdateOn = false; 				// Boolean for specifying if automatic updates are on
	
	private ToggleButton mUpdateToggle;		// Toggle updates on / off 
	private EditText mUpdateMin;			// Editor for editing update period
	private SharedPreferences mSettings;	// Preferences 
	private Editor mEditor;					// Preferences editor
	
    @Override
	public void onCreate(Bundle savedInstanceState) {
    	super.onCreate(savedInstanceState);
    	if(ContentViewHandler.trySetContentView(R.layout.settings, this)==true){
    	}
    	mInstance = this;
    }
    
    /**
     * Initializing settings
     */
    public void initialize()
    {
        mSettings = getSharedPreferences(PREFS_NAME, 0);
        mEditor = mSettings.edit();
        int mode = mSettings.getInt(PREFS_MODE, PREFS_MODE_FETCH_ON_UPDATE_USE_JS);
        int updateMode = mSettings.getInt(PREFS_UPDATE_MODE, -1);
        //String updateTime = mSettings.getString(PREFS_UPDATE_TIME, "-1");
        boolean favourites = mSettings.getBoolean(PREFS_FAVOURITES, false);
    	
        Button b2 = (Button) findViewById(R.id.btn_ClearDb);
        Button b3 = (Button) findViewById(R.id.btn_EditFavourites);
        mUpdateToggle = (ToggleButton) findViewById(R.id.toggleButton_update);
        mUpdateMin = (EditText) findViewById(R.id.edit_update_minutes);
        
        int minutes = (updateMode==-1) ? 3 : updateMode; // update period must be at least 3 minutes 
        mUpdateMin.setText(Integer.toString(minutes));
        if(updateMode!=-1){
        	mUpdateToggle.setChecked(true);
        	mUpdateMin.setEnabled(false);
        	mUpdateOn = true;
        } else {
        	mUpdateToggle.setChecked(false);
        	mUpdateMin.setEnabled(true);
        	mUpdateOn = false;
        }
        mChkFavourites = (CheckBox) findViewById(R.id.chk_Favourites);
        mChkFavourites.setChecked(favourites);
        
        mRadioFetchOnConv = (RadioButton) findViewById(R.id.radio0);
        mRadioFetchOnUpdJs = (RadioButton) findViewById(R.id.radio2);
        mRGroup = (RadioGroup) findViewById(R.id.radioGroup1);
        
        adapter = ArrayAdapter.createFromResource(this, R.array.update_modes, android.R.layout.simple_spinner_item);
        
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        
        switch (mode) {
		case PREFS_MODE_FETCH_ON_CONVERT:
			mRGroup.check(R.id.radio0);
			mRadioFetchOnConv.setChecked(true);
			break;
		case PREFS_MODE_FETCH_ON_UPDATE_USE_JS:
			mRGroup.check(R.id.radio2);
			mRadioFetchOnUpdJs.setChecked(true);
			break;
		default:
			break;
		}
        
        b2.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				Bundle bundle = setupReturnBundle();
				bundle.putBoolean("clear db", true);
		    	mEditor.putBoolean(PREFS_FIRST_RUN, true);
		    	mEditor.commit();
		    	
                Intent intent = new Intent();
                intent.putExtras(bundle);
                setResult(RESULT_OK, intent);
                finish();
				
			}
		});
        b3.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
	        	try {
	        		mFavouritesEdited = true;
					Intent i = new Intent(mInstance, FavouritesActivity.class);
					startActivityForResult(i, ACTIVITY_FAVOURITES);
				} catch (Exception e) {
					e.printStackTrace();
					Log.w("CC", e.getMessage());
				}
			}
		});
        
        mUpdateToggle.setOnCheckedChangeListener((OnCheckedChangeListener) this);
    }

    /**
     * Collects edited settings data and puts them to return bundle
     * @return Bundle containing what was edited it settings activity
     */
    public Bundle setupReturnBundle(){
		Bundle bundle = new Bundle();
		bundle.putBoolean("favourites", mChkFavourites.isChecked());
		switch (mRGroup.getCheckedRadioButtonId()) {
		case R.id.radio0:
			bundle.putInt(PREFS_MODE, PREFS_MODE_FETCH_ON_CONVERT);
			break;
		case R.id.radio2:
			bundle.putInt(PREFS_MODE, PREFS_MODE_FETCH_ON_UPDATE_USE_JS);
			break;
		default:
			bundle.putInt(PREFS_MODE, PREFS_MODE_FETCH_ON_UPDATE_USE_JS);
			break; // newer happen
		}
		bundle.putBoolean("favourites_edited", mFavouritesEdited);
		return bundle;
    }
    
    /**
     * Handling favourites activity result, currently does nothing
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
    	super.onActivityResult(requestCode, resultCode, intent);
    	if(intent!=null){
	    	switch (requestCode) {
			case ACTIVITY_FAVOURITES:
				break;
			default:
				break;
			}
    	}
    }
    
    /**
     * Handling return from settings.
     */
	@Override
	public void onBackPressed() {
		Bundle b = setupReturnBundle();
        Intent intent = new Intent();
        intent.putExtras(b);
        setResult(RESULT_OK, intent);		
        finish();
	}
    
    @Override
    protected void onStop(){
    	super.onStop();
    	mEditor.putBoolean(PREFS_FAVOURITES, mChkFavourites.isChecked());
    	switch (mRGroup.getCheckedRadioButtonId()) {
		case R.id.radio0:
			mEditor.putInt(PREFS_MODE, PREFS_MODE_FETCH_ON_CONVERT);
			break;
		case R.id.radio2:
			mEditor.putInt(PREFS_MODE, PREFS_MODE_FETCH_ON_UPDATE_USE_JS);			
			break;
		default:
			break;
		}
    	mEditor.commit();
    }

	@Override
	public void onItemSelected(AdapterView<?> arg0, View arg1, int position,
			long id) {
	}

	
	@Override
	public void onNothingSelected(AdapterView<?> arg0) {		
	}

	/**
	 * Handling update service start and stop
	 */
	@Override
	public void onCheckedChanged(CompoundButton arg0, boolean arg1) {
		if(arg0.getId()==R.id.toggleButton_update){
			if(mUpdateOn!=arg1){
				mUpdateOn = arg1;
				if (arg1) {
					startUpdService();
				} else {
					stopUpdService();
				}
			}
		}
	}

	/**
	 * Stopping update service
	 */
	private void stopUpdService() {
		mUpdateMin.setEnabled(true);
		stopService(new Intent(this, UpdateService.class));
		mEditor.putInt(SettingsActivity.PREFS_UPDATE_MODE, -1);
		mRadioFetchOnUpdJs.setEnabled(true);
		mRadioFetchOnConv.setEnabled(true);
	}

	/**
	 * Starting update service
	 */
	private void startUpdService() {
		if(UpdateService.isRunning==false){
			int minutes = Integer.parseInt(mUpdateMin.getText().toString());
			if(minutes<3)
			{
				minutes = 3;
				mUpdateMin.setText("3");
			}
			mUpdateMin.setEnabled(false);
			mEditor.putInt(SettingsActivity.PREFS_UPDATE_MODE, minutes);
			mEditor.commit();
			startService(new Intent(this, UpdateService.class));
			mRadioFetchOnUpdJs.setChecked(true);
			mRadioFetchOnUpdJs.setEnabled(false);
			mRadioFetchOnConv.setChecked(false);
			mRadioFetchOnConv.setEnabled(false);
		}		
	}

}

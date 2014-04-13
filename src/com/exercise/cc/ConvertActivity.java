package com.exercise.cc;

import java.util.Vector;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.res.Resources;
import android.os.Bundle;
import android.text.Editable;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import com.exercise.cc.dbHandling.DbHandler;
import com.exercise.cc.webHandling.UpdateCurrenciesTask;
import com.exercise.cc.webHandling.WebLogic;


/**
 * Main activity class, displays UI for converting currencies 	
 * @author Matti
 *
 */
public class ConvertActivity extends Activity implements OnClickListener,
														 OnItemSelectedListener,
														 IUpdateResultHandler 
{
	// Menu constants identifiers	
	public static final int MENU_UPDATE_ID = Menu.FIRST;
	public static final int MENU_SETTINGS_ID = Menu.FIRST + 1;
	public static final int MENU_HELP_ID = Menu.FIRST + 2;
	// Identifiers for activities that give result
    private static final int ACTIVITY_SETTINGS=0;
    
	private Spinner m_dynamicSpinner1; // Control for selecting currency 
	private Spinner m_dynamicSpinner2; // Control for selecting currency
	
	private EditText m_amount1; // Control for entering amount for currency conversion
	private EditText m_amount2; // Control for displaying conversion result
	private ArrayAdapter<CharSequence> m_adapterForSpinner1; 
	private ArrayAdapter<CharSequence> m_adapterForSpinner2;
	
	private TextView m_infoText; 		// text for displaying currency names
	private TextView m_btnCalculate; 	// button for initiating currency calculation
	public DbHandler m_DbHandler; 		// Database handler instance
	private Converter m_Converter; 		// Class handling the conversion process 
	private WebLogic m_WebLogic = null;	// Class handling web related things
	
	private Boolean m_ShowFavouritesOnly = false; // For remembering if favourites are shown
	private SharedPreferences mSettings; // For handling application settings
	private Button m_btnSwitch;	// Button for switching conversion direction
	private boolean mFirstRun;	// Specify if we are running software for the first time
	private String mUpdateTime; // Time when database was last updated	
	private int mConvertMode;	// The mode in which conversions are run
	private TextView m_updateText; // For displaying time when database was updated or an error 
	public ConverterApplication mCA; // Application class for accessing common utilities
	
	private boolean mListenSelection; // Used for turning off changed notifications from spinners
									  // when doing the switch, otherwise conversion is triggered
									  // to happen multiple times

	/**
	 * Main activity creation 
	 */
    @Override
    public void onCreate(Bundle savedInstanceState) {
    	mListenSelection = true;
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        Resources r = getResources();
        mSettings = getSharedPreferences(SettingsActivity.PREFS_NAME, 0);
        boolean favourites = mSettings.getBoolean(SettingsActivity.PREFS_FAVOURITES, false);
        mFirstRun = mSettings.getBoolean(SettingsActivity.PREFS_FIRST_RUN, true);
        mConvertMode = mSettings.getInt(SettingsActivity.PREFS_MODE, SettingsActivity.PREFS_MODE_FETCH_ON_UPDATE_USE_JS);        
        
        mCA = (ConverterApplication) getApplication();
        m_WebLogic = WebLogic.getInstance(mCA);
        
        m_dynamicSpinner1 = (Spinner) findViewById(R.id.spinner1);
        m_dynamicSpinner2 = (Spinner) findViewById(R.id.spinner2);
        m_amount1 = (EditText) findViewById(R.id.amount1_conversion);
        m_amount2 = (EditText) findViewById(R.id.amount2_conversion);
        setM_infoText((TextView) findViewById(R.id.provider_textview));
        m_updateText = (TextView) findViewById(R.id.update_textview);
        
        m_btnCalculate = (Button) findViewById(R.id.calculate_button);
        m_btnSwitch = (Button) findViewById(R.id.switch_button);
        
        m_amount1.setOnKeyListener(new OnEnterKeyListener(this, R.id.amount1_conversion));
        
        m_adapterForSpinner1 = new ArrayAdapter<CharSequence>(this, android.R.layout.simple_spinner_item);
        m_adapterForSpinner1.setDropDownViewResource(android.R.layout.simple_spinner_item);
        m_dynamicSpinner1.setAdapter(m_adapterForSpinner1);
        m_adapterForSpinner1.setNotifyOnChange(true);
        m_dynamicSpinner1.setOnItemSelectedListener(this);

        m_adapterForSpinner2 = new ArrayAdapter<CharSequence>(this, android.R.layout.simple_spinner_item);
        m_adapterForSpinner2.setDropDownViewResource(android.R.layout.simple_spinner_item);
        m_dynamicSpinner2.setAdapter(m_adapterForSpinner2);
        m_adapterForSpinner2.setNotifyOnChange(true);
        m_dynamicSpinner2.setOnItemSelectedListener(this);        

        m_btnCalculate.setOnClickListener((OnClickListener)this);
        m_btnSwitch.setOnClickListener((OnClickListener)this);
        
        m_DbHandler = DbHandler.getInstance(this);
        m_DbHandler.open();
        
        readCurrenciesFromDbAndFillSpinners(favourites);
        mUpdateTime = mSettings.getString(SettingsActivity.PREFS_UPDATE_TIME, "no update time");

        //if(mFirstRun||m_dynamicSpinner1.getCount()==0){
        if(m_dynamicSpinner1.getCount()==0){
        	mFirstRun=true;
        	//getM_infoText().setText("First run, select update from menu to load currencies");
    		UpdateCurrenciesTask task = new UpdateCurrenciesTask(m_WebLogic, this, this, mCA);
    		task.execute("getDollarRates");
    		m_updateText.setText("Started fetching currency rates");        	
        } else {        	
        	getM_infoText().setText("");
        	m_updateText.setText("db updated: " + mUpdateTime);
        }
        m_Converter = new Converter(m_DbHandler, m_WebLogic, this, mCA, getApplicationContext());
        
    }
    
    /**
     * Switch currency conversion direction
     */
    protected void doSwitch() {
    	mListenSelection = false;
    	int position1 = m_dynamicSpinner1.getSelectedItemPosition();
    	int position2 = m_dynamicSpinner2.getSelectedItemPosition();
    	m_dynamicSpinner1.setSelection(position2);
    	m_dynamicSpinner2.setSelection(position1);
    	doConversion();
    	
	}

    /**
     * Setting menu
     */
	@Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_menu, menu);
        return true;
    }
    
	/**
	 * Handling menu option selection events
	 */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
    	// code below stopped working so for some reason replaced it with title string comparisons
//    	switch (item.getItemId()) {
//    	case R.id.menu_update: // update
//    		UpdateCurrenciesTask task = new UpdateCurrenciesTask(m_WebLogic, this, this, mCA);
//    		task.execute("getDollarRates");
//    		m_updateText.setText("Started fetching currency rates");
//            return true;
//        case R.id.menu_settings: // settings
//        	Intent i = new Intent(this, SettingsActivity.class);
//        	startActivityForResult(i, ACTIVITY_SETTINGS);
//        	return true;
//        case R.id.menu_help: // help
//        	Intent i2 = new Intent(this, HelpActivity.class);        	
//        	startActivity(i2);
//        	return true;
//        }
    	String title = item.getTitle().toString();
    	String update_str = mCA.mResources.getString(R.string.update_str).toString();
    	String settings_str = mCA.mResources.getString(R.string.settings_str).toString();
    	String help_str = mCA.mResources.getString(R.string.help_str).toString();
    	if( title.equals(update_str))
    	{
    		UpdateCurrenciesTask task = new UpdateCurrenciesTask(m_WebLogic, this, this, mCA);
    		task.execute("getDollarRates");
    		m_updateText.setText("Started fetching currency rates");
            return true;    		
    	}
    	else if(title.equals(settings_str))
    	{
        	Intent i = new Intent(this, SettingsActivity.class);
        	startActivityForResult(i, ACTIVITY_SETTINGS);
        	return true;
    	} 
    	else if(title.equals(help_str))
    	{
        	Intent i2 = new Intent(this, HelpActivity.class);        	
        	startActivity(i2);
        	return true;
    	}
        return super.onOptionsItemSelected(item);
    }

	@Override
    public void handleUpdateResult(String result){
		if(mFirstRun){
			readCurrenciesFromDbAndFillSpinners(false);
		}
		Editor e = mSettings.edit();
		e.putBoolean(SettingsActivity.PREFS_FIRST_RUN, false);
		e.commit();
		mUpdateTime = mCA.currentTime();
		m_updateText.setText("Db updated: " + mUpdateTime);
		String err = mCA.getStringPreferenceSetting(SettingsActivity.PREFS_ERRORS); // check if update for db has failed
		if(err!="0"){m_updateText.append("Last db update failed: " + err);}
		mCA.setUpdateTime(mUpdateTime);
    }
        
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
    	try {
			super.onActivityResult(requestCode, resultCode, intent);
			if(intent!=null){				
				Bundle extras = intent.getExtras();
				switch (requestCode) {
				case ACTIVITY_SETTINGS:
					mConvertMode = extras.getInt(SettingsActivity.PREFS_MODE);
					Boolean favourites = extras.getBoolean("favourites");
					Boolean favEdited = extras.getBoolean("favourites_edited"); 
					
					if(m_ShowFavouritesOnly!=favourites || (favourites && favEdited)){
						m_adapterForSpinner1.clear();
						m_adapterForSpinner2.clear();
						readCurrenciesFromDbAndFillSpinners(favourites);
						m_ShowFavouritesOnly = favourites;
					} 
 
					if(extras.getBoolean("clear db")){
						m_DbHandler.clearDb();
						mFirstRun = true;
					} else { getM_infoText().append(" data provider " + mConvertMode);}
					break;
	
				default:
					break;
				}
			}
		} catch (Exception e) {
			Log.d("CC", e.getMessage());
		}
    }

    @Override
    protected void onDestroy(){
    	m_DbHandler.close();
    	super.onDestroy();
    }  
    
    /**
     * Handling return key press event
     * @param component the component that received key press
     */
    public void returnPressedCallback(int component){
    	if(component==R.id.amount1_conversion){
    		doConversion();
    	}
    }
    
    @Override
    public void onResume(){
    	super.onResume();
    	m_DbHandler.open();
//    	updateSpinners();
    	
    }
    
    private void updateSpinners() { // !!
		m_adapterForSpinner1.clear();
		m_adapterForSpinner2.clear();
		readCurrenciesFromDbAndFillSpinners(true);
	}

	@Override
    public void onPause(){
    	super.onPause();
    	m_DbHandler.close();
    }
    
    /**
     * Reads selected currencies and amount, passes them to converter that carries on
     * to do the conversion asynchronously
     * sets the last time database was updated and writes the currency names info
     */
    private void doConversion(){
    	try{
    		m_amount2.setText("");
	    	Editable edit = m_amount1.getText();
	    	String strAmount = edit.toString();
	    	CharSequence cs1 = m_adapterForSpinner1.getItem(m_dynamicSpinner1.getSelectedItemPosition());
	    	CharSequence cs2 = m_adapterForSpinner2.getItem(m_dynamicSpinner2.getSelectedItemPosition());
	    	
	    	if(!cs1.equals(cs2)){
	    		
		    	float amount = Float.valueOf(strAmount);
		    	m_Converter.convert(amount, cs1.toString(), cs2.toString(), mSettings);
		    	String name1 = mCA.m_SupportedCurrencies.get(cs1.toString());
		    	String name2 = mCA.m_SupportedCurrencies.get(cs2.toString());
		    	switch(mConvertMode){
		    		case SettingsActivity.PREFS_MODE_FETCH_ON_UPDATE_USE_JS:
		    			getM_infoText().setText(name1 +" to " +name2);
		    			mUpdateTime = mCA.getUpdateTime();
		    			mUpdateTime = mCA.getStringPreferenceSetting(SettingsActivity.PREFS_UPDATE_TIME);
		    			m_updateText.setText("db updated: " + mUpdateTime);
		    			String err = mCA.getStringPreferenceSetting(SettingsActivity.PREFS_ERRORS); // check if update for db has failed
		    			if(err!="0"){m_updateText.append("Last db update failed: " + err);}
		    			break;
		    		case SettingsActivity.PREFS_MODE_FETCH_ON_CONVERT:
		    			m_amount2.setText("wait..");
		    			getM_infoText().setText(name1 +" to " +name2 + " fetched from web");	    		
		    			break;
		    		default:
		    			break;
		    	}
	    	} else {
	    		m_amount2.setText("Select different currencies");
	    	}
    	} catch (Exception ex){
    		m_amount2.setText(ex.getMessage(), TextView.BufferType.EDITABLE);
    	}
    }
    
	@Override    
    public void handleConversionResult(float result)
    {
    	String strResult = Float.toString(result);
    	m_amount2.setText(strResult,  TextView.BufferType.EDITABLE);
    	mListenSelection = true;
    }

	/**
	 * Reads the currencies from database and fills the spinners
	 * @param favs specifies if only favourites are displayed
	 */
    private void readCurrenciesFromDbAndFillSpinners(Boolean favs){
    	if(!m_DbHandler.mDb.isOpen()){
    		m_DbHandler.open();
    	}
    	Vector<String> currencies = m_DbHandler.getCurrencyShortNames(favs);
    	for (String string : currencies) {
    		m_adapterForSpinner1.add(string);
    		m_adapterForSpinner2.add(string);
		}
    }

    /**
     * Click event handler
     */
    public void onClick(View view) {
    	int id = view.getId();
    	switch (id) {
		case R.id.calculate_button:
			doConversion();
			break;
		case R.id.switch_button:
			doSwitch();
			break;
		default:
			break;
		}    	
    }

    /**
     * Handling spinner item selections
     */
	@Override
	public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2,
			long arg3) {
		m_amount2.setText("");
		if(mListenSelection==true){
			if(m_amount1.getText().toString()!=""){
				this.doConversion();
			}
		}
	}

	@Override
	public void onNothingSelected(AdapterView<?> arg0) {
	}

	public void setM_infoText(TextView m_infoText) {
		this.m_infoText = m_infoText;
	}

	public TextView getM_infoText() {
		return m_infoText;
	}
    
}
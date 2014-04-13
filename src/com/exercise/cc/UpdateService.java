package com.exercise.cc;

import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import com.exercise.cc.webHandling.UpdateCurrenciesTask;
import com.exercise.cc.webHandling.WebLogic;


/**
 * Service for updating database periodically
 * @author Matti
 *
 */
public class UpdateService extends Service implements IUpdateResultHandler 
{
	public static boolean isRunning = false;
	
	private WebLogic mWL = null;
	private ConverterApplication mCA = null;
	
    private Handler mHandler = new Handler();
    public static final int ONE_MINUTE = 60000;
    public static final int TWO_MINUTE = 120000;
    public static final int ONE_N_HALF_MINUTE = 900000;
    public static final int SECONDS_10 = 10000;
    private int mUpdateTime = 120000;
    private UpdateService mInstance;
    
    /**
     * Since services run also in UI thread, we need to 
     * start task in this method, so that heavy service
     * method does not jam the UI
     */
    private Runnable periodicTask = new Runnable() {
        public void run() {
            Log.v("CC","Awake");
        	Toast.makeText(getApplicationContext(), "Upd. currencies db", Toast.LENGTH_LONG).show();
    		UpdateCurrenciesTask task = new UpdateCurrenciesTask(mWL, mInstance, mInstance, mCA);
    		task.execute("getDollarRates");
            mHandler.postDelayed(periodicTask, mUpdateTime);
        }
        
    };
    
	@Override
	public IBinder onBind(Intent arg0) {
		return null;
	}

    @Override
    public void onCreate() {
    	mCA = (ConverterApplication)getApplication();
    	mWL = WebLogic.getInstance(mCA);
    	mInstance = this;
    	mUpdateTime = 
    		mCA.getIntPreferenceSetting(SettingsActivity.PREFS_UPDATE_MODE) * ONE_MINUTE;
    	if (mUpdateTime<1) mUpdateTime = 1 * ONE_MINUTE;
        mHandler.postDelayed(periodicTask, SECONDS_10);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mHandler.removeCallbacks(periodicTask);
        Toast.makeText(this, "Service onDestroy() ", Toast.LENGTH_LONG).show();
    }

	@Override
	public void handleUpdateResult(String result) {
		String updTime = mCA.currentTime();
		mCA.setUpdateTime(updTime);
		mCA.setStringPreferenceSetting(SettingsActivity.PREFS_UPDATE_TIME, updTime);
		String err = mWL.getErrors();
		mCA.setStringPreferenceSetting(SettingsActivity.PREFS_ERRORS, err);
		Toast.makeText(getApplicationContext(), "Curr. db updated", Toast.LENGTH_LONG).show();

	}

	/**
	 * Not used but still need to be implemented for IUpdateResultHandler, because the
	 * other method in interaface is used
	 */
	@Override
	public void handleConversionResult(float rate) {
	}
}

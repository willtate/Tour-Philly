package com.squidgle.philly;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;

public class DashActivity extends Activity implements LocationListener 
{
	//Menu Items
	public static final int SETTINGS_ID = 1000;
	//Preference
	SharedPreferences mPrefs;
	Boolean mDisableGPSWarning;
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) 
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dashboard);
        //get any relevant preferences
        getPrefs();
        //setup listeners for all the buttons on the dashboard
        dashButtonListeners();
        //check if GPS service is enabled
        if(!mDisableGPSWarning) {
        	checkGPS();
        }
    }
    
    /**
     * Get any Preferences that may be relevant to this Activity
     */
    
	private void getPrefs() {
		mPrefs = PreferenceManager.getDefaultSharedPreferences(this);
		mDisableGPSWarning = mPrefs.getBoolean("disableGPSWarning", false);
	}

	/**
     * When the application opens, check if GPS is enabled.  If not, post a short 
     * warning to the user about the slight loss in functionality.  They are able 
     * to dismiss this dialog.  They can also enter the GPS settings from here and 
     * enable GPS
     */
    
    private void checkGPS()
    {
    	LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
    	locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000L,1.0f, this);
    	boolean isGPS = locationManager.isProviderEnabled (LocationManager.GPS_PROVIDER);
    	if(isGPS == false) {
    		AlertDialog.Builder builder = new AlertDialog.Builder(this);
			builder.setTitle("Your GPS is disabled!")
				.setMessage("To get better use out of this application, you should consider enabling your GPS." + 
							"  Enabling GPS isn't necessary, but it is suggested.  You can enter the settings of " +
							"this application to disable this warning.")
			    .setCancelable(false)
			    .setPositiveButton("Open GPS Settings", new DialogInterface.OnClickListener() {
			    	@Override
			    	public void onClick(DialogInterface dialog, int id) {
			    		startActivityForResult(new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS), 0);
			    	}
			    })
			    .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int id) {
						dialog.cancel();
					}
				});
			AlertDialog alert = builder.create();
			alert.show();
    	}
    }
    
    /**
     * Setup all onClickListenters for the dashboard buttons
     */
    
    private void dashButtonListeners() 
    {
    	Button locationView = (Button) findViewById(R.id.locationViewButton);
    	locationView.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				startLocationView();
			}
		});
    }
    
    /**
     * Start the Activity to view all locations
     */
    
    private void startLocationView() 
    {
    	startActivity(new Intent(this, LocationView.class));
    }
    
    @Override
   	public boolean onCreateOptionsMenu(Menu menu) {
   		super.onCreateOptionsMenu(menu);
   		menu.add(0, SETTINGS_ID, 0, R.string.menu_settings).setIcon(android.R.drawable.ic_menu_manage);
   		return true;
   	}
    
    /**
     * Handle when a menu item is selected.  This function simply handles the logic and passes
     * the actual action on to another function.
     */
    
    @Override
	public boolean onMenuItemSelected(int featureId, MenuItem item) {
    	switch(item.getItemId()) {
        case SETTINGS_ID:
        	launchSettings();
        	return true;
        default:
        	return super.onMenuItemSelected(featureId, item);
    	}
	}
    
    /**
     * Launch the Settings/Preferences Activity
     */
    
    private void launchSettings()
    {
    	startActivity(new Intent(this, Settings.class));
    }

	@Override
	public void onLocationChanged(Location location) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onProviderDisabled(String provider) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onProviderEnabled(String provider) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onStatusChanged(String provider, int status, Bundle extras) {
		// TODO Auto-generated method stub
		
	}
}
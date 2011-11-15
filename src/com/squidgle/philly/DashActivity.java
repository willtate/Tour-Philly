package com.squidgle.philly;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

public class DashActivity extends Activity implements LocationListener 
{
	public static final String TAG = "Squidgle-Philly";
	//Menu Items
	public static final int SETTINGS_ID = 1000;
	//URL for Update
	public static final String UPDATE_URL = "http://www.squidgle.com/api";
	//Preference
	SharedPreferences mPrefs;
	Boolean mDisableGPSWarning;
	Context mContext;
	
	ProgressDialog progressDialog;
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) 
    {
        super.onCreate(savedInstanceState);
        mContext = this;
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
    	
    	Button refreshButton = (Button) findViewById(R.id.refreshInfoButton);
    	refreshButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				RestTask task = new RestTask();
				task.execute(UPDATE_URL);
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
	
	/**
	 * Start an AsyncTask to perform an HTTP GET to retrieve the JSON response from
	 * the Squidgle REST server.
	 * @author will
	 *
	 */
	
	public class RestTask extends AsyncTask<String, Void, Integer> {

		@Override
		protected Integer doInBackground(String... urls) 
		{
			StringBuilder builder = new StringBuilder();
			HttpClient client = new DefaultHttpClient();
			HttpGet httpGet = new HttpGet(urls[0]);
			Integer retval = null;
			try {
				HttpResponse response = client.execute(httpGet);
				StatusLine statusLine = response.getStatusLine();
				int statusCode = statusLine.getStatusCode();
				if (statusCode == 200) {
					HttpEntity entity = response.getEntity();
					InputStream content = entity.getContent();
					BufferedReader reader = new BufferedReader(
							new InputStreamReader(content));
					String line = "";
					while ((line = reader.readLine()) != null) {
						builder.append(line);
					}
					//crease a JSONArray from the response string
					JSONArray jsonArray = new JSONArray(builder.toString());
					retval = jsonArray.length();
					//open the database
					DbAdapter dbHelper = new DbAdapter(mContext);
					dbHelper.open();
					//drops the old table and creates a new one for entry
					dbHelper.prepare();
					//iterate through the JSONArray adding items to the database
					for (int i = 0; i < jsonArray.length(); i++) {
						Log.i(TAG, "Inserting item");
						JSONObject jsonObject = jsonArray.getJSONObject(i);
						dbHelper.insert(jsonObject.getString("title"),
							jsonObject.getString("snippet"),
							jsonObject.getInt("lat"),
							jsonObject.getInt("long"));
					}
					Log.i(TAG, "Finished, closing DB");
					//always remember to clean up!
					dbHelper.close();
				} else {
					Toast.makeText(mContext, R.string.refresh_error, Toast.LENGTH_LONG).show();
				}
			} catch (ClientProtocolException e) {
				Toast.makeText(mContext, R.string.refresh_error, Toast.LENGTH_LONG).show();
				Log.e(TAG, e.getMessage());
			} catch (IOException e) {
				Toast.makeText(mContext, R.string.refresh_error, Toast.LENGTH_LONG).show();
				Log.e(TAG, e.getMessage());
			} catch (JSONException e) {
				Toast.makeText(mContext, R.string.json_parse_error, Toast.LENGTH_LONG).show();
			}
			return retval;
		}

		protected void onPostExecute(Integer entries)
		{
			progressDialog.dismiss();
			if(entries != null) {
				Toast.makeText(mContext, "Successfully retrieved " + entries.intValue() + " entries!", Toast.LENGTH_SHORT).show();
			}
		}
		
		protected void onPreExecute()
		{
			progressDialog = ProgressDialog.show(mContext, "", "Refreshing location database...");
		}
	}
}
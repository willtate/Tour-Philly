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
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
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
	//Dialogs
	public static final int INITIAL_DIALOG = 2000;
	//Menu Items
	public static final int SETTINGS_ID = 1000;
	//URL for Update
	public static final String UPDATE_URL = "http://www.squidgle.com/api";
	//Preference
	SharedPreferences mPrefs;
	Boolean mDisableGPSWarning;
	Boolean mInitialLaunch;
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
        
        if(mInitialLaunch) {
        	showDialog(INITIAL_DIALOG);
        }
    }
    
    /**
     * Get any Preferences that may be relevant to this Activity
     */
    
	private void getPrefs() 
	{
		mPrefs = PreferenceManager.getDefaultSharedPreferences(this);
		mDisableGPSWarning = mPrefs.getBoolean("disableGPSWarning", false);
		//is this our first time launching the application?
		mInitialLaunch = mPrefs.getBoolean("initialLaunch", true);
		if(mInitialLaunch) {
			SharedPreferences.Editor editor = mPrefs.edit();
			editor.putBoolean("initialLaunch", false);
			editor.commit();
		}
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
    	Button refreshButton = (Button) findViewById(R.id.refreshInfoButton);
    	refreshButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				refreshLocations();
			}
		});
    }
    
    @Override
   	public boolean onCreateOptionsMenu(Menu menu) 
    {
   		super.onCreateOptionsMenu(menu);
   		menu.add(0, SETTINGS_ID, 0, R.string.menu_settings).setIcon(android.R.drawable.ic_menu_manage);
   		return true;
   	}
    
    /**
     * Handle when a menu item is selected.  This function simply handles the logic and passes
     * the actual action on to another function.
     */
    
    @Override
	public boolean onMenuItemSelected(int featureId, MenuItem item)
    {
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
	 * Display a dialog to the user on the first application launch.  This dialog
	 * will tell them the application must pull down the location information to display
	 * on the map overlay.
	 * @return The AlertDialog to display
	 */
	
	public Dialog initialDialog() 
	{
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		Resources resource = this.getResources();
		builder.setTitle("Welcome to " + resource.getString(R.string.app_name) + "!")
				.setMessage("Since this is your first launch we need to download map information from our servers.  This may take a minute.")
				.setCancelable(false)
				.setPositiveButton("OK", new DialogInterface.OnClickListener() {
		           public void onClick(DialogInterface dialog, int id) {
		        	  refreshLocations();
		           }
		       });
		return builder.create();
	}
	
	@Override
	protected Dialog onCreateDialog(int id) 
	{
		Dialog dialog;
		switch(id) {
		case INITIAL_DIALOG:
			dialog = initialDialog();
			break;
		default:
			dialog = null;
			break;
		}
		return dialog;
	}
	
	/**
	 * Start the AsyncTask to contact servers for location information
	 */
	
	public void refreshLocations() 
	{
		if(!isData()) {
			Toast.makeText(this, "Unable to contact server! No data connection available", Toast.LENGTH_LONG).show();
		}
		RestTask task = new RestTask();
		task.execute(UPDATE_URL);
	}
	
	/**
	 * 
	 */

	public boolean isData()
	{
		ConnectivityManager cManager = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
		if(cManager != null) {
			NetworkInfo[] netInfo = cManager.getAllNetworkInfo();
			for(int i = 0; i < netInfo.length; i++) {
				if(netInfo[i].getState() == NetworkInfo.State.CONNECTED) {
					return true;
				}
			}
		}
		return false;
	}
	
	/**
	 * Start an AsyncTask to perform an HTTP GET to retrieve the JSON response from
	 * the Squidgle REST server.
	 * @author will
	 *
	 */
	
	public class RestTask extends AsyncTask<String, Void, Integer> 
	{

		/**
		 * The meat of the AsyncTask.  This will get a JSON response from the server, parse it, and input it
		 * into a local database for faster querying later.
		 */
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
		/**
		 * Dismiss the progress dialog as we've now finished the download
		 */
		protected void onPostExecute(Integer entries)
		{
			progressDialog.dismiss();
			if(entries != null) {
				Toast.makeText(mContext, "Successfully retrieved " + entries.intValue() + " entries!", Toast.LENGTH_SHORT).show();
			}
		}
		/**
		 * Display an infinite progress dialog once we start the location download
		 */
		protected void onPreExecute()
		{
			progressDialog = ProgressDialog.show(mContext, "", "Refreshing location database...");
		}
	}
}
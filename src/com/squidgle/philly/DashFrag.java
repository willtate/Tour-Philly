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

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

public class DashFrag extends Fragment implements LocationListener
{
	public static final String TAG = "Squidgle-Philly";
	public static final String REFRESH_IN_PROGRESS = "refreshInProgress";
	//Dialogs
	public static final int INITIAL_DIALOG = 2000;
	//Menu Items
	public static final int SETTINGS_ID = 1000;
	//URL for Update
	public static final String UPDATE_URL = "http://www.squidgle.com/api";
	
	private SharedPreferences mPrefs;
	private RestTask mTask;
	private Boolean mDisableGPSWarning;
	private Boolean mInitialLaunch;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setHasOptionsMenu(true);
		getPrefs();
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View v = inflater.inflate(R.layout.dashboard, container, false);
		dashButtonListeners(v);
		return v;
	}
	
	
	
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		//check if GPS service is enabled
        if(!mDisableGPSWarning) {
        	checkGPS();
        }
        
        if(mInitialLaunch) {
        	getActivity().showDialog(INITIAL_DIALOG);
        }
		restoreTask(savedInstanceState);
	}

	/**
     * When the application opens, check if GPS is enabled.  If not, post a short 
     * warning to the user about the slight loss in functionality.  They are able 
     * to dismiss this dialog.  They can also enter the GPS settings from here and 
     * enable GPS
     */
    
    private void checkGPS()
    {
    	LocationManager locationManager = (LocationManager) getActivity().getSystemService(Context.LOCATION_SERVICE);
    	locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000L,1.0f, (LocationListener) getActivity());
    	boolean isGPS = locationManager.isProviderEnabled (LocationManager.GPS_PROVIDER);
    	if(isGPS == false) {
    		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
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
     * Get any Preferences that may be relevant to this Activity
     */
    
	private void getPrefs() 
	{
		mPrefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
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
     * Setup all onClickListenters for the dashboard buttons
     */
    
    private void dashButtonListeners(View v) 
    {    	
    	Button refreshButton = (Button) v.findViewById(R.id.refreshInfoButton);
    	refreshButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				refreshLocations();
			}
		});
    }
    
	/**
	 * Start the AsyncTask to contact servers for location information
	 */
	
	public void refreshLocations() 
	{
		if(!isData()) {
			Toast.makeText(getActivity(), "Unable to contact server! No data connection available", Toast.LENGTH_LONG).show();
		}
		mTask = new RestTask();
		mTask.execute(UPDATE_URL);
	}
	
	/**
	 * Check for data connection
	 */

	public boolean isData()
	{
		ConnectivityManager cManager = (ConnectivityManager) getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
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
	
	
	
	private void restoreTask(Bundle savedInstanceState)
	{
		if(savedInstanceState != null && savedInstanceState.getBoolean(REFRESH_IN_PROGRESS)) {
			refreshLocations();
		}
	}

	@Override
	public void onSaveInstanceState(Bundle outState) 
	{
		super.onSaveInstanceState(outState);
		saveTask(outState);
	}
	
	private void saveTask(Bundle outState)
	{
		final RestTask task = mTask; 
		if(task != null && task.getStatus() != AsyncTask.Status.FINISHED) {
			task.cancel(true);
			outState.putBoolean(REFRESH_IN_PROGRESS, true);
		}
		mTask = null;
	}
	
	/**
	 * Start an AsyncTask to perform an HTTP GET to retrieve the JSON response from
	 * the Squidgle REST server.
	 * @author will
	 *
	 */

	public class RestTask extends AsyncTask<String, Void, Integer> 
	{
		private ProgressDialog mProgressDialog;
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
					DbAdapter dbHelper = new DbAdapter(getActivity());
					dbHelper.open();
					//drops the old table and creates a new one for entry
					dbHelper.prepare();
					//iterate through the JSONArray adding items to the database
					for (int i = 0; i < jsonArray.length(); i++) {
						JSONObject jsonObject = jsonArray.getJSONObject(i);
						dbHelper.insert(jsonObject.getString("title"),
							jsonObject.getString("snippet"),
							jsonObject.getInt("lat"),
							jsonObject.getInt("long"));
					}
					//always remember to clean up!
					dbHelper.close();
				} else {
					Toast.makeText(getActivity(), R.string.refresh_error, Toast.LENGTH_LONG).show();
				}
			} catch (ClientProtocolException e) {
				Toast.makeText(getActivity(), R.string.refresh_error, Toast.LENGTH_LONG).show();
				Log.e(TAG, e.getMessage());
			} catch (IOException e) {
				Toast.makeText(getActivity(), R.string.refresh_error, Toast.LENGTH_LONG).show();
				Log.e(TAG, e.getMessage());
			} catch (JSONException e) {
				Toast.makeText(getActivity(), R.string.json_parse_error, Toast.LENGTH_LONG).show();
			}
			return retval;
		}
		/**
		 * Dismiss the progress dialog as we've now finished the download
		 */
		protected void onPostExecute(Integer entries)
		{
			if(mProgressDialog.isShowing()) {
				mProgressDialog.dismiss();
			}
			if(entries != null) {
				Toast.makeText(getActivity(), "Successfully retrieved " + entries.intValue() + " entries!", Toast.LENGTH_SHORT).show();
			}
		}
		/**
		 * Display an infinite progress dialog once we start the location download
		 */
		protected void onPreExecute()
		{
			mProgressDialog = ProgressDialog.show(getActivity(), "", "Refreshing location database...");
		}
	}

	@Override
	public void onLocationChanged(Location arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onProviderDisabled(String arg0) {
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

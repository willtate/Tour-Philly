package com.squidgle.philly;

import java.util.ArrayList;
import java.util.List;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.database.Cursor;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.ItemizedOverlay;
import com.google.android.maps.MapActivity;
import com.google.android.maps.MapController;
import com.google.android.maps.MapView;
import com.google.android.maps.MyLocationOverlay;
import com.google.android.maps.Overlay;
import com.google.android.maps.OverlayItem;

public class LocationView extends MapActivity 
{
	static final int PHILLY_LAT = 39952335;
	static final int PHILLY_LONG = -75163789;
	static final int CITY_ZOOM_LEVEL = 14;
	MapView mMapView;
	
	List<Overlay> mapOverlays;
	LocationOverlay mItemizedOverlay;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) 
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.location_view);
		initMap();
		//create point for central hall
		createPoints();
		//add users current location
		MyLocationOverlay myLocationOverlay = new MyLocationOverlay(this, mMapView);
		//notify user is they have location services disabled
		if(!myLocationOverlay.enableMyLocation()) {
			Toast.makeText(this, "Unable to get users location", Toast.LENGTH_LONG).show();
		}
		
	    mapOverlays.add(myLocationOverlay);
	}
	
	/**
	 * Initialize MapView object, enable built-in zoom controls and set
	 * default zoom level to an appropriate value 
	 */
	
	private void initMap() {
		mMapView = (MapView) findViewById(R.id.mapView);
		mMapView.setBuiltInZoomControls(true);

		MapController mapController = mMapView.getController();
		mapController.setZoom(CITY_ZOOM_LEVEL);
		
		mapOverlays = mMapView.getOverlays();
		mItemizedOverlay = new LocationOverlay(this.getResources().getDrawable(R.drawable.androidmarker));
	}
	
	/**
	 * Create all points from dataset.  This will include lat, long, title and snippet
	 * for all items.  Items will be added to the overlay.
	 */
	
	private void createPoints() {
		DbAdapter dbHelper = new DbAdapter(this);
		dbHelper.open();
		Cursor c = dbHelper.fetchAllItems();
		c.moveToFirst();
		while(c.isAfterLast() == false) {
			String title = c.getString(c.getColumnIndexOrThrow(DbAdapter.KEY_TITLE));
			String snippet = c.getString(c.getColumnIndexOrThrow(DbAdapter.KEY_SNIPPET));
			int latitude = c.getInt(c.getColumnIndexOrThrow(DbAdapter.KEY_LATITUDE));
			int longitude = c.getInt(c.getColumnIndexOrThrow(DbAdapter.KEY_LONGITUDE));
			Log.i(DashActivity.TAG, "Adding Item: " + title + " lat:"+latitude+" long:"+longitude);
			GeoPoint point = new GeoPoint(latitude ,longitude);
			OverlayItem overlayitem = new OverlayItem(point, title, snippet);
			mItemizedOverlay.addOverlay(overlayitem);
			mapOverlays.add(mItemizedOverlay);
			c.moveToNext();
		}
		c.close();
		dbHelper.close();
	}
	
	@Override
	protected boolean isRouteDisplayed() 
	{
	    return false;
	}
	
	private class LocationOverlay extends ItemizedOverlay<OverlayItem> 
	{
		
		private ArrayList<OverlayItem> mOverlays = new ArrayList<OverlayItem>();

		public LocationOverlay(Drawable defaultMarker) 
		{
			super(boundCenterBottom(defaultMarker));
		}

		@Override
		protected OverlayItem createItem(int i) 
		{
			return mOverlays.get(i);
		}

		@Override
		public int size() 
		{
			return mOverlays.size();
		}
		
		public void addOverlay(OverlayItem overlay) 
		{
		    mOverlays.add(overlay);
		    populate();
		}

		@Override
		protected boolean onTap(int i) 
		{
			OverlayItem item = mOverlays.get(i);
			String title = item.getTitle();
			String snippet = item.getSnippet();
			
			AlertDialog.Builder builder = new AlertDialog.Builder(LocationView.this);
			builder.setMessage(snippet)
				.setTitle(title)
			    .setCancelable(true)
			    .setPositiveButton("OK", new DialogInterface.OnClickListener() {
			    	public void onClick(DialogInterface dialog, int id) {
			    		dialog.cancel();
			    	}
			    });
			AlertDialog alert = builder.create();
			alert.show();
			return true;
		}
	}
}

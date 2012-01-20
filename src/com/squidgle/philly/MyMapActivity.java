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

public class MyMapActivity extends MapActivity 
{
	static final int PHILLY_LAT = 39952450;
	static final int PHILLY_LONG = -75163526;
	static final int CITY_ZOOM_LEVEL = 14;
	static final int ITEM_ZOOM_LEVEL = 16;
	private MapView mMapView;
	
	private List<Overlay> mMapOverlays;
	private LocationOverlay mItemizedOverlay;
	private Long mRowId;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) 
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.map_activity);
		Bundle extras;
		//grab any extras
		if (savedInstanceState == null) {
			extras = getIntent().getExtras();
			if (extras == null) {
				mRowId = null;
			} else {
				mRowId = (Long) extras.getSerializable(DbAdapter.KEY_ROWID);
			}
		} else {
			mRowId = (Long) savedInstanceState.getSerializable(DbAdapter.KEY_ROWID);
		}
		initMap();
		//create points
		createPoints();
		//add users current location
		MyLocationOverlay myLocationOverlay = new MyLocationOverlay(this, mMapView);
		//notify user is they have location services disabled
		if(!myLocationOverlay.enableMyLocation()) {
			Toast.makeText(this, "Unable to get user's location", Toast.LENGTH_LONG).show();
		}
		
	    mMapOverlays.add(myLocationOverlay);
	}
	
	/**
	 * Initialize MapView object, enable built-in zoom controls and set
	 * default zoom level to an appropriate value 
	 */
	
	private void initMap() 
	{
		mMapView = (MapView) findViewById(R.id.mapview);
		mMapView.setBuiltInZoomControls(true);
		//Sets an initial zoom on City Hall
		MapController mapController = mMapView.getController();
		mapController.setZoom(CITY_ZOOM_LEVEL);
		mapController.setCenter(new GeoPoint(PHILLY_LAT, PHILLY_LONG));
		
		mMapOverlays = mMapView.getOverlays();
		mItemizedOverlay = new LocationOverlay(this.getResources().getDrawable(R.drawable.androidmarker));
	}
	
	/**
	 * Create all points from dataset.  This will include lat, long, title and snippet
	 * for all items.  Items will be added to the overlay.
	 */
	
	private void createPoints() 
	{
		DbAdapter dbHelper = new DbAdapter(this);
		dbHelper.open();
		GeoPoint point;
		OverlayItem overlayitem;
		Cursor c;
		String title, snippet;
		int latitude = 0, longitude = 0;
		//If a single item ID was passed in we display only that item, otherwise display all items
		if(mRowId == null) {
			c = dbHelper.fetchAllItems(null);
			Log.d("Philly", "Number of Overlays: " + c.getCount());
		} else {
			c = dbHelper.fetchItem(mRowId);
			Log.d("Philly", "Overlay from row: " + Long.toString(mRowId));
		}
		c.moveToFirst();
		
		while(c.isAfterLast() == false) {
			title = c.getString(c.getColumnIndexOrThrow(DbAdapter.KEY_TITLE));
			snippet = c.getString(c.getColumnIndexOrThrow(DbAdapter.KEY_SNIPPET));
			latitude = c.getInt(c.getColumnIndexOrThrow(DbAdapter.KEY_LATITUDE));
			longitude = c.getInt(c.getColumnIndexOrThrow(DbAdapter.KEY_LONGITUDE));
			point = new GeoPoint(latitude ,longitude);
			overlayitem = new OverlayItem(point, title, snippet);
			mItemizedOverlay.addOverlay(overlayitem);
			mMapOverlays.add(mItemizedOverlay);
			c.moveToNext();
		}
		c.close();
		dbHelper.close();
		
		//if item is a single point, center and zoom to it
		if(mRowId != null) {
			MapController mapController = mMapView.getController();
			mapController.setCenter(new GeoPoint(latitude, longitude));
			mapController.setZoom(ITEM_ZOOM_LEVEL);
		}
		
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
			
			AlertDialog.Builder builder = new AlertDialog.Builder(MyMapActivity.this);
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

	@Override
	protected void onPause() {
		super.onPause();
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putInt(DbAdapter.KEY_LATITUDE, mMapView.getMapCenter().getLatitudeE6());
		outState.putInt(DbAdapter.KEY_LONGITUDE, mMapView.getMapCenter().getLongitudeE6());
		outState.putInt("zoom", mMapView.getZoomLevel());
		outState.putSerializable(DbAdapter.KEY_ROWID, mRowId);
	}
	

	@Override
	protected void onRestoreInstanceState(Bundle savedInstanceState) {
		super.onRestoreInstanceState(savedInstanceState);
		MapController mapController = mMapView.getController();
		mapController.setCenter(
				new GeoPoint(
						savedInstanceState.getInt(DbAdapter.KEY_LATITUDE), 
						savedInstanceState.getInt(DbAdapter.KEY_LONGITUDE)
						)
				);
		mapController.setZoom(savedInstanceState.getInt("zoom"));
	}

	@Override
	protected void onResume() {
		super.onResume();
	}
}

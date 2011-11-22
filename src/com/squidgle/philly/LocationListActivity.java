package com.squidgle.philly;

import android.app.ListActivity;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.view.View;
import android.widget.ListView;

public class LocationListActivity extends ListActivity 
{
	DbAdapter mDbHelper;
	@Override
	protected void onCreate(Bundle savedInstanceState) 
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.location_list);
		openDatabase();
		fillData();
	}
	
	public void openDatabase()
	{
		if(mDbHelper == null) {
			mDbHelper = new DbAdapter(this);
			mDbHelper.open();
		}
	}
	
	public void fillData()
	{
		Cursor c = mDbHelper.fetchAllItems();
        startManagingCursor(c);
        
        LocationAdapter locationAdapter = new LocationAdapter(this, c);
        setListAdapter(locationAdapter);
	}

	/**
	 * Upon clicking a list item launch the MapView activity, but only display the
	 * item the user clicked.
	 */
	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		super.onListItemClick(l, v, position, id);
		Intent i = new Intent(this, LocationView.class);
		i.putExtra(DbAdapter.KEY_ROWID, id);
		startActivity(i);
	}

}

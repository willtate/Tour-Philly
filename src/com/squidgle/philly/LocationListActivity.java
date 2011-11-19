package com.squidgle.philly;

import android.app.ListActivity;
import android.database.Cursor;
import android.os.Bundle;

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

}

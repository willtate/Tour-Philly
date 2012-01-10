package com.squidgle.philly;

import android.app.ListActivity;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SubMenu;
import android.view.View;
import android.widget.ListView;

public class LocationListActivity extends ListActivity 
{
	private static final int SORT_ID = 1000;
	private static final int MENU_TITLE_DESC = 1001;
	private static final int MENU_TITLE_ASC = 1002;

	DbAdapter mDbHelper;
	@Override
	protected void onCreate(Bundle savedInstanceState) 
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.location_list);
		openDatabase();
		fillData(DbAdapter.TITLE_ASC);
	}
	
	public void openDatabase()
	{
		if(mDbHelper == null) {
			mDbHelper = new DbAdapter(this);
			mDbHelper.open();
		}
	}
	
	public void fillData(String sortOrder)
	{
		Cursor c = mDbHelper.fetchAllItems(sortOrder);
        startManagingCursor(c);
        
        LocationAdapter locationAdapter = new LocationAdapter(this, c);
        setListAdapter(locationAdapter);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) 
	{
		super.onCreateOptionsMenu(menu);
		SubMenu sub;
		sub = menu.addSubMenu(0, SORT_ID, 0, R.string.menu_sort).setIcon(android.R.drawable.ic_menu_sort_by_size);
		sub.add(0, MENU_TITLE_ASC, 0, R.string.menu_title_asc);
		sub.add(0, MENU_TITLE_DESC, 0, R.string.menu_title_desc);
		return true;
	}
	
	

	@Override
	public boolean onMenuItemSelected(int featureId, MenuItem item) 
	{
		switch (item.getItemId()) {
		case MENU_TITLE_ASC:
			fillData(DbAdapter.TITLE_ASC);
			return true;
		case MENU_TITLE_DESC:
			fillData(DbAdapter.TITLE_DESC);
			return true;
		default:
			return super.onMenuItemSelected(featureId, item);
		}
		
	}

	/**
	 * Upon clicking a list item launch the MapView activity, but only display the
	 * item the user clicked.
	 */
	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) 
	{
		super.onListItemClick(l, v, position, id);
		Intent i = new Intent(this, LocationMapActivity.class);
		Long rowId = id;
		i.putExtra(DbAdapter.KEY_ROWID, rowId);
		startActivity(i);
	}

}

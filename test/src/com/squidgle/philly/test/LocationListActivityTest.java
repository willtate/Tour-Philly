package com.squidgle.philly.test;

import android.app.ListActivity;
import android.content.Context;
import android.test.ActivityInstrumentationTestCase2;
import android.widget.ListView;
import android.widget.TextView;

import com.squidgle.philly.LocationAdapter;
import com.squidgle.philly.LocationListActivity;

public class LocationListActivityTest extends
		ActivityInstrumentationTestCase2<LocationListActivity> {

	Context mContext;
	ListActivity mActivity;
	LocationAdapter mAdapter;
	ListView mList;
	TextView mText;
	
	public LocationListActivityTest() {
		super("com.squidgle.philly", LocationListActivity.class);
	}

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		
		setActivityInitialTouchMode(false);
		
		mActivity = (ListActivity) getActivity();
		mContext = mActivity.getApplicationContext();
		
		mList = mActivity.getListView();
		mAdapter = (LocationAdapter) mList.getAdapter();
	}
	
	public void testPreConditions() 
	{
		assertTrue(mList.isClickable());
		assertTrue(mList.isEnabled());
		assertTrue(mList.getEmptyView() != null);
		assertTrue(mAdapter != null);
	}
}

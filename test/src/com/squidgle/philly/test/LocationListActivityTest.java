package com.squidgle.philly.test;

import android.app.ListActivity;
import android.test.ActivityInstrumentationTestCase2;
import android.view.KeyEvent;
import android.widget.ListView;

import com.squidgle.philly.LocationAdapter;
import com.squidgle.philly.LocationListActivity;

public class LocationListActivityTest extends
		ActivityInstrumentationTestCase2<LocationListActivity> 
{
	public final static int LIST_INITIAL_POSITION = 0;
	public final static int LIST_TEST_POSITION = 10;

	private ListActivity mActivity;
	private LocationAdapter mListAdapter;
	private ListView mList;
	
	public LocationListActivityTest() 
	{
		super("com.squidgle.philly", LocationListActivity.class);
	}

	@Override
	protected void setUp() throws Exception 
	{
		super.setUp();
		
		setActivityInitialTouchMode(false);
		
		mActivity = (ListActivity) getActivity();
		mList = mActivity.getListView();
		mListAdapter = (LocationAdapter) mList.getAdapter();
	}
	
	/**
	 * Test some big picture stuff
	 */
	public void testPreConditions() 
	{
		assertTrue(mList.isClickable());
		assertTrue(mList.isEnabled());
		assertNotNull(mList.getEmptyView());
		assertNotNull(mListAdapter);
	}
	
	/**
	 * Verify the List UI is scrollable and such
	 */
	public void testListUI()
	{
		mActivity.runOnUiThread(new Runnable() {
			public void run() {
				mList.requestFocus();
				mList.setSelection(LIST_INITIAL_POSITION);
			}
		});
		
	    for (int i = 1; i <= LIST_TEST_POSITION; i++) {
	    	sendKeys(KeyEvent.KEYCODE_DPAD_DOWN);
	    }
	    
	    assertTrue(mList.getItemAtPosition(LIST_TEST_POSITION).equals(mList.getSelectedItem()));
	    
	}	
}

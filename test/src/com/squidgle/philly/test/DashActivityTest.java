package com.squidgle.philly.test;

import android.app.Activity;
import android.content.Context;
import android.test.ActivityInstrumentationTestCase2;
import android.widget.Button;

import com.squidgle.philly.DashActivity;
import com.squidgle.philly.DbAdapter;

public class DashActivityTest extends
		ActivityInstrumentationTestCase2<DashActivity> 
{
	Activity mActivity;
	Context mContext;	
	Button mRefreshButton;
	DbAdapter mDbAdapter;

	public DashActivityTest() {
		super("com.squidgle.philly", DashActivity.class);
	}

	@Override
	protected void setUp() throws Exception 
	{
		super.setUp();
		
		setActivityInitialTouchMode(false);
		
		mActivity = getActivity();
		mContext = mActivity.getApplicationContext();
		
		mRefreshButton = (Button) mActivity.findViewById(com.squidgle.philly.R.id.refreshInfoButton);
		
		mDbAdapter = new DbAdapter(mContext);
	}

	public void testPreConditions() 
	{
		assertTrue(mRefreshButton.isClickable());
		assertTrue(mRefreshButton.isEnabled());
		assertTrue(mDbAdapter.open() != null);
		
	}

	@Override
	protected void tearDown() throws Exception {
		mDbAdapter.close();
		super.tearDown();
	}
	
	
	
}

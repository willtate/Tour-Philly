package com.squidgle.philly.test;

import android.app.Activity;
import android.app.Instrumentation;
import android.test.ActivityInstrumentationTestCase2;
import android.widget.Button;

import com.squidgle.philly.DashActivity;

public class DashActivityTest extends
		ActivityInstrumentationTestCase2<DashActivity> 
{
	private Activity mActivity;
	private Button mRefreshButton;
	private Instrumentation mInstrumentation;

	public DashActivityTest() {
		super("com.squidgle.philly", DashActivity.class);
	}

	@Override
	protected void setUp() throws Exception 
	{
		super.setUp();
		
		setActivityInitialTouchMode(false);
		
		mInstrumentation = getInstrumentation();		
		mActivity = getActivity();
		mRefreshButton = (Button) mActivity.findViewById(com.squidgle.philly.R.id.refreshInfoButton);
	}

	public void testPreConditions() 
	{
		assertNotNull(mActivity);
		assertTrue(mRefreshButton.isClickable());
		assertTrue(mRefreshButton.isEnabled());
		
	}
	
	public void testRefreshButton()
	{
		mActivity.runOnUiThread(new Runnable() {
			public void run() {
				mRefreshButton.performClick();
			}
		});
		mInstrumentation.waitForIdleSync();
	}	
}

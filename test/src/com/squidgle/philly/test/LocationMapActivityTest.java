package com.squidgle.philly.test;

import android.test.ActivityInstrumentationTestCase2;

import com.google.android.maps.MapActivity;
import com.squidgle.philly.MyMapActivity;

public class LocationMapActivityTest extends
		ActivityInstrumentationTestCase2<MyMapActivity> 
{

	private MapActivity mActivity;
	
	public LocationMapActivityTest() 
	{
		super("com.squidgle.philly", MyMapActivity.class);
	}

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		
		mActivity = (MapActivity) getActivity();
		
	}

}

package com.squidgle.philly.test;

import android.test.ActivityInstrumentationTestCase2;

import com.google.android.maps.MapActivity;
import com.squidgle.philly.LocationMapActivity;

public class LocationMapActivityTest extends
		ActivityInstrumentationTestCase2<LocationMapActivity> 
{

	private MapActivity mActivity;
	
	public LocationMapActivityTest() 
	{
		super("com.squidgle.philly", LocationMapActivity.class);
	}

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		
		mActivity = (MapActivity) getActivity();
		
	}

}

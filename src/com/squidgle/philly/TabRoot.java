package com.squidgle.philly;

import android.app.TabActivity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TabHost;
import android.widget.TabHost.TabSpec;
import android.widget.TextView;

@SuppressWarnings("deprecation")
public class TabRoot extends TabActivity {
	
	TabHost mTabHost;
	
	public void onCreate(Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);
	    setContentView(R.layout.main);

	    mTabHost = getTabHost();  // The activity TabHost
	    mTabHost.getTabWidget().setDividerDrawable(R.drawable.tab_divider);

	    // setup our tabs
	    setupTab("Dashboard", new Intent(this, DashActivity.class));
	    setupTab("Map", new Intent(this, LocationMapActivity.class));
	    setupTab("List", new Intent(this, LocationListActivity.class));

	    mTabHost.setCurrentTab(0);
	}
	
	/**
	 * Create the Tab with our custom view and add it to the TabHost
	 * @param tag The String to be displayed in the tab
	 * @param i	The intent with the Activity to launch for that tab
	 */
	
	private void setupTab(final String tag, final Intent intent) {
		View tabview = createTabView(mTabHost.getContext(), tag);
	    TabSpec setContent = mTabHost.newTabSpec(tag).setIndicator(tabview).setContent(intent);
		mTabHost.addTab(setContent);
	}
	
	/**
	 * Create the inflated view for the Tab
	 * @param context Application Context
	 * @param text The String to be displayed in the tab
	 * @return The inflated view for the TOab
	 */
	
	private static View createTabView(final Context context, final String text) {
		View view = LayoutInflater.from(context).inflate(R.layout.tab, null);
		TextView tv = (TextView) view.findViewById(R.id.tabsText);
		tv.setText(text);
		return view;
	}
}

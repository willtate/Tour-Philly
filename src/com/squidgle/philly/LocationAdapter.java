package com.squidgle.philly;

import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.TextView;

public class LocationAdapter extends CursorAdapter 
{
	private LayoutInflater mLayoutInflater;

	public LocationAdapter(Context context, Cursor c) 
	{
		super(context, c);
		mLayoutInflater = LayoutInflater.from(context);
	}

	@Override
	public void bindView(View v, Context context, Cursor c) 
	{
		String title = c.getString(c.getColumnIndexOrThrow(DbAdapter.KEY_TITLE));
		TextView titleText = (TextView) v.findViewById(R.id.list_title);
		titleText.setText(title);
	}

	@Override
	public View newView(Context context, Cursor c, ViewGroup parent) 
	{
		View v = mLayoutInflater.inflate(R.layout.location_list_row, parent, false);
		return v;
	}

}

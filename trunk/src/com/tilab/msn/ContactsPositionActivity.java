package com.tilab.msn;

import android.os.Bundle;

import com.google.android.maps.MapActivity;
import com.google.android.maps.MapView;

public class ContactsPositionActivity extends MapActivity {

	@Override
	protected void onCreate(Bundle icicle) {
		super.onCreate(icicle);
		
		MapView mv = new MapView(this);

		addContentView(mv, null);
	}
	
}

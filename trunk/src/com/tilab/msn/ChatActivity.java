package com.tilab.msn;

import java.util.ArrayList;

import android.app.Activity;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.ListView;

public class ChatActivity extends Activity {


	ListView partsList;
	
	protected void onCreate(Bundle icicle) {
		// TODO Auto-generated method stub
		super.onCreate(icicle);
		setContentView(R.layout.chat);
		ArrayList parts = (ArrayList)getIntent().getSerializableExtra(ContactListActivity.OTHER_PARTICIPANTS);
		
		partsList = (ListView) findViewById(R.id.partsList);
		String[] names = new String[parts.size()];
		for(int i=0; i<parts.size();i++){
			names[i] = ((Contact)parts.get(i)).getName();
			
		}
		ArrayAdapter aAdapter = new ArrayAdapter(this,android.R.layout.simple_spinner_item, names);
		partsList.setAdapter(aAdapter);
		
	}
	

}

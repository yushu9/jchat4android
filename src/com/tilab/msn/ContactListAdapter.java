package com.tilab.msn;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.content.Context;
import android.content.Resources;
import android.graphics.Typeface;
import android.location.Location;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewInflate;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

public class ContactListAdapter extends BaseAdapter {

	private List<Contact> otherContactList;
	private Context context;
	private Location myLocation;
	private ViewInflate inflater;
	private Map<String, View> contactViewMap; 
	
	
	public ContactListAdapter(Context c){
		context = c;
	    inflater = (ViewInflate)context.getSystemService(Context.INFLATE_SERVICE);
		myLocation = ContactManager.getInstance().getMyContact().getLocation();
	}
	
	public void addAndUpdate(String key, Contact contact){
		View view = contactViewMap.get(key);
		if (view==null){
			View v = inflater.inflate(R.layout.element_layout, null,null);
			TextView tv = (TextView)v.findViewById(R.id.contact_name);
			tv.setText(contact.getName());
			TextView tv1 = (TextView)v.findViewById(R.id.contact_dist);
			float dist = myLocation.distanceTo(contact.getLocation())/1000;
			String.valueOf(dist);		
			tv1.setText(String.valueOf(dist)+ " km");
			synchronized (contactViewMap){
				contactViewMap.put(key, v);
	        }
		}else{
			TextView tv1 = (TextView)view.findViewById(R.id.contact_dist);
			float dist = myLocation.distanceTo(contact.getLocation())/1000;
			String.valueOf(dist);		
			tv1.setText(String.valueOf(dist)+ " km");
			
		}			
	}
	
		
	public void remove(String ID){	
		synchronized(contactViewMap){
		contactViewMap.remove(ID);
		}
	}
	
	public int getCount() {
		// TODO Auto-generated method stub
		return otherContactList.size();
	}

	
	public Object getItem(int arg0) {
		// TODO Auto-generated method stub
		return otherContactList.get(arg0);
	}

	
	public long getItemId(int position) {
		// TODO Auto-generated method stub
		return position;
	}

	public void updateAdapter(Location myLoc, List<Contact> newList){
		myLocation = myLoc;
		otherContactList = newList;
	}	
	
	public View getView(int position, View convertView, ViewGroup parent) {
		Contact c = otherContactList.get(position);
		
	    return v;	
	}	
}

	

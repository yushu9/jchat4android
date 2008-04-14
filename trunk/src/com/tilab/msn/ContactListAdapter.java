package com.tilab.msn;

import java.util.ArrayList;
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
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

public class ContactListAdapter extends BaseAdapter {
	private List<ContactInfo> contactInfoList;
	private Context context;
	private Location myLocation;
	private ViewInflate inflater;
	
	
	public ContactListAdapter(Context c){
		context = c;
	    inflater = (ViewInflate)context.getSystemService(Context.INFLATE_SERVICE);
		myLocation = ContactManager.getInstance().getMyContact().getLocation();
		contactInfoList = new ArrayList<ContactInfo>();
		}
	
	
	
	public void addAndUpdate(Contact contact){
		ContactInfo ci = new ContactInfo(contact.getPhoneNumber());
	    int position = contactInfoList.indexOf(ci);		
		if (position == -1){			
			synchronized (contactInfoList) {
				contactInfoList.add(position, ci);
			}				
	    }			
			
		}			
	
	
		
	public void remove(Contact c){			
		ContactInfo ci= new ContactInfo(c.getPhoneNumber());
	    contactInfoList.remove(ci);				
	}
	
	public int getCount() {
		// TODO Auto-generated method stub
		return contactInfoList.size();
	}

	
	public Object getItem(int arg0) {
		// TODO Auto-generated method stub
		ContactInfo ci = contactInfoList.get(arg0);		
		return ContactManager.getInstance().getContactByAgentId(ci.getID());
	}
	
	public long getItemId(int position) {
		// TODO Auto-generated method stub
		return position;
	}

	
	public View getView(int position, View convertView, ViewGroup parent) {
		ContactInfo ci = contactInfoList.get(position);	
		String contactInfoID = ci.getID();
		Contact c = ContactManager.getInstance().getContactByAgentId(contactInfoID);		 
		View v = inflater.inflate(R.layout.element_layout, null,null);
		CheckBox cb = (CheckBox)v.findViewById(R.id.contact_check_box);
		cb.setOnCheckedChangeListener(new CheckBoxClickListener(position));
		TextView tv = (TextView)v.findViewById(R.id.contact_name);		 
		tv.setText(c.getName());
		TextView tv1 = (TextView)v.findViewById(R.id.contact_dist);
		float dist = myLocation.distanceTo(c.getLocation())/1000;	
		tv1.setText(String.valueOf(dist)+ " km");				
		return v;
	}

	private class CheckBoxClickListener implements CheckBox.OnCheckedChangeListener{
         
		private int selectedListPos;
		
		public CheckBoxClickListener(int selectedPos){
			this.selectedListPos = selectedPos;
		}				

		@Override
		public void onCheckedChanged(CompoundButton arg0, boolean arg1) {
			ContactInfo ci= contactInfoList.get(selectedListPos);
		    ci.setChecked(arg1);
				
		}
		
	}
	
	private class ContactInfo {	
		
		public ContactInfo(String contactID){
			checked=false;
			this.contactID= contactID;
		}
		
		public void setChecked(boolean value){
			checked= value;
		}
		
		public boolean isChecked() {
			return checked;
		}
		public String getID(){
			return contactID;
		}
		
		
		
		@Override
		public boolean equals(Object o) {
			
			boolean retval= false;
			
			if(o instanceof ContactInfo){				
				ContactInfo ci = (ContactInfo) o;
				retval= ci.contactID.equals(contactID);		
			}
			return retval;	
		}		
		
		boolean checked;
		String contactID;		
	} 
}


	

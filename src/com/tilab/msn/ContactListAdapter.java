package com.tilab.msn;

import java.util.ArrayList;
import java.util.Collection;
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
	
	private List<ContactViewInfo> contactViewInfoList;
	private Context context;
	private ViewInflate inflater;
	
	
	public ContactListAdapter(Context c){
		context = c;
	    inflater = (ViewInflate)context.getSystemService(Context.INFLATE_SERVICE);		
	    contactViewInfoList = new ArrayList<ContactViewInfo>();
	}
	
	
	
	
	public int getCount() {
		// TODO Auto-generated method stub
		return contactViewInfoList.size();
	}

	
	public Object getItem(int arg0) {
		// TODO Auto-generated method stub
		ContactViewInfo cvi = contactViewInfoList.get(arg0);		
		return ContactManager.getInstance().getContactByAgentId(cvi.contactId);
	}
	
	public long getItemId(int position) {
		// TODO Auto-generated method stub
		return position;
	}

	
	
	public View getView(int position, View convertView, ViewGroup parent) {
		return contactViewInfoList.get(position).contactView;
	}

	public List<Contact> getAllSelectedItems(){
		List<Contact> contactsSelected = new ArrayList<Contact>();
		
		for (ContactViewInfo contactsInfo : contactViewInfoList) {
			Contact cont = ContactManager.getInstance().getContactByAgentId(contactsInfo.contactId);
			contactsSelected.add(cont);
		}
		
		return contactsSelected;
	}
	
	public void initialize(){
		List<Contact> localContactList = ContactManager.getInstance().getOtherContactList();
		Contact myContact = ContactManager.getInstance().getMyContact();
		
		for (Contact contact : localContactList) {
			ContactViewInfo cvi = new ContactViewInfo(contact.getPhoneNumber());
			cvi.updateView(contact, myContact);
			contactViewInfoList.add(cvi);
		}
		
	}
	
	public void update(){
		Contact myContact = ContactManager.getInstance().getMyContact();
		
		//retrieve the list of ID of newly added contacts
		List<String> newlyAdded = ContactManager.getInstance().getLastAddedContacts();
		//retrieve the list of ID of recently deleted contacts
		List<String> recentlyDeleted = ContactManager.getInstance().getLastDeletedContacts();
		
		//Ok, now we should update the views
		//For each newly added contact add it
		for (String contactId : newlyAdded) {
			ContactViewInfo cvi = new ContactViewInfo(contactId);
			contactViewInfoList.add(cvi);
		}
		
		//Now for all deleted contact delete it
		for (String contactId : recentlyDeleted) {
			ContactViewInfo cvi = new ContactViewInfo(contactId);
			contactViewInfoList.remove(cvi);
		}
		
		//At the end update all contacts
		for (ContactViewInfo viewInfo : contactViewInfoList) {
			viewInfo.updateView(ContactManager.getInstance().getContactByAgentId(viewInfo.contactId), myContact);
		}
	}
	
	private class ContactViewInfo {
		
		public boolean equals(Object o) {
			boolean retVal =false;
			
			if (o instanceof ContactViewInfo){
				ContactViewInfo cvInfo = (ContactViewInfo) o;
				retVal = cvInfo.contactId.equals(this.contactId);
			}
				
			return retVal;
		}

		public View contactView;
		public String contactId;
		
		public ContactViewInfo(String contactId){
			this.contactId = contactId;	
		}
		
		
		public void setStyle(int style){
			
		}
		
		public void updateView(Contact c, Contact myContact){
			//this contact is new and has no view
			if (contactView == null){
				//create a new view and start filling it
				contactView = inflater.inflate(R.layout.element_layout, null, null);
				//Set the contact name
				TextView contactNameTxt = (TextView) contactView.findViewById(R.id.contact_name);
				contactNameTxt.setText(c.getName());
			}
			
			TextView contactDistTxt = (TextView) contactView.findViewById(R.id.contact_dist);
			Location myContactLoc = myContact.getLocation();
			float distInMeters  = myContactLoc.distanceTo(c.getLocation());
			float distInKm = distInMeters / 1000.0f;
			String distKmAsString = String.valueOf(distInKm);
			StringBuffer buf = new StringBuffer(distKmAsString);
			buf.append(" km");
			buf.append(" from me");
			contactDistTxt.setText(buf.toString());
		}
		
	}
	
}


	

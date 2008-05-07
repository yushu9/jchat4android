package com.tilab.msn;

import jade.util.Logger;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.android.maps.MyLocationOverlay;

import android.content.Context;
import android.content.Resources;
import android.graphics.Typeface;
import android.location.Location;
import android.telephony.gsm.stk.TextAttribute;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewInflate;
import android.webkit.WebSettings.TextSize;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextSetting;
import android.widget.TextView;

public class ContactListAdapter extends BaseAdapter {
	
	private final Logger myLogger = Logger.getMyLogger(this.getClass().getName());
	private List<ContactViewInfo> contactViewInfoList;
	private Context context;
	private ViewInflate inflater;	
	
	public ContactListAdapter(Context c){
		context = c;
	    inflater = (ViewInflate)context.getSystemService(Context.INFLATE_SERVICE);		
	    contactViewInfoList = new ArrayList<ContactViewInfo>();
	}
		
	public int getCount() {		
		return contactViewInfoList.size();
	}

	
	public Object getItem(int arg0) {
		ContactViewInfo cvi = contactViewInfoList.get(arg0);		
		return ContactManager.getInstance().getContactByAgentId(cvi.contactId);
	}
	
	public long getItemId(int position) {
		return position;
	}

	public void clear(){
		contactViewInfoList.clear();
	}	
	
	public View getView(int position, View convertView, ViewGroup parent) {
		return contactViewInfoList.get(position).contactView;
	}

	public List<Contact> getAllSelectedItems(){
		List<Contact> contactsSelected = new ArrayList<Contact>();
		
		for (ContactViewInfo contactViewInfo : contactViewInfoList) {
			View v = contactViewInfo.contactView;
			CheckBox cb = (CheckBox) v.findViewById(R.id.contact_check_box);
			if (cb.isChecked()){
				Contact cont = ContactManager.getInstance().getContactByAgentId(contactViewInfo.contactId);
				contactsSelected.add(cont);
			}
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
	
	public void update(ContactListChanges changes){
		Contact myContact = ContactManager.getInstance().getMyContact();
		
		if (changes.contactsAdded.size() > 0 || changes.contactsDeleted.size() > 0)
			myLogger.log(Logger.INFO, "Modifications reported from updating thread...\n " +
					"Contacts added: " + changes.contactsAdded.size() + 
					"\nContacts deleted: " + changes.contactsDeleted.size());		
		
		//Ok, now we should update the views
		//For each newly added contact add it
		for (String contactId : changes.contactsAdded) {
			ContactViewInfo cvi = new ContactViewInfo(contactId);
			contactViewInfoList.add(cvi);
		}
		
		//Now for all deleted contact delete it
		for (String contactId : changes.contactsDeleted) {
			ContactViewInfo cvi = new ContactViewInfo(contactId);
			contactViewInfoList.remove(cvi);
		}
		
		//At the end update all contacts
		for (ContactViewInfo viewInfo : contactViewInfoList) {
			viewInfo.updateView(ContactManager.getInstance().getContactByAgentId(viewInfo.contactId), myContact);
		}
	}
	
	private class ContactViewInfo {
		
		public static final int ONLINE_STYLE=-2;
		public static final int OFFLINE_STYLE=-3;
		public View contactView;
		public String contactId;

		public boolean equals(Object o) {
			boolean retVal =false;
			
			if (o instanceof ContactViewInfo){
				ContactViewInfo cvInfo = (ContactViewInfo) o;
				retVal = cvInfo.contactId.equals(this.contactId);
			}
				
			return retVal;
		}

	
	
		public ContactViewInfo(String contactId){
			this.contactId = contactId;	
		}
		
		
		private void setStyle(int style){
			TextView contactName = (TextView) contactView.findViewById(R.id.contact_name);
			TextView contactDist = (TextView) contactView.findViewById(R.id.contact_dist);
			CheckBox contactCheckBox = (CheckBox) contactView.findViewById(R.id.contact_check_box);
			Resources res = context.getResources();
			
			switch (style) {
				case ONLINE_STYLE:
					contactName.setTextColor(res.getColor(R.color.online_contact_color));
					contactName.setTextSize(TypedValue.COMPLEX_UNIT_SP,20);
					contactDist.setTextColor(res.getColor(R.color.online_contact_color));
					contactDist.setTextSize(TypedValue.COMPLEX_UNIT_SP,15);
					contactCheckBox.setEnabled(true);
			break;

				case OFFLINE_STYLE:
					contactName.setTextColor(res.getColor(R.color.offline_contact_color));
					contactName.setTextSize(TypedValue.COMPLEX_UNIT_SP,20);
					contactDist.setTextColor(res.getColor(R.color.offline_contact_color));
					contactDist.setTextSize(TypedValue.COMPLEX_UNIT_SP,15);
					contactCheckBox.setEnabled(false);
				break;
			}
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
			
			
			if (c.isOnline()){
				setStyle(ONLINE_STYLE);
				Location myContactLoc = myContact.getLocation();
				float distInMeters  = myContactLoc.distanceTo(c.getLocation());
				float distInKm = distInMeters / 1000.0f;
				String distKmAsString = String.valueOf(distInKm);
				StringBuffer buf = new StringBuffer(distKmAsString);
				buf.append(" km");
				buf.append(" from me");
				contactDistTxt.setText(buf.toString());
			} else {
				setStyle(OFFLINE_STYLE);
				contactDistTxt.setText(context.getResources().getText(R.string.label_for_offline));
			}
		}		
	}
	
}


	

package com.tilab.msn;

import jade.util.Logger;

import java.util.ArrayList;
import java.util.Map;

import java.util.List;

import android.content.Context;
import android.content.Resources;

import android.location.Location;

import android.util.TypedValue;

import android.view.View;
import android.view.ViewGroup;
import android.view.ViewInflate;

import android.widget.BaseAdapter;
import android.widget.CheckBox;

import android.widget.TextView;

// TODO: Auto-generated Javadoc
/**
 * The Class ContactListAdapter.
 */
public class ContactListAdapter extends BaseAdapter {
	
	/** The my logger. */
	private final Logger myLogger = Logger.getMyLogger(this.getClass().getName());
	
	/** The contact view info list. */
	private List<ContactViewInfo> contactViewInfoList;
	
	/** The context. */
	private Context context;
	
	/** The inflater. */
	private ViewInflate inflater;	
	
	/**
	 * Instantiates a new contact list adapter.
	 * 
	 * @param c the c
	 */
	public ContactListAdapter(Context c){
		context = c;
	    inflater = (ViewInflate)context.getSystemService(Context.INFLATE_SERVICE);		
	    contactViewInfoList = new ArrayList<ContactViewInfo>();
	}
		
	/* (non-Javadoc)
	 * @see android.widget.Adapter#getCount()
	 */
	public int getCount() {		
		return contactViewInfoList.size();
	}

	
	/**
	 * Uncheck all.
	 */
	public void uncheckAll(){
		for (ContactViewInfo cview : contactViewInfoList) {
			cview.uncheck();
		} 
	}
	
	/* (non-Javadoc)
	 * @see android.widget.Adapter#getItem(int)
	 */
	public Object getItem(int arg0) {
		ContactViewInfo cvi = contactViewInfoList.get(arg0);		
		return cvi.contactId;
	}
	
	/* (non-Javadoc)
	 * @see android.widget.Adapter#getItemId(int)
	 */
	public long getItemId(int position) {
		return position;
	}

	/**
	 * Clear.
	 */
	public void clear(){
		contactViewInfoList.clear();
	}	
	
	/* (non-Javadoc)
	 * @see android.widget.Adapter#getView(int, android.view.View, android.view.ViewGroup)
	 */
	public View getView(int position, View convertView, ViewGroup parent) {
		View v= null;
		//FIXME: sometimes I get -1 as position from the callback! Is this an Android bug?
		//This try / catch should provide a quick and dirty workaround... 
		try {
			v = contactViewInfoList.get(position).contactView;
		} catch(IndexOutOfBoundsException ex){
			myLogger.log(Logger.SEVERE,"ERROR: a runtime exception should be thrown! Value of position is: " + position );
			//I returns view at pos 0 when I got -1, but we should check!!!!
			v = contactViewInfoList.get(0).contactView;
		}
		return v;
	}

	/**
	 * Gets the all selected item ids.
	 * 
	 * @return the all selected item ids
	 */
	public List<String> getAllSelectedItemIds(){
		List<String> contactsSelectedIds = new ArrayList<String>();
		
		for (ContactViewInfo contactViewInfo : contactViewInfoList) {
			View v = contactViewInfo.contactView;
			CheckBox cb = (CheckBox) v.findViewById(R.id.contact_check_box);
			if (cb.isChecked()){
				contactsSelectedIds.add(contactViewInfo.contactId);
			}
		}
		
		return contactsSelectedIds;
	}
	
	/**
	 * Initialize.
	 */
	public void initialize(){
		Map<String, Contact> localContactMap = ContactManager.getInstance().getAllContacts();
		Contact myContact = ContactManager.getInstance().getMyContact();
		Map<String, ContactLocation> contactLocMap = ContactManager.getInstance().getAllContactLocations();
		ContactLocation myCloc = ContactManager.getInstance().getMyContactLocation();
		
		for (String phoneNum : localContactMap.keySet()) {
			ContactViewInfo cvi = new ContactViewInfo(phoneNum);
			cvi.updateView(localContactMap.get(phoneNum), contactLocMap.get(phoneNum), myContact, myCloc);
			contactViewInfoList.add(cvi);
		}
		
	}
	
	/**
	 * Update.
	 * 
	 * @param changes the changes
	 */
	public void update(ContactListChanges changes){
		Contact myContact = ContactManager.getInstance().getMyContact();
		ContactLocation cMyLoc = ContactManager.getInstance().getMyContactLocation();
		
		Map<String,Contact> cMap = ContactManager.getInstance().getAllContacts();
		Map<String,ContactLocation> cLocMap = ContactManager.getInstance().getAllContactLocations();
		
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
			viewInfo.updateView(cMap.get(viewInfo.contactId), cLocMap.get(viewInfo.contactId), myContact, cMyLoc);
		}
	}
	
	/**
	 * The Class ContactViewInfo.
	 */
	private class ContactViewInfo {
		
		/** The Constant ONLINE_STYLE. */
		public static final int ONLINE_STYLE=-2;
		
		/** The Constant OFFLINE_STYLE. */
		public static final int OFFLINE_STYLE=-3;
		
		/** The contact view. */
		public View contactView;
		
		/** The contact id. */
		public String contactId;

		/* (non-Javadoc)
		 * @see java.lang.Object#equals(java.lang.Object)
		 */
		public boolean equals(Object o) {
			boolean retVal =false;
			
			if (o instanceof ContactViewInfo){
				ContactViewInfo cvInfo = (ContactViewInfo) o;
				retVal = cvInfo.contactId.equals(this.contactId);
			}
				
			return retVal;
		}

		/**
		 * Uncheck.
		 */
		public void uncheck(){
			CheckBox contactCheckBox = (CheckBox) contactView.findViewById(R.id.contact_check_box);
			contactCheckBox.setChecked(false);
		}
	
		/**
		 * Instantiates a new contact view info.
		 * 
		 * @param contactId the contact id
		 */
		public ContactViewInfo(String contactId){
			this.contactId = contactId;	
		}
		
		
		/**
		 * Sets the style.
		 * 
		 * @param style the new style
		 */
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
		
		/**
		 * Update view.
		 * 
		 * @param c the c
		 * @param cloc the cloc
		 * @param myContact the my contact
		 * @param cMyLoc the c my loc
		 */
		public void updateView(Contact c, ContactLocation cloc, Contact myContact, ContactLocation cMyLoc){
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
				float distInMeters  = cMyLoc.distanceTo(cloc);
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


	

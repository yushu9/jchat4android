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

/**
 * Adapter describing the content of the main contact listview.
 * Implements a custom listview with CheckBoxes inside each item.
 * <p>
 * It uses a custom layout that is dynamically loaded from xml for list entries
 * 
 * @author Cristina Cuccè
 * @author Marco Ughetti 
 * @author Stefano Semeria
 * @author Tiziana Trucco
 * @version 1.0 
 */
public class ContactListAdapter extends BaseAdapter {
	
	/** 
	 * Logger for debugging purposes 
	 */
	private final Logger myLogger = Logger.getMyLogger(this.getClass().getName());
	
	/** 
	 * List of the Views used for diplaying contact information in contact list. 
	 */
	private List<ContactViewInfo> contactViewInfoList;
	
	/** 
	 * Application context for this adapter. 
	 */
	private Context context;
	
	/** 
	 * The system inflater used to inflate views from xml. 
	 */
	private ViewInflate inflater;	
	
	/**
	 * Instantiates a new contact list adapter.
	 * 
	 * @param c the application context (activity)
	 */
	public ContactListAdapter(final Context c){
		context = c;
	    inflater = (ViewInflate)context.getSystemService(Context.INFLATE_SERVICE);		
	    contactViewInfoList = new ArrayList<ContactViewInfo>();
	}
		
	/**
	 * Returns the number of contact views in this list
	 * 
	 * @return number of {@link ContactViewInfo} in this adapter
	 */ 
	public final int getCount() {		
		return contactViewInfoList.size();
	}

	
	/**
	 * Uncheck all the items in this list (deselect every checkbox)
	 */
	public final void uncheckAll(){
		for (ContactViewInfo cview : contactViewInfoList) {
			cview.uncheck();
		} 
	}
	
	/**
	 * Returns the contact in the given position
	 * 
	 *  @param index index of the contact
	 *  @return the contact in the given position
	 */
	public final Object getItem(final int index) {
		ContactViewInfo cvi = contactViewInfoList.get(index);		
		return cvi.contactId;
	}
	
	/**
	 * Returns an id for the view
	 * 
	 * @param index of the item
	 * @return id for the item
	 */
	public final long getItemId(final int position) {
		return position;
	}

	/**
	 * Resets the list of contacts
	 * 
	 */
	public final void clear(){
		contactViewInfoList.clear();
	}	
	
	/** 
	 * Returns a view representing the contact in the given position. The view is stored inside a {@link ContactViewInfo}
	 * object.
	 * 
	 *  @see BaseAdapter
	 */
	public final View getView(final int position, final View convertView, final ViewGroup parent) {
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
	 * Retrieves all the Ids (phone numbers) of the checked items in the list
	 * 
	 * @return list of all checked items
	 */
	public final List<String> getAllSelectedItemIds(){
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
	 * Initialize the adapter by populating it with all available contacts and by creating the {@link ContactViewInfo}
	 * for each item. Every other modification shall be incremental.
	 * 
	 */
	public final void initialize(){
		Map<String, Contact> localContactMap = ContactManager.getInstance().getAllContacts();
		Contact myContact = ContactManager.getInstance().getMyContact();
		Map<String, ContactLocation> contactLocMap = ContactManager.getInstance().getAllContactLocations();
		ContactLocation myCloc = ContactManager.getInstance().getMyContactLocation();
		
		for (String phoneNum : localContactMap.keySet()) {
			ContactViewInfo cvi = new ContactViewInfo(phoneNum);
			cvi.updateView(localContactMap.get(phoneNum), contactLocMap.get(phoneNum),  myCloc);
			contactViewInfoList.add(cvi);
		}
		
	}
	
	/**
	 * Update the contact list adapter by using a list of changes. This method shall be called by the agent
	 * (posted on the UI thread) with a list of all new contacts to add and all contacts to remove.
	 * 
	 * @param changes list of all changes (list of all new contacts and removed contacts)
	 */
	public final void update(final ContactListChanges changes){
		ContactLocation cMyLoc = ContactManager.getInstance().getMyContactLocation();
		
		Map<String,Contact> cMap = ContactManager.getInstance().getAllContacts();
		Map<String,ContactLocation> cLocMap = ContactManager.getInstance().getAllContactLocations();
		
		if (changes.contactsAdded.size() > 0 || changes.contactsDeleted.size() > 0) {
			myLogger.log(Logger.INFO, "Modifications reported from updating thread...\n " +
					"Contacts added: " + changes.contactsAdded.size() + 
					"\nContacts deleted: " + changes.contactsDeleted.size());
		}		
		
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
			viewInfo.updateView(cMap.get(viewInfo.contactId), cLocMap.get(viewInfo.contactId),  cMyLoc);
		}
	}
	
	/**
	 * Represents each of the item in the list (as a view)
	 */
	private class ContactViewInfo {
		
		/** 
		 * Drawing style for online contacts. 
		 */
		public static final int ONLINE_STYLE=-2;
		
		/** 
		 * Drawing style for offline contacts.
		 */
		public static final int OFFLINE_STYLE=-3;
		
		/** 
		 * View associated to the item of the list described by this {@link ContactViewInfo}. 
		 */
		public View contactView;
		
		/** 
		 * The contact phone number. 
		 */
		public String contactId;

		/**
		 * Return true if the two {@link ContactViewInfo} are related to the same contact 
		 * (the two contacts have the same phone number)
		 * 
		 * @param o object to be compared
		 * @return true if objects are equals, false otherwise
		 */
		public boolean equals(final Object o) {
			boolean retVal =false;
			
			if (o instanceof ContactViewInfo){
				ContactViewInfo cvInfo = (ContactViewInfo) o;
				retVal = cvInfo.contactId.equals(this.contactId);
			}
				
			return retVal;
		}

		/**
		 * Uncheck the checkbox inside this view
		 */
		public void uncheck(){
			CheckBox contactCheckBox = (CheckBox) contactView.findViewById(R.id.contact_check_box);
			contactCheckBox.setChecked(false);
		}
	
		/**
		 * Instantiates a new contact view info.
		 * 
		 * @param contactId the contact phone number
		 */
		public ContactViewInfo(final String contactId){
			this.contactId = contactId;	
		}
		
		
		/**
		 * Sets the drawing style for this contact (ONLINE/OFFLINE)
		 * 
		 * @param style the new style
		 */
		private void setStyle(final int style){
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
				default:
					break;
				
			}
		}
		
		/**
		 * Updates the content of this view, based on the parameters passed.
		 * 
		 * @param c the contact this view is related to
		 * @param cloc the location of the contact 
		 * @param cMyLoc the location of the my contact
		 */
		public void updateView(final Contact c, final ContactLocation cloc,  final ContactLocation cMyLoc){
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


	

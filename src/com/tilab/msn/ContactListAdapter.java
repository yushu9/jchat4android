package com.tilab.msn;

import java.util.List;

import android.content.Context;
import android.content.Resources;
import android.graphics.Typeface;
import android.location.Location;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

public class ContactListAdapter extends BaseAdapter {

	private List<Contact> otherContactList;
	private Context context;
	private Location myLocation;
	
	public ContactListAdapter(Context c){
		context = c;
		myLocation = ContactManager.getInstance().getMyContact().getLocation();
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
		
		ContactListViewItem item;
		Contact c = otherContactList.get(position);
		
		
		//If we don't rceive a view
		if (convertView == null){
			item = new ContactListViewItem(context, c, myLocation);
		} else {
			item = (ContactListViewItem) convertView;
			item.setContactName(c.getName());
			float distance = myLocation.distanceTo(c.getLocation())/1000.0f;
			item.setContactDistance(distance);
			item.setContactStatus(c.isOnline());
		}
		
		return item;		
	}

	//This class reperesents a single item of the list
	private class ContactListViewItem extends LinearLayout {

		private LinearLayout layout;
		private ImageView iv;
		private TextView contactNameTextView;
		private TextView contactDistanceTextView;
		
		private final int CONTACT_NAME_SIZE = 16;
		private final int CONTACT_DISTANCE_SIZE =14 ;
		
		public ContactListViewItem(Context context, Contact c, Location myContactLocation) {
			super(context);
		
			// TODO Auto-generated constructor stub
			this.setOrientation(LinearLayout.HORIZONTAL);
			layout = new LinearLayout(context);
			layout.setOrientation(LinearLayout.VERTICAL);
			iv = new ImageView(context);
			this.addView(iv, new LinearLayout.LayoutParams( 
                    LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
			contactNameTextView = new TextView(context);
			contactDistanceTextView = new TextView(context);
			
			contactNameTextView.setTypeface(Typeface.DEFAULT_ITALIC);
			contactNameTextView.setTextSize(TypedValue.COMPLEX_UNIT_PX, CONTACT_NAME_SIZE);
			contactNameTextView.setText(c.getName());
			
			//The condition for showing distances is that the update of myContact location is started
			//and the contact is online
			if ( c.isOnline()) {
				Location contactLoc = c.getLocation();
				float distKm = myContactLocation.distanceTo(contactLoc) /1000.0f;
				contactDistanceTextView.setText(String.valueOf(distKm) + " km");
			} else {
				contactDistanceTextView.setText("Not Available");
			}
			contactDistanceTextView.setTextSize(TypedValue.COMPLEX_UNIT_PX, CONTACT_DISTANCE_SIZE);
			
			layout.addView(contactNameTextView, new LinearLayout.LayoutParams( 
                    LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
			layout.addView(contactDistanceTextView, new LinearLayout.LayoutParams( 
                    LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
			
			this.addView(layout, new LinearLayout.LayoutParams( 
                    LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
			
			setContactStatus(c.isOnline());
		}
		
		
		public void setIcon(int res){
			iv.setImageDrawable(getContext().getResources().getDrawable(res));
			this.addView(iv,0);
		}
		
		public void setContactName(String name){
			contactNameTextView.setText(name);
		}
		
		public void setContactDistance(float distance){
			contactDistanceTextView.setText(String.valueOf(distance));
		}
		
		public CharSequence getContactDistance(){
			return contactDistanceTextView.getText();
		}
		
		public CharSequence getContactName(){
			return contactNameTextView.getText();
		}
		
		public void setContactStatus(boolean online){
			Resources res = getContext().getResources();
			
			if (online){
				contactNameTextView.setTextColor(res.getColor(R.color.online_contact_color));
				contactDistanceTextView.setTextColor(res.getColor(R.color.online_contact_color));
			} else {
				contactNameTextView.setTextColor(res.getColor(R.color.offline_contact_color));
				contactDistanceTextView.setTextColor(res.getColor(R.color.offline_contact_color));
			}
		}
	}
}


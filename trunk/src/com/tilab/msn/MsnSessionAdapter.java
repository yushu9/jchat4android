package com.tilab.msn;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Random;

import android.graphics.Color;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewInflate;
import android.widget.BaseAdapter;
import android.widget.TextView;

public class MsnSessionAdapter extends BaseAdapter {

	private LinkedList<View> messageViews;
	private ViewInflate theInflater;
	private MsnSession theSession;
	private ContactColorGenerator colorGenerator;
	
	public MsnSessionAdapter(ViewInflate vi){
		theInflater = vi;
		messageViews = new LinkedList<View>();
		colorGenerator = new ContactColorGenerator();
	}
	
	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		return messageViews.size();
	}
	
	
	//Set a new Session
	//Each time a session is set we clear the list of messages and rebuild it using messages in the new session
	public void setNewSession(MsnSession session){
		theSession = session;
		messageViews.clear();
		
		List<MsnSessionMessage> messages = theSession.getMessageList();
		for (MsnSessionMessage msnSessionMessage : messages) {
			addMessageView(msnSessionMessage);
		}
	}
	
	//Create a new view for the given message
	public void addMessageView(MsnSessionMessage msg){
		View messageView = theInflater.inflate(R.layout.session_msg_layout, null, null);
	
		TextView senderNameTxtView = (TextView) messageView.findViewById(R.id.sender_name);
		senderNameTxtView.setText(msg.getSenderName());
		senderNameTxtView.setTextColor(colorGenerator.getColor(msg.getSenderNumTel()));
		TextView timeTextView = (TextView) messageView.findViewById(R.id.time_arrived);
		timeTextView.setText(msg.getTimeReceivedAsString());
		TextView contentTextView = (TextView) messageView.findViewById(R.id.message_txt);
		contentTextView.setText(msg.getMessageContent());

		messageViews.addFirst(messageView);
	}
	
	@Override
	public Object getItem(int arg0) {
		// TODO Auto-generated method stub
		List<MsnSessionMessage> messageList = theSession.getMessageList();
		MsnSessionMessage msg = messageList.get(arg0);
		return msg;
	}

	@Override
	public long getItemId(int arg0) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		// TODO Auto-generated method stub
		View v = messageViews.get(position);
		return v;
	}

	//This inner class generates a random color for a given contact phone number 
	private class  ContactColorGenerator{
		private Map<String,Integer>  contactColorMap = new HashMap<String, Integer>();
		private Random generator = new Random();
		
		public int getColor(String contactName){
			Integer color = contactColorMap.get(contactName);
			int colAsInt=0;
			
			//If color not available
			if (color == null){
				//Create a new random one
				colAsInt = Color.rgb(generator.nextInt(236)+10, generator.nextInt(236)+10, generator.nextInt(236)+10);
				//Put it into the map
				contactColorMap.put(contactName, new Integer(colAsInt));
			} else {
				//retrieve the already created color
				colAsInt = color.intValue();
			}
			return colAsInt;
		}
		
	}
}

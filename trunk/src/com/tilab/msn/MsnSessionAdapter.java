package com.tilab.msn;


import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;


import android.content.Resources;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewInflate;
import android.widget.BaseAdapter;
import android.widget.TextView;

// TODO: Auto-generated Javadoc
/**
 * The Class MsnSessionAdapter.
 */
public class MsnSessionAdapter extends BaseAdapter {

	/** The message views. */
	private LinkedList<View> messageViews;
	
	/** The inflater. */
	private ViewInflate theInflater;
	
	/** The session. */
	private MsnSession theSession;
	
	/** The color generator. */
	private ContactColorGenerator colorGenerator;
	
	/**
	 * Instantiates a new msn session adapter.
	 * 
	 * @param vi the vi
	 * @param res the res
	 */
	public MsnSessionAdapter(ViewInflate vi, Resources res){
		theInflater = vi;
		messageViews = new LinkedList<View>();
		colorGenerator = new ContactColorGenerator(res);
	}
	
	/* (non-Javadoc)
	 * @see android.widget.Adapter#getCount()
	 */
	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		return messageViews.size();
	}	
	
	//Set a new Session
	//Each time a session is set we clear the list of messages and rebuild it using messages in the new session
	/**
	 * Sets the new session.
	 * 
	 * @param session the new new session
	 */
	public void setNewSession(MsnSession session){
		theSession = session;
		messageViews.clear();
		
		List<MsnSessionMessage> messages = theSession.getMessageList();
		
		for (MsnSessionMessage msnSessionMessage : messages) {
			addMessageView(msnSessionMessage);
		}
	}
	
	//Create a new view for the given message
	/**
	 * Adds the message view.
	 * 
	 * @param msg the msg
	 */
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
	
	/* (non-Javadoc)
	 * @see android.widget.Adapter#getItem(int)
	 */
	@Override
	public Object getItem(int arg0) {
		// TODO Auto-generated method stub
		List<MsnSessionMessage> messageList = theSession.getMessageList();
		MsnSessionMessage msg = messageList.get(arg0);
		return msg;
	}

	/* (non-Javadoc)
	 * @see android.widget.Adapter#getItemId(int)
	 */
	@Override
	public long getItemId(int arg0) {
		
		return 0;
	}

	/* (non-Javadoc)
	 * @see android.widget.Adapter#getView(int, android.view.View, android.view.ViewGroup)
	 */
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		
		View v = messageViews.get(position);
		return v;
	}

	//This inner class generates a random color for a given contact phone number 
	/**
	 * The Class ContactColorGenerator.
	 */
	private class  ContactColorGenerator{
		
		/** The contact color map. */
		private Map<String,Integer>  contactColorMap; 
		
		/** The color palette. */
		private int[] colorPalette;
		
		/** The counter. */
		private int counter;
		
		/**
		 * Instantiates a new contact color generator.
		 * 
		 * @param res the res
		 */
		public ContactColorGenerator(Resources res){
			contactColorMap = new HashMap<String, Integer>();
			colorPalette = new int[10];
			counter =0;
			loadPalette(res);
			
		}
		
		/**
		 * Load palette.
		 * 
		 * @param res the res
		 */
		private void loadPalette(Resources res){
			colorPalette[0] = res.getColor(R.color.chat_dark_yellow);
			colorPalette[1] = res.getColor(R.color.chat_dark_orange);
			colorPalette[2] = res.getColor(R.color.chat_grass_green);
			colorPalette[3] = res.getColor(R.color.chat_pale_yellow);
			colorPalette[4] = res.getColor(R.color.chat_dark_pink);
			colorPalette[5] = res.getColor(R.color.chat_light_orange);
			colorPalette[6] = res.getColor(R.color.chat_dark_green);
			colorPalette[7] = res.getColor(R.color.chat_olive_green);
			colorPalette[8] = res.getColor(R.color.chat_earth_brown);
			colorPalette[9] = res.getColor(R.color.chat_strong_purple);
		}
		
		/**
		 * Gets the color.
		 * 
		 * @param contactName the contact name
		 * 
		 * @return the color
		 */
		public int getColor(String contactName){
			Integer color = contactColorMap.get(contactName);
			int colAsInt=0;
			
			
			//If color not available
			if (color == null){
				//Create a new random one
				colAsInt = colorPalette[counter];
				//Put it into the map
				contactColorMap.put(contactName, new Integer(colAsInt));
				counter = (counter + 1)% 10;
			} else {
				//retrieve the already created color
				colAsInt = color.intValue();
			}
			return colAsInt;
		}
		
	}
}

package com.tilab.msn;



import jade.lang.acl.ACLMessage;
import android.util.DateUtils;

// TODO: Auto-generated Javadoc
/**
 * This class contains data exchanged during message session
 * For now only text, it could be interesting using a generic approach.
 * 
 * @author s.semeria
 */

public class MsnSessionMessage {
	
	/** The time. */
	private long time;	
	
	/** The message content. */
	private String messageContent;
	
	/** The message sender name. */
	private String messageSenderName;
	
	/** The sender phone num. */
	private String senderPhoneNum;
	
	//Stores the data and save the current date
	/**
	 * Instantiates a new msn session message.
	 * 
	 * @param message the message
	 * @param senderName the sender name
	 * @param senderTel the sender tel
	 */
	public MsnSessionMessage(String message, String senderName, String senderTel){
		this(message,senderName,senderTel, System.currentTimeMillis());
	}
	
	/**
	 * Instantiates a new msn session message.
	 * 
	 * @param msg the msg
	 */
	public MsnSessionMessage(ACLMessage msg){
		this.senderPhoneNum = msg.getSender().getLocalName();
		this.messageSenderName = ContactManager.getInstance().getContact(senderPhoneNum).getName();
		this.messageContent = msg.getContent();
		this.time = System.currentTimeMillis();
	}
	
	/**
	 * Instantiates a new msn session message.
	 * 
	 * @param message the message
	 */
	public MsnSessionMessage(MsnSessionMessage message){
		this.senderPhoneNum = new String(message.senderPhoneNum);
		this.messageSenderName = new String(message.messageSenderName);
		this.messageContent = new String(message.messageContent);
		this.time = message.time;
	}
	
	//
	/**
	 * Instantiates a new msn session message.
	 * 
	 * @param message the message
	 * @param senderName the sender name
	 * @param senderTel the sender tel
	 * @param timestamp the timestamp
	 */
	public MsnSessionMessage(String message, String senderName, String senderTel, long timestamp){
		time = timestamp;
		messageContent = message;
		messageSenderName = senderName;
		senderPhoneNum = senderTel;
	}

	/**
	 * Gets the sender num tel.
	 * 
	 * @return the sender num tel
	 */
	public String getSenderNumTel(){
		return senderPhoneNum;
	}
	
	/**
	 * Gets the time.
	 * 
	 * @return the time
	 */
	public long getTime() {
		return time;
	}

	/**
	 * Gets the message content.
	 * 
	 * @return the message content
	 */
	public String getMessageContent() {
		return messageContent;
	}
	
	/**
	 * Gets the sender name.
	 * 
	 * @return the sender name
	 */
	public String getSenderName(){
		return messageSenderName;
	}
		
	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object o) {
		if ( !(o instanceof MsnSessionMessage) ) {
			return false;
		}
		MsnSessionMessage msg = (MsnSessionMessage) o;
		return (msg.messageContent.equals(messageContent) && msg.messageSenderName.equals(messageSenderName));
	} 
	
	/**
	 * Gets the time received as string.
	 * 
	 * @return the time received as string
	 */
	public String getTimeReceivedAsString(){
		return DateUtils.timeString(time).toString();
	}

	/**
	 * Gets the relative time span as string.
	 * 
	 * @return the relative time span as string
	 */
	public String getRelativeTimeSpanAsString(){
		return DateUtils.getRelativeTimeSpanString(time, System.currentTimeMillis(), DateUtils.SECOND_IN_MILLIS).toString();
	}
	
	
	
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString(){
		StringBuffer buffer = new StringBuffer();
		buffer.append("At ");
		buffer.append(getTimeReceivedAsString());
		buffer.append(" ");
		buffer.append(messageSenderName);
		buffer.append(" says: \n");
		buffer.append(messageContent);
		buffer.append("\n\n");
		return buffer.toString();
	}
}

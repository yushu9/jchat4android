package com.tilab.msn;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * This class contains data exchanged during message session
 * For now only text, it could be interesting using a generic approach
 * @author s.semeria
 *
 */

public class MsnSessionMessage {
	private Date timeReceived;
	private String messageContent;
	private String messageSenderName;
	private SimpleDateFormat formatter; 
	
	private final String formatPattern = "HH.mm.ss ";
	
	//Stores the data and save the current date
	public MsnSessionMessage(String message, String senderName){
		this(message,senderName,new Date());
	}
	
	
	//
	public MsnSessionMessage(String message, String senderName, Date timestamp ){
		timeReceived = timestamp;
		messageContent = message;
		messageSenderName = senderName;
		formatter = new SimpleDateFormat(formatPattern, Locale.ITALIAN);
	}

	public Date getTimeReceived() {
		return timeReceived;
	}

	public String getMessageContent() {
		return messageContent;
	}
	
	public String getSenderName(){
		return messageSenderName;
	}
		
	@Override
	public boolean equals(Object o) {
		if ( !(o instanceof MsnSessionMessage) ) {
			return false;
		}
		MsnSessionMessage msg = (MsnSessionMessage) o;
		return (msg.messageContent.equals(messageContent));
	} 
	
	public String getTimeReceivedAsString(){
		return formatter.format(timeReceived);
	}
	
	@Override
	public String toString(){
		StringBuffer buffer = new StringBuffer();
		buffer.append("At ");
		buffer.append(getTimeReceivedAsString());
		buffer.append(" ");
		buffer.append(messageSenderName);
		buffer.append(" says: \n");
		buffer.append(messageContent);
		return buffer.toString();
	}
}

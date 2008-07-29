/*****************************************************************
 jChat is a  chat application for Android based on JADE
  Copyright (C) 2008 Telecomitalia S.p.A. 
 
 GNU Lesser General Public License

 This is free software; you can redistribute it and/or
 modify it under the terms of the GNU Lesser General Public
 License as published by the Free Software Foundation, 
 version 2.1 of the License. 

 This software is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 Lesser General Public License for more details.

 You should have received a copy of the GNU Lesser General Public
 License along with this software; if not, write to the
 Free Software Foundation, Inc., 59 Temple Place - Suite 330,
 Boston, MA  02111-1307, USA.
 *****************************************************************/

package it.telecomitalia.jchat;

import java.util.List;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.IntentReceiver;
import android.os.Bundle;
import android.os.Handler;
import android.telephony.gsm.SmsManager;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

/**
 * Activity that allows a user to write an SMS message text and to send it to a contact.
 * SMS functionalities are provided by SMSManager class, and a custom IntentReceiver is used to receive 
 * all messages.
 * <p>
 * Please note that the emulator for now always report success after sending an SMS  
 * 
 * @author Cristina Cucè
 * @author Marco Ughetti 
 * @author Stefano Semeria
 * @author Tiziana Trucco
 * @version 1.0
 */

public class SendSMSActivity extends Activity {

	/**
	 * Text body of SMS
	 */
	private EditText smsBody;
	/**
	 * Action that shall be used for SMS correctly sent (It shall be catched by {@link SMSIntentReceiver})
	 */
	public static final String SMS_SENT_ACTION="com.tilab.msn.SMS_SENT";
	/**
	 * Action that shall be used for error in sending SMS (It shall be catched by {@link SMSIntentReceiver})
	 */
	public static final String SMS_ERROR_ACTION="com.tilab.msn.SMS_ERROR";
	/**
	 * Name of SMS address parameter
	 */
	public static final String SMS_ADDRESS_PARAM="SMS_ADDRESS_PARAM";
	/**
	 * Name of SMS address list
	 */
	public static final String SMS_ADDRESS_LIST="SMS_ADDRESS_LIST";
	/**
	 * List of SMS receivers' addresses 
	 */
	private List<String> addresses;

	/**
	 * Time in milliseconds used for targeting the display of toast and statusbar for the sending of SMS (nothing is really sent)
	 * the system just simulates some work
	 */
	private long timeBase;
	/**
	 * Instance of progress dialog shown after sending SMS
	 */
	private ProgressDialog dlg;

	/**
	 * Counter of all messages that shall be sent to all contacts
	 */
	private int messageCounter=0;
	/**
	 * Number of messages that shall be sent to each contact
	 */
	private int numberOfMessages=0;
	/**
	 * Handler used to post delayed Toast (for simulating SMS sending)
	 */
	private Handler handler;
	/**
	 * Customized intent receiver for receiving SMS
	 */
	private IntentReceiver smsReceiver;
	

	
	/**
	 * Overrides Activity.onCreate() for the current activity,
	 * It basically prepares the gui and registers the customized intent receiver providing 
	 * our own IntentFilter.
	 * 
	 */
	protected void onCreate(Bundle icicle) {
		// TODO Auto-generated method stub
		super.onCreate(icicle);
		timeBase=0;
		setContentView(R.layout.sms_dialog);
		messageCounter=0;
		handler = new Handler();
		
		smsReceiver = new SMSIntentReceiver(handler);
		IntentFilter myFilter = new IntentFilter();
		myFilter.addAction(SMS_SENT_ACTION);
		myFilter.addAction(SMS_ERROR_ACTION);
		
		registerReceiver(smsReceiver,myFilter );
		
		Intent dataIntent = getIntent();
	    addresses= (List<String>) dataIntent.getSerializableExtra(SMS_ADDRESS_LIST);
	    
		EditText title = (EditText) super.findViewById(R.id.addressEdt);
		title.setText(formatAdresses(addresses));
	
		smsBody = (EditText) super.findViewById(R.id.smsbodyEdt);
		ImageButton sendBtn = (ImageButton) super.findViewById(R.id.sendsmsBtn);
		
		sendBtn.setOnClickListener( new View.OnClickListener(){

			/**
			 * Handles sending of SMS, dividing the message in sub messages, sending them and showing a progress dialog 
			 */
			public void onClick(View arg0) {
				SmsManager smsMgr = SmsManager.getDefault();
				String smsText = smsBody.getText().toString();
				
				if (smsText.length() > 0){
					
					ImageButton btn = (ImageButton)arg0;
					btn.setEnabled(false);
					List<String> messages = smsMgr.divideMessage(smsText);
					StringBuffer buffer = new StringBuffer();
					
					if (messages.size() > 1) {
						buffer.append("SMS is too long! A total of ");
						buffer.append(messages.size());
						buffer.append(" messages will be sent!");
					}
					
					
					if (buffer.length() > 0){
						Toast.makeText(SendSMSActivity.this, buffer.toString(), 3000).show();
						timeBase=3000;
					}
					
					messageCounter =(SendSMSActivity.this.addresses.size()) * (messages.size()); 
					numberOfMessages= messageCounter;
					
					for (String address : SendSMSActivity.this.addresses) {
						
						Intent sentIntent = new Intent(SendSMSActivity.SMS_SENT_ACTION);
						sentIntent.putExtra(SMS_ADDRESS_PARAM, address);
						
						Intent errorIntent = new Intent(SendSMSActivity.SMS_ERROR_ACTION);
						errorIntent.putExtra(SMS_ADDRESS_PARAM, address);

						for (String msg : messages){
							smsMgr.sendTextMessage(address,null, msg,sentIntent,null,errorIntent);
						}
					}

					dlg = new ProgressDialog(SendSMSActivity.this);
					dlg.setIndeterminate(true);
					dlg.setCancelable(false);
					
					dlg.setMessage("Sending  " + messages.size() + " messages to "  +  SendSMSActivity.this.addresses.size() + " contacts...");
					
					handler.postDelayed(
							new Runnable(){

								public void run() {
									dlg.show();
								}
							}
							, timeBase+1000);
					
					
				} else {
					Toast.makeText(SendSMSActivity.this,SendSMSActivity.this.getText(R.string.error_sms_empty) , 3000).show();
				}
				
			}
			
		});
	}
	
	/**
	 * Returns the current time base for showing Toasts
	 * @return the current time base
	 */
	public long getTimeBase(){
		return timeBase;
	}
	
	/**
	 * Unregisters the customized SMS Intent receiver
	 */
	protected void onDestroy() {
		super.onDestroy();
		unregisterReceiver(smsReceiver);
	}

	/**
	 * Checks if all messages have been sent
	 * 
	 * @return true if all SMS have been sent to all receivers, false otherwise
	 */
	public boolean allMessagesSent() {
		return (messageCounter == 0);
	}
	

	/**
	 * Returns the number of messages to be sent to each contact
	 * @return number of messages to be sent
	 */
	public  int getNumberOfMessages(){
		return numberOfMessages;
	}
	
	/**
	 * Decreases the number of messages that still needs to be sent. 
	 */
	public   void notifyMessageSent(){
		if (messageCounter > 0){
			messageCounter--;
		}
	}

	/**
	 * Prepares a string chaining together all the phone numbers (SMS addresses) using ";" as a separator
	 * @param addresses for all contacts
	 * @return the string containing all phone numbers chained together
	 */
	private String formatAdresses(List<String> addresses){
		StringBuffer buffer = new StringBuffer();
		
		int numOfAddr = addresses.size();
		
		for (int i=0; i < numOfAddr-1; i++){
			buffer.append(addresses.get(i));
			buffer.append("; ");
		}
		
		buffer.append(addresses.get(numOfAddr-1));
		
		return buffer.toString();
	}


	/**
	 * Returns the progress dialog instance
	 * @return the progress dialog
	 */
	public  ProgressDialog getProgressDialog(){
		return dlg;
	}


}

package com.tilab.msn;
import java.util.ArrayList;
import java.util.List;


import jade.lang.acl.ACLMessage;
import jade.util.Logger;

import android.app.Activity;
import android.app.ActivityPendingResult;
import android.app.Notification;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;

import android.widget.Toast;


class IncomingNotificationUpdater {

		private Activity activity;
		private final int MAX_NOTIFICATION_NUMBER=10;
		private static final Logger myLogger = Logger.getMyLogger(IncomingNotificationUpdater.class.getName());
		private List<Integer> notificationList;
		private NotificationManager manager;
		private Handler handle;
		
		private final long NOTIFICATION_UPDATE_DELAY = 1000;
		
		public IncomingNotificationUpdater(Activity act) {
			handle = new Handler();
			activity = act;
			notificationList = new ArrayList<Integer>(MAX_NOTIFICATION_NUMBER);
			manager = (NotificationManager) activity.getSystemService(Context.NOTIFICATION_SERVICE);
		}
	
		public void createSessionNotification(String sessionId){
			handle.post(new AddNotificationRunnable(sessionId));
		}
		
		public void updateSessionNotification(ACLMessage msg) {
				handle.post(new UpdateNotificationRunnable(msg));
		}
		
		public void updateSessionNotificationDelayed(ACLMessage msg){
				handle.postDelayed(new UpdateNotificationRunnable(msg),NOTIFICATION_UPDATE_DELAY);
		}
		
		public void removeSessionNotification(String sessionId){
			manager.cancel(Integer.parseInt(sessionId));
		}
		
		public void removeAllNotifications(){
			
			for (int i =0; i < notificationList.size(); i++) {
				Integer index = notificationList.get(i);
				manager.cancel(index.intValue());
				myLogger.log(Logger.INFO, "Removing notification with ID " + index.intValue());
			}
			notificationList.clear();
		}
		
		
		//This runnable adds an empty notification for a new session 
		private class AddNotificationRunnable implements Runnable {
			
			private String sessionId;
			
			public AddNotificationRunnable(String sessionId){
				this.sessionId = sessionId;
			}
			
			public void run() {
				MsnSession session = MsnSessionManager.getInstance().retrieveSession(sessionId);
				List<Contact> participants = session.getAllParticipants();
				String title = session.toString();
				
				Intent viewChatIntent = new Intent(Intent.VIEW_ACTION);
				viewChatIntent.addCategory(Intent.DEFAULT_CATEGORY);
				viewChatIntent.setLaunchFlags(Intent.NEW_TASK_LAUNCH | Intent.SINGLE_TOP_LAUNCH);
				viewChatIntent.setData(session.getSessionIdAsUri());
				
				int numParticipants = participants.size() +1;
				ActivityPendingResult result = activity.createActivityPendingResult(ContactListActivity.CHAT_ACTIVITY_CLOSED, false);
				viewChatIntent.putExtra(ContactListActivity.ID_ACTIVITY_PENDING_RESULT, result);
				
				Notification notif = new Notification(activity,
							 R.drawable.incoming,
							 "",
							 System.currentTimeMillis(),
							 title,
							 numParticipants + " participants",
							 viewChatIntent,
							 android.R.drawable.app_icon_background,
							 null,
							 null);				      
			        
			 	Integer integer = new Integer(Integer.parseInt(sessionId));
				notificationList.add(integer);
				int index = integer.intValue();
				String logMsg = "New notification added with ID " + index;
				//Add or update notification
				manager.notify(index,notif);
				myLogger.log(Logger.INFO, logMsg);
			}
			
			
			
		}
		
		private String concatNames(List<Contact> contacts){
			StringBuffer output = new StringBuffer();
			Contact myC = ContactManager.getInstance().getMyContact();
			
			for (Contact contact : contacts) {
				output.append(contact.getName());
				output.append(";");
			}
			
			output.append(myC.getName());
			
			return output.toString();
		}
		
		private class UpdateNotificationRunnable implements Runnable{

			private ACLMessage message;
			
			public UpdateNotificationRunnable(ACLMessage msg){
				this.message = msg;
				
			}
				
			public void run() {
				String logMsg;
				//get the message id from the conversation id hashcode
				String indexStr =  message.getConversationId();
				int index = Integer.parseInt(indexStr);
				
				Intent viewChatIntent = new Intent(Intent.VIEW_ACTION);
				viewChatIntent.addCategory(Intent.DEFAULT_CATEGORY);
				viewChatIntent.setLaunchFlags(Intent.NEW_TASK_LAUNCH | Intent.SINGLE_TOP_LAUNCH);
				MsnSession session = MsnSessionManager.getInstance().retrieveSession(message.getConversationId());
				viewChatIntent.setData(session.getSessionIdAsUri());
				String phoneNumber= message.getSender().getLocalName();
				Contact cont = ContactManager.getInstance().getContact(phoneNumber);
				
				ActivityPendingResult result = activity.createActivityPendingResult(ContactListActivity.CHAT_ACTIVITY_CLOSED, false);
				viewChatIntent.putExtra(ContactListActivity.ID_ACTIVITY_PENDING_RESULT, result);
				
				Toast.makeText(activity, "New Message arrived from " + cont.getName() , 3000).show();
				
				Notification notif = new Notification(activity,
							 R.drawable.chat,
							 "A Message is arrived",
							 System.currentTimeMillis(),
							 session.toString(),
							 "Msg from " + cont.getName(),
							 viewChatIntent,
							 android.R.drawable.app_icon_background,
							 null,
							 null);
				
				//If we have a new notification we store apart its id for canceling
				logMsg = "Updated existing notification with ID " + index;
				
				
				//Add or update notification
				manager.notify(index,notif);
				myLogger.log(Logger.INFO, logMsg);
			}
			
		}
	
		public void showDisconnectionToast(String contactName){
			activity.runOnUIThread(new DisconnectionToastRunnable(contactName));
		}
		
		private class DisconnectionToastRunnable implements Runnable{

			private String contactName;
			
			public DisconnectionToastRunnable(String contactName){
				this.contactName = contactName;
			}
			
			public void run() {
				// TODO Auto-generated method stub				
				Toast.makeText(activity, contactName + " is now offline!", 3000).show();
			}
			
		}
}

package com.tilab.msn;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.android.maps.MyLocationOverlay;

import jade.core.AID;
import jade.lang.acl.ACLMessage;
import jade.util.Logger;
import jade.util.leap.Iterator;
import android.app.Activity;
import android.app.Notification;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.util.DateUtils;


class IncomingNotificationUpdater {

		private Activity activity;
		private final int MAX_NOTIFICATION_NUMBER=10;
		private static final Logger myLogger = Logger.getMyLogger(IncomingNotificationUpdater.class.getName());
		private List<Integer> notificationList;
		private NotificationManager manager;
		
		public IncomingNotificationUpdater(Activity act) {
		
			activity = act;
			notificationList = new ArrayList<Integer>(MAX_NOTIFICATION_NUMBER);
			manager = (NotificationManager) activity.getSystemService(Context.NOTIFICATION_SERVICE);
		}
	
		public void postMessageNotification(ACLMessage msg) {
			
				activity.runOnUIThread(new PostNotificationRunnable(msg));
				
		}
		
		public void removeSessionNotification(String sessionId){
			manager.cancel(sessionId.hashCode());
		}
		
		public void removeAllNotifications(){
			
			for (int i =0; i < notificationList.size(); i++) {
				Integer index = notificationList.get(i);
				manager.cancel(index.intValue());
				myLogger.log(Logger.INFO, "Removing notification with ID " + index.intValue());
			}
			notificationList.clear();
		}
		
		private class PostNotificationRunnable implements Runnable{

			private ACLMessage message;
			
			public PostNotificationRunnable(ACLMessage msg){
				this.message = msg;
			}
				
			public void run() {
				String logMsg;
				//get the message id from the conversation id hashcode
				int index =  message.getConversationId().hashCode();
				Integer indexAsInteger = new Integer(index);
				
				Intent viewChatIntent = new Intent(Intent.VIEW_ACTION);
				viewChatIntent.addCategory(Intent.DEFAULT_CATEGORY);
				MsnSession session = MsnSessionManager.getInstance().retrieveSession(message.getConversationId());
				viewChatIntent.setData(session.getSessionIdAsUri());
				
				Contact cont = ContactManager.getInstance().getContactByAgentId(message.getSender().getLocalName());
				
				Notification notif = new Notification(activity,
							 R.drawable.incoming,
							 "Instant Message is arrived",
							 System.currentTimeMillis(),
							 cont.getName(),
							 "says: " + message.getContent(),
							 viewChatIntent,
							 android.R.drawable.app_icon_background,
							 null,
							 null);
				
				//If we have a new notification we store apart its id for canceling
				if (!notificationList.contains(indexAsInteger)){
					notificationList.add(indexAsInteger);
					logMsg = "New notification added with ID " + index;
				} else {
					logMsg = "Updated existing notification with ID " + index;
				}
				
				//Add or update notification
				manager.notify(index,notif);
				myLogger.log(Logger.INFO, logMsg);
			}
			
		}
}

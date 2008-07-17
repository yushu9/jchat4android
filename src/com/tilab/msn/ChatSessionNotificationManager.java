package com.tilab.msn;
import jade.util.Logger;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.app.ActivityPendingResult;
import android.app.Notification;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;


/**
 * Manager class for incoming  notifications. 
 * <p>
 * Provides support for adding, removing notifications or updating existing notifications in status bar 
 * This class should be similar to the Android <code>NotificationManager</code> but provides the functionality of removing
 * all notifications
 * 
 * @author Cristina Cuccè
 * @author Marco Ughetti 
 * @author Stefano Semeria
 * @author Tiziana Trucco
 * @version 1.0 
 */
class ChatSessionNotificationManager {

		/**
		 * The instance of the ChatSessionNotificationManager
		 */
		private static ChatSessionNotificationManager theInstance;
	
		/** 
		 * Reference to an activity (needed to post notifications). 
		 * It seems it's not really important which Activity to use 
		 */
		private Activity activity;
		
		/** 
		 * Max number of notifications that can be shown to the user   
		 */
		private final int MAX_NOTIFICATION_NUMBER=10;
		
		/** 
		 * Instance of the logger, used for debugging 
		 */
		private static final Logger myLogger = Logger.getMyLogger(ChatSessionNotificationManager.class.getName());
		
		/** 
		 * List of all notification displayed at a specific time. 
		 */
		private List<Integer> notificationList;
		
		/**
		 * Instance of the notification manager used to display/remove the notifications 
		 */
		private NotificationManager manager;
		
		
		/**
		 * Instantiates a new chat session notification manager.
		 * 
		 * @param act Instance of the activity needed to instantiate the notification manager 
		 */
		public ChatSessionNotificationManager(Activity act) {
			activity = act;
			notificationList = new ArrayList<Integer>(MAX_NOTIFICATION_NUMBER);
			manager = (NotificationManager) activity.getSystemService(Context.NOTIFICATION_SERVICE);
		}
	
	
	
		
		
		/**
		 * Adds a session notification on the status bar. Must be called by UI thread.
		 * 
		 * @param sessionId id of the session that must be notified
		 */
		public void addNewSessionNotification(String sessionId){
			int index = Integer.parseInt(sessionId);
			Notification sessionNotif = makeSessionNotification(sessionId);
			addNotification(index, sessionNotif);
		}
		
		/** 
		 * Adds a new message notification on the status bar. Must be called by UI thread.
		 *
		 * @param sessionId Id of the session related to this notification
		 * @param msg session message that will be notified
		 */
		public void addNewMsgNotification(String sessionId, MsnSessionMessage msg){
			int index = Integer.parseInt(sessionId);
			Notification sessionNotif = makeMsgNotification(sessionId, msg);
			addNotification(index, sessionNotif);
		}
		
		/**
		 * Adds a <code>Notification</code> object on the status bar with the given id. It keeps track of the notification in a list. 
		 * 
		 * @param index index for this notification ()
		 * @param notif the notification to be added
		 * @see Notification
		 */
		private void addNotification(int index, Notification notif){
			Integer indexAsInteger = Integer.valueOf(index);
			
			if (!notificationList.contains(indexAsInteger)){
				notificationList.add(indexAsInteger);
			}
			
			manager.notify(index,notif);
		}
		
		
		/**
		 * Utility method that creates a session notification object to be added
		 * 
		 * @param sessionId id of the session to be notified
		 * @return the Notification object
		 */
		private Notification makeSessionNotification(String sessionId){
			MsnSession session = MsnSessionManager.getInstance().retrieveSession(sessionId);
			int numberOfParticipants = session.getParticipantCount();
			String title = session.toString();
			
			Intent viewChatIntent = new Intent(Intent.VIEW_ACTION);
			viewChatIntent.addCategory(Intent.DEFAULT_CATEGORY);
			viewChatIntent.setLaunchFlags(Intent.NEW_TASK_LAUNCH | Intent.SINGLE_TOP_LAUNCH);
			viewChatIntent.setData(session.getSessionIdAsUri());
			
			ActivityPendingResult result = activity.createActivityPendingResult(ContactListActivity.CHAT_ACTIVITY_CLOSED, false);
			viewChatIntent.putExtra(ContactListActivity.ID_ACTIVITY_PENDING_RESULT, result);
			
			Notification notif = new Notification(activity,
						 R.drawable.incoming,
						 "",
						 System.currentTimeMillis(),
						 title,
						 numberOfParticipants + " participants",
						 viewChatIntent,
						 android.R.drawable.app_icon_background,
						 null,
						 null);
			return notif;
		}
		
		
		/**
		 * Utility method that creates a message notification object to be added.
		 * 
		 * @param sessionId id of the session whose message will be notified
		 * @param msg the message to be notified
		 * 
		 * @return the <code>Notification</code> object
		 */
		private Notification makeMsgNotification(String sessionId, MsnSessionMessage msg){
			MsnSession session = MsnSessionManager.getInstance().retrieveSession(sessionId);
			String title = session.toString();
			
			Intent viewChatIntent = new Intent(Intent.VIEW_ACTION);
			viewChatIntent.addCategory(Intent.DEFAULT_CATEGORY);
			viewChatIntent.setLaunchFlags(Intent.NEW_TASK_LAUNCH | Intent.SINGLE_TOP_LAUNCH);
			viewChatIntent.setData(session.getSessionIdAsUri());
			
			ActivityPendingResult result = activity.createActivityPendingResult(ContactListActivity.CHAT_ACTIVITY_CLOSED, false);
			viewChatIntent.putExtra(ContactListActivity.ID_ACTIVITY_PENDING_RESULT, result);
			
			Notification notif = new Notification(activity,
						 R.drawable.chat,
						 "A Message is arrived",
						 System.currentTimeMillis(),
						 title,
						 "Msg from " + msg.getSenderName(),
						 viewChatIntent,
						 android.R.drawable.app_icon_background,
						 null,
						 null);		
			return notif;
		}
		
		/**
		 * Initialize the session manager instance.
		 * Please note that the activity is only needed once: after creating the manager, it 
		 * can be accessed by <code>getInstance()</code>
		 * 
		 * 
		 * @param act the main activity used for sending notification
		 */
		public static  void  create(Activity act){
			
			if (theInstance == null)
				theInstance = new ChatSessionNotificationManager(act);
		
		}
		
		/**
		 * Returns the instance of the notification manager
		 * @return the {@link ChatSessionNotificationManager} instance
		 */
		public static ChatSessionNotificationManager getInstance(){
			return theInstance;
		}
		
		/**
		 * Removes the session notification.
		 * 
		 * @param sessionId id of the notification to be removed
		 */
		public void removeSessionNotification(String sessionId){
			manager.cancel(Integer.parseInt(sessionId));
		}
		
		/**
		 * Removes all notifications.
		 * 
		 */
		public void removeAllNotifications(){
			
			for (int i =0; i < notificationList.size(); i++) {
				Integer index = notificationList.get(i);
				manager.cancel(index.intValue());
				myLogger.log(Logger.INFO, "Removing notification with ID " + index.intValue());
			}
			notificationList.clear();
		}
		
				
}

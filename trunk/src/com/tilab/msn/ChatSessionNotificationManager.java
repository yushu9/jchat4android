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
import android.widget.Toast;


// TODO: Auto-generated Javadoc
/**
 * The Class ChatSessionNotificationManager.
 */
class ChatSessionNotificationManager {

		/** The activity. */
		private Activity activity;
		
		/** The MA x_ notificatio n_ number. */
		private final int MAX_NOTIFICATION_NUMBER=10;
		
		/** The Constant myLogger. */
		private static final Logger myLogger = Logger.getMyLogger(ChatSessionNotificationManager.class.getName());
		
		/** The notification list. */
		private List<Integer> notificationList;
		
		/** The manager. */
		private NotificationManager manager;
		
		
		/**
		 * Instantiates a new chat session notification manager.
		 * 
		 * @param act the act
		 */
		public ChatSessionNotificationManager(Activity act) {
			activity = act;
			notificationList = new ArrayList<Integer>(MAX_NOTIFICATION_NUMBER);
			manager = (NotificationManager) activity.getSystemService(Context.NOTIFICATION_SERVICE);
		}
	
		/**
		 * Post new session notification.
		 * 
		 * @param sessionId the session id
		 */
		public void postNewSessionNotification(String sessionId){
			int index = Integer.parseInt(sessionId);
			Notification sessionNotif = makeSessionNotification(sessionId);
			activity.runOnUIThread(new NotificationRunnable(index,sessionNotif));
		}
		
		/**
		 * Show toast.
		 * 
		 * @param msg the msg
		 * @param duration the duration
		 */
		public void showToast(String msg, int duration){
			activity.runOnUIThread(new ToastRunnable(msg,duration));
		}
		
		/**
		 * Post new msg notification.
		 * 
		 * @param sessionId the session id
		 * @param msg the msg
		 */
		public void postNewMsgNotification(String sessionId, MsnSessionMessage msg) {
			int index = Integer.parseInt(sessionId);
			Notification sessionNotif = makeMsgNotification(sessionId, msg);
			activity.runOnUIThread(new NotificationRunnable(index,sessionNotif));
		}
		
		/**
		 * Adds the new session notification.
		 * 
		 * @param sessionId the session id
		 */
		public void addNewSessionNotification(String sessionId){
			int index = Integer.parseInt(sessionId);
			Notification sessionNotif = makeSessionNotification(sessionId);
			addNotification(index, sessionNotif);
		}
		
		/**
		 * Adds the new msg notification.
		 * 
		 * @param sessionId the session id
		 * @param msg the msg
		 */
		public void addNewMsgNotification(String sessionId, MsnSessionMessage msg){
			int index = Integer.parseInt(sessionId);
			Notification sessionNotif = makeMsgNotification(sessionId, msg);
			addNotification(index, sessionNotif);
		}
		
		/**
		 * Adds the notification.
		 * 
		 * @param index the index
		 * @param notif the notif
		 */
		private void addNotification(int index, Notification notif){
			Integer indexAsInteger = new Integer(index);
			
			if (!notificationList.contains(indexAsInteger)){
				notificationList.add(indexAsInteger);
			}
			
			manager.notify(index,notif);
		}
		
		
		/**
		 * Make session notification.
		 * 
		 * @param sessionId the session id
		 * 
		 * @return the notification
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
		 * Make msg notification.
		 * 
		 * @param sessionId the session id
		 * @param msg the msg
		 * 
		 * @return the notification
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
		 * Removes the session notification.
		 * 
		 * @param sessionId the session id
		 */
		public void removeSessionNotification(String sessionId){
			manager.cancel(Integer.parseInt(sessionId));
		}
		
		/**
		 * Removes the all notifications.
		 */
		public void removeAllNotifications(){
			
			for (int i =0; i < notificationList.size(); i++) {
				Integer index = notificationList.get(i);
				manager.cancel(index.intValue());
				myLogger.log(Logger.INFO, "Removing notification with ID " + index.intValue());
			}
			notificationList.clear();
		}
		
		
		//This runnable adds an empty notification for a new session 
		/**
		 * The Class NotificationRunnable.
		 */
		private class NotificationRunnable implements Runnable {
			
			/** The notif id. */
			private int notifId;
			
			/** The notif. */
			private Notification notif;
			
			/**
			 * Instantiates a new notification runnable.
			 * 
			 * @param index the index
			 * @param notif the notif
			 */
			public NotificationRunnable(int index, Notification notif){
				this.notifId = index;
				this.notif = notif;
			}
			
			/* (non-Javadoc)
			 * @see java.lang.Runnable#run()
			 */
			public void run() {
				addNotification(notifId, notif);
			}
			
		}
		
		
		/**
		 * The Class ToastRunnable.
		 */
		private class ToastRunnable implements Runnable {

			/** The duration. */
			private int duration;
			
			/** The message. */
			private String message;
			
			/**
			 * Instantiates a new toast runnable.
			 * 
			 * @param msg the msg
			 * @param duration the duration
			 */
			public ToastRunnable(String msg, int duration) {
				this.message = msg;
				this.duration = duration;
			}
			
			/* (non-Javadoc)
			 * @see java.lang.Runnable#run()
			 */
			@Override
			public void run() {
				Toast.makeText(activity, message, duration).show();
			}
			
		}
}

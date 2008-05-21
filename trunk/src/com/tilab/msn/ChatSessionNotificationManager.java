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


class ChatSessionNotificationManager {

		private Activity activity;
		private final int MAX_NOTIFICATION_NUMBER=10;
		private static final Logger myLogger = Logger.getMyLogger(ChatSessionNotificationManager.class.getName());
		private List<Integer> notificationList;
		private NotificationManager manager;
		
		
		public ChatSessionNotificationManager(Activity act) {
			activity = act;
			notificationList = new ArrayList<Integer>(MAX_NOTIFICATION_NUMBER);
			manager = (NotificationManager) activity.getSystemService(Context.NOTIFICATION_SERVICE);
		}
	
		public void postNewSessionNotification(String sessionId){
			int index = Integer.parseInt(sessionId);
			Notification sessionNotif = makeSessionNotification(sessionId);
			activity.runOnUIThread(new NotificationRunnable(index,sessionNotif));
		}
		
		public void showToast(String msg, int duration){
			activity.runOnUIThread(new ToastRunnable(msg,duration));
		}
		
		public void postNewMsgNotification(String sessionId, MsnSessionMessage msg) {
			int index = Integer.parseInt(sessionId);
			Notification sessionNotif = makeMsgNotification(sessionId, msg);
			activity.runOnUIThread(new NotificationRunnable(index,sessionNotif));
		}
		
		public void addNewSessionNotification(String sessionId){
			int index = Integer.parseInt(sessionId);
			Notification sessionNotif = makeSessionNotification(sessionId);
			addNotification(index, sessionNotif);
		}
		
		public void addNewMsgNotification(String sessionId, MsnSessionMessage msg){
			int index = Integer.parseInt(sessionId);
			Notification sessionNotif = makeMsgNotification(sessionId, msg);
			addNotification(index, sessionNotif);
		}
		
		private void addNotification(int index, Notification notif){
			Integer indexAsInteger = new Integer(index);
			
			if (!notificationList.contains(indexAsInteger)){
				notificationList.add(indexAsInteger);
			}
			
			manager.notify(index,notif);
		}
		
		
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
		private class NotificationRunnable implements Runnable {
			
			private int notifId;
			private Notification notif;
			
			public NotificationRunnable(int index, Notification notif){
				this.notifId = index;
				this.notif = notif;
			}
			
			public void run() {
				addNotification(notifId, notif);
			}
			
		}
		
		
		private class ToastRunnable implements Runnable {

			private int duration;
			private String message;
			
			public ToastRunnable(String msg, int duration) {
				this.message = msg;
				this.duration = duration;
			}
			
			@Override
			public void run() {
				Toast.makeText(activity, message, duration).show();
			}
			
		}
}

package co.idealwebsolutions.omgremote;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.IBinder;

public class NotificationService extends Service {
	
	private NotificationManager notificationManager;
	private State currentState;
	private static final int UID = 9101773;
	
	public static final String BROADCAST_TAG = "co.idealwebsolutions.NOTIFY";
	
	private enum State {
		NOT_NOTIFIED, NOTIFIED
	}
	
	@Override
	public void onCreate() {
		super.onCreate();
	}
	
	@Override
	public void onDestroy() {
		if(notificationManager != null) {
			notificationManager.cancel(UID);
			currentState = State.NOT_NOTIFIED;
		}
		super.onDestroy();
	}
	
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		super.onStartCommand(intent, flags, startId);
		
		notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
		currentState = State.NOT_NOTIFIED;
	
		return START_STICKY;
	}
	
	/*
	private void packNotification(final String msg) {
		int icon = R.drawable.ic_launcher;
		CharSequence tickerText = "New message received!";
		long when = System.currentTimeMillis();
		
		if(currentState == State.NOTIFIED) {
			currentState = State.NOT_NOTIFIED;
			return;
		}

		Notification notification = new Notification(icon, tickerText, when);
		notification.ledOnMS = 500;
		notification.ledOffMS = 100;
		notification.ledARGB = Color.RED;
		notification.flags |= Notification.FLAG_SHOW_LIGHTS;
		notification.flags |= Notification.FLAG_AUTO_CANCEL;
		Context context = getApplicationContext();
		
		CharSequence contentTitle = "Stranger ";
		CharSequence contentText = "Communities: ";
		Intent notificationIntent = new Intent(this, ChatActivity.class);
		
		PendingIntent contentIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0);

		notification.setLatestEventInfo(context, contentTitle, contentText, contentIntent);
		notificationManager.cancel(UID);
		notificationManager.notify(UID, notification);
		currentState = State.NOTIFIED;
	} */

	@Override
	public IBinder onBind(Intent intent) {
		// TODO Auto-generated method stub
		return null;
	}

}

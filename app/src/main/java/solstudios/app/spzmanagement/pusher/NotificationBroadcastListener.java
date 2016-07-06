package solstudios.app.spzmanagement.pusher;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import solstudios.app.spzmanagement.LogTask;


public class NotificationBroadcastListener extends BroadcastReceiver {

	// private boolean screenOff;
	public static final String TAB = "NotificationBroadcastListener";
	public static final String CLASS = "com.solbadguyky.phapluatonline.notification";
	private Context context;
	private SharedPreferences sharedPreferences;

	@Override
	public void onReceive(Context context, Intent intent) {
		this.context = context;
		if (intent.getExtras() != null) {
			try {

			} catch (Exception e) {
				new LogTask(e.getMessage(), TAB, LogTask.LOG_E);
			}
			return;
		}

		ConnectivityManager cm = (ConnectivityManager) context
				.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo info = cm.getActiveNetworkInfo();

		// Tools getTools = new Tools((Activity)context);

		if ("android.intent.action.BOOT_COMPLETED".equals(intent.getAction())) {
			new LogTask("onBootCompleted", TAB, LogTask.LOG_I);
			// servicesTool.intFeedServices();
		} else if (info != null) {
			if (info.isConnected()) {
				new LogTask("Network connected", TAB, LogTask.LOG_I);
				NetworkInfo[] netInfo = cm.getAllNetworkInfo();
				for (NetworkInfo ni : netInfo) {
					boolean isConnected = false;
					if (ni.getTypeName().equalsIgnoreCase("WIFI")) {
						if (ni.isConnected()) {
							isConnected = true;
						}

					} else if (ni.getTypeName().equalsIgnoreCase("MOBILE")) {
						if (ni.isConnected()) {
							isConnected = true;
						}
					}
					if (isConnected) {
						PusherHelper pusherHelper = new PusherHelper(context);
						//pusherHelper.checkPusherConnection();
					}
				}

			} else if (info.isFailover()) {
				new LogTask("Network disconnected", TAB, LogTask.LOG_I);

			}
		} else {
			new LogTask("Network disconnected", TAB, LogTask.LOG_I);
		}
		/*
		 * else { Log.d(TAG, TAB + "FeedMode is not active"); }
		 */
	}

}

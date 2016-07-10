package solstudios.app.spzmanagement.pusher;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.pusher.client.connection.ConnectionStateChange;

import solstudios.app.spzmanagement.LogTask;
import solstudios.app.spzmanagement.PusherBroadcast;


public class NotificationBroadcastListener extends BroadcastReceiver {

    // private boolean screenOff;
    public static final String TAB = "NotificationBroadcastListener";
    private Context context;
    private SharedPreferences sharedPreferences;

    @Override
    public void onReceive(Context context, Intent intent) {
        this.context = context;
        new LogTask("onReceive|intent:" + intent.getAction(), TAB, LogTask.LOG_I);

        // Tools getTools = new Tools((Activity)context);
        if (PusherBroadcast.getActionIntent(PusherBroadcast.ACTION_SUBSCRIPTION_EVENT).equals(intent.getAction())
                || PusherBroadcast.getActionIntent(PusherBroadcast.ACTION_SUBSCRIPTION_CHANNEL).equals(intent.getAction())) {
            if (intent.hasExtra(PusherBroadcast.INTENT_SUBSCRIPTION)) {
                getSubcriptionBundle(intent.getBundleExtra(PusherBroadcast.INTENT_SUBSCRIPTION));
            }
        } else if (intent.hasExtra(PusherBroadcast.CONNECTION_STATE_CHANGED)) {
            getConnectionIntent(intent.getStringExtra(PusherBroadcast.CONNECTION_STATE_CHANGED));
        }

        if ("android.intent.action.BOOT_COMPLETED".equals(intent.getAction())) {
            new LogTask("onBootCompleted", TAB, LogTask.LOG_I);
            // servicesTool.intFeedServices();
        } else if ("aandroid.net.conn.CONNECTIVITY_CHANGE".equals(intent.getAction())) {
            ConnectivityManager cm = (ConnectivityManager) context
                    .getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo info = cm.getActiveNetworkInfo();
            if (info != null) {
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

                        }
                    }

                } else if (info.isFailover()) {
                    new LogTask("Network disconnected", TAB, LogTask.LOG_I);

                }
            } else {
                new LogTask("Network disconnected", TAB, LogTask.LOG_I);
            }
        }
        /*
         * else { Log.d(TAG, TAB + "FeedMode is not active"); }
		 */
    }

    private void getSubcriptionBundle(Bundle bundle) {
        new LogTask("getSubcriptionBundle", TAB, LogTask.LOG_I);
        String channel = bundle.getString(PusherBroadcast.SUBSCRIPTION_CHANNEL, null);
        String event = bundle.getString(PusherBroadcast.SUBSCRIPTION_EVENT, null);
        String message = bundle.getString(PusherBroadcast.SUBSCRIPTION_MESSAGE, null);

        if (channel != null && event != null && message != null) {
            // new LogTask("getSubcriptionBundle", TAB, LogTask.LOG_D);
        }
    }

    private void getConnectionIntent(String jsonConnectionString) {
        new LogTask("getConnectionIntent", TAB, LogTask.LOG_I);
        try {
            ConnectionStateChange connectionStateChange =
                    new Gson().fromJson(jsonConnectionString, ConnectionStateChange.class);

        } catch (JsonSyntaxException e) {

        }
    }
}

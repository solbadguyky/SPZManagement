package solstudios.app.spzmanagement;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.pusher.client.connection.ConnectionStateChange;

/**
 * Created by solbadguyky on 7/2/16.
 */

public abstract class PusherBroadcast extends BroadcastReceiver {
    public static final String PACKAGE_NAME = " solstudios.app.spzmanagement";

    public static final String INTENT_SUBSCRIPTION = "subscription intent";
    public static final String INTENT_SUBSCRIPTION_SUCCESS = "subscription success intent";
    public static final String INTENT_CONNECTION = "connection intent";

    public static final String ACTION_SUBSCRIPTION_EVENT = "onSubcriptionEvent";
    public static final String ACTION_SUBSCRIPTION_CHANNEL = "onSubcriptionChannel";
    public static final String ACTION_CONNECTION_CHANGED = "onDefaultConnectionChanged";
    public static final String TAB = " PusherBroadcast";

    public static final String SUBSCRIPTION_CHANNEL = "incomming channel";
    public static final String SUBSCRIPTION_EVENT = "incomming event";
    public static final String SUBSCRIPTION_MESSAGE = " incomming_message";

    public static final String CONNECTION_STATE_CHANGED = " connection_statechange";
    public static final String CONNECTION_ERROR = " connection_error";

    public static String getActionIntent(String action) {
        return PACKAGE_NAME + "." + action;
    }

    public abstract void onDefaultConnectionChanged(ConnectionStateChange connectionStateChange);

    public abstract void onSubscriptionEvent(String channel, String event, String message);

    public abstract void onSubscriptionSucceeded(String channelName);

    abstract void onError(Exception e);

    @Override
    public void onReceive(Context context, Intent intent) {
        new LogTask("PusherBroadcastReceiver|intent:" + intent, TAB, LogTask.LOG_D);
        getPusherIntent(intent);
    }

    private void getPusherIntent(Intent intent) {
        if (intent.hasExtra(INTENT_SUBSCRIPTION)) {
            getSubcriptionBundle(intent.getBundleExtra(INTENT_SUBSCRIPTION));
        } else if (intent.hasExtra(INTENT_SUBSCRIPTION_SUCCESS)) {
            getSubcriptionBundle(intent.getBundleExtra(INTENT_SUBSCRIPTION_SUCCESS));
        } else if (intent.hasExtra(CONNECTION_STATE_CHANGED)) {
            getConnectionIntent(intent.getStringExtra(CONNECTION_STATE_CHANGED));
        }
    }

    private void getSuccessSubscriptionChannel(Bundle bundle) {
        String channel = bundle.getString(PusherBroadcast.SUBSCRIPTION_CHANNEL, null);
        if (channel != null) {
            onSubscriptionSucceeded(channel);
        }
    }

    private void getSubcriptionBundle(Bundle bundle) {
        String channel = bundle.getString(PusherBroadcast.SUBSCRIPTION_CHANNEL, null);
        String event = bundle.getString(PusherBroadcast.SUBSCRIPTION_EVENT, null);
        String message = bundle.getString(PusherBroadcast.SUBSCRIPTION_MESSAGE, null);

        if (channel != null && event != null && message != null) {
            onSubscriptionEvent(channel, event, message);
        }
    }

    private void getConnectionIntent(String jsonConnectionString) {
        try {
            ConnectionStateChange connectionStateChange = new Gson().fromJson(jsonConnectionString, ConnectionStateChange.class);
            onDefaultConnectionChanged(connectionStateChange);
        } catch (JsonSyntaxException e) {
            onError(e);
        }
    }

}
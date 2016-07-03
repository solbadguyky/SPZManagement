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
    public static final String INTENT_BINDING = "binding intent";
    public static final String INTENT_SUBSCRIPTION_SUCCESS = "subscription success intent";
    public static final String INTENT_CONNECTION = "connection intent";


    public static final String ACTION_BIND_EVENT = "onBindEvent";
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

    public abstract void onBindedEvent(String channel, String event);


    abstract void onError(Exception e);

    @Override
    public void onReceive(Context context, Intent intent) {
        new LogTask("PusherBroadcastReceiver|intent:" + intent, TAB, LogTask.LOG_D);
        getPusherIntent(intent);
    }

    private void getPusherIntent(Intent intent) {
        if (intent.hasExtra(INTENT_BINDING)) {
            getBindBundle(intent.getBundleExtra(INTENT_BINDING));
        } else if (intent.hasExtra(INTENT_SUBSCRIPTION)) {
            getSubcriptionBundle(intent.getBundleExtra(INTENT_SUBSCRIPTION));
        } else if (intent.hasExtra(INTENT_SUBSCRIPTION_SUCCESS)) {
            getSuccessSubscriptionChannel(intent.getBundleExtra(INTENT_SUBSCRIPTION_SUCCESS));
        } else if (intent.hasExtra(CONNECTION_STATE_CHANGED)) {
            getConnectionIntent(intent.getStringExtra(CONNECTION_STATE_CHANGED));
        }
    }

    private void getSuccessSubscriptionChannel(Bundle bundle) {
        new LogTask("getSuccessSubscriptionChannel", TAB, LogTask.LOG_I);
        String channel = bundle.getString(PusherBroadcast.SUBSCRIPTION_CHANNEL, null);
        if (channel != null) {
            onSubscriptionSucceeded(channel);
        }
    }

    private void getSubcriptionBundle(Bundle bundle) {
        new LogTask("getSubcriptionBundle", TAB, LogTask.LOG_I);
        String channel = bundle.getString(PusherBroadcast.SUBSCRIPTION_CHANNEL, null);
        String event = bundle.getString(PusherBroadcast.SUBSCRIPTION_EVENT, null);
        String message = bundle.getString(PusherBroadcast.SUBSCRIPTION_MESSAGE, null);

        if (channel != null && event != null && message != null) {
            onSubscriptionEvent(channel, event, message);
        }
    }


    private void getBindBundle(Bundle bundle) {
        new LogTask("getBindBundle", TAB, LogTask.LOG_I);
        String channel = bundle.getString(PusherBroadcast.SUBSCRIPTION_CHANNEL, null);
        String event = bundle.getString(PusherBroadcast.SUBSCRIPTION_EVENT, null);

        if (channel != null && event != null) {
            onBindedEvent(channel, event);
        }
    }

    private void getConnectionIntent(String jsonConnectionString) {
        new LogTask("getSubcriptionBundle", TAB, LogTask.LOG_I);
        try {
            ConnectionStateChange connectionStateChange = new Gson().fromJson(jsonConnectionString, ConnectionStateChange.class);
            onDefaultConnectionChanged(connectionStateChange);
        } catch (JsonSyntaxException e) {
            onError(e);
        }
    }

   /* public static Intent createCustomIntent() {
        Intent onEventItent = new Intent(PusherBroadcast.getActionIntent(PusherBroadcast.ACTION_SUBSCRIPTION_CHANNEL));

        Bundle subscriptionBundle = new Bundle();
        subscriptionBundle.putString(PusherBroadcast.SUBSCRIPTION_CHANNEL, s);
        subscriptionBundle.putString(PusherBroadcast.SUBSCRIPTION_EVENT, s1);
        subscriptionBundle.putString(PusherBroadcast.SUBSCRIPTION_MESSAGE, s2);

        onEventItent.putExtra(PusherBroadcast.INTENT_SUBSCRIPTION, subscriptionBundle);

        context.sendBroadcast(onEventItent);
    } */
}
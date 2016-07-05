package solstudios.app.spzmanagement;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.widget.TextView;

import com.pusher.client.connection.ConnectionStateChange;

import solstudios.app.spzmanagement.pusher.PusherHelper;

/**
 * Created by solbadguyky on 7/5/16.
 */

public class LogActivity extends BaseActivity {
    public static final String TAB = "LogActivity;";

    private TextView logMessageTextView;

    private PusherBroadcast pusherBroadcastReceiver;
    private PusherHelper pusherHelper;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.log_activity);
    }

    @Override
    public void initValue() {
        pusherHelper = new PusherHelper(this);
    }

    @Override
    public void initView() {
        logMessageTextView = (TextView) findViewById(R.id.log_activity_textView_LogMessage);
    }

    @Override
    public void setupValue() {

    }

    @Override
    public void setupView() {

    }

    @Override
    protected void onResume() {
        super.onResume();

        if (pusherBroadcastReceiver == null) {
            pusherBroadcastReceiver = new PusherBroadcast() {

                @Override
                public void onDefaultConnectionChanged(ConnectionStateChange connectionStateChange) {
                    new LogTask("onDefaultConnectionChanged|connectionStateChange:" + connectionStateChange.getPreviousState()
                            + " -> " + connectionStateChange.getCurrentState(),
                            TAB, LogTask.LOG_I);
                    onConnectionStateChange(connectionStateChange);
                }

                @Override
                public void onSubscriptionEvent(String channel, String event, String message) {
                    new LogTask("onSubscriptionEvent|channel:" + channel
                            + ",event:" + event + "\nmessage:" + message,
                            TAB, LogTask.LOG_I);
                    LogActivity.this.onSubscriptionEvent(channel, event, message);
                }

                @Override
                public void onSubscriptionSucceeded(String channelName) {
                    new LogTask("onSubscriptionSucceeded|channelName:" + channelName,
                            TAB, LogTask.LOG_I);
                    //subscriptionSucceeded(channelName);
                }

                @Override
                public void onBindedEvent(String channel, String event) {
                    new LogTask("onBindedEvent|channelName:" + channel + ",event:" + event,
                            TAB, LogTask.LOG_I);
                    //bindSucceeded(channel, event);
                }

                @Override
                void onError(Exception e) {

                }

                @Override
                public void onReceive(Context context, Intent intent) {
                    super.onReceive(context, intent);
                }
            };
        }
        IntentFilter filterPusherMessages = new IntentFilter();
        filterPusherMessages.addAction(PusherBroadcast.getActionIntent(PusherBroadcast.ACTION_SUBSCRIPTION_CHANNEL));
        filterPusherMessages.addAction(PusherBroadcast.getActionIntent(PusherBroadcast.ACTION_BIND_EVENT));
        filterPusherMessages.addAction(PusherBroadcast.getActionIntent(PusherBroadcast.ACTION_SUBSCRIPTION_EVENT));
        filterPusherMessages.addAction(PusherBroadcast.getActionIntent(PusherBroadcast.ACTION_CONNECTION_CHANGED));
        this.registerReceiver(pusherBroadcastReceiver, filterPusherMessages);
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (pusherBroadcastReceiver != null) {
            this.unregisterReceiver(pusherBroadcastReceiver);
        }
    }

    private void onConnectionStateChange(ConnectionStateChange connectionStateChange) {
        switch (connectionStateChange.getCurrentState()) {
            case CONNECTED:
                //logTextView.setText("Connected");
                logMessageTextView.append("Connected \n");
                break;
            case DISCONNECTED:
                logMessageTextView.append("Disconnected \n");
                break;
            case CONNECTING:
                //logTextView.setText("Connecting");
                break;
        }
    }

    private void onSubscriptionEvent(String channel, String event, String message) {
        String stringBuilder = new String();
        stringBuilder = "--------------------" + "\n" + "Channel: " + channel + "\n" + "Event:" + event + "\n" + "Message:" + message;
        logMessageTextView.append(stringBuilder + "\n");
    }
}

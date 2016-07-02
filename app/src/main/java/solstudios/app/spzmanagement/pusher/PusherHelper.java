package solstudios.app.spzmanagement.pusher;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.widget.Toast;

import com.google.gson.Gson;
import com.pusher.client.Pusher;
import com.pusher.client.channel.Channel;
import com.pusher.client.channel.ChannelEventListener;
import com.pusher.client.channel.SubscriptionEventListener;
import com.pusher.client.connection.ConnectionEventListener;
import com.pusher.client.connection.ConnectionState;
import com.pusher.client.connection.ConnectionStateChange;

import solstudios.app.spzmanagement.ChannelStack;
import solstudios.app.spzmanagement.LogTask;
import solstudios.app.spzmanagement.PusherBroadcast;


public class PusherHelper {
    public static final String TAB = "PusherHelper";

    public static final String PUSHER_HOST_URL = "http://www.lamthong.xyz/plo/pusher_content.json";
    public static final String TAG_ROOT = "plo_pusher_content";
    public static final String TAG_APPID = "app_id";
    public static final String TAG_CHANNEL = "pusher_channel";
    public static final String TAG_EVENT = "pusher_event";
    public static final String TAG_CLUSTER = "cluster";

    public static final String CLIENT_APPID = "client_appid";
    public static final String CLIENT_CLUSTER = "client_cluster";

    public static final String HOST_APPID = "host_appid";
    public static final String HOST_KEY = "host_key";
    public static final String HOST_SECRET = "host_secret";
    public static final String DEFAULT_HOST_APPID = "185986";
    public static final String DEFAULT_HOST_KEY = "97291cb4922b057300d6";
    public static final String DEFAULT_HOST_SECRET = "5440e10e64b7410854f6";

    public static final String DEFAULT_CLIENT_APPID = "97291cb4922b057300d6";
    public static final String DEFAULT_CLIENT_CLUSTER = "ap1";
    public static final String DEFAULT_CLIENT_CHANNEL = "plo_homepage";
    public static final String DEFAULT_CLIENT_EVENT = "plo_hot_news";

    public static final String DEFAULT_CLIENT_TESTCHANNEL = "test_channel";
    public static final String DEFAULT_CLIENT_TESTEVENT = "test_event";

    public static final long MAXIMUM_PUSHER_WAITING_TIME = 1800000;
    boolean isOverrideDefaultSettings;
    private Context context;
    private NotificationHelper notificationHelper;
    private PusherConnectionListener pusherListener;
    private SharedPreferences sharedPreferences;
    private String pusherChannel, pusherEvent;
    private Pusher pusher;

    // private IPusherHelpder iPusherHelpder;

    public PusherHelper(Context context) {
        pusher = PusherClientInstance.INSTANCE.getInstance(context, false);
        init(context);
    }


    public PusherHelper(Context context, boolean reInit) {
        pusher = PusherClientInstance.INSTANCE.getInstance(context, reInit);
        init(context);
    }


    private void init(Context context) {
        this.context = context;
        notificationHelper = new NotificationHelper(context);
        sharedPreferences = PreferenceManager
                .getDefaultSharedPreferences(context);
    }


    public void connectToPusherClient(@Nullable SubscriptionEventListener subscriptionEventListener)
            throws Exception {
        new LogTask(
                "connectToPusherClient|appid:"
                        + PusherClientInstance.INSTANCE.currentAppID,
                TAB, LogTask.LOG_I);

        for (solstudios.app.spzmanagement.Channel mChannel : ChannelStack.getInstance()) {
            new LogTask("getChannels|mChannel:" + mChannel.getChannelName(), TAB, LogTask.LOG_D);
            if (pusher.getChannel(mChannel.getChannelName()) == null) {
                Channel pchannel = pusher.subscribe(mChannel.getChannelName(), new DefaultSubscriptionChannelListener());
                /*for (solstudios.app.spzmanagement.Channel.Event mEvent : mChannel.getEvents()) {
                    new LogTask("getChannels|mEvent:" + mEvent.getEventName(), TAB, LogTask.LOG_D);
                    if (subscriptionEventListener != null) {
                        pchannel.bind(mEvent.getEventName(), subscriptionEventListener);
                    } else {
                        pchannel.bind(mEvent.getEventName(), new DefaultSubscriptionEventListener());
                    }

                }*/
            } else {
               /* for (solstudios.app.spzmanagement.Channel.Event mEvent : mChannel.getEvents()) {
                    new LogTask("getChannels|mEvent:" + mEvent.getEventName(), TAB, LogTask.LOG_D);
                    if (!pusher.getChannel(mChannel.getChannelName()).isSubscribed()) {
                        if (subscriptionEventListener != null) {
                            pusher.getChannel(mChannel.getChannelName()).bind(mEvent.getEventName(), subscriptionEventListener);
                        } else {
                            pusher.getChannel(mChannel.getChannelName()).bind(mEvent.getEventName(), new DefaultSubscriptionEventListener());
                        }
                    }
                }
 */
            }

        }
        //pchannel.bind(event.getEventName(), subscriptionEventListener);
        //pusher.connect(connectionEventListener, ConnectionState.ALL);
    }

    private void getChannels() {
        new LogTask("getChannels|channelStack:" + ChannelStack.getInstance(), TAB, LogTask.LOG_D);

        for (solstudios.app.spzmanagement.Channel mChannel : ChannelStack.getInstance()) {
            new LogTask("getChannels|mChannel:" + mChannel.getChannelName(), TAB, LogTask.LOG_D);
            for (solstudios.app.spzmanagement.Channel.Event mEvent : mChannel.getEvents()) {
                new LogTask("getChannels|mEvent:" + mEvent.getEventName(), TAB, LogTask.LOG_D);
            }
        }
    }

    public void subcribe(solstudios.app.spzmanagement.Channel channel, solstudios.app.spzmanagement.Channel.Event event,
                         @Nullable SubscriptionEventListener subscriptionEventListener) {
        if (pusher.getConnection().getState() == ConnectionState.CONNECTED
                || pusher.getConnection()
                .getState() == ConnectionState.CONNECTING) {

            new LogTask("getChannels|mChannel:" + channel.getChannelName(), TAB, LogTask.LOG_D);
            if (pusher.getChannel(channel.getChannelName()) == null) {
                Channel pchannel = pusher.subscribe(channel.getChannelName());
                if (subscriptionEventListener != null) {
                    pchannel.bind(event.getEventName(), subscriptionEventListener);
                } else {
                    pchannel.bind(event.getEventName(), new DefaultSubscriptionEventListener());
                }
            } else {
                new LogTask("getChannels|mEvent:" + event.getEventName(), TAB, LogTask.LOG_D);
                if (!pusher.getChannel(channel.getChannelName()).isSubscribed())
                    if (subscriptionEventListener != null) {
                        pusher.getChannel(channel.getChannelName()).bind(event.getEventName(), subscriptionEventListener);
                    } else {
                        pusher.getChannel(channel.getChannelName()).bind(event.getEventName(), new DefaultSubscriptionEventListener());
                    }
            }

        } else {
            new LogTask("subcribe|error: channel is disconnected", TAB, LogTask.LOG_E);
            checkConnection();
        }
    }

    public void checkConnection() {
        pusher.connect(new DefaultConnectionEventListener(), ConnectionState.ALL);
    }

    /**
     * Tải thông tin về pusher client, lưu trữ tại
     */
    /*public static void downloadPusherClientInfo(final Context context) {
        new LogTask("downloadPusherClientInfo", TAB, LogTask.LOG_I);
        ServiceHelper serviceHelper = new ServiceHelper(context);
        serviceHelper.downloadJSON(API.API_PUSHER_CLIENT,
                new Response.Listener<JSONObject>() {

                    @Override
                    public void onResponse(JSONObject jsonObject) {
                        // TODO Auto-generated method stub
                        new LogTask(
                                "DownloadTask|downloadPusherData:"
                                        + jsonObject.toString(),
                                TAB, LogTask.LOG_D);
                        update(context, jsonObject);

                    }
                }, new Response.ErrorListener() {

                    @Override
                    public void onErrorResponse(VolleyError arg0) {
                        // TODO Auto-generated method stub

                    }
                });
    }

    /**
     * Parse pusher-json in server
     *
     * @param context    application context
     * @param jsonObject {@link JSONObject}
     */
    /*public static void update(Context context, JSONObject jsonObject) {
        new LogTask("update", TAB, LogTask.LOG_I);
        try {
            JSONArray jsonArray = jsonObject
                    .getJSONArray(PusherHelper.TAG_ROOT);
            for (int i = 0; i < jsonArray.length(); i++) {

                JSONObject e = jsonArray.getJSONObject(i);

                new LogTask("update|appid="
                        + e.getString(PusherHelper.TAG_APPID).toString()
                        + ",pusher_channel="
                        + e.getString(PusherHelper.TAG_CHANNEL).toString()
                        + ",pusher_event="
                        + e.getString(PusherHelper.TAG_EVENT).toString()
                        + ",cluster="
                        + e.getString(PusherHelper.TAG_CLUSTER).toString(), TAB,
                        LogTask.LOG_D);

                String appid = e.getString(PusherHelper.TAG_APPID).toString();
                String channel = e.getString(PusherHelper.TAG_CHANNEL)
                        .toString();
                String event = e.getString(PusherHelper.TAG_EVENT).toString();
                String cluster = e.getString(PusherHelper.TAG_CLUSTER)
                        .toString();

                /// write new pusher-client-info
                SharedPreferences defaultSharedPreferences = PreferenceManager
                        .getDefaultSharedPreferences(context);
                Editor editor = defaultSharedPreferences.edit();
                editor.putString(SettingActivity.PREF_PUSHER_CLIENT_APPID,
                        appid);
                editor.putString(SettingActivity.PREF_PUSHER_CLIENT_CHANNEL,
                        channel);
                editor.putString(SettingActivity.PREF_PUSHER_CLIENT_EVENT,
                        event);
                editor.putString(SettingActivity.PREF_PUSHER_CLIENT_CLUSTER,
                        cluster);
                editor.commit();

            }

        } catch (JSONException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    } */
    public void checkPusherConnection() {
        new LogTask("checkPusherConnection", TAB, LogTask.LOG_I);
        resume();
    }

    /**
     * Ngắt kết nối (tạm thời) khi khởi động ứng dụng
     */
    public void disconnect() {
        new LogTask("disconnect", TAB, LogTask.LOG_I);

        if (pusher.getConnection().getState() == ConnectionState.CONNECTED
                || pusher.getConnection()
                .getState() == ConnectionState.CONNECTING) {
            pusher.disconnect();
        }
    }

    public void unsubscribe(solstudios.app.spzmanagement.Channel channel) {
        if (pusher.getConnection().getState() == ConnectionState.CONNECTED
                || pusher.getConnection()
                .getState() == ConnectionState.CONNECTING) {
            if (pusher.getChannel(channel.getChannelName()) != null) {
                if (pusher.getChannel(channel.getChannelName()).isSubscribed()) {
                    pusher.unsubscribe(pusher.getChannel(channel.getChannelName()).getName());
                } else {
                    Toast.makeText(context, "Pusher had not subcribed this channel: " + channel.getChannelName(), Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(context, "This channel: " + channel.getChannelName() + " is not existed", Toast.LENGTH_SHORT).show();

            }

        } else {
            Toast.makeText(context, "Pusher is off now!", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Kết nối pusher-client
     */
    public void resume() {
        new LogTask("resume", TAB, LogTask.LOG_I);
        onResume();
    }

    private void onResume() {
        try {
            if (pusher.getConnection()
                    .getState() == ConnectionState.DISCONNECTED
                    || pusher.getConnection()
                    .getState() == ConnectionState.DISCONNECTING) {
                checkConnection();
            }
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }


    public void addPusherConnectionListener(PusherConnectionListener listener) {
        this.pusherListener = listener;
    }

    public interface PusherConnectionListener {
        void onConnectionStateChange(
                ConnectionStateChange connectionStateChange);

        void onError(String arg0, String arg1, Exception arg2);

        void onReceiveMessage(String channelName, String eventName,
                              final String data);
    }

    public class DefaultSubscriptionEventListener implements SubscriptionEventListener {

        public DefaultSubscriptionEventListener() {

        }

        @Override
        public void onEvent(String s, String s1, String s2) {
            Intent onEventItent = new Intent(PusherBroadcast.getActionIntent(PusherBroadcast.ACTION_SUBSCRIPTION_EVENT));

            Bundle subscriptionBundle = new Bundle();
            subscriptionBundle.putString(PusherBroadcast.SUBSCRIPTION_CHANNEL, s);
            subscriptionBundle.putString(PusherBroadcast.SUBSCRIPTION_EVENT, s1);
            subscriptionBundle.putString(PusherBroadcast.SUBSCRIPTION_MESSAGE, s2);

            onEventItent.putExtra(PusherBroadcast.INTENT_SUBSCRIPTION, subscriptionBundle);

            context.sendBroadcast(onEventItent);
        }


    }

    public class DefaultSubscriptionChannelListener implements ChannelEventListener {

        @Override
        public void onSubscriptionSucceeded(String channelName) {
            Intent onEventItent = new Intent(PusherBroadcast.getActionIntent(PusherBroadcast.ACTION_SUBSCRIPTION_CHANNEL));

            Bundle subscriptionBundle = new Bundle();
            subscriptionBundle.putString(PusherBroadcast.SUBSCRIPTION_CHANNEL, channelName);
            onEventItent.putExtra(PusherBroadcast.INTENT_SUBSCRIPTION_SUCCESS, subscriptionBundle);

            context.sendBroadcast(onEventItent);
        }

        @Override
        public void onEvent(String s, String s1, String s2) {
            Intent onEventItent = new Intent(PusherBroadcast.getActionIntent(PusherBroadcast.ACTION_SUBSCRIPTION_CHANNEL));

            Bundle subscriptionBundle = new Bundle();
            subscriptionBundle.putString(PusherBroadcast.SUBSCRIPTION_CHANNEL, s);
            subscriptionBundle.putString(PusherBroadcast.SUBSCRIPTION_EVENT, s1);
            subscriptionBundle.putString(PusherBroadcast.SUBSCRIPTION_MESSAGE, s2);

            onEventItent.putExtra(PusherBroadcast.INTENT_SUBSCRIPTION, subscriptionBundle);

            context.sendBroadcast(onEventItent);
        }
    }

    private class DefaultConnectionEventListener implements ConnectionEventListener {

        public DefaultConnectionEventListener() {

        }

        @Override

        public void onConnectionStateChange(ConnectionStateChange connectionStateChange) {
            Intent onConnectionStateChangeIntent = new Intent(PusherBroadcast.getActionIntent(PusherBroadcast.ACTION_CONNECTION_CHANGED));
            onConnectionStateChangeIntent.putExtra(PusherBroadcast.CONNECTION_STATE_CHANGED, new Gson().toJson(connectionStateChange));
            context.sendBroadcast(onConnectionStateChangeIntent);
        }

        @Override
        public void onError(String s, String s1, Exception e) {

        }
    }
}

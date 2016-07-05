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
    private SharedPreferences sharedPreferences;
    private String pusherChannel, pusherEvent;

    // private IPusherHelpder iPusherHelpder;

    public PusherHelper(Context context) {
        init(context);
    }


    public PusherHelper(Context context, boolean reInit) {
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
        Pusher pusher = PusherClientInstance.getInstance(context).getCurrentPusherInstance();
        for (solstudios.app.spzmanagement.Channel mChannel : ChannelStack.getInstance().getChannelStack()) {
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

        for (solstudios.app.spzmanagement.Channel mChannel : ChannelStack.getInstance().getChannelStack()) {
            new LogTask("getChannels|mChannel:" + mChannel.getChannelName(), TAB, LogTask.LOG_D);
            for (solstudios.app.spzmanagement.Channel.Event mEvent : mChannel.getEvents()) {
                new LogTask("getChannels|mEvent:" + mEvent.getEventName(), TAB, LogTask.LOG_D);
            }
        }
    }

    public Channel subscribe(solstudios.app.spzmanagement.Channel channel,
                             @Nullable SubscriptionEventListener subscriptionEventListener) {
        Pusher pusher = PusherClientInstance.getInstance(context).getCurrentPusherInstance();
        if (pusher.getConnection().getState() == ConnectionState.CONNECTED
                || pusher.getConnection()
                .getState() == ConnectionState.CONNECTING) {
            Channel pchannel;
            if (pusher.getChannel(channel.getChannelName()) == null) {
                pchannel = pusher.subscribe(channel.getChannelName(), new DefaultSubscriptionChannelListener());
            } else {
                pchannel = pusher.getChannel(channel.getChannelName());
            }
            return pchannel;
        } else {
            new LogTask("subcribe|error: channel is disconnected", TAB, LogTask.LOG_E);
            // checkConnection();
            return null;
        }
    }

    public boolean isSubcribed(solstudios.app.spzmanagement.Channel channel) {
        return isSubcribed(channel.getChannelName());
    }

    public boolean isSubcribed(String channelName) {
        Pusher pusher = PusherClientInstance.getInstance(context).getCurrentPusherInstance();

        if (pusher.getChannel(channelName) != null) {
            if (pusher.getChannel(channelName).isSubscribed())
                return true;
            else return false;
        } else {
            return false;
        }
    }

    public boolean isConnected() {
        Pusher pusher = PusherClientInstance.getInstance(context).getCurrentPusherInstance();
        if (pusher.getConnection().getState() == ConnectionState.CONNECTED
                || pusher.getConnection().getState() == ConnectionState.CONNECTING) {
            return true;
        } else {
            return false;
        }
    }


    public void bind(solstudios.app.spzmanagement.Channel mChannel, solstudios.app.spzmanagement.Channel.Event event,
                     @Nullable SubscriptionEventListener subscriptionEventListener) {
        new LogTask("bind|channel:" + mChannel.getChannelName() + ",event:" + event.getEventName(), TAB, LogTask.LOG_E);
        Pusher pusher = PusherClientInstance.getInstance(context).getCurrentPusherInstance();
        if (pusher.getChannel(mChannel.getChannelName()) == null
                || !pusher.getChannel(mChannel.getChannelName()).isSubscribed()) {
            subscribe(mChannel, null);
            return;
        } else {
            Channel channel = pusher.getChannel(mChannel.getChannelName());

            if (subscriptionEventListener != null) {
                channel.bind(event.getEventName(), subscriptionEventListener);
            } else {
                channel.bind(event.getEventName(), new DefaultSubscriptionEventListener());
            }


            Intent onEventItent = new Intent(PusherBroadcast.getActionIntent(PusherBroadcast.ACTION_BIND_EVENT));
            Bundle subscriptionBundle = new Bundle();
            subscriptionBundle.putString(PusherBroadcast.SUBSCRIPTION_CHANNEL, mChannel.getChannelName());
            subscriptionBundle.putString(PusherBroadcast.SUBSCRIPTION_EVENT, event.getEventName());
            onEventItent.putExtra(PusherBroadcast.INTENT_BINDING, subscriptionBundle);
            context.sendBroadcast(onEventItent);
        }

    }

    public void bind(Channel pusherChannel, solstudios.app.spzmanagement.Channel.Event event,
                     @Nullable SubscriptionEventListener subscriptionEventListener) {
        new LogTask("subcribe|error: channel is disconnected", TAB, LogTask.LOG_E);
        if (subscriptionEventListener != null) {
            pusherChannel.bind(event.getEventName(), subscriptionEventListener);
        } else {
            pusherChannel.bind(event.getEventName(), new DefaultSubscriptionEventListener());
        }

        Intent onEventItent = new Intent(PusherBroadcast.getActionIntent(PusherBroadcast.ACTION_BIND_EVENT));
        Bundle subscriptionBundle = new Bundle();
        subscriptionBundle.putString(PusherBroadcast.SUBSCRIPTION_CHANNEL, pusherChannel.getName());
        subscriptionBundle.putString(PusherBroadcast.SUBSCRIPTION_EVENT, event.getEventName());
        onEventItent.putExtra(PusherBroadcast.INTENT_BINDING, subscriptionBundle);
        context.sendBroadcast(onEventItent);

    }

    public void subscribe(solstudios.app.spzmanagement.Channel channel) {
        Pusher pusher = PusherClientInstance.getInstance(context).getCurrentPusherInstance();
        if (pusher.getConnection().getState() == ConnectionState.CONNECTED
                || pusher.getConnection()
                .getState() == ConnectionState.CONNECTING) {
            if (pusher.getChannel(channel.getChannelName()) == null) {
                pusher.subscribe(channel.getChannelName(), new DefaultSubscriptionChannelListener());
            }
        }
    }

    /**
     * Tải thông tin về pusher client, lưu trữ tại
     */

    /** public static void downloadPusherClientInfo(final Context context) {
     new LogTask("downloadPusherClientInfo", TAB, LogTask.LOG_I);
     ServiceHelper serviceHelper = new ServiceHelper(context);
     serviceHelper.downloadJSON(API.API_PUSHER_CLIENT,
     new Response.Listener<JSONObject>() {

    @Override public void onResponse(JSONObject jsonObject) {
    // TODO Auto-generated method stub
    new LogTask(
    "DownloadTask|downloadPusherData:"
    + jsonObject.toString(),
    TAB, LogTask.LOG_D);
    update(context, jsonObject);

    }
    }, new Response.ErrorListener() {

    @Override public void onErrorResponse(VolleyError arg0) {
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

    /**
     * public static void update(Context context, JSONObject jsonObject) {
     * new LogTask("update", TAB, LogTask.LOG_I);
     * try {
     * JSONArray jsonArray = jsonObject
     * .getJSONArray(PusherHelper.TAG_ROOT);
     * for (int i = 0; i < jsonArray.length(); i++) {
     * <p>
     * JSONObject e = jsonArray.getJSONObject(i);
     * <p>
     * new LogTask("update|appid="
     * + e.getString(PusherHelper.TAG_APPID).toString()
     * + ",pusher_channel="
     * + e.getString(PusherHelper.TAG_CHANNEL).toString()
     * + ",pusher_event="
     * + e.getString(PusherHelper.TAG_EVENT).toString()
     * + ",cluster="
     * + e.getString(PusherHelper.TAG_CLUSTER).toString(), TAB,
     * LogTask.LOG_D);
     * <p>
     * String appid = e.getString(PusherHelper.TAG_APPID).toString();
     * String channel = e.getString(PusherHelper.TAG_CHANNEL)
     * .toString();
     * String event = e.getString(PusherHelper.TAG_EVENT).toString();
     * String cluster = e.getString(PusherHelper.TAG_CLUSTER)
     * .toString();
     * <p>
     * /// write new pusher-client-info
     * SharedPreferences defaultSharedPreferences = PreferenceManager
     * .getDefaultSharedPreferences(context);
     * Editor editor = defaultSharedPreferences.edit();
     * editor.putString(SettingActivity.PREF_PUSHER_CLIENT_APPID,
     * appid);
     * editor.putString(SettingActivity.PREF_PUSHER_CLIENT_CHANNEL,
     * channel);
     * editor.putString(SettingActivity.PREF_PUSHER_CLIENT_EVENT,
     * event);
     * editor.putString(SettingActivity.PREF_PUSHER_CLIENT_CLUSTER,
     * cluster);
     * editor.commit();
     * <p>
     * }
     * <p>
     * } catch (JSONException e) {
     * // TODO Auto-generated catch block
     * e.printStackTrace();
     * }
     * }
     */

    public void checkPusherConnection() {
        new LogTask("checkPusherConnection", TAB, LogTask.LOG_I);
        resume();
    }

    /**
     * Ngắt kết nối (tạm thời) khi khởi động ứng dụng
     */
    public void disconnect() {
        new LogTask("disconnect", TAB, LogTask.LOG_I);
        PusherClientInstance.getInstance(context).disconnect();
    }

    public void unsubscribe(solstudios.app.spzmanagement.Channel channel) {
        Pusher pusher = PusherClientInstance.getInstance(context).getCurrentPusherInstance();
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

        Pusher pusher = PusherClientInstance.getInstance(context).getCurrentPusherInstance();
        if (pusher.getConnection()
                .getState() == ConnectionState.CONNECTED
                || pusher.getConnection()
                .getState() == ConnectionState.CONNECTING) {
            //checkConnection();

            ///
            ConnectionStateChange connectionStateChange = new ConnectionStateChange(ConnectionState.CONNECTING, ConnectionState.CONNECTED);
            Intent onConnectionStateChangeIntent = new Intent(PusherBroadcast.getActionIntent(PusherBroadcast.ACTION_CONNECTION_CHANGED));
            onConnectionStateChangeIntent.putExtra(PusherBroadcast.CONNECTION_STATE_CHANGED, new Gson().toJson(connectionStateChange));
            context.sendBroadcast(onConnectionStateChangeIntent);
        } else {
            onResume();
        }

    }

    private void onResume() {

        Pusher pusher = PusherClientInstance.getInstance(context).getCurrentPusherInstance();
        if (pusher.getConnection()
                .getState() == ConnectionState.DISCONNECTED
                || pusher.getConnection()
                .getState() == ConnectionState.DISCONNECTING) {
            //checkConnection();
            PusherClientInstance.getInstance(context).connect();
        }

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

}

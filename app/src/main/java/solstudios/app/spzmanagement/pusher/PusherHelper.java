package solstudios.app.spzmanagement.pusher;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.widget.Toast;

import com.pusher.client.Pusher;
import com.pusher.client.channel.Channel;
import com.pusher.client.channel.SubscriptionEventListener;
import com.pusher.client.connection.ConnectionEventListener;
import com.pusher.client.connection.ConnectionState;
import com.pusher.client.connection.ConnectionStateChange;

import org.json.JSONObject;

import solstudios.app.spzmanagement.ChannelStack;
import solstudios.app.spzmanagement.LogTask;


public class PusherHelper {
    public static final String TAB = "PusherHelper";

    public static final String PUSHER_HOST_URL = "http://www.lamthong.xyz/plo/pusher_content.json";
    public static final String TAG_ROOT = "plo_pusher_content";
    public static final String TAG_APPID = "app_id";
    public static final String TAG_CHANNEL = "pusher_channel";
    public static final String TAG_EVENT = "pusher_event";
    public static final String TAG_CLUSTER = "cluster";

    public static final String CLIENT_APPID = "client_appid";
    public static final String CLIENT_CHANNEL = "client_channel";
    public static final String CLIENT_EVENT = "client_event";
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

    public PusherHelper(Context context) {
        pusher = PusherClientInstance.INSTANCE.getInstance(context, false);
        init(context);
    }

    ;

    public PusherHelper(Context context, boolean reInit) {
        pusher = PusherClientInstance.INSTANCE.getInstance(context, reInit);
        init(context);
    }

    ;

    private void init(Context context) {
        this.context = context;
        notificationHelper = new NotificationHelper(context);
        sharedPreferences = PreferenceManager
                .getDefaultSharedPreferences(context);
    }


    public void connectToPusherClient(ConnectionEventListener connectionEventListener, SubscriptionEventListener subscriptionEventListener)
            throws Exception {
        new LogTask(
                "connectToPusherClient|appid:"
                        + PusherClientInstance.INSTANCE.currentAppID,
                TAB, LogTask.LOG_I);

        for (solstudios.app.spzmanagement.Channel mChannel : ChannelStack.getInstance()) {
            new LogTask("getChannels|mChannel:" + mChannel.getChannelName(), TAB, LogTask.LOG_D);
            if (pusher.getChannel(mChannel.getChannelName()) == null) {
                Channel pchannel = pusher.subscribe(mChannel.getChannelName());
                for (solstudios.app.spzmanagement.Channel.Event mEvent : mChannel.getEvents()) {
                    new LogTask("getChannels|mEvent:" + mEvent.getEventName(), TAB, LogTask.LOG_D);
                    pchannel.bind(mEvent.getEventName(), subscriptionEventListener);
                }
            } else {
                for (solstudios.app.spzmanagement.Channel.Event mEvent : mChannel.getEvents()) {
                    new LogTask("getChannels|mEvent:" + mEvent.getEventName(), TAB, LogTask.LOG_D);
                    if (!pusher.getChannel(mChannel.getChannelName()).isSubscribed())
                        pusher.getChannel(mChannel.getChannelName()).bind(mEvent.getEventName(), subscriptionEventListener);
                }

            }

        }
        //pchannel.bind(event.getEventName(), subscriptionEventListener);
        pusher.connect(connectionEventListener, ConnectionState.ALL);
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

    public void checkConnection(ConnectionEventListener connectionEventListener) {
        pusher.connect(connectionEventListener, ConnectionState.ALL);
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
                checkConnection(new ConnectionEventListener() {
                    @Override
                    public void onConnectionStateChange(ConnectionStateChange connectionStateChange) {
                        if (connectionStateChange.getCurrentState() == ConnectionState.CONNECTED)
                            new Handler().post(new Runnable() {
                                @Override
                                public void run() {
                                    Toast.makeText(context, "ReConnected Successfully!", Toast.LENGTH_SHORT).show();
                                }
                            });
                        else if (connectionStateChange.getCurrentState() == ConnectionState.DISCONNECTED) {
                            new Handler().post(new Runnable() {
                                @Override
                                public void run() {
                                    Toast.makeText(context, "Resumed error", Toast.LENGTH_SHORT).show();
                                }
                            });
                        }
                    }

                    @Override
                    public void onError(String s, String s1, Exception e) {

                    }
                });
            }
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }


    public void addPusherConnectionListener(PusherConnectionListener listener) {
        this.pusherListener = listener;
    }

    public interface PusherInterface {
        void finishDownloadPusherData(JSONObject jsonObject);
    }

    public interface PusherConnectionListener {
        void onConnectionStateChange(
                ConnectionStateChange connectionStateChange);

        void onError(String arg0, String arg1, Exception arg2);

        void onReceiveMessage(String channelName, String eventName,
                              final String data);
    }
}

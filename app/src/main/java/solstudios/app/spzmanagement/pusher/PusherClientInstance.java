package solstudios.app.spzmanagement.pusher;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;

import com.google.gson.Gson;
import com.pusher.client.Pusher;
import com.pusher.client.PusherOptions;
import com.pusher.client.connection.ConnectionEventListener;
import com.pusher.client.connection.ConnectionState;
import com.pusher.client.connection.ConnectionStateChange;

import solstudios.app.spzmanagement.LogTask;
import solstudios.app.spzmanagement.PusherBroadcast;


public class PusherClientInstance {
    public static final String TAB = "PusherClientInstance";
    public static final String THROWABLE_CONNECTED = "Aldready Connected";
    public static final String THROWABLE_CHANNEL_SUBSCRIBED = "Aldready SUBSCRIBED CHANNEL";

    private static PusherClientInstance mPusherClientInstance;
    public String curAppid, curCluster;
    private Context context;
    private Pusher pusher;

    private PusherClientInstance(Context context, @Nullable String appid, @Nullable String cluster) {
        this.context = context;
        ///create new pusher
        if (appid != null && cluster != null) {
            pusher = createNewPusher(appid, cluster);
        } else {
            pusher = createNewPusher();
        }

    }

    public static synchronized PusherClientInstance getInstance(Context context) {
        if (null == mPusherClientInstance) {
            mPusherClientInstance = new PusherClientInstance(context, null, null);
        }
        return mPusherClientInstance;
    }

    public static synchronized PusherClientInstance getInstance(Context context,
                                                                @Nullable String appid,
                                                                @Nullable String cluster) {
        if (mPusherClientInstance == null) {
            mPusherClientInstance = new PusherClientInstance(context, appid, cluster);
        } else {
            mPusherClientInstance.disconnect(mPusherClientInstance.getCurrentPusherInstance());
            mPusherClientInstance = new PusherClientInstance(context, appid, cluster);
        }

        return mPusherClientInstance;
    }


    public static synchronized PusherClientInstance newInstance(Context context) {
        if (mPusherClientInstance != null) {
            mPusherClientInstance.disconnect(mPusherClientInstance.getCurrentPusherInstance());
        }

        mPusherClientInstance = new PusherClientInstance(context, null, null);

        return mPusherClientInstance;
    }

    private Pusher createNewPusher(String appid, String cluster) {
        return create(appid, cluster);
    }

    private Pusher createNewPusher() {
        SharedPreferences sharedPreferences = PreferenceManager
                .getDefaultSharedPreferences(context);
        String appID = sharedPreferences.getString(
                PusherHelper.CLIENT_APPID,
                PusherHelper.DEFAULT_CLIENT_APPID);
        String cluster = sharedPreferences.getString(
                PusherHelper.CLIENT_CLUSTER,
                PusherHelper.DEFAULT_CLIENT_APPID);
        return create(appID, cluster);
    }

    private Pusher create(String appid, String cluster) {
        PusherOptions options = new PusherOptions();
        new LogTask("createNewPusher|appid:" + appid + " ,cluster:" + cluster, TAB, LogTask.LOG_I);
        options.setCluster(cluster);
        pusher = new Pusher(appid, options);

        ///save instance
        curAppid = appid;
        curCluster = cluster;

        return pusher;
    }

    public void connect() {
        pusher.connect(new DefaultConnectionEventListener(context), ConnectionState.ALL);
    }

    public void connect(Pusher pusher) {
        pusher.connect(new DefaultConnectionEventListener(context), ConnectionState.ALL);
    }

    public void disconnect() {
        pusher.disconnect();
    }

    public void disconnect(Pusher pusher) {
        pusher.disconnect();
    }

    public Pusher getCurrentPusherInstance() {
        if (pusher == null) {
            return createNewPusher();
        } else {
            return pusher;
        }
    }

    private static class DefaultConnectionEventListener implements ConnectionEventListener {
        private Context context;

        public DefaultConnectionEventListener(Context context) {
            this.context = context;
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

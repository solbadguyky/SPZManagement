package solstudios.app.spzmanagement.pusher;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

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
    private static PusherClientInstance mPusherClientInstance;
    private Context context;
    private Pusher pusher;
    private String currentAppID, currentCluster;

    private PusherClientInstance(Context context) {
        this.context = context;

        if (pusher == null) {
            ///create new pusher
            pusher = createNewPusher();
        }
    }

    public static synchronized PusherClientInstance getInstance(Context context) {
        if (null == mPusherClientInstance) {
            mPusherClientInstance = new PusherClientInstance(context);
        }
        return mPusherClientInstance;
    }

    public static synchronized PusherClientInstance newInstance(Context context) {
        if (mPusherClientInstance != null) {
            mPusherClientInstance.disconnect(mPusherClientInstance.getCurrentPusherInstance());
        }

        mPusherClientInstance = new PusherClientInstance(context);
        return mPusherClientInstance;
    }

    private Pusher createNewPusher() {
        SharedPreferences sharedPreferences = PreferenceManager
                .getDefaultSharedPreferences(context);
        PusherOptions options = new PusherOptions();

        String appID = sharedPreferences.getString(
                PusherHelper.CLIENT_APPID,
                PusherHelper.DEFAULT_CLIENT_APPID);
        String cluster = sharedPreferences.getString(
                PusherHelper.CLIENT_CLUSTER,
                PusherHelper.DEFAULT_CLIENT_APPID);

        new LogTask("createNewPusher|appid:" + appID + " ,cluster:" + cluster, TAB, LogTask.LOG_I);

        options.setCluster(cluster);
        pusher = new Pusher(appID, options);

        /// save instance
        currentAppID = appID;
        currentCluster = cluster;

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

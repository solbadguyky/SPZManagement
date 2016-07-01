package solstudios.app.spzmanagement.pusher;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.pusher.client.Pusher;
import com.pusher.client.PusherOptions;
import com.pusher.client.connection.ConnectionState;

import solstudios.app.spzmanagement.LogTask;


public enum PusherClientInstance {
    INSTANCE;

    public static final String TAB = "PusherClientInstance";

    private static Pusher pusher;
    public String currentAppID;

    public Pusher getInstance(Context context, boolean needReInit) {
        if (pusher == null) {
            synchronized (INSTANCE) {
                if (pusher == null) {
                    createNewPusher(context);
                } else {
                    if (needReInit) {
                        reInit(context);
                    }
                }
            }
        } else {
            if (needReInit) {
                reInit(context);
            }
        }
        return pusher;
    }

    private void createNewPusher(Context context) {
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
    }

    private void reInit(Context context) {
        if (pusher.getConnection().getState() == ConnectionState.CONNECTED
                || pusher.getConnection()
                .getState() == ConnectionState.CONNECTING) {
            pusher.disconnect();
        }
        createNewPusher(context);
    }
}

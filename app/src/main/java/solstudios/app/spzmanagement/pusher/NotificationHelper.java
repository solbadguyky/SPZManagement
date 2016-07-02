package solstudios.app.spzmanagement.pusher;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;

import org.json.JSONObject;

import solstudios.app.spzmanagement.LogTask;


public class NotificationHelper {
    public static final String TAB = "NotificationHelper";

    public static final String TAG_NOTIFICATION = "notification";
    public static final String TAG_NOTIFICATION_ID = "id";
    public static final String TAG_NOTIFICATION_TITLE = "title";
    public static final String TAG_NOTIFICATION_SUMMARY = "summary";
    public static final String TAG_NOTIFICATION_USER = "user";
    public static final String TAG_NOTIFICATION_URL = "url";


    private Context context;
    private NotifcationInterface notificationListener;
    private SharedPreferences sharedPreferences;
    private String pusherAppId, pusherAppCluster, pusherAppChannel, pusherAppEvent;

    public NotificationHelper(Context context) {
        this.context = context;
        init();
    }

    private void init() {

        sharedPreferences = PreferenceManager
                .getDefaultSharedPreferences(context);
        pusherAppId = sharedPreferences.getString(
                PusherHelper.CLIENT_APPID,
                PusherHelper.DEFAULT_CLIENT_APPID);
        pusherAppCluster = sharedPreferences.getString(
                PusherHelper.CLIENT_CLUSTER,
                PusherHelper.DEFAULT_CLIENT_CLUSTER);

    }

    public void addOnNotificationListener(NotifcationInterface listener) {
        this.notificationListener = listener;
    }

    public NotificationItem.Item parseServerItems(String source) throws Exception {
        try {
            JSONObject jsonObject = new JSONObject(source);
            new LogTask("Pusher: " + source, TAB, LogTask.LOG_D);

            return null;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public NotificationItem parseNotificationJson(String source)
            throws Exception {
        try {
            JSONObject jsonObject = new JSONObject(source);
            new LogTask("Pusher: " + source, TAB, LogTask.LOG_D);
            int notificationId = jsonObject.getInt(TAG_NOTIFICATION_ID);


            return null;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public void showNotification(NotificationItem notificationItem) {
        if (notificationItem != null) {
            try {

                pushNotificationText(notificationItem);

            } catch (Exception e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }

    public void pushNotificationText(NotificationItem notificationItem)
            throws Exception {
        NotificationItem.Item item = notificationItem.getItem();

        String title = item.title;
        String text = item.summary;
        long notificationID = item.itemid;


        try {
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {

                NotificationCompat.Builder builder = new NotificationCompat.Builder(
                        context).setAutoCancel(true)
                        .setDeleteIntent(createOnDismissedIntent(
                                context, notificationItem))
                        .setContentTitle(title).setContentText(text);

                Uri alarmSound = RingtoneManager
                        .getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
                builder.setSound(alarmSound);
                builder.setDefaults(NotificationCompat.DEFAULT_VIBRATE);

                Intent notificationIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://www.google.com"));
                PendingIntent contentIntent = PendingIntent.getActivity(context, 0, notificationIntent, 0);

                builder.setContentIntent(contentIntent);

                NotificationManager nManager = (NotificationManager) context
                        .getSystemService(Context.NOTIFICATION_SERVICE);

                nManager.notify((int) notificationID, builder.build());

                // .notify(notificationID, builder.build());
            } else {
                // Lollipop specific setColor method goes here.
                NotificationCompat.Builder builder = new NotificationCompat.Builder(
                        context)
                        .setAutoCancel(true).setContentTitle(title)
                        .setDeleteIntent(createOnDismissedIntent(
                                context, notificationItem))
                        .setContentText(text);
                builder.setColor(Color.parseColor("#AC0000"));

                Uri alarmSound = RingtoneManager
                        .getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
                builder.setSound(alarmSound);
                builder.setDefaults(NotificationCompat.DEFAULT_VIBRATE);


                Intent notificationIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://www.google.com"));
                PendingIntent contentIntent = PendingIntent.getActivity(context, 0, notificationIntent, 0);

                builder.setContentIntent(contentIntent);

                NotificationManager nManager = (NotificationManager) context
                        .getSystemService(Context.NOTIFICATION_SERVICE);
                nManager.notify((int) notificationID, builder.build());
            }

        } catch (Exception e) {
            if (notificationListener != null) {
                notificationListener.Error(notificationItem);
            }

            e.printStackTrace();
        }

    }

    private PendingIntent createOnDismissedIntent(Context context,
                                                  NotificationItem notificationItem) {
        Intent intent = new Intent(context,
                NotificationBroadcastListener.class);

        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                context.getApplicationContext(), (int) notificationItem.getItem().itemid,
                intent, PendingIntent.FLAG_UPDATE_CURRENT);
        return pendingIntent;
    }

    public interface NotifcationInterface {
        void onProcessPusherItem(NotificationItem rawItem);

        void Completed(NotificationItem processedItem);

        void Error(NotificationItem item);
    }

}

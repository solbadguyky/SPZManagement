package solstudios.app.spzmanagement;

import android.database.sqlite.SQLiteException;

import com.orm.SugarRecord;
import com.orm.dsl.Ignore;
import com.orm.dsl.Unique;

import java.util.ArrayList;

/**
 * Created by solbadguyky on 7/1/16.
 */
public class ChannelRecord extends SugarRecord {
    public static final String TAB = "ChannelRecord";

    @Unique
    long channelId;

    String channelName;

    @Ignore
    String[] channelEvents;

    public ChannelRecord() {

    }

    public ChannelRecord(long id, String name, String[] channelEvents) {
        this.channelId = id;
        this.channelName = name;
        this.channelEvents = channelEvents;
    }

    public static void saveAllRecords() throws SQLiteException {
        try {
            ChannelRecord maskChannelRecord = ChannelRecord.findById(ChannelRecord.class, 1);
        } catch (SQLiteException e) {
            new Throwable("Table not found");
            ChannelRecord maskchannelRecord = new ChannelRecord();
            maskchannelRecord.channelId = -1;
            maskchannelRecord.channelName = "Mask Channel";
            maskchannelRecord.channelEvents = new String[]{};
            maskchannelRecord.save();
        }

        ArrayList<Channel> channels = ChannelStack.getInstance();
        //String queryString = "SELECT * FROM " + TAB_ITEM + " WHERE "
        //        + COL_NOTI_ID + " =? ";
        if (ChannelRecord.isSugarEntity(ChannelRecord.class)) {
            for (Channel channel : channels) {
                if (ChannelRecord.find(ChannelRecord.class, "CHANNEL_ID = ?", new String[]{channel.getChannelName()}).size() > 0) {
                    new LogTask("Dettect ", TAB, LogTask.LOG_D);
                } else {
                    new LogTask("New ", TAB, LogTask.LOG_D);

                }
            }

        } else {
            new LogTask("Error ", TAB, LogTask.LOG_D);

        }


    }
}

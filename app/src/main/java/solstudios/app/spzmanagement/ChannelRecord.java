package solstudios.app.spzmanagement;

import android.database.sqlite.SQLiteException;

import com.orm.SugarRecord;
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

    String channelEvents;

    public ChannelRecord() {

    }

    public ChannelRecord(long id, String name, String channelEvents) {
        this.channelId = id;
        this.channelName = name;
        this.channelEvents = channelEvents;
    }

    /**
     * Lưu lại tất cả các channels records hiện có
     *
     * @return true nếu như không có lỗi gì xảy ra
     * @throws SQLiteException Nếu có sự cố về DB
     */
    public static boolean saveAllRecords() throws SQLiteException {
        try {
            ChannelRecord.findById(ChannelRecord.class, 1);
        } catch (SQLiteException e) {
            ChannelRecord maskchannelRecord = new ChannelRecord();
            maskchannelRecord.channelId = -1;
            maskchannelRecord.channelName = "Mask Channel";
            maskchannelRecord.channelEvents = "Mask Event";
            maskchannelRecord.save();
            return false;
        }

        ArrayList<Channel> channels = ChannelStack.getInstance();
        if (ChannelRecord.isSugarEntity(ChannelRecord.class)) {
            for (Channel channel : channels) {
                if (ChannelRecord.find(ChannelRecord.class, "CHANNEL_ID = ?", new String[]{channel.getChannelName()}).size() > 0) {
                    new LogTask("Dettect ", TAB, LogTask.LOG_D);
                    updateRecord(channel);
                } else {
                    new LogTask("New ", TAB, LogTask.LOG_D);
                    saveRecord(channel);
                }
            }

        } else {
            new LogTask("Error ", TAB, LogTask.LOG_D);
            return false;
        }
        return true;

    }

    public static void saveRecord(Channel channel) {
        if (ChannelRecord.isSugarEntity(ChannelRecord.class)) {
            ChannelRecord newchannelRecord = new ChannelRecord();
            newchannelRecord.channelId = channel.getChannelId();
            newchannelRecord.channelName = channel.getChannelName();
            newchannelRecord.channelEvents = channel.getEventsString();
            newchannelRecord.save();
        }
    }

    public static void updateRecord(Channel channel) {
        ChannelRecord newchannelRecord = new ChannelRecord();
        newchannelRecord.channelId = channel.getChannelId();
        newchannelRecord.channelName = channel.getChannelName();
        newchannelRecord.channelEvents = channel.getEventsString();
        newchannelRecord.update();
    }
}

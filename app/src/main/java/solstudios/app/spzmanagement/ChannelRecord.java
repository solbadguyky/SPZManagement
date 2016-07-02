package solstudios.app.spzmanagement;

import android.database.sqlite.SQLiteException;

import com.orm.SugarRecord;
import com.orm.dsl.NotNull;
import com.orm.dsl.Unique;

import java.util.ArrayList;
import java.util.List;

/**
 * Quản lí dữ liệu các channel được lưu trên app
 * Created by solbadguyky on 7/1/16.
 */
public class ChannelRecord extends SugarRecord {
    public static final String TAB = "ChannelRecord";
    @Unique
    @NotNull
    int channelId;
    String channelName;
    String channelEvents;

    public ChannelRecord() {

    }

    public ChannelRecord(int id, String name, String channelEvents) {
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
                    return updateRecord(channel);
                } else {
                    new LogTask("New ", TAB, LogTask.LOG_D);
                    return saveRecord(channel);
                }
            }

        } else {
            new LogTask("Error ", TAB, LogTask.LOG_D);
            return false;
        }
        return true;

    }

    /**
     * Lưu channel mới vào DB
     *
     * @param channel
     * @return true nếu channel được thêm vào thành công
     */
    public static boolean saveRecord(Channel channel) {
        if (ChannelRecord.isSugarEntity(ChannelRecord.class)) {
            ChannelRecord newchannelRecord = new ChannelRecord();
            newchannelRecord.channelId = channel.getChannelId();
            newchannelRecord.channelName = channel.getChannelName();
            newchannelRecord.channelEvents = channel.getEventsString();
            try {
                newchannelRecord.channelEvents = MySqliteHelper.convertArrayToJSONObject(channel.getEvents()).toString();
            } catch (Exception e) {
                e.printStackTrace();
            }
            if (newchannelRecord.save() > 0) {
                return true;
            } else return false;
        }

        return false;
    }

    /**
     * Cập nhật channel đã tồn tại
     *
     * @param channel
     * @return true nếu như channel được cập nhật thành công
     */
    public static boolean updateRecord(Channel channel) {
        new LogTask("updateRecord|channel:" + channel, TAB, LogTask.LOG_I);
        if (ChannelRecord.isSugarEntity(ChannelRecord.class)) {
            ChannelRecord newchannelRecord = new ChannelRecord();
            newchannelRecord.channelId = channel.getChannelId();
            newchannelRecord.channelName = channel.getChannelName();

            newchannelRecord.channelEvents = channel.getEventsString();

            try {
                newchannelRecord.channelEvents = MySqliteHelper.convertArrayToJSONObject(channel.getEvents()).toString();
            } catch (Exception e) {
                e.printStackTrace();
            }
            if (newchannelRecord.update() > 0) return true;
            else return false;
        }
        return false;
    }

    /**
     * Xóa event trong channel
     *
     * @param channel
     * @param event
     * @return
     * @throws SQLiteException
     */
    public static boolean deleteEvent(Channel channel, Channel.Event event) throws SQLiteException {
        new LogTask("deleteEvent|channel:" + channel + ",event:" + event, TAB, LogTask.LOG_I);
        if (ChannelRecord.isSugarEntity(ChannelRecord.class)) {
            List<ChannelRecord> recordList = ChannelRecord.find(
                    ChannelRecord.class, " CHANNEL_ID = ?", String.valueOf(channel.getChannelId()));
            if (recordList.size() > 0) {
                for (ChannelRecord record : recordList) {
                    new LogTask("deleteEvent|channelid:" + record.channelId + ",channelName:" + record.channelName, TAB, LogTask.LOG_D);
                    new LogTask("deleteEvent|events:" + record.channelEvents, TAB, LogTask.LOG_D);
                    new LogTask("deleteEvent|selected_event:" + event.getEventName(), TAB, LogTask.LOG_D);

                    channel.removeEventByName(event.getEventName());
                    if (channel.getEvents().size() > 0) {
                        updateRecord(channel);
                    } else {
                        deleteChannel(channel);
                    }

                }
            }
            return true;
        }
        return false;
    }


    /**
     * Xóa channel và toàn bộ event bên trong
     *
     * @param channel
     * @return
     * @throws SQLiteException
     */
    public static boolean deleteChannel(Channel channel) throws SQLiteException {
        new LogTask("deleteChannel|channel:" + channel, TAB, LogTask.LOG_I);
        if (ChannelRecord.isSugarEntity(ChannelRecord.class)) {
            List<ChannelRecord> recordList = ChannelRecord.find(
                    ChannelRecord.class, " CHANNEL_ID = ?", String.valueOf(channel.getChannelId()));

            //.findWithQuery(ChannelRecord.class, " CHANNEL_ID = ?", "114064");
            if (recordList.size() > 0) {
                for (ChannelRecord record : recordList) {
                    //  new LogTask("deleteChannel|channelid:" + record.channelId + ",channelName:" + record.channelName, TAB, LogTask.LOG_D);
                    //   new LogTask("deleteChannel|events:" + record.channelEvents, TAB, LogTask.LOG_D);
                    new LogTask("deleteChannel|try to delele:" + record.delete(), TAB, LogTask.LOG_D);
                }
            }
            return true;
        }
        return false;
    }

    public Channel convertRecordToChannel(ChannelRecord channelRecord) {
        Channel channel = new Channel();
        channel.setChannelId(channelRecord.channelId);
        channel.setChannelName(channelRecord.channelName);

        return null;
    }
}

package solstudios.app.spzmanagement;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * Created by solbadguyky on 6/30/16.
 */
public class ChannelStack {
    public static final String TAB = "ChannelStack";

    private static ChannelStack mChannelStackInstance = null;

    private IChannelStack iChannelStack;
    private ArrayList<Channel> channelArrayList;

    public ChannelStack() {
        init();
    }

    public static synchronized ChannelStack getInstance() {
        if (null == mChannelStackInstance) {
            mChannelStackInstance = new ChannelStack();
        }
        return mChannelStackInstance;
    }

    private void init() {
        channelArrayList = new ArrayList<>();
        channelArrayList.addAll(getAllRecords());
    }

    public void setItemChangeListener(IChannelStack itemChangeListener) {
        if (mChannelStackInstance.iChannelStack == null) {
            mChannelStackInstance.iChannelStack = itemChangeListener;
        }
    }

    private ArrayList<Channel> getAllRecords() {
        ArrayList<Channel> channelArrayList = new ArrayList<>();
        ArrayList<ChannelRecord> channelRecords = (ArrayList<ChannelRecord>) ChannelRecord.listAll(ChannelRecord.class);
        if (channelArrayList.isEmpty()) {
            for (ChannelRecord record : channelRecords) {
                Channel channel = new Channel();
                channel.setChannelName(record.channelName);
                channel.setChannelId(record.channelId);

                try {
                    JSONObject jsonObject = new JSONObject(record.channelEvents);
                    channel.setEventList(MySqliteHelper.getEventsFromJSONObject(jsonObject));
                } catch (JSONException e) {
                    e.printStackTrace();
                    channel.setEventList(new ArrayList<Channel.Event>());
                }
                channelArrayList.add(channel);
            }
        }
        return channelArrayList;
    }

    public ArrayList<Channel> getChannelStack() {
        if (channelArrayList == null) {
            channelArrayList = new ArrayList<>();
        }
        return this.channelArrayList;
    }

    /**
     * Override phương thức add của list, nhằm phát hiện sự thay đổi của danh sách channel
     * Override array_list.add method to catch changing data
     *
     * @param object channel thêm vào
     * @return
     */
    public boolean add(Channel object) {
        new LogTask("add", TAB, LogTask.LOG_I);
        boolean result = channelArrayList.add(object);
        if (iChannelStack != null) {
            iChannelStack.onItemChanged();
        }
        return result;
    }

    public Channel set(int index, Channel object) {
        new LogTask("set", TAB, LogTask.LOG_I);
        Channel channel = channelArrayList.set(index, object);
        if (iChannelStack != null) {
            iChannelStack.onItemChanged();
        }
        return channel;
    }

    /**
     * Override @add method để kiểm tra channel được thêm vào
     *
     * @param channel
     * @return true nếu channel được thêm mới/thay thế, false nếu như không có sự thay đổi nào
     */
    public boolean addChannel(Channel channel) {
        int index = matchIndex(channel);
        if (index > -1 && index < channelArrayList.size()) {
            Channel newChannel = syncChannel(index, channel);
            if (newChannel != null) {
                set(index, newChannel);
                return true;
            }
            return false;
        }
        return add(channel);
    }

    /**
     * Thêm channel bằng channel name
     *
     * @param channelName tên của channel được thêm vào
     *                    (Lưu ý: Nếu đã có channel trùng tên thì channel mới sẽ được gộp
     *                    vào channel cũ)
     * @return
     */
    public boolean addChannel(String channelName) {
        int index = matchIndex(channelName);
        if (index > -1 && index < channelArrayList.size()) {
            Channel newChannel = channelArrayList.get(index);
            if (newChannel != null) {
                set(index, newChannel);
                return true;
            }
            return false;
        } else {
            Channel newChannel = new Channel();
            newChannel.setChannelName(channelName);
            return add(newChannel);
        }

    }

    /**
     * So sánh channel mới danh sách channel hiện có, nếu channel mới đã có trong danh sách
     * thì so sánh các event có trong channel
     *
     * @param channel channel cần so sánh
     * @return channel mới (nếu có) hoặc channel cũ được bổ sung thêm event mới
     */
    public Channel syncChannel(int index, Channel channel) {
        return syncEvent(channelArrayList.get(index), channel);
    }

    /**
     * Đồng bộ giữa event cũ và mới từ 2 channel
     *
     * @param rootchannel channel gốc tương ứng
     * @param channel     channel mới
     * @return nếu có event nào trùng
     */
    private Channel syncEvent(Channel rootchannel, Channel channel) {
        Channel maskChannel = rootchannel;
        for (Channel.Event event : channel.getEvents()) {
            if (!checkMatchEvent(rootchannel, event)) {
                maskChannel.addEvent(event);
            }
        }
        return maskChannel;
    }

    public boolean addEvent(Channel channel, Channel.Event event) {
        new LogTask("addEvent|channel:" + channel.getChannelName() + ",event:" + event.getEventName(), TAB, LogTask.LOG_D);
        if (channelArrayList.contains(channel)) {
            int index = channelArrayList.indexOf(channel);
            if (channelArrayList.get(index).addEvent(event)) {
                callOnChanged();
            }
        } else {
            /// không có channel cần tìm
            return false;
        }
        ///catch all events
        return false;
    }

    public boolean addEvent(Channel channel, String eventName) {
        new LogTask("addEvent|channel:" + channel.getChannelName() + ",event:" + eventName, TAB, LogTask.LOG_D);

        if (channelArrayList.contains(channel)) {
            int index = channelArrayList.indexOf(channel);
            if (channelArrayList.get(index).addEvent(eventName)) {
                callOnChanged();
            }
        } else {
            /// không có channel cần tìm
            return false;
        }
        ///catch all events
        return false;
    }

    public boolean removeEvent(Channel channel, Channel.Event event) {
        new LogTask("removeEvent|channel:" + channel + ",event:" + event, TAB, LogTask.LOG_D);
        if (channelArrayList.contains(channel)) {
            Channel maskChannel = channelArrayList.get(channelArrayList.indexOf(channel));
            if (maskChannel.getEvents().contains(event)) {
                ArrayList<Channel.Event> eventArrayList = maskChannel.getEvents();
                if (eventArrayList.remove(event)) {
                    return true;
                } else {
                    ///tìm xem có event nào cùng tên không
                    for (Channel.Event childEvent : eventArrayList) {
                        if (childEvent.getChannelName().equals(event.getEventName())) {
                            return true;
                        }
                    }
                }
            } else {
                ///không có event cần tìm
                return false;
            }
        } else {
            /// không có channel cần tìm
            return false;
        }
        ///catch all events
        return false;
    }

    public void removeChannel(Channel channel) {
        int index = matchIndex(channel);
        if (index < channelArrayList.size()) {
            channelArrayList.remove(channel);
        }

    }

    public void checkNullChannel(Channel channel) {
        if (channel.getEvents() == null) {
            channelArrayList.remove(channel);
        }
    }

    /**
     * Tìm ra vị trí, nơi mà channel mới cùng tên với một channel cũ
     *
     * @param channel channel mới cần tìm vị trí
     * @return vị trí cần tìm
     */
    public int matchIndex(Channel channel) {
        if (channelArrayList.contains(channel)) {
            return channelArrayList.indexOf(channel);
        }
        return matchIndex(channel.getChannelName());
    }

    public int matchIndex(String channelName) {
        int i = 0;
        for (Channel mChannell : channelArrayList) {
            if (mChannell.getChannelName().equals(channelName)) {
                return i;
            }
            i++;
        }
        return -1;
    }

    public boolean checkMatchEvent(Channel rootchannel, Channel.Event event) {

        for (Channel.Event rEvent : rootchannel.getEvents()) {
            if (rEvent.getEventName().equals(event.getEventName())) {
                return true;
            }

        }
        return false;
    }

    private void callOnChanged() {
        if (iChannelStack != null) {
            iChannelStack.onItemChanged();
        }
    }

    public interface IChannelStack {
        void onItemChanged();
    }
}

package solstudios.app.spzmanagement;

import java.util.ArrayList;

/**
 * Created by solbadguyky on 6/30/16.
 */
public class ChannelStack extends ArrayList<Channel> {
    private static ChannelStack mChannelStackInstance = null;

    public static synchronized ChannelStack getInstance() {
        if (null == mChannelStackInstance) {
            mChannelStackInstance = new ChannelStack();
        }
        return mChannelStackInstance;
    }

    /**
     * Override @add method để kiểm tra channel được thêm vào
     *
     * @param channel
     * @return true nếu channel được thêm mới, false nếu như channel bị replace
     */
    @Override
    public boolean add(Channel channel) {
        int index = matchIndex(channel);
        if (index < size()) {
            Channel newChannel = syncChannel(index, channel);
            if (newChannel != null) {
                super.set(matchIndex(channel), newChannel);
                return false;
            }
            return true;
        }
        return super.add(channel);
    }

    /**
     * So sánh channel mới danh sách channel hiện có, nếu channel mới đã có trong danh sách thì so sánh các event có trong channel
     *
     * @param channel channel cần so sánh
     * @return channel mới (nếu có) hoặc channel cũ được bổ sung thêm event mới
     */
    public Channel syncChannel(int index, Channel channel) {
        return syncEvent(get(index), channel);
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

    /**
     * Tìm ra vị trí, nơi mà channel mới cùng tên với một channel cũ
     *
     * @param channel channel mới cần tìm vị trí
     * @return vị trí cần tìm
     */
    public int matchIndex(Channel channel) {
        int i = 0;
        for (Channel mChannell : this) {
            if (mChannell.getChannelName().equals(channel.getChannelName())) {
                break;
            }
            i++;
        }
        return i;
    }

    public boolean checkMatchEvent(Channel rootchannel, Channel.Event event) {

        for (Channel.Event rEvent : rootchannel.getEvents()) {
            if (rEvent.getEventName().equals(event.getEventName())) {
                return true;
            }

        }
        return false;
    }
}

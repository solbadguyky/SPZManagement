package solstudios.app.spzmanagement;

import android.app.Fragment;
import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;

/**
 * Created by solbadguyky on 6/30/16.
 */
public class BaseChannelAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    public static final String TAB = "BaseAdapter";

    ArrayList<Channel> channelArrayList;
    private BaseAdapterInterface baseAdapterInterface;

    public BaseChannelAdapter(Context context) {
        init(context);

    }


    public BaseChannelAdapter(Context context, ArrayList<Channel> initArray) {
        init(context);
        channelArrayList.addAll(initArray);
    }

    private void init(Context context) {
        baseAdapterInterface = (BaseAdapterInterface) context;

        if (channelArrayList == null)
            channelArrayList = new ArrayList<>();

    }

    public void addChannel(Channel channel) {
        channelArrayList.add(channel);
    }

    public void addChannels(ArrayList<Channel> channels) {
        this.channelArrayList.addAll(channels);
    }

    public void clear() {
        if (channelArrayList != null) {
            channelArrayList.clear();
        }
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.channel, parent, false);
        return new MyViewHolder.ChannelViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof MyViewHolder.ChannelViewHolder) {
            setUpChannelView((MyViewHolder.ChannelViewHolder) holder, channelArrayList.get(position));
        }
    }

    private void setUpChannelView(MyViewHolder.ChannelViewHolder itemView, Channel channel) {
        String eventsString = new String();
        int count = 0;
        for (Channel.Event event : channel.getEvents()) {
            if (count < channel.getEvents().size() - 1) {
                eventsString += event.getEventName() + ",";
            } else {
                eventsString += event.getEventName();
            }
            count++;
        }
        itemView.buttonAction.setText(channel.getChannelName() + ":" + eventsString);
        itemView.buttonAction.setOnClickListener(new ButtonListener(channel));
    }

    @Override
    public int getItemCount() {
        new LogTask("getItemCount:" + channelArrayList.size(), TAB, LogTask.LOG_I);
        return channelArrayList.size();
    }

    @Override
    public int getItemViewType(int position) {
        return super.getItemViewType(position);
    }

    public class ButtonListener implements View.OnClickListener {
        private Channel channel;

        public ButtonListener(Channel channel) {
            this.channel = channel;
        }

        @Override
        public void onClick(View v) {
            if (baseAdapterInterface != null) {
                baseAdapterInterface.onChannelButtonClick(channel);
            }
        }

    }

    public interface BaseAdapterInterface {
        void onChannelButtonClick(Channel channel);
    }
}

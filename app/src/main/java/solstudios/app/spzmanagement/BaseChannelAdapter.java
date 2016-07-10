package solstudios.app.spzmanagement;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;

import solstudios.app.spzmanagement.pusher.PusherHelper;

/**
 * Created by solbadguyky on 6/30/16.
 */
public class BaseChannelAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    public static final String TAB = "BaseAdapter";

    ArrayList<Channel> channelArrayList;
    private BaseAdapterInterface baseAdapterInterface;
    private PusherHelper pusherHelper;


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

        pusherHelper = new PusherHelper(context);

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

        itemView.textViewChannelName.setText(channel.getChannelName());
        itemView.textViewChannelEvents.setText("(" + channel.getEvents().size() + " event)");
        itemView.buttonAction.setOnClickListener(new ButtonListener(channel));
        itemView.buttonAction.setOnLongClickListener(new ButtonLongClickListener(channel));

        ///check channel's subscription
        if (pusherHelper.isSubcribed(channel)) {
            itemView.checkBoxChannelStatus.setChecked(true);
        } else {
            itemView.checkBoxChannelStatus.setChecked(false);
        }
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

    public interface BaseAdapterInterface {
        void onChannelButtonClick(Channel channel);

        void onChannelButtonLongClick(Channel channel);
    }

    private class ButtonListener implements View.OnClickListener {
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

    private class ButtonLongClickListener implements View.OnLongClickListener {
        private Channel channel;

        public ButtonLongClickListener(Channel channel) {
            this.channel = channel;
        }

        @Override
        public boolean onLongClick(View v) {
            if (baseAdapterInterface != null) {
                baseAdapterInterface.onChannelButtonLongClick(channel);
            }
            return false;
        }
    }
}

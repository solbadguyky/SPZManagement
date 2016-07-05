package solstudios.app.spzmanagement;

import android.support.v7.widget.AppCompatCheckBox;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

/**
 * Created by solbadguyky on 6/30/16.
 */
public class MyViewHolder {
    public static class ChannelViewHolder extends RecyclerView.ViewHolder {

        public Button buttonAction;
        public TextView textViewChannelName, textViewChannelEvents;
        public AppCompatCheckBox checkBoxChannelStatus;

        public ChannelViewHolder(View itemView) {
            super(itemView);
            buttonAction = (Button) itemView.findViewById(R.id.channel_button_Action);
            textViewChannelName = (TextView) itemView.findViewById(R.id.channel_textView_ChannelName);
            textViewChannelEvents = (TextView) itemView.findViewById(R.id.channel_textView_ChannelEvent);
            checkBoxChannelStatus = (AppCompatCheckBox) itemView.findViewById(R.id.channel_checkBox_Status);

        }
    }

    public static class EventViewHolder extends RecyclerView.ViewHolder {

        public TextView eventNameTextView;

        public EventViewHolder(View itemView) {
            super(itemView);
            eventNameTextView = (TextView) itemView.findViewById(R.id.event_textView_EventName);
        }
    }

}

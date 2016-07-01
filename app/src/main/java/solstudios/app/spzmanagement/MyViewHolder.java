package solstudios.app.spzmanagement;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

/**
 * Created by solbadguyky on 6/30/16.
 */
public class MyViewHolder {
    public static class ChannelViewHolder extends RecyclerView.ViewHolder {

        public Button buttonAction;

        public ChannelViewHolder(View itemView) {
            super(itemView);
            buttonAction = (Button) itemView.findViewById(R.id.channel_button_Action);

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

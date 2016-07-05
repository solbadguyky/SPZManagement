package solstudios.app.spzmanagement;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.AppCompatCheckBox;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Created by solbadguyky on 7/1/16.
 */
public class EventDialog extends DialogFragment {
    public static final String TAB = "EventDialog";

    private Channel channel;
    private IEventDialogItem iEventDialogItem;

    public EventDialog() {
        super();
    }

    /**
     * Create a new instance of MyDialogFragment, providing "num"
     * as an argument.
     */
    static EventDialog newInstance(Channel channel) {
        EventDialog f = new EventDialog();

        // Supply num input as an argument.
        Bundle args = new Bundle();
        args.putSerializable(Channel.TAB, channel);
        f.setArguments(args);

        return f;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder alertDialogBuild = new AlertDialog.Builder(getActivity());
        LayoutInflater layoutInflater = getActivity().getLayoutInflater();
        View customView = layoutInflater.inflate(R.layout.event_dialog, null);
        alertDialogBuild.setView(customView);
        alertDialogBuild.setTitle(channel.getChannelName());
        alertDialogBuild.setMessage("Click Single Row To View Debug Message.Long Click to Delete Event");

        alertDialogBuild.setNeutralButton("Add more events", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (iEventDialogItem != null) {
                    iEventDialogItem.onAddMoreEvents(channel);
                }

            }
        });

        alertDialogBuild.setPositiveButton("Delete Channel (All Events Will be Deleted)", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                //.deleteChannel(channel);
                if (iEventDialogItem != null) {
                    iEventDialogItem.onDeleteChannel(channel);
                }
            }
        });

        ///display event list
        ListView listView = (ListView) customView.findViewById(R.id.event_diaglog_listView);
        //final String[] eventArr = channel.getEventsArray();
        //ArrayAdapter<String> adapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_list_item_1, eventArr);
        EventAdapter eventAdapter = new EventAdapter(getActivity(), channel.getEvents());

        listView.setAdapter(eventAdapter);

        listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                if (iEventDialogItem != null) {
                    iEventDialogItem.onLongPressItem(channel, channel.getEvents().get(position));

                }

                dismiss();
                /*if (ChannelRecord.deleteEvent(channel, event)) {
                    dismiss();
                    return true;
                } */
                return false;
            }
        });

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (iEventDialogItem != null) {
                    iEventDialogItem.onPressItem(channel, channel.getEvents().get(position));
                }

                dismiss();
            }
        });

        return alertDialogBuild.create();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return super.onCreateView(inflater, container, savedInstanceState);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        channel = (Channel) getArguments().getSerializable(Channel.TAB);
        if (iEventDialogItem == null) {
            iEventDialogItem = (IEventDialogItem) getActivity();
        }
    }

    @Override
    public void onAttach(Context context) {
        new LogTask("onAttach", TAB, LogTask.LOG_I);
        super.onAttach(context);
        try {
            iEventDialogItem = (IEventDialogItem) context;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onDismiss(DialogInterface dialog) {
        super.onDismiss(dialog);
        if (iEventDialogItem != null) {
            iEventDialogItem.onDismiss(TAB);
        }
    }

    public interface IEventDialogItem {
        void onLongPressItem(Channel channel, Channel.Event event);

        void onPressItem(Channel channel, Channel.Event event);

        void onDeleteChannel(Channel channel);

        void onAddMoreEvents(Channel channel);

        void onDismiss(String name);
    }

    public class EventAdapter extends ArrayAdapter<Channel.Event> {

        public static final String TAB = "EventAdapter";

        private ArrayList<Channel.Event> eventArrayList;

        public EventAdapter(Context context, ArrayList<Channel.Event> initEvent) {
            super(context, 0);
            this.eventArrayList = initEvent;
        }

        @Override
        public int getCount() {
            if (this.eventArrayList == null) {
                this.eventArrayList = new ArrayList<>();
            }
            return this.eventArrayList.size();
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            //return super.getView(position, convertView, parent);
            Channel.Event item = eventArrayList.get(position);
            View eventView = LayoutInflater.from(parent.getContext()).inflate(R.layout.event, parent, false);
            TextView eventNameTextView = (TextView) eventView.findViewById(R.id.event_textView_EventName);
            AppCompatCheckBox eventStatusCheckBox = (AppCompatCheckBox) eventView.findViewById(R.id.event_checkBox_Status);

            eventNameTextView.setText(item.getEventName());

            return eventView;
        }
    }

}

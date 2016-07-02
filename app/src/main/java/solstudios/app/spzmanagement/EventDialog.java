package solstudios.app.spzmanagement;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

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
        final String[] eventArr = channel.getEventsArray();
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_list_item_1, eventArr);
        listView.setAdapter(adapter);

        listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                if (iEventDialogItem != null) {
                    iEventDialogItem.onLongPressItem(channel, channel.getEvents().get(position));
                }
                /*if (ChannelRecord.deleteEvent(channel, event)) {
                    dismiss();
                    return true;
                } */
                return false;
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

    public interface IEventDialogItem {
        void onLongPressItem(Channel channel, Channel.Event event);

        void onPressItem(Channel channel, Channel.Event event);

        void onDeleteChannel(Channel channel);
    }
}

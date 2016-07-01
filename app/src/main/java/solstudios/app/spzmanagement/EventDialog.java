package solstudios.app.spzmanagement;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.util.ArrayList;

/**
 * Created by solbadguyky on 7/1/16.
 */
public class EventDialog extends DialogFragment {
    public static final String TAB = "EventDialog";

    private Channel channel;

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

            }
        });
        ListView listView = (ListView) customView.findViewById(R.id.event_diaglog_listView);

        ArrayList<String> eventsString = new ArrayList<>();
        int count = 0;
        for (Channel.Event event : channel.getEvents()) {
            eventsString.add(event.getEventName().toString());
            count++;
        }
        String[] eventArr = new String[eventsString.size()];
        eventsString.toArray(eventArr);
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_list_item_1, eventArr);
        listView.setAdapter(adapter);
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
    }
}

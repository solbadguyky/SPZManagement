package solstudios.app.spzmanagement.pusher;

import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.TextInputEditText;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import solstudios.app.spzmanagement.Channel;
import solstudios.app.spzmanagement.R;

/**
 * Created by solbadguyky on 7/3/16.
 */

public class InputFragment extends DialogFragment {

    public static final String TAB = "InputFragment";

    private View dialogView;
    private TextInputEditText textInputEditText;

    private Channel currentChannel;
    private IFragmentInput iFragmentInput;

    public InputFragment() {
    }

    public static InputFragment newEvent(Channel channel) {
        InputFragment inputFragment = new InputFragment();
        Bundle bundle = new Bundle();
        bundle.putSerializable(Channel.TAB, channel);
        inputFragment.setArguments(bundle);
        return inputFragment;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        return super.onCreateView(inflater, container, savedInstanceState);
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        android.support.v7.app.AlertDialog.Builder alertDialogBuilder =
                new android.support.v7.app.AlertDialog.Builder(getActivity());
        dialogView = getActivity().getLayoutInflater().inflate(R.layout.event_input, null);
        alertDialogBuilder.setView(dialogView);

        initValue();
        initView();
        setupValue();
        setupView();

        alertDialogBuilder.setTitle("Add Event");
        alertDialogBuilder.setPositiveButton("Add", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (iFragmentInput != null) {
                    if (getEventString() != null) {
                        iFragmentInput.onSaveEvent(currentChannel, getEventString());
                    }
                    //iFragmentInput.onSaveEvent(currentChannel);
                }
            }
        });

        return alertDialogBuilder.create();
    }

    @Override
    public void onDismiss(DialogInterface dialog) {
        if (iFragmentInput != null) {
            iFragmentInput.onDismiss(TAB);

        }
        super.onDismiss(dialog);
    }

    private void initValue() {
        currentChannel = (Channel) getArguments().getSerializable(Channel.TAB);
        if (currentChannel == null) {
            this.dismiss();
        }
    }

    private void initView() {
        textInputEditText = (TextInputEditText) dialogView.findViewById(R.id.event_input_editText);
    }

    private void setupValue() {

    }

    private void setupView() {

    }

    public void setNewChannel(Channel channel) {
        this.currentChannel = channel;
    }

    public void setInputChangeListener(IFragmentInput listener) {
        if (this.iFragmentInput == null) {
            this.iFragmentInput = listener;
        }
    }

    private String getEventString() {
        String eventString = textInputEditText.getText().toString();
        if (eventString != null && !eventString.isEmpty()) {
            return eventString;
        } else {
            textInputEditText.setError("Null is not allowed here! Sorry Bro");
            return null;
        }
    }


    public interface IFragmentInput {
        void onSaveEvent(Channel channel, Channel.Event event);

        void onSaveEvent(Channel channel, String event);

        void onDismiss(String name);
    }

}

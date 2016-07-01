package solstudios.app.spzmanagement;

import android.app.DialogFragment;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.view.menu.MenuBuilder;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.pusher.client.Pusher;
import com.pusher.client.channel.SubscriptionEventListener;
import com.pusher.client.connection.ConnectionEventListener;
import com.pusher.client.connection.ConnectionState;
import com.pusher.client.connection.ConnectionStateChange;

import java.util.ArrayList;

import solstudios.app.spzmanagement.pusher.PusherHelper;

/**
 * Created by solbadguyky on 6/30/16.
 */
public class ConfigurationActivity extends BaseActivity implements BaseChannelAdapter.BaseAdapterInterface {
    public static final String TAB = "Configuration Activity";

    private SharedPreferences sharedPreferences;

    ///large layout
    private CoordinatorLayout coordinatorLayout;
    private RecyclerView channelRecyclerView;
    private LinearLayoutManager layoutManager;


    ///small layout
    private Toolbar toolbar;
    private android.app.ActionBar actionBar;
    private Snackbar snackbar;
    private Button submitButton, connectButton, checkChannelButton;
    private EditText appIdEditText, channelEditText, eventEditText, clusterEditText;
    private TextView conenctionTextView;
    private Bundle pusherInfoBundle;
    private String appId, channel, event, cluster;
    private String old_appId, old_channel, old_event, old_cluster;
    private BaseChannelAdapter baseAdapter;

    private PusherHelper pusherHelper;
    private Pusher currentPusherInstance;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setContentView(R.layout.configuration_frame);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        this.getMenuInflater().inflate(R.menu.configuration_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.configuration_menu_save:
                save();

                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void initValue() {
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        pusherInfoBundle = new Bundle();
        appId = new String();
        channel = new String();
        event = new String();
        cluster = new String();

        pusherHelper = new PusherHelper(this);

        ///retrieve old prefs
        old_appId = sharedPreferences.getString(PusherHelper.CLIENT_APPID, null);
        old_cluster = sharedPreferences.getString(PusherHelper.CLIENT_CLUSTER, null);
    }

    @Override
    public void initView() {
        coordinatorLayout = (CoordinatorLayout) findViewById(R.id.configuration_frame);
        toolbar = (Toolbar) findViewById(R.id.configuration_frame_toolBar);
        channelRecyclerView = (RecyclerView) findViewById(R.id.configuration_frame_recyclerView);

        checkChannelButton = (Button) findViewById(R.id.menu_button_CheckChannel);
        submitButton = (Button) findViewById(R.id.menu_button_Submit);
        connectButton = (Button) findViewById(R.id.menu_button_Connect);
        appIdEditText = (EditText) findViewById(R.id.menu_editText_AppID);
        channelEditText = (EditText) findViewById(R.id.menu_editText_Channel);
        eventEditText = (EditText) findViewById(R.id.menu_editText_Event);
        clusterEditText = (EditText) findViewById(R.id.menu_editText_Cluster);
        conenctionTextView = (TextView) findViewById(R.id.menu_textView_Connect);

    }

    @Override
    public void setupValue() {
        this.setSupportActionBar(toolbar);

        actionBar = getActionBar();
        final Drawable upArrow = ContextCompat.getDrawable(this,
                R.drawable.abc_ic_ab_back_mtrl_am_alpha);
        upArrow.setColorFilter(getResources().getColor(R.color.White),
                PorterDuff.Mode.SRC_ATOP);
        getSupportActionBar().setHomeAsUpIndicator(upArrow);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        toolbar.setNavigationOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                new LogTask("Pop Pop Pop!!!", TAB, LogTask.LOG_D);
                onBackPressed();
            }
        });
    }

    @Override
    public void setupView() {
        channelRecyclerView.setNestedScrollingEnabled(true);

        layoutManager = new LinearLayoutManager(this);
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        layoutManager.setAutoMeasureEnabled(true);
        channelRecyclerView.setLayoutManager(layoutManager);
        baseAdapter = new BaseChannelAdapter(this);
        channelRecyclerView.setAdapter(baseAdapter);

        appIdEditText.addTextChangedListener(new EditTextChangeListener(appIdEditText));
        channelEditText.addTextChangedListener(new EditTextChangeListener(channelEditText));
        eventEditText.addTextChangedListener(new EditTextChangeListener(eventEditText));
        clusterEditText.addTextChangedListener(new EditTextChangeListener(clusterEditText));

        ///snackbar
        snackbar = Snackbar
                .make(coordinatorLayout, "Had a snack at Snackbar", Snackbar.LENGTH_LONG);
        snackbar.setActionTextColor(Color.RED);
        View snackbarView = snackbar.getView();
        snackbarView.setBackgroundColor(Color.DKGRAY);
        TextView textView = (TextView) snackbarView.findViewById(android.support.design.R.id.snackbar_text);
        textView.setTextColor(Color.YELLOW);

        ///pusher id
        if (old_appId != null) {
            if (!old_appId.isEmpty()) {
                appIdEditText.setHint(old_appId);
            }
        }

        if (old_cluster != null) {
            if (!old_cluster.isEmpty()) {
                clusterEditText.setHint(old_cluster);
            }
        }

        if (old_appId == null || old_cluster == null) {
            disableChannelView();
            snackbar.setText("Pusher has not been configured!");
            snackbar.show();

        } else if (old_appId.isEmpty() || old_cluster.isEmpty()) {
            disableChannelView();
            snackbar.setText("Pusher has not been configured!");
            snackbar.show();

        } else {
            connect();
        }

        connectButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                connect();
            }
        });

        submitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                submitChannel();
            }
        });

        checkChannelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getChannels();
            }
        });

        ///channel info
        if (old_channel != null) {
            if (!old_channel.isEmpty()) {
                channelEditText.setHint(old_channel);
            }
        }

        if (old_event != null) {
            if (!old_event.isEmpty()) {
                eventEditText.setHint(old_event);
            }
        }


    }

    private void disableChannelView() {
        submitButton.setEnabled(false);
        channelEditText.setEnabled(false);
        eventEditText.setEnabled(false);
    }

    private void enableEventView() {
        submitButton.setEnabled(true);
        channelEditText.setEnabled(true);
        eventEditText.setEnabled(true);
    }

    private void checkValueAgain() {
        appId = appIdEditText.getText().toString();
        channel = channelEditText.getText().toString();
        event = eventEditText.getText().toString();
        cluster = clusterEditText.getText().toString();
    }

    private void submitChannel() {
        new LogTask("submitBundle", TAB, LogTask.LOG_I);
        checkValueAgain();
        if (!channel.isEmpty() && !event.isEmpty()) {
            writeValueToPref(channel, PusherHelper.CLIENT_CHANNEL);
            writeValueToPref(event, PusherHelper.CLIENT_EVENT);
            try {
                Channel mchannel = new Channel(channel);
                Channel.Event mEvent = new Channel.Event(event);
                mchannel.addEvent(mEvent);
                subcribeChannel(mchannel, mEvent);
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            snackbar.setText("Please Enter Values Before Submitting Configuration!");
            snackbar.show();
        }
    }

    private void connect() {
        new LogTask("connect", TAB, LogTask.LOG_I);
        checkValueAgain();
        if (!appId.isEmpty() && !cluster.isEmpty()) {
            writeValueToPref(appId, PusherHelper.CLIENT_APPID);
            writeValueToPref(cluster, PusherHelper.CLIENT_CLUSTER);
            onConnecting();
        } else {
            if (old_appId != null && old_cluster != null) {
                onConnecting();
            } else {
                snackbar.setText("Please Enter Values Before Connecting!");
                snackbar.show();
            }
        }
    }

    private void onConnecting() {
        new LogTask("onConnecting", TAB, LogTask.LOG_I);
        pusherHelper = new PusherHelper(this, true);
        pusherHelper.checkConnection(new ConnectionEventListener() {
            @Override
            public void onConnectionStateChange(final ConnectionStateChange connectionStateChange) {
                new LogTask("connect|ConnectionEventListener,connectionStateChange:" + connectionStateChange.getCurrentState().name(), TAB, LogTask.LOG_I);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        //conenctionTextView.setText(connectionStateChange.getCurrentState().name());

                        if (connectionStateChange.getCurrentState() == ConnectionState.CONNECTED) {
                            conenctionTextView.setText(connectionStateChange.getCurrentState().name());
                            snackbar.setText("Successfully Connected!").show();
                            enableEventView();
                            getChannels();
                        } else if (connectionStateChange.getCurrentState() == ConnectionState.DISCONNECTED) {
                            snackbar.setAction("Retry", new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    connect();
                                }
                            }).setText("Disconnected").show();
                        }
                    }
                });
            }

            @Override
            public void onError(String s, String s1, final Exception e) {
                new LogTask("connect|ConnectionEventListener,error,s1:" + s1 + ",\ns:" + s + ",\ne:" + e.getMessage(), TAB, LogTask.LOG_I);

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        conenctionTextView.setText(e.getMessage());
                    }
                });

            }
        });
    }

    private void subcribeChannel(final Channel mChannel, Channel.Event mEvent) throws Exception {
        ChannelStack.getInstance().add(mChannel);
        pusherHelper = new PusherHelper(this, true);
        pusherHelper.connectToPusherClient(new ConnectionEventListener() {
            @Override
            public void onConnectionStateChange(final ConnectionStateChange connectionStateChange) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        //conenctionTextView.setText(connectionStateChange.getCurrentState().name());

                        if (connectionStateChange.getCurrentState() == ConnectionState.CONNECTED) {
                            conenctionTextView.setText(connectionStateChange.getCurrentState().name());

                            snackbar.setText("Successfully Connected!").show();

                            enableEventView();
                            getChannels();
                        } else if (connectionStateChange.getCurrentState() == ConnectionState.DISCONNECTED) {
                            snackbar.setAction("Retry", new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    connect();
                                }
                            }).setText("Disconnected").show();
                        }
                    }
                });
            }

            @Override
            public void onError(String s, String s1, final Exception e) {
                new LogTask("connect|ConnectionEventListener,error,s1:" + s1 + ",\ns:" + s + ",\ne:" + e.getMessage(), TAB, LogTask.LOG_I);

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        conenctionTextView.setText(e.getMessage());
                    }
                });
            }
        }, new SubscriptionEventListener() {
            @Override
            public void onEvent(String s, String s1, String s2) {
                new LogTask("SubscriptionEventListener|onEvent,s:" + s + "\ns1:" + s1 + ",\ns2:" + s2, TAB, LogTask.LOG_D);
            }
        });

    }

    private void writeValueToPref(String value, String key) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(key, value);
        editor.commit();

    }

    private void getChannels() {
        new LogTask("getChannels|channelStack:" + ChannelStack.getInstance(), TAB, LogTask.LOG_D);
        ArrayList<Channel> channelList = new ArrayList<>();
        for (Channel mChannel : ChannelStack.getInstance()) {
            new LogTask("getChannels|mChannel:" + mChannel.getChannelName(), TAB, LogTask.LOG_D);
            channelList.add(mChannel);
            for (Channel.Event mEvent : mChannel.getEvents()) {
                new LogTask("getChannels|mEvent:" + mEvent.getEventName(), TAB, LogTask.LOG_D);
            }
        }

        if (baseAdapter == null) {
            baseAdapter = new BaseChannelAdapter(this);
        } else {
            baseAdapter.clear();
        }

        baseAdapter.addChannels(channelList);

        baseAdapter.notifyDataSetChanged();

    }

    private void save() {
        AlertDialog.Builder alertDiaglogBuilder = new AlertDialog.Builder(this);
        alertDiaglogBuilder.setTitle("Save Confirm");
        alertDiaglogBuilder.setCancelable(true);
        alertDiaglogBuilder.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {


            }
        });


        alertDiaglogBuilder.setPositiveButton("Save", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                ChannelRecord.saveAllRecords();


            }
        });

        alertDiaglogBuilder.show();
    }

    @Override
    public void onChannelButtonClick(Channel channel) {
        showDialog(channel);
    }

    void showDialog(Channel channel) {

        // DialogFragment.show() will take care of adding the fragment
        // in a transaction.  We also want to remove any currently showing
        // dialog, so make our own transaction and take care of that here.
        DialogFragment newFragment = EventDialog.newInstance(
                channel);
        newFragment.show(getFragmentManager(), EventDialog.TAB);

    }

    private class EditTextChangeListener implements TextWatcher {
        private int viewId;

        public EditTextChangeListener(View v) {
            this.viewId = v.getId();
        }


        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {

        }

        @Override
        public void afterTextChanged(Editable s) {
            switch (viewId) {
                case R.id.menu_editText_AppID:
                    appId = appIdEditText.getText().toString();
                    break;
                case R.id.menu_editText_Channel:
                    channel = channelEditText.getText().toString();
                    break;
                case R.id.menu_editText_Event:
                    event = eventEditText.getText().toString();
                    break;
                case R.id.menu_editText_Cluster:
                    cluster = clusterEditText.getText().toString();
                    break;
            }

        }
    }

}
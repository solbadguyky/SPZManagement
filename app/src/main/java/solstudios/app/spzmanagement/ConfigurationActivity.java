package solstudios.app.spzmanagement;

import android.app.DialogFragment;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
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
import com.pusher.client.connection.ConnectionState;
import com.pusher.client.connection.ConnectionStateChange;

import java.util.ArrayList;
import java.util.Queue;

import solstudios.app.spzmanagement.pusher.PusherHelper;

/**
 * Created by solbadguyky on 6/30/16.
 */
public class ConfigurationActivity extends BaseActivity implements BaseChannelAdapter.BaseAdapterInterface,
        EventDialog.IEventDialogItem {
    public static final String TAB = "Configuration Activity";
    ///snackbar queue messages
    Queue<QueueMessage> queueMessages;
    private SharedPreferences sharedPreferences;
    ///large layout
    private CoordinatorLayout coordinatorLayout;
    private RecyclerView channelRecyclerView;
    private LinearLayoutManager layoutManager;
    ///small layout
    private Toolbar toolbar;
    private android.app.ActionBar actionBar;
    //private Snackbar snackbar;
    private Button submitButton, connectButton, checkChannelButton;
    private EditText appIdEditText, channelEditText, eventEditText, clusterEditText;
    private TextView conenctionTextView;
    private Bundle pusherInfoBundle;
    private String appId, cluster;
    private String old_appId, old_cluster;
    private BaseChannelAdapter baseAdapter;
    ///pusher instance
    private PusherHelper pusherHelper;
    private Pusher currentPusherInstance;
    ///pusher broadcaster
    private PusherBroadcast pusherBroadcastReceiver;
    private QueueMessage.SnackbarManager snackbarManager;

    @Override
    protected void onResume() {
        super.onResume();

        if (pusherBroadcastReceiver == null) {
            pusherBroadcastReceiver = new PusherBroadcast() {

                @Override
                public void onDefaultConnectionChanged(ConnectionStateChange connectionStateChange) {
                    new LogTask("onDefaultConnectionChanged|connectionStateChange:" + connectionStateChange.getPreviousState()
                            + " -> " + connectionStateChange.getCurrentState(),
                            TAB, LogTask.LOG_I);
                    onConnectionStateChange(connectionStateChange);
                }

                @Override
                public void onSubscriptionEvent(String channel, String event, String message) {
                    new LogTask("onSubscriptionEvent|channel:" + channel
                            + ",event:" + event + "\nmessage:" + message,
                            TAB, LogTask.LOG_I);

                }

                @Override
                public void onSubscriptionSucceeded(String channelName) {
                    new LogTask("onSubscriptionSucceeded|channelName:" + channelName,
                            TAB, LogTask.LOG_I);
                    subscriptionSucceeded(channelName);
                }

                @Override
                public void onBindedEvent(String channel, String event) {
                    new LogTask("onBindedEvent|channelName:" + channel + ",event:" + event,
                            TAB, LogTask.LOG_I);
                    bindSucceeded(channel, event);
                }

                @Override
                void onError(Exception e) {

                }

                @Override
                public void onReceive(Context context, Intent intent) {
                    super.onReceive(context, intent);
                }
            };

            IntentFilter filterPusherMessages = new IntentFilter();
            filterPusherMessages.addAction(PusherBroadcast.getActionIntent(PusherBroadcast.ACTION_SUBSCRIPTION_CHANNEL));
            filterPusherMessages.addAction(PusherBroadcast.getActionIntent(PusherBroadcast.ACTION_BIND_EVENT));
            filterPusherMessages.addAction(PusherBroadcast.getActionIntent(PusherBroadcast.ACTION_SUBSCRIPTION_EVENT));
            filterPusherMessages.addAction(PusherBroadcast.getActionIntent(PusherBroadcast.ACTION_CONNECTION_CHANGED));
            this.registerReceiver(pusherBroadcastReceiver, filterPusherMessages);
        }

    }

    @Override
    protected void onPause() {
        super.onPause();
        if (pusherBroadcastReceiver != null) {
            this.unregisterReceiver(pusherBroadcastReceiver);

        }
    }

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
            addMessage(new QueueMessage("Pusher has not been configured!"));

        } else if (old_appId.isEmpty() || old_cluster.isEmpty()) {
            addMessage(new QueueMessage("Pusher has not been configured!"));

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

    }

    private void onConnectionStateChange(ConnectionStateChange connectionStateChange) {
        conenctionTextView.setText(connectionStateChange.getCurrentState().name());

        if (connectionStateChange.getCurrentState() == ConnectionState.CONNECTING
                || connectionStateChange.getCurrentState() == ConnectionState.DISCONNECTING) {
            onChangingConnection();
        } else if (connectionStateChange.getCurrentState() == ConnectionState.CONNECTED) {
            connected();
        } else if (connectionStateChange.getCurrentState() == ConnectionState.DISCONNECTED) {
            disconnected();
        }
    }

    private void onChangingConnection() {
        //disable button during changing connection
        connectButton.setEnabled(false);
        submitButton.setEnabled(false);

        appIdEditText.setEnabled(false);
        clusterEditText.setEnabled(false);

    }

    private void connected() {
        connectButton.setEnabled(true);
        submitButton.setEnabled(true);

        connectButton.setText("Disconnect");
        appIdEditText.setEnabled(false);
        clusterEditText.setEnabled(false);

        connectButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pusherHelper.disconnect();
            }
        });
    }

    private void disconnected() {
        connectButton.setEnabled(true);
        submitButton.setEnabled(true);

        connectButton.setText("Connect");
        appIdEditText.setEnabled(true);
        clusterEditText.setEnabled(true);

        connectButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pusherHelper.resume();
            }
        });
    }

    private void checkValueAgain() {
        appId = appIdEditText.getText().toString();
        cluster = clusterEditText.getText().toString();
    }

    private void submitChannel() {
        new LogTask("submitBundle", TAB, LogTask.LOG_I);
        checkValueAgain();
        try {
            if (getChannelString() == null || getEventString() == null) {
                return;
            } else {
                Channel mchannel = new Channel(getChannelString());
                if (!getEventString().isEmpty()) {
                    Channel.Event mEvent = new Channel.Event(getEventString());
                    mchannel.addEvent(mEvent);
                    // subcribeChannel(mchannel, mEvent);
                    pusherHelper.subscribe(mchannel, mEvent, null);
                } else {
                    pusherHelper.subscribe(mchannel);
                }

            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void subscriptionSucceeded(String channelName) {
        QueueMessage queueMessage = new QueueMessage();
        queueMessage.setMessage(channelName + " has been successfully subscribed");
        final Snackbar snackbar = QueueMessage.createSnackBar(coordinatorLayout, queueMessage);
        snackbar.setAction("Dismiss", new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (snackbar != null) {
                    snackbar.dismiss();
                }
            }
        });
        addMessage(snackbar);
    }

    private void bindSucceeded(String channelName, String event) {
        QueueMessage queueMessage = new QueueMessage();
        queueMessage.setMessage(channelName + "/" + event + " has been successfully binded");
        final Snackbar snackbar = QueueMessage.createSnackBar(coordinatorLayout, queueMessage);
        snackbar.setAction("Dismiss", new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (snackbar != null) {
                    snackbar.dismiss();
                }
            }
        });
        addMessage(snackbar);
    }

    private void insertChannel() {

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
                addMessage(new QueueMessage("Please Enter Values Before Connecting!"));

            }
        }
    }

    private void onConnecting() {
        new LogTask("onConnecting", TAB, LogTask.LOG_I);
        pusherHelper.resume();
        /*pusherHelper.checkConnection(new ConnectionEventListener() {
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
        }); */
    }

    private void subcribeAllChannels(final Channel mChannel, Channel.Event mEvent) throws Exception {
        ChannelStack.getInstance().add(mChannel);
        pusherHelper.connectToPusherClient(null);
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

    private String getChannelString() {
        String channelString = channelEditText.getText().toString();

        if (channelString != null && !channelString.isEmpty()) {
            return channelString;
        } else {
            channelEditText.setError("This Field is required!");
            return null;
        }

    }

    private String getEventString() {
        String eventString = eventEditText.getText().toString();
        if (!eventString.isEmpty() && eventString != null) {
            return eventString;
        } else {
            eventEditText.setError("This Field is required!");
            return null;
        }

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

    @Override
    public void onLongPressItem(Channel channel, Channel.Event event) {
        if (ChannelStack.getInstance().removeEvent(channel, event)) {
            if (getFragmentManager().findFragmentByTag(EventDialog.TAB) != null) {
                ((EventDialog) getFragmentManager().findFragmentByTag(EventDialog.TAB)).dismiss();
            }
            getChannels();
        }
    }

    @Override
    public void onPressItem(Channel channel, Channel.Event event) {

    }

    @Override
    public void onDeleteChannel(Channel channel) {
        new LogTask("onDeleteChannel|channel:" + channel, TAB, LogTask.LOG_D);
        ChannelStack.getInstance().remove(channel);
        pusherHelper.unsubscribe(channel);
        getChannels();
    }

    //@Override
    public void onDefaultConnectionChanged(ConnectionStateChange connectionStateChange) {

    }

    // @Override
    public void onSubcriptionEvent(String channel, String event, String message) {

    }

    private void addMessage(QueueMessage queueMessage) {
        new LogTask("addMessage|queue message,message:" + queueMessage.getMessage(), TAB, LogTask.LOG_I);
        if (snackbarManager == null) {
            snackbarManager = new QueueMessage.SnackbarManager();
        }
        snackbarManager.addToQueue(QueueMessage.createSnackBar(coordinatorLayout, queueMessage));
        //snackbarManager.show();
    }

    private void addMessage(Snackbar snackbar) {
        if (snackbarManager == null) {
            snackbarManager = new QueueMessage.SnackbarManager();
        }
        snackbarManager.addToQueue(snackbar);
        //snackbarManager.show();
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

                    break;
                case R.id.menu_editText_Event:

                    break;
                case R.id.menu_editText_Cluster:
                    cluster = clusterEditText.getText().toString();
                    break;
            }

        }
    }
}
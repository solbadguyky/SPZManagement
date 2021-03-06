package solstudios.app.spzmanagement;

import android.app.DialogFragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
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
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener;
import com.pusher.client.Pusher;
import com.pusher.client.connection.ConnectionState;
import com.pusher.client.connection.ConnectionStateChange;

import java.util.Queue;

import solstudios.app.spzmanagement.pusher.InputFragment;
import solstudios.app.spzmanagement.pusher.PusherClientInstance;
import solstudios.app.spzmanagement.pusher.PusherHelper;

/**
 * Created by solbadguyky on 6/30/16.
 */
public class ConfigurationActivity extends BaseActivity implements BaseChannelAdapter.BaseAdapterInterface,
        EventDialog.IEventDialogItem, ChannelStack.IChannelStack, InputFragment.IFragmentInput, OnConnectionFailedListener {
    public static final String TAB = "Configuration Activity";
    public static final String DEFAULT_CHANNEL = "Default Channel";
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
    private EditText channelEditText;
    private EditText appIdEditText, clusterEditText;
    private TextView conenctionTextView;
    private Bundle pusherInfoBundle;
    private String old_appId, old_cluster;
    private BaseChannelAdapter baseAdapter;
    ///pusher instance
    private PusherHelper pusherHelper;
    ///pusher broadcaster
    private PusherBroadcast pusherBroadcastReceiver;
    private QueueMessage.SnackbarManager snackbarManager;
    private GoogleSignIn googleSignIn;
    private String defaultChannelKey;

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
                public void onUnBindedEvent(String channel, String event) {
                    new LogTask("onUnBindedEvent|channel:" + channel + ",event:" + event,
                            TAB, LogTask.LOG_I);
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
            filterPusherMessages.addAction(PusherBroadcast.getActionIntent(PusherBroadcast.ACTION_UNBIND_EVENT));
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
        pusherHelper = new PusherHelper(this);

        ///retrieve old prefs
        old_appId = sharedPreferences.getString(PusherHelper.CLIENT_APPID, null);
        old_cluster = sharedPreferences.getString(PusherHelper.CLIENT_CLUSTER, null);

        if (getIntent().getStringExtra(DEFAULT_CHANNEL) != null) {
            defaultChannelKey = getIntent().getStringExtra(DEFAULT_CHANNEL);
        }

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
        clusterEditText = (EditText) findViewById(R.id.menu_editText_Cluster);
        conenctionTextView = (TextView) findViewById(R.id.menu_textView_Connect);

    }

    @Override
    public void setupValue() {
        this.setSupportActionBar(toolbar);

        actionBar = getActionBar();
        final Drawable upArrow = ContextCompat.getDrawable(this,
                R.drawable.common_google_signin_btn_icon_dark_disabled);
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

        ChannelStack.getInstance().setItemChangeListener(this);
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
        clusterEditText.addTextChangedListener(new EditTextChangeListener(clusterEditText));

        connectButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                connect(getAppId(), getCluster());
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
            Pusher pusher = PusherClientInstance.getInstance(this).getCurrentPusherInstance();
            if (pusher.getConnection()
                    .getState() == ConnectionState.DISCONNECTED
                    || pusher.getConnection()
                    .getState() == ConnectionState.DISCONNECTING) {
                connect();
            } else {
                onConnectionStateChange(new ConnectionStateChange(ConnectionState.CONNECTING,
                        ConnectionState.CONNECTED));
            }
        }


    }

    private String getAppId() {
        String appid = appIdEditText.getText().toString();
        if (appid.isEmpty()) {
            appIdEditText.setError("This field is required");
            return null;
        } else {
            return appid;
        }
    }

    private String getCluster() {
        String cluster = clusterEditText.getText().toString();
        if (cluster.isEmpty()) {
            appIdEditText.setError("This field is required");
            return null;
        } else {
            return cluster;
        }
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
        save();

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
                connect(getAppId(), getCluster());
            }
        });
    }

    private void subcribeChannel(Channel channel) {
        new LogTask("submitBundle", TAB, LogTask.LOG_I);
        // subcribeChannel(mchannel, mEvent);
        if (pusherHelper.isSubcribed(channel)) {
            QueueMessage queueMessage = new QueueMessage();
            queueMessage.setMessage("Channel has aldready subbed. lol");
            addMessage(queueMessage);
        } else {
            pusherHelper.subscribe(channel, null);
        }
    }

    private void submitChannel() {
        new LogTask("submitBundle", TAB, LogTask.LOG_I);
        try {
            if (getChannelString() == null) {
                return;
            } else {
                Channel mchannel = new Channel(getChannelString());
                subcribeChannel(mchannel);
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

        ///add channel to stack
        ChannelStack.getInstance().addChannel(channelName);
    }

    private void bindSucceeded(String channelName, String event) {
        QueueMessage queueMessage = new QueueMessage();
        if (pusherHelper.isSubcribed(channelName)) {
            queueMessage.setMessage(channelName + "/" + event + " has been successfully binded");
        } else {
            queueMessage.setMessage("Please subscribe channel first");
        }

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
        pusherHelper.connect();
    }

    private void connect(String appid, String cluster) {
        pusherHelper.connect(appid, cluster);
    }

    private void writeValueToPref(String value, String key) {
        new LogTask("writeValueToPref|value:" + value + ",key:" + key, TAB, LogTask.LOG_I);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(key, value);
        editor.apply();
    }

    private void getChannels() {
        //new LogTask("getChannels|channelStack:" + ChannelStack.getInstance(), TAB, LogTask.LOG_D);

        if (baseAdapter == null) {
            baseAdapter = new BaseChannelAdapter(this);
        } else {
            baseAdapter.clear();
        }

        baseAdapter.addChannels(ChannelStack.getInstance().getChannelStack());
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

                ///saved pusher appid/cluster
                if (getAppId() != null && getCluster() != null) {
                    writeValueToPref(getAppId(), PusherHelper.CLIENT_APPID);
                    writeValueToPref(getCluster(), PusherHelper.CLIENT_CLUSTER);
                }
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

    @Override
    public void onChannelButtonClick(Channel channel) {
        showDialog(channel);
    }

    @Override
    public void onChannelButtonLongClick(Channel channel) {
        subcribeChannel(channel);
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
        if (pusherHelper.isConnected()) {
            pusherHelper.bind(channel, event, null);
        } else {
            Toast.makeText(this, "No connection", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onDeleteChannel(Channel channel) {
        new LogTask("onDeleteChannel|channel:" + channel, TAB, LogTask.LOG_D);
        ChannelStack.getInstance().removeChannel(channel);
        pusherHelper.unsubscribe(channel);
        getChannels();
    }

    @Override
    public void onAddMoreEvents(Channel channel) {
        InputFragment inputFragment;
        if (getFragmentManager().findFragmentByTag(InputFragment.TAB) != null) {
            inputFragment = (InputFragment) getFragmentManager().findFragmentByTag(InputFragment.TAB);
            inputFragment.setNewChannel(channel);
        } else {
            inputFragment = InputFragment.newEvent(channel);
            inputFragment.setInputChangeListener(this);
            FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
            fragmentTransaction.add(inputFragment, InputFragment.TAB);
            fragmentTransaction.addToBackStack(InputFragment.TAB);
            fragmentTransaction.commit();
        }

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

    @Override
    public void onItemChanged() {
        new LogTask("onItemChanged", TAB, LogTask.LOG_I);
        getChannels();
    }

    @Override
    public void onSaveEvent(Channel channel, Channel.Event event) {

    }

    @Override
    public void onSaveEvent(Channel channel, String event) {
        new LogTask("onSaveEvent|channle:" + channel.getChannelName() + ",event:" + event, TAB, LogTask.LOG_D);
        ChannelStack.getInstance().addEvent(channel, event);
    }

    @Override
    public void onDismiss(String name) {
        switch (name) {
            case EventDialog.TAB:
                if (getFragmentManager().findFragmentByTag(EventDialog.TAB) != null) {
                    getFragmentManager().popBackStack(EventDialog.TAB, FragmentManager.POP_BACK_STACK_INCLUSIVE);
                }
                break;
            case InputFragment.TAB:
                if (getFragmentManager().findFragmentByTag(InputFragment.TAB) != null) {
                    getFragmentManager().popBackStack(InputFragment.TAB, FragmentManager.POP_BACK_STACK_INCLUSIVE);
                }
                break;
        }

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

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


        }
    }
}
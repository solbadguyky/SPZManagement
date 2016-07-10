package solstudios.app.spzmanagement;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.app.LoaderManager.LoaderCallbacks;
import android.content.Context;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.Loader;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.ContactsContract;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.OptionalPendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.pusher.client.connection.ConnectionState;
import com.pusher.client.connection.ConnectionStateChange;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.concurrent.TimeUnit;

import solstudios.app.spzmanagement.pusher.PusherClientInstance;
import solstudios.app.spzmanagement.pusher.PusherHelper;

import static android.Manifest.permission.READ_CONTACTS;
import static solstudios.app.spzmanagement.LoginActivity.LoginState.LOGIN_GOOGLE;

/**
 * A login screen that offers login via email/password.
 */
public class LoginActivity extends BaseActivity implements LoaderCallbacks<Cursor>,
        GoogleApiClient.OnConnectionFailedListener, OnClickListener, GoogleApiClient.ConnectionCallbacks {

    public static final String TAB = "LoginActivity";
    public static final String USER_MAIL = "UserMail";
    public static final String USER_PASS = "UserPass";
    public static final String USER_LOGIN_HASH_VALUE = "User_Login_Hash";
    public static final String USER_LOGIN_TIME = "User_Login_Time";
    public static final String TEST_CHANNEL = "iproject-activities";
    public static final String TEST_EVENT = "default-event";
    public static final String TEST_APPKEY = "8c0e6575f270765b2f79";
    public static final String TEST_APPCLUSTER = "ap1";
    /**
     * Id to identity READ_CONTACTS permission request.
     */
    private static final int REQUEST_READ_CONTACTS = 0;
    private static final int REQUEST_RUN_IN_BACKGROUND = 1;
    /**
     * A dummy authentication store containing known user names and passwords.
     * TODO: remove after connecting to a real authentication system.
     */
    private static final String[] DUMMY_CREDENTIALS = new String[]{
            "solstudios@gmail.com:sol1234", "test@account:123456"
    };
    private static final int RC_SIGN_IN = 9001;
    /**
     * Keep track of the login task to ensure we can cancel it if requested.
     */
    private UserLoginTask mAuthTask = null;
    // UI references.
    private AutoCompleteTextView mEmailView;
    private EditText mPasswordView;
    private View mProgressView;
    private View mLoginFormView;
    private Button mEmailSignInButton;
    private TextView logTextView;
    ///SPZ_Management
    private LoginState mLoginState;
    private boolean canConfig = false;
    private SharedPreferences sharedPreferences;
    private PusherBroadcast pusherBroadcastReceiver;
    private PusherHelper pusherHelper;
    private GoogleApiClient mGoogleApiClient;
    private SignInButton googleSignInButton;
    private Button mSignOutButton;
    private String myHashUserId;
    ///saved instance
    private String oldUserId;
    private String oldUserPass;
    private String savedHash;
    private long timecache = -1;
    private boolean canAutomaticallySignin = false;
    private boolean canUseGoogleSignIn = true;
    private boolean firstSignin = true;
    private boolean signOutByApp = false;
    private AlertDialog messageAlertDiaglog;

    @Override
    public void onConnected(@Nullable Bundle bundle) {

    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void initValue() {
        pusherHelper = new PusherHelper(this);
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
    }

    @Override
    public void initView() {
        mEmailView = (AutoCompleteTextView) findViewById(R.id.email);
        mPasswordView = (EditText) findViewById(R.id.password);
        mEmailSignInButton = (Button) findViewById(R.id.email_sign_in_button);

        mLoginFormView = findViewById(R.id.login_form);
        mProgressView = findViewById(R.id.login_progress);

        logTextView = (TextView) findViewById(R.id.textView_LoginStatus);

        mSignOutButton = (Button) findViewById(R.id.sign_out_button);
    }

    @Override
    public void setupValue() {

    }

    @Override
    public void setupView() {
        mPasswordView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int id, KeyEvent keyEvent) {
                if (id == R.id.login || id == EditorInfo.IME_NULL) {
                    attemptLogin();
                    return true;
                }
                return false;
            }
        });

        mEmailSignInButton.setOnClickListener(this);
        mSignOutButton.setOnClickListener(this);

        // Set up the login form.
        populateAutoComplete();


        if (canUseSavedInstance()) {
            Toast.makeText(this, "Can you old instance", Toast.LENGTH_SHORT).show();
            mEmailView.setText(oldUserId);
            mPasswordView.setText(oldUserPass);

            canAutomaticallySignin = true;
        } else {
            Toast.makeText(this, "Time out. Please Sign in again!", Toast.LENGTH_SHORT).show();
            clearOutDateInstance();
        }


        if (canAutomaticallySignin) {
            logIn(oldUserId, oldUserPass, savedHash);
        }
    }

    private boolean canUseSavedInstance() {
        oldUserId = sharedPreferences.getString(USER_MAIL, null);
        oldUserPass = sharedPreferences.getString(USER_PASS, null);
        savedHash = sharedPreferences.getString(USER_LOGIN_HASH_VALUE, null);
        timecache = sharedPreferences.getLong(USER_LOGIN_TIME, -1);

        long totalTime = Math.abs(timecache - Calendar.getInstance().getTimeInMillis());
        long hours = TimeUnit.MILLISECONDS.toHours(totalTime);
        new LogTask("hours:" + hours, TAB, LogTask.LOG_D);
        new LogTask("oldUserId:" + oldUserId, TAB, LogTask.LOG_D);
        new LogTask("oldUserPass:" + oldUserPass, TAB, LogTask.LOG_D);
        new LogTask("savedHash:" + savedHash, TAB, LogTask.LOG_D);

        if (hours > 24) {
            return false;
        } else {
            if (oldUserId != null && oldUserPass != null && savedHash != null) {
                return true;
            } else {
                return false;
            }
        }
    }

    private void clearOutDateInstance() {
        deletePrefKey(USER_MAIL, USER_PASS, USER_LOGIN_HASH_VALUE, USER_LOGIN_TIME);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        googleSignInButton = (SignInButton) findViewById(R.id.sign_in_button);
        googleSignInButton.setOnClickListener(this);

        ///check if my device has google play service
        AlertDialog alertDialog = new AlertDialog.Builder(this).setMessage(
                "You dont have Google Play Service, so I can only let you login by App Account")
                .create();
        try {
            GoogleApiAvailability api = GoogleApiAvailability.getInstance();
            int code = api.isGooglePlayServicesAvailable(this);
            if (code == ConnectionResult.SUCCESS) {
                // Do Your Stuff Here
                configGoogleApiClientIfNeeeeeeeed();
            } else {
                canUseGoogleSignIn = false;
                disableGoogleSignIn();
                alertDialog.show();
            }
        } catch (Exception e) {
            disableGoogleSignIn();
            alertDialog.show();
        }

    }

    private void configGoogleApiClientIfNeeeeeeeed() {
        // [START configure_signin]
        // Configure sign-in to request the user's ID, email address, and basic
        // profile. ID and basic profile are included in DEFAULT_SIGN_IN.
        String serverClientId = getString(R.string.server_client_id);
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .requestServerAuthCode(serverClientId, false)
                //.requestIdToken("AIzaSyDIo7H0lyNeH5_rpF2NhXu8CbWb4ch4Ztg")
                .build();
        // [END configure_signin]

        // [START build_client]
        // Build a GoogleApiClient with access to the Google Sign-In API and the
        // options specified by gso.

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this /* FragmentActivity */, this /* OnConnectionFailedListener */)
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .build();
    }

    private void disableGoogleSignIn() {
        googleSignInButton.setVisibility(View.GONE);
        googleSignInButton.setOnClickListener(null);
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (canUseGoogleSignIn) {
            OptionalPendingResult<GoogleSignInResult> opr = Auth.GoogleSignInApi.silentSignIn(mGoogleApiClient);
            if (opr.isDone()) {
                // If the user's cached credentials are valid, the OptionalPendingResult will be "done"
                // and the GoogleSignInResult will be available instantly.
                new LogTask("Got cached sign-in", TAB, LogTask.LOG_D);
                GoogleSignInResult result = opr.get();
                handleSignInResult(result);
            } else {
                // If the user has not previously signed in on this device or the sign-in has expired,
                // this asynchronous branch will attempt to sign in the user silently.  Cross-device
                // single sign-on will occur in this branch.
                //showProgressDialog();
                opr.setResultCallback(new ResultCallback<GoogleSignInResult>() {
                    @Override
                    public void onResult(GoogleSignInResult googleSignInResult) {
                        //hideProgressDialog();
                        handleSignInResult(googleSignInResult);
                    }
                });
            }
        }

    }

    private void populateAutoComplete() {
        if (!mayRequestContacts()) {
            return;
        }

        getLoaderManager().initLoader(0, null, this);
    }

    private boolean mayRequestContacts() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            return true;
        }
        if (checkSelfPermission(READ_CONTACTS) == PackageManager.PERMISSION_GRANTED) {
            return true;
        }
        if (shouldShowRequestPermissionRationale(READ_CONTACTS)) {
            Snackbar.make(mEmailView, R.string.permission_rationale, Snackbar.LENGTH_INDEFINITE)
                    .setAction(android.R.string.ok, new View.OnClickListener() {
                        @Override
                        @TargetApi(Build.VERSION_CODES.M)
                        public void onClick(View v) {
                            requestPermissions(new String[]{READ_CONTACTS}, REQUEST_READ_CONTACTS);
                        }
                    });
        } else {
            requestPermissions(new String[]{READ_CONTACTS}, REQUEST_READ_CONTACTS);
        }
        return false;
    }

    /**
     * Callback received when a permissions request has been completed.
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        if (requestCode == REQUEST_READ_CONTACTS) {
            if (grantResults.length == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                populateAutoComplete();
            }
        }
    }

    /**
     * Attempts to sign in or register the account specified by the login form.
     * If there are form errors (invalid email, missing fields, etc.), the
     * errors are presented and no actual login attempt is made.
     */
    private void attemptLogin() {
        if (mAuthTask != null) {
            return;
        }

        // Reset errors.
        mEmailView.setError(null);
        mPasswordView.setError(null);

        // Store values at the time of the login attempt.
        String email = mEmailView.getText().toString();
        String password = mPasswordView.getText().toString();

        boolean cancel = false;
        View focusView = null;

        // Check for a valid password, if the user entered one.
        if (!TextUtils.isEmpty(password) && !isPasswordValid(password)) {
            mPasswordView.setError(getString(R.string.error_invalid_password));
            focusView = mPasswordView;
            cancel = true;
        }

        // Check for a valid email address.
        if (TextUtils.isEmpty(email)) {
            mEmailView.setError(getString(R.string.error_field_required));
            focusView = mEmailView;
            cancel = true;
        } else if (!isEmailValid(email)) {
            mEmailView.setError(getString(R.string.error_invalid_email));
            focusView = mEmailView;
            cancel = true;
        }

        //check for a valid password
        if (TextUtils.isEmpty(password)) {
            mPasswordView.setError(getString(R.string.error_field_required));
            focusView = mPasswordView;
            cancel = true;
        } else if (!isPasswordValid(password)) {
            mPasswordView.setError(getString(R.string.error_incorrect_password));
            focusView = mPasswordView;
            cancel = true;
        }

        if (cancel) {
            // There was an error; don't attempt login and focus the first
            // form field with an error.
            focusView.requestFocus();
        } else {
            // Show a progress spinner, and kick off a background task to
            // perform the user login attempt.
            showProgress(true);
            logIn(email, password, null);
        }
    }

    private void logIn(String email, String password, @Nullable String hashkey) {
        mAuthTask = new UserLoginTask(email, password, hashkey);
        mAuthTask.execute((Void) null);
    }

    private boolean isEmailValid(String email) {
        //TODO: Replace this with your own logic
        return email.contains("@");
    }

    private boolean isPasswordValid(String password) {
        //TODO: Replace this with your own logic
        return password.length() > 4;
    }

    /**
     * Shows the progress UI and hides the login form.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    private void showProgress(final boolean show) {
        // On Honeycomb MR2 we have the ViewPropertyAnimator APIs, which allow
        // for very easy animations. If available, use these APIs to fade-in
        // the progress spinner.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
            int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);

            mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
            mLoginFormView.animate().setDuration(shortAnimTime).alpha(
                    show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
                }
            });

            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            mProgressView.animate().setDuration(shortAnimTime).alpha(
                    show ? 1 : 0).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
                }
            });
        } else {
            // The ViewPropertyAnimator APIs are not available, so simply show
            // and hide the relevant UI components.
            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
        }
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        return new CursorLoader(this,
                // Retrieve data rows for the device user's 'profile' contact.
                Uri.withAppendedPath(ContactsContract.Profile.CONTENT_URI,
                        ContactsContract.Contacts.Data.CONTENT_DIRECTORY), ProfileQuery.PROJECTION,

                // Select only email addresses.
                ContactsContract.Contacts.Data.MIMETYPE +
                        " = ?", new String[]{ContactsContract.CommonDataKinds.Email
                .CONTENT_ITEM_TYPE},

                // Show primary email addresses first. Note that there won't be
                // a primary email address if the user hasn't specified one.
                ContactsContract.Contacts.Data.IS_PRIMARY + " DESC");
    }

    @Override
    public void onLoadFinished(Loader<Cursor> cursorLoader, Cursor cursor) {
        List<String> emails = new ArrayList<>();
        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            emails.add(cursor.getString(ProfileQuery.ADDRESS));
            cursor.moveToNext();
        }

        addEmailsToAutoComplete(emails);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> cursorLoader) {

    }

    private void addEmailsToAutoComplete(List<String> emailAddressCollection) {
        //Create adapter to tell the AutoCompleteTextView what to show in its dropdown list.
        ArrayAdapter<String> adapter =
                new ArrayAdapter<>(LoginActivity.this,
                        android.R.layout.simple_dropdown_item_1line, emailAddressCollection);

        mEmailView.setAdapter(adapter);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        this.getMenuInflater().inflate(R.menu.menu, menu);
        return canConfig;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_pusher:
                Intent configurationActivity = new Intent(this, ConfigurationActivity.class);
                startActivity(configurationActivity);

                return true;

            case R.id.menu_log:
                Intent logActivity = new Intent(this, LogActivity.class);
                startActivity(logActivity);
                return true;

            case R.id.menu_run_in_background:
                finish();
                return true;

        }
        return super.onOptionsItemSelected(item);
    }

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
                    LoginActivity.this.onSubscriptionEvent(channel, event, message);
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
                    //bindSucceeded(channel, event);
                }

                @Override
                public void onUnBindedEvent(String channel, String event) {

                }

                @Override
                void onError(Exception e) {

                }

                @Override
                public void onReceive(Context context, Intent intent) {
                    super.onReceive(context, intent);
                }
            };
        }
        IntentFilter filterPusherMessages = new IntentFilter();
        filterPusherMessages.addAction(PusherBroadcast.getActionIntent(PusherBroadcast.ACTION_SUBSCRIPTION_CHANNEL));
        filterPusherMessages.addAction(PusherBroadcast.getActionIntent(PusherBroadcast.ACTION_BIND_EVENT));
        filterPusherMessages.addAction(PusherBroadcast.getActionIntent(PusherBroadcast.ACTION_SUBSCRIPTION_EVENT));
        filterPusherMessages.addAction(PusherBroadcast.getActionIntent(PusherBroadcast.ACTION_CONNECTION_CHANGED));
        this.registerReceiver(pusherBroadcastReceiver, filterPusherMessages);
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (pusherBroadcastReceiver != null) {
            this.unregisterReceiver(pusherBroadcastReceiver);
        }
    }

    @Override
    public void finish() {
        super.finish();
    }

    private void connect(String myHashUserId) {
        if (myHashUserId != null) {
            if (!myHashUserId.isEmpty()) {
                try {
                    pusherHelper.resume();
                } catch (Throwable throwable) {
                    throwable.printStackTrace();

                    if (throwable.getMessage().equals(PusherClientInstance.THROWABLE_CONNECTED)) {
                        Toast.makeText(this, "Connected", Toast.LENGTH_SHORT).show();

                        onConnectionStateChange(new ConnectionStateChange(ConnectionState.CONNECTING,
                                ConnectionState.CONNECTED));
                    }
                }
            }
        }
    }

    private void disconnect() {
        pusherHelper.disconnect();
    }

    private void onConnectionStateChange(ConnectionStateChange connectionStateChange) {
        switch (connectionStateChange.getCurrentState()) {
            case CONNECTED:
                String appid = PusherClientInstance.getInstance(this).curAppid;
                //String c = PusherClientInstance.getInstance(this).curAppid;
                logTextView.append("\nPusher Client: Connected\n");
                logTextView.append("\nPusher Client app_key: " + appid);

                subscribeDefaultChannel(myHashUserId);

                updateUI(true);
                break;

            case DISCONNECTED:

                canConfig = true;
                invalidateOptionsMenu();

                logTextView.append("\nPusher Client: Disconnected.\nPlease Check Pusher's configuration!!!");

                ///request default config
                if (firstSignin) {
                    if (!signOutByApp) {
                        createAutoConfigDialog();
                    }
                }

                signOutByApp = false;

                ///update ui
                updateUI(false);
                break;
            case CONNECTING:
                logTextView.append("\nConnecting...");
                break;
        }
    }

    private void writeStringToPref(String key, String value) {
        new LogTask("writeStringToPref|value:" + value + ",key:" + key, TAB, LogTask.LOG_I);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(key, value);
        editor.apply();
    }

    private void writeValueToPref(String key, long value) {
        new LogTask("writeValueToPref|value:" + value + ",key:" + key, TAB, LogTask.LOG_I);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putLong(key, value);
        editor.apply();
    }

    private void deletePrefKey(String... keys) {
        new LogTask("writeStringToPref|key:" + keys, TAB, LogTask.LOG_I);
        SharedPreferences.Editor editor = sharedPreferences.edit();

        for (String key : keys) {
            editor.remove(key);
        }
        editor.apply();
    }

    private void subscribeDefaultChannel(String defaultChannelKey) {
        try {
            pusherHelper.subscribe(defaultChannelKey, "default-event");
        } catch (Throwable throwable) {
            throwable.printStackTrace();
            if (throwable.getMessage().equals(PusherClientInstance.THROWABLE_CHANNEL_SUBSCRIBED)) {
                subscriptionSucceeded(defaultChannelKey);
            }
        }

        try {
            pusherHelper.subscribe(TEST_CHANNEL, TEST_EVENT);
        } catch (Throwable throwable) {
            throwable.printStackTrace();
            if (throwable.getMessage().equals(PusherClientInstance.THROWABLE_CHANNEL_SUBSCRIBED)) {
                subscriptionSucceeded(TEST_CHANNEL);
            }
        }

    }

    private void subscriptionSucceeded(String channelName) {
        if (channelName.equals(myHashUserId)) {
            canConfig = true;
            invalidateOptionsMenu();
        }

        ///print log channel name
        logTextView.append("\nPusher Client, subscriptionSucceeded channel: " + channelName);
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.sign_in_button:
                signIn();
                break;
            case R.id.email_sign_in_button:
                attemptLogin();
                break;
            case R.id.sign_out_button:
                signOut();
                break;
        }
    }

    private void signIn() {
        Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(mGoogleApiClient);
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }


    // [START onActivityResult]
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
            handleSignInResult(result);
        }
    }
    // [END onActivityResult]

    // [START handleSignInResult]
    private void handleSignInResult(GoogleSignInResult result) {
        Log.d(TAB, "handleSignInResult:" + result.isSuccess());
        if (result.isSuccess()) {
            // Signed in successfully, show authenticated UI.
            GoogleSignInAccount acct = result.getSignInAccount();
            logTextView.append("\nLogin by Google: " + acct.getDisplayName());
            logTextView.append("\nToken: " + acct.getServerAuthCode());

            mLoginState = LOGIN_GOOGLE;
            updateUI(true);
        } else {
            // Signed out, show unauthenticated UI.
            updateUI(false);
        }
    }

    // [START signOut]
    private void signOut() {
        new LogTask("signOut,mLoginState=" + mLoginState, TAB, LogTask.LOG_D);
        signOutByApp = true;
        switch (mLoginState) {
            case LOGIN_GOOGLE:
                if (canUseGoogleSignIn) {
                    Toast.makeText(this, "Log out using google api", Toast.LENGTH_SHORT).show();
                    Auth.GoogleSignInApi.signOut(mGoogleApiClient).setResultCallback(
                            new ResultCallback<Status>() {
                                @Override
                                public void onResult(Status status) {
                                    // [START_EXCLUDE]
                                    updateUI(false);
                                    // [END_EXCLUDE]
                                }
                            });
                }
                break;
            case LOGIN_NATIVE:
                Toast.makeText(this, "Log out using app api", Toast.LENGTH_SHORT).show();
                pusherHelper.disconnect();
                break;
            case LOGOUT:
                pusherHelper.disconnect();
                break;
        }
        clearOutDateInstance();
    }
    // [END signOut]

    // [START revokeAccess]
    private void revokeAccess() {
        Auth.GoogleSignInApi.revokeAccess(mGoogleApiClient).setResultCallback(
                new ResultCallback<Status>() {
                    @Override
                    public void onResult(Status status) {
                        // [START_EXCLUDE]
                        updateUI(false);
                        // [END_EXCLUDE]
                    }
                });
    }

    // [END revokeAccess]

    private void updateUI(boolean signedIn) {
        if (signedIn) {
            mEmailSignInButton.setVisibility(View.GONE);
            mSignOutButton.setVisibility(View.VISIBLE);

            ///disable fields
            mEmailView.setEnabled(false);
            mPasswordView.setEnabled(false);

        } else {
            mEmailSignInButton.setVisibility(View.VISIBLE);
            googleSignInButton.setVisibility(View.VISIBLE);

            mSignOutButton.setVisibility(View.GONE);

            ///enable fields
            mEmailView.setEnabled(true);
            mPasswordView.setEnabled(true);

            mLoginState = LoginState.LOGOUT;
            logTextView.append("\nLogged out!");
        }

        if (!canUseGoogleSignIn) {
            googleSignInButton.setVisibility(View.GONE);
        }
    }

    private void createAutoConfigDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Use default Pusher's configuration");
        builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                pusherHelper.connect(TEST_APPKEY, TEST_APPCLUSTER);

            }
        });

        builder.setNegativeButton("No, let's me config by myself :D", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                firstSignin = false;

            }
        });
        builder.create().show();
    }

    private void onSubscriptionEvent(String channel, String event, String msg) {
        if (messageAlertDiaglog == null) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("New Message");
            final String message = "Channel: " + channel
                    + "\nEvent: " + event + "\nMessage: " + msg;
            builder.setMessage(message);
            builder.setNeutralButton("Close & Save", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    LogActivity.WriteLog(LoginActivity.this, message);
                }
            });

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
                builder.setOnDismissListener(new DialogInterface.OnDismissListener() {
                    @Override
                    public void onDismiss(DialogInterface dialogInterface) {
                        LogActivity.WriteLog(LoginActivity.this, message);
                    }
                });
            }

            messageAlertDiaglog = builder.create();
            messageAlertDiaglog.show();

        } else {
            messageAlertDiaglog.dismiss();
            messageAlertDiaglog = null;
            onSubscriptionEvent(channel, event, msg);
        }
    }

    public enum LoginState {
        LOGIN_GOOGLE, LOGIN_NATIVE, LOGOUT
    }

    private interface ProfileQuery {
        String[] PROJECTION = {
                ContactsContract.CommonDataKinds.Email.ADDRESS,
                ContactsContract.CommonDataKinds.Email.IS_PRIMARY,
        };

        int ADDRESS = 0;
        int IS_PRIMARY = 1;
    }

    /**
     * Represents an asynchronous login/registration task used to authenticate
     * the user.
     */
    public class UserLoginTask extends AsyncTask<Void, Void, Boolean> {

        private String mEmail;
        private String mPassword;
        private String hashKey;

        UserLoginTask(String email, String password, @Nullable String hashkey) {
            mEmail = email;
            mPassword = password;

            if (hashkey != null) {
                this.hashKey = hashkey;
            }
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            // TODO: attempt authentication against a network service.
            if (hashKey != null) {
                checkLegibleHash(hashKey);
                try {
                    // Simulate network access.
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    return false;
                }
            }

            if (checkAccount()) {
                try {
                    ///create hashkey
                    myHashUserId = getHashKey(mEmail, mPassword);
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    return false;
                }
                return true;
            }


            // TODO: register the new account here.
            return false;
        }

        @Override
        protected void onPostExecute(final Boolean success) {
            mAuthTask = null;
            showProgress(false);

            if (success) {
                Toast.makeText(LoginActivity.this, "Retrieved Hashid: " + myHashUserId, Toast.LENGTH_SHORT).show();

                logTextView.append("\nChecked successfully");
                logTextView.append("\nMy Channel Name: " + myHashUserId);

                ///save login info
                writeStringToPref(USER_MAIL, mEmail);
                writeStringToPref(USER_PASS, mPassword);
                writeStringToPref(USER_LOGIN_HASH_VALUE, myHashUserId);

                ///renewable hashkey timeline
                writeValueToPref(USER_LOGIN_TIME, Calendar.getInstance().getTimeInMillis());

                ///resume
                connect(myHashUserId);

                ///
                mLoginState = LoginState.LOGIN_NATIVE;

            } else {
                canConfig = false;
                mPasswordView.setError(getString(R.string.error_incorrect_password));
                mPasswordView.requestFocus();

            }
        }

        @Override
        protected void onCancelled() {
            mAuthTask = null;
            showProgress(false);
        }

        private boolean checkAccount() {
            String[] dummyAccountList = DUMMY_CREDENTIALS;
            for (String dummyAccount : dummyAccountList) {
                if (dummyAccount.split(":").length == 2) {
                    String dummyId = dummyAccount.split(":")[0];
                    String dummyPass = dummyAccount.split(":")[1];

                    if (dummyId.equals(mEmail) && dummyPass.equals(mPassword)) {
                        return true;
                    }
                }

            }
            return false;
        }

        private boolean checkLegibleHash(@Nullable String hashkey) {
            if (hashkey != null && existOnServer(hashkey)) {
                myHashUserId = hashkey;
                return true;
            } else {
                myHashUserId = Long.toHexString(mEmail.hashCode()
                        + mPassword.hashCode() * Calendar.getInstance().getTimeInMillis());
                return false;
            }
        }

        private boolean existOnServer(String hashkey) {
            return true;
        }

        private String getHashKey(String email, String pass) {
            return Long.toHexString(email.hashCode() + pass.hashCode() * Calendar.getInstance().getTimeInMillis());

        }
    }
}


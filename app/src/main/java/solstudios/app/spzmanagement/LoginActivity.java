package solstudios.app.spzmanagement;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.app.LoaderManager.LoaderCallbacks;
import android.content.Context;
import android.content.CursorLoader;
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
import android.support.design.widget.Snackbar;
import android.text.TextUtils;
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

import com.pusher.client.connection.ConnectionStateChange;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import solstudios.app.spzmanagement.pusher.PusherClientInstance;
import solstudios.app.spzmanagement.pusher.PusherHelper;

import static android.Manifest.permission.READ_CONTACTS;

/**
 * A login screen that offers login via email/password.
 */
public class LoginActivity extends BaseActivity implements LoaderCallbacks<Cursor> {
    public static final String TAB = "LoginActivity";
    public static final String USER_MAIL = "UserMail";
    public static final String USER_PASS = "UserPass";
    public static final String USER_LOGIN_HASH_VALUE = "User_Login_Hash";

    /**
     * Id to identity READ_CONTACTS permission request.
     */
    private static final int REQUEST_READ_CONTACTS = 0;

    /**
     * A dummy authentication store containing known user names and passwords.
     * TODO: remove after connecting to a real authentication system.
     */
    private static final String[] DUMMY_CREDENTIALS = new String[]{
            "foo@example.com:hello", "bar@example.com:world"
    };
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
    private boolean canConfig = false;
    private SharedPreferences sharedPreferences;
    private PusherBroadcast pusherBroadcastReceiver;
    private PusherHelper pusherHelper;

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

        mEmailSignInButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                attemptLogin();
            }
        });

        // Set up the login form.
        populateAutoComplete();
        checkSavedInstance();
    }

    private boolean checkSavedInstance() {
        String oldUserId = sharedPreferences.getString(USER_MAIL, null);
        String oldUserPass = sharedPreferences.getString(USER_PASS, null);
        String savedHash = sharedPreferences.getString(USER_LOGIN_HASH_VALUE, null);

        if (oldUserId != null && oldUserPass != null && savedHash != null) {
            mEmailView.setText(oldUserId);
            mPasswordView.setText(oldUserPass);

            changeLoginButtonState(99);
            return true;
        } else {
            changeLoginButtonState(0);
            return false;
        }
    }

    private void changeLoginButtonState(int loginState) {
        switch (loginState) {
            case -1:
                mEmailSignInButton.setText("Reconnect");
                mEmailSignInButton.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        attemptLogin();
                    }
                });
                break;
            case 0:
                mEmailSignInButton.setText("Login");
                mEmailSignInButton.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        attemptLogin();
                    }
                });
                break;
            case 1:
                mEmailSignInButton.setText("Logout");
                mEmailSignInButton.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        disconnect();
                    }
                });
                break;
            case 99:
                mEmailSignInButton.setText("Re-Login");
                mEmailSignInButton.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        String savedHash = sharedPreferences.getString(USER_LOGIN_HASH_VALUE, null);
                        connect(savedHash);
                    }
                });
                break;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
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
            mAuthTask = new UserLoginTask(email, password);
            mAuthTask.execute((Void) null);
        }
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

                }

                @Override
                public void onSubscriptionSucceeded(String channelName) {
                    new LogTask("onSubscriptionSucceeded|channelName:" + channelName,
                            TAB, LogTask.LOG_I);
                    //subscriptionSucceeded(channelName);
                }

                @Override
                public void onBindedEvent(String channel, String event) {
                    new LogTask("onBindedEvent|channelName:" + channel + ",event:" + event,
                            TAB, LogTask.LOG_I);
                    //bindSucceeded(channel, event);
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

    private void connect(String myHashUserId) {
        if (myHashUserId != null) {
            if (!myHashUserId.isEmpty()) {
                pusherHelper.resume();
            }
        }
    }

    private void disconnect() {
        pusherHelper.disconnect();
    }

    private void onConnectionStateChange(ConnectionStateChange connectionStateChange) {
        switch (connectionStateChange.getCurrentState()) {
            case CONNECTED:
                //logTextView.setText("Connected");
                changeLoginButtonState(1);
                String appid = PusherClientInstance.getInstance(this).curAppid;
                //String c = PusherClientInstance.getInstance(this).curAppid;
                logTextView.append("\nPusher Client: Connected. Using pusher_app_key : " + appid);

                canConfig = true;
                invalidateOptionsMenu();

                break;
            case DISCONNECTED:
                canConfig = false;
                invalidateOptionsMenu();
                changeLoginButtonState(-1);
                logTextView.append("\nPusher Client: Disconnected. Please Check Pusher's configurations");
                break;
            case CONNECTING:
                logTextView.append("\nConnecting..");
                break;
        }
    }

    private void writeValueToPref(String key, String value) {
        new LogTask("writeValueToPref|value:" + value + ",key:" + key, TAB, LogTask.LOG_I);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(key, value);
        editor.apply();
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

        private String mEmail = "null";
        private String mPassword = "null";
        private String myHashUserId;

        UserLoginTask(String email, String password) {
            mEmail = email;
            mPassword = password;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            // TODO: attempt authentication against a network service.

            myHashUserId = Long.toHexString(mEmail.hashCode() + mPassword.hashCode() * Calendar.getInstance().getTimeInMillis());
            try {
                // Simulate network access.
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                return false;
            }

            for (String credential : DUMMY_CREDENTIALS) {
                String[] pieces = credential.split(":");
                if (pieces[0].equals(mEmail)) {
                    // Account exists, return true if the password matches.
                    return pieces[1].equals(mPassword);
                }
            }

            // TODO: register the new account here.
            return true;
        }

        @Override
        protected void onPostExecute(final Boolean success) {
            mAuthTask = null;
            showProgress(false);

            if (success) {
                Toast.makeText(LoginActivity.this, "Retrieved Hashid: " + myHashUserId, Toast.LENGTH_SHORT).show();

                logTextView.setText("MyId: " + myHashUserId);

                ///save login info
                writeValueToPref(USER_MAIL, mEmail);
                writeValueToPref(USER_PASS, mPassword);
                writeValueToPref(USER_LOGIN_HASH_VALUE, myHashUserId);

                //mEmailSignInButton.setEnabled(false);

                mEmailView.setEnabled(false);
                mPasswordView.setEnabled(false);

                canConfig = true;
                invalidateOptionsMenu();

                ///resume
                connect(myHashUserId);

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
    }
}


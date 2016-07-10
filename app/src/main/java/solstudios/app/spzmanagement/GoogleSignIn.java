package solstudios.app.spzmanagement;

import android.content.Context;
import android.support.v4.app.FragmentActivity;

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.GoogleApiClient;

/**
 * Created by solbadguyky on 7/7/16.
 */

public class GoogleSignIn {
    public static final String TAB = "GoogleSignIn";
    public static GoogleApiClient mGoogleApiClient;

    public GoogleSignIn(Context context, GoogleApiClient.OnConnectionFailedListener onConnectionFailedListener) {
        mGoogleApiClient = new GoogleApiClient.Builder(context)
                .enableAutoManage((FragmentActivity) context, onConnectionFailedListener)
                .addApi(Auth.GOOGLE_SIGN_IN_API, createRequestSignIn())
                .build();
    }

    public GoogleApiClient getmGoogleApiClient() {
        return mGoogleApiClient;
    }

    private GoogleSignInOptions createRequestSignIn() {
        // Configure sign-in to request the user's ID, email address, and basic
        // profile. ID and basic profile are included in DEFAULT_SIGN_IN.
        return new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .build();
    }
}

package se.heinrisch.talkclient.adapters;

import android.os.Bundle;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;

public class TalkCallbackAdapter {
    private static final String TAG = "TalkCallbackAdapter Unimplemented callback";

    public void onConnected(Bundle bundle) {
        Log.i(TAG, "onConnected");
    }

    public void onConnectionSuspended(int cause) {
        Log.i(TAG, "onConnectionSuspended");
    }

    public void onConnectionFailed(ConnectionResult connectionResult) {
        Log.i(TAG, "onConnectionFailed");
    }
}

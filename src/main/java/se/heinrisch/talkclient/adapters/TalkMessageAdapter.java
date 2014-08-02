package se.heinrisch.talkclient.adapters;

import android.util.Log;

import com.google.android.gms.wearable.DataMap;
import com.google.android.gms.wearable.MessageApi;
import com.google.android.gms.wearable.MessageEvent;

public class TalkMessageAdapter {
    private static final String TAG = "TalkMessageAdapter Unimplemented callback";

    public void onResult(MessageApi.SendMessageResult sendMessageResult) {
        Log.e(TAG, "onResult");
    }

    public void onMessageReceived(MessageEvent messageEvent) {
        Log.e(TAG, "messageEvent");
    }
}

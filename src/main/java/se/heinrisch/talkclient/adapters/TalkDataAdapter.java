package se.heinrisch.talkclient.adapters;

import android.graphics.Bitmap;
import android.util.Log;

import com.google.android.gms.wearable.DataApi;
import com.google.android.gms.wearable.DataItem;
import com.google.android.gms.wearable.DataMap;
import com.google.android.gms.wearable.DataMapItem;

import java.util.List;

public class TalkDataAdapter {
    private static final String TAG = "TalkDataAdapter Unimplemented callback";

    public void onSendResult(String path, DataItem dataItem) {
        Log.e(TAG, "onSendResult()");
    }

    public void onRecieveResult(DataMap dataMap) {
        Log.e(TAG, "onRecieveResult()");
    }

    public void onSendBitmapResult(DataItem dataItem) {
        Log.e(TAG, "onSendBitmapResult()");
    }

    public void onRecieveBitmapResult(String assetName, Bitmap bitmap) {
        Log.e(TAG, "onRecieveBitmapResult()");
    }
}

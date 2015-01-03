package se.heinrisch.talkclient.adapters;

import android.graphics.Bitmap;
import android.util.Log;

import com.google.android.gms.wearable.DataItem;
import com.google.android.gms.wearable.DataMap;

public class TalkDataAdapter {
    private static final String TAG = "TalkDataAdapter Unimplemented callback";

    public void onSendResult(String path, DataItem dataItem) {
        Log.i(TAG, "onSendResult()");
    }

    public void onRecieveResult(DataMap dataMap) {
        Log.i(TAG, "onRecieveResult()");
    }

    public void onSendBitmapResult(DataItem dataItem) {
        Log.i(TAG, "onSendBitmapResult()");
    }

    public void onRecieveBitmapResult(String assetName, Bitmap bitmap) {
        Log.i(TAG, "onRecieveBitmapResult()");
    }

    public void onDataDeleted(DataItem dataItem) {
        Log.i(TAG, "onDataDeleted()");
    }
    public void onDataChanged(DataItem dataItem) {
        Log.i(TAG, "onDataChanged()");
    }
}

package se.heinrisch.talkclient;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.wearable.Asset;
import com.google.android.gms.wearable.DataApi;
import com.google.android.gms.wearable.DataEvent;
import com.google.android.gms.wearable.DataEventBuffer;
import com.google.android.gms.wearable.DataItemBuffer;
import com.google.android.gms.wearable.DataMap;
import com.google.android.gms.wearable.DataMapItem;
import com.google.android.gms.wearable.MessageApi;
import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.NodeApi;
import com.google.android.gms.wearable.PutDataMapRequest;
import com.google.android.gms.wearable.PutDataRequest;
import com.google.android.gms.wearable.Wearable;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

import se.heinrisch.talkclient.adapters.TalkCallbackAdapter;
import se.heinrisch.talkclient.adapters.TalkDataAdapter;
import se.heinrisch.talkclient.adapters.TalkMessageAdapter;

public class TalkClient implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, MessageApi.MessageListener, DataApi.DataListener {

    public final static String TAG = "TalkClient";
    public final static String DATA_ARRAY = "data-array";
    public final static String DATA_ITEM = "data-item";

    private GoogleApiClient mGoogleApiClient;
    private TalkCallbackAdapter mTalkCallbackAdapter;
    private TalkMessageAdapter mTalkMessageAdapter;
    private TalkDataAdapter mTalkDataAdapter;

    public TalkClient(Context context) {
        mTalkCallbackAdapter = new TalkCallbackAdapter();
        mTalkMessageAdapter = new TalkMessageAdapter();
        mTalkDataAdapter = new TalkDataAdapter();

        mGoogleApiClient = new GoogleApiClient.Builder(context)
                .addApi(Wearable.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();
    }

    public void setTalkCallbackAdapter(TalkCallbackAdapter adapter) {
        mTalkCallbackAdapter = adapter;
    }

    public void setTalkMessageAdapter(TalkMessageAdapter adapter) {
        mTalkMessageAdapter = adapter;
    }

    public void setTalkDataAdapter(TalkDataAdapter adapter) {
        mTalkDataAdapter = adapter;
    }

    public void connectClient() {
        if (!mGoogleApiClient.isConnected()) {
            mGoogleApiClient.connect();
        }
    }

    public void disconnectClient() {
        if (mGoogleApiClient.isConnected()) {
            mGoogleApiClient.disconnect();
        }
    }

    @Override
    public void onConnected(Bundle bundle) {
        Wearable.MessageApi.addListener(mGoogleApiClient, this);
        Wearable.DataApi.addListener(mGoogleApiClient, this);
        mTalkCallbackAdapter.onConnected(bundle);
    }

    @Override
    public void onConnectionSuspended(int cause) {
        Wearable.MessageApi.removeListener(mGoogleApiClient, this);
        Wearable.DataApi.removeListener(mGoogleApiClient, this);
        mTalkCallbackAdapter.onConnectionSuspended(cause);
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        mTalkCallbackAdapter.onConnectionFailed(connectionResult);
    }

    public void sendMessage(final String path) {
        sendMessage(path, null);
    }

    public void sendMessage(final String path, DataMap dataMap) {
        final byte[] data = dataMap != null ? dataMap.toByteArray() : new byte[0];
        Wearable.NodeApi.getConnectedNodes(mGoogleApiClient).setResultCallback(
                new ResultCallback<NodeApi.GetConnectedNodesResult>() {
                    @Override
                    public void onResult(NodeApi.GetConnectedNodesResult getConnectedNodesResult) {
                        for (final Node node : getConnectedNodesResult.getNodes()) {
                            Wearable.MessageApi.sendMessage(mGoogleApiClient, node.getId(), path, data)
                                    .setResultCallback(getSendMessageResultCallback());
                        }
                    }
                }
        );
    }

    private ResultCallback<MessageApi.SendMessageResult> getSendMessageResultCallback() {
        return new ResultCallback<MessageApi.SendMessageResult>() {
            @Override
            public void onResult(MessageApi.SendMessageResult sendMessageResult) {
                mTalkMessageAdapter.onResult(sendMessageResult);
            }
        };
    }

    @Override
    public void onMessageReceived(MessageEvent messageEvent) {
        mTalkMessageAdapter.onMessageReceived(messageEvent);
    }

    @Override
    public void onDataChanged(DataEventBuffer dataEvents) {
        for (DataEvent event : dataEvents) {
            if (event.getType() == DataEvent.TYPE_DELETED) {
                mTalkDataAdapter.onDataDeleted(event.getDataItem());
            } else if (event.getType() == DataEvent.TYPE_CHANGED) {
                mTalkDataAdapter.onDataChanged(event.getDataItem());
            }
        }
    }

    public void syncDataMap(String path, DataMap dataMap) {
        PutDataMapRequest putDataMapRequest = PutDataMapRequest.create(path);
        putDataMapRequest.getDataMap().putDataMap(DATA_ITEM, dataMap);
        PutDataRequest request = putDataMapRequest.asPutDataRequest();
        PendingResult<DataApi.DataItemResult> pendingResult = Wearable.DataApi.putDataItem(mGoogleApiClient, request);
        pendingResult.setResultCallback(new ResultCallback<DataApi.DataItemResult>() {
            @Override
            public void onResult(DataApi.DataItemResult dataItemResult) {
                mTalkDataAdapter.onSendResult(dataItemResult.getDataItem().getUri().getPath(), dataItemResult.getDataItem());
            }
        });
    }

    public void syncDataArray(String path, ArrayList<DataMap> dataMaps) {
        PutDataMapRequest putDataMapRequest = PutDataMapRequest.create(path);
        putDataMapRequest.getDataMap().putDataMapArrayList(DATA_ARRAY, dataMaps);
        PutDataRequest request = putDataMapRequest.asPutDataRequest();
        PendingResult<DataApi.DataItemResult> pendingResult = Wearable.DataApi.putDataItem(mGoogleApiClient, request);
        pendingResult.setResultCallback(new ResultCallback<DataApi.DataItemResult>() {
            @Override
            public void onResult(DataApi.DataItemResult dataItemResult) {
                mTalkDataAdapter.onSendResult(dataItemResult.getDataItem().getUri().getPath(), dataItemResult.getDataItem());
            }
        });
    }

    public void getDataItems(final String path) {
        PendingResult<DataItemBuffer> results = Wearable.DataApi.getDataItems(mGoogleApiClient);
        results.setResultCallback(new ResultCallback<DataItemBuffer>() {
            @Override
            public void onResult(DataItemBuffer dataItems) {
                for (int i = 0; i < dataItems.getCount(); i++) {
                    final DataMapItem dataMapItem = DataMapItem.fromDataItem(dataItems.get(i));
                    if (dataMapItem.getUri().getPath().equals(path)) {
                        mTalkDataAdapter.onRecieveResult(dataMapItem.getDataMap());
                        dataItems.release();
                        return;
                    }
                }
                mTalkDataAdapter.onRecieveResult(new DataMap());
                dataItems.release();
            }
        });
    }

    private static Asset createAssetFromBitmap(Bitmap bitmap) {
        if (bitmap != null) {
            final ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, byteStream);
            return Asset.createFromBytes(byteStream.toByteArray());
        }
        return null;
    }

    public void syncBitmap(String path, String assetName, Bitmap bitmap) {
        Asset asset = createAssetFromBitmap(bitmap);
        if (asset != null) {
            PutDataMapRequest dataMap = PutDataMapRequest.create(path + "/" + assetName);
            dataMap.getDataMap().putAsset(assetName, asset);
            PutDataRequest request = dataMap.asPutDataRequest();
            PendingResult<DataApi.DataItemResult> pendingResult = Wearable.DataApi.putDataItem(mGoogleApiClient, request);
            pendingResult.setResultCallback(new ResultCallback<DataApi.DataItemResult>() {
                @Override
                public void onResult(DataApi.DataItemResult dataItemResult) {
                    mTalkDataAdapter.onSendBitmapResult(dataItemResult.getDataItem());
                }
            });
        } else {
            Log.e(TAG, "syncBitmap(): asset is null");
        }
    }

    public void getBitmap(final Context context, final String path, final String assetName) {
        PendingResult<DataItemBuffer> results = Wearable.DataApi.getDataItems(mGoogleApiClient);
        results.setResultCallback(new ResultCallback<DataItemBuffer>() {
            @Override
            public void onResult(DataItemBuffer dataItems) {
                for (int i = 0; i < dataItems.getCount(); i++) {
                    final DataMapItem dataMapItem = DataMapItem.fromDataItem(dataItems.get(i));
                    if (dataMapItem.getUri().getPath().equals(path + "/" + assetName)) {
                        getAssetAndReturnOnMainThread(dataMapItem, assetName, context);
                        continue;
                    }
                }
                dataItems.release();
            }
        });
    }

    private void getAssetAndReturnOnMainThread(DataMapItem dataMapItem, final String assetName, final Context context) {
        final Asset asset = dataMapItem.getDataMap().getAsset(assetName);
        if (asset != null) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    final Bitmap bitmap = loadBitmapFromAsset(asset);
                    Handler mainHandler = new Handler(context.getMainLooper());
                    mainHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            mTalkDataAdapter.onRecieveBitmapResult(assetName, bitmap);
                        }
                    });
                }
            }).start();
        } else {
            Log.e(TAG, "getBitmap() asset is null");
        }
    }

    public Bitmap loadBitmapFromAsset(Asset asset) {
        if (asset == null) {
            throw new IllegalArgumentException("Asset must be non-null");
        }
        ConnectionResult result = mGoogleApiClient.blockingConnect(1000, TimeUnit.MILLISECONDS);
        if (!result.isSuccess()) {
            return null;
        }
        InputStream assetInputStream = Wearable.DataApi.getFdForAsset(
                mGoogleApiClient, asset).await().getInputStream();
        mGoogleApiClient.disconnect();

        if (assetInputStream == null) {
            return null;
        }
        return BitmapFactory.decodeStream(assetInputStream);
    }
}

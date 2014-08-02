TalkClient
=======

A simple helper for communication between Android Wear and connected devices.

Usage
--------

#In your activity, create a TalkClient and connect it:

```java
public class Sample extends Activity {
    private TalkClient mTalkClient;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sample);
        mTalkClient = new TalkClient(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        mTalkClient.connectClient();
    }

    @Override
    protected void onDestroy() {
        mTalkClient.disconnectClient();
        super.onDestroy();
    }
}
```

#Send a message:

```java
    private void sendMessage() {
        DataMap dataMap = new DataMap();
        dataMap.putString("data", "Hello World");
        mTalkClient.setTalkMessageAdapter(new TalkMessageAdapter(){
            @Override
            public void onResult(MessageApi.SendMessageResult sendMessageResult) {
                Log.e(TAG, "Message sent");
            }
        });
        mTalkClient.sendMessage("/my-path", dataMap);
    }
```

#Recieve a message in active app:

```java
    @Override
    protected void doStuff() {
        mTalkClient.setTalkMessageAdapter(new TalkMessageAdapter() {
        @Override
        public void onMessageReceived(MessageEvent messageEvent) {
            final DataMap dataMap = DataMap.fromByteArray(messageEvent.getData());
            //Do stuff
        }
      });
    }
```

#Recieve data on inactive (or active app):

```java
public class ListenerServiceMobile extends WearableListenerService {

    private TalkClient mTalkClient;

    @Override
    public void onCreate() {
        super.onCreate();
        mTalkClient = new TalkClient(getApplicationContext());
        mTalkClient.connectClient();
    }

    @Override
    public void onMessageReceived(MessageEvent messageEvent) {
        final DataMap dataMap = DataMap.fromByteArray(messageEvent.getData());
        //Do things
    }
    
    @Override
    public void onDestroy() {
        mTalkClient.disconnectClient();
        super.onDestroy();
    }
}
``` 

With the serives, don't forget to add to manifest:

```xml
    <application android:allowBackup="true"
        android:label="@string/app_name"
        android:icon="@drawable/ic_launcher"
        android:theme="@style/AppTheme">
...
        <service android:name=".ListenerServiceMobile">
            <intent-filter>
                <action android:name="com.google.android.gms.wearable.BIND_LISTENER" />
            </intent-filter>
        </service>
...
    </application>
```

#Sync Data between app and watch:

```java
        mTalkClient.syncDataArray("/my-path-to-data, dataMaps);
```

#Retrive synched data:

```java
        mTalkClient.setTalkDataAdapter(new TalkDataAdapter() {
            @Override
            public void onRecieveResult(DataMap dataMap) {
                List<DataMap> dataMaps = dataMap.getDataMapArray(TalkClient.DATA_ARRAY);
                //Do Stuff
            }
        });
        mTalkClient.getDataItems("/my-path-to-data");
```

#Sync bitmap asset:

```java
        mTalkClient.syncBitmap("/my-bitmap-path", "asset-name", photo);
```

#Retrive synched data:

```java
        mTalkClient.setTalkDataAdapter(new TalkDataAdapter() {
            @Override
            public void onRecieveBitmapResult(String assetName, Bitmap bitmap) {
                //Do things
            }
        });

        mTalkClient.getBitmap(MainActivityWear.this, "/my-bitmap-path", "asset-name");
```

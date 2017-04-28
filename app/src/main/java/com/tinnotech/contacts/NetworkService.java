package com.tinnotech.contacts;

import android.app.Service;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.support.annotation.IntDef;
import android.util.Log;

import cn.teemo.www.aidl.ISocketService;

public class NetworkService extends Service {
    private static final String TAG = "NetworkService";

    private boolean mNetworkBinded = false;
    private ISocketService mNetworkService;

    public NetworkService() {
    }

    @Override
    public void onCreate() {
        super.onCreate();
        bindNetWork();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if(!mNetworkBinded){
            Log.e(TAG, "NetworkService not bind");
        }
        else{
            int type = intent.getIntExtra("type", 0);
            String json = intent.getStringExtra("json");
            if((type != 0) && (json != null)){
                try {
                    mNetworkService.send(type, json, new byte[]{'a'});
                }catch(Exception e){

                }
            }
        }
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        if(mNetworkBinded) {
            mNetworkBinded = false;
            unbindService(mNetworkConn);
        }
        super.onDestroy();
    }

    @Override
    public IBinder onBind(Intent intent) {
       return null;
    }

    private void bindNetWork() {
        Intent intent = new Intent("cn.teemo.www.network.ISocketService");
        intent.setPackage("cn.teemo.www.network");
        bindService(intent, mNetworkConn, Service.BIND_AUTO_CREATE);
    }
    private ServiceConnection mNetworkConn = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            mNetworkService = ISocketService.Stub.asInterface(iBinder);
            mNetworkBinded = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            if(!mNetworkBinded)
                bindNetWork();

            mNetworkBinded = false;
        }
    };
}

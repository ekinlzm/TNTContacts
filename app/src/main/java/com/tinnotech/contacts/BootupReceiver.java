package com.tinnotech.contacts;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class BootupReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        // todo 第一次开机检查  contacts初始化完毕没有？
        Intent actionIntent = new Intent(context, NetworkIntentService.class);
        actionIntent.setAction("com.tinnotech.contacts.NETWORK_ACTION");
        actionIntent.putExtra("type", NetworkIntentService.ACTION_TYPE_INIT);
        context.startService(actionIntent);
    }
}

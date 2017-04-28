package com.tinnotech.contacts;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class BootupReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        context.startService(new Intent(context, NetworkService.class));
        // todo 第一次开机检查 如果所有版本号都为0， 那么清理电话本
        Intent actionIntent = new Intent(context, ActionIntentService.class);
        actionIntent.setAction("com.tinnotech.contacts.NETWORK_ACTION");
        actionIntent.putExtra("type", ActionIntentService.ACTION_TYPE_INIT);
        context.startService(actionIntent);
    }
}

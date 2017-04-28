package com.tinnotech.contacts;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class BootupReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        // todo 第一次开机检查 如果所有版本号都为0， 那么清理电话本
        context.startService(new Intent(context, NetworkService.class));
    }
}

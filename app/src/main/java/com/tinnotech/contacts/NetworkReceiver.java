package com.tinnotech.contacts;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class NetworkReceiver extends BroadcastReceiver {
    private static final String TAG = "NetworkReceiver";
    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent == null)
            return;
        Log.i(TAG, "action = " + intent.getAction().toString());
        Intent recIntent = new Intent(context, ActionIntentService.class);
        recIntent.setAction("com.tinnotech.contacts.NETWORK_ACTION");
        recIntent.putExtra("type", intent.getIntExtra("type", 0));
        recIntent.putExtra("json", intent.getStringExtra("json"));
        context.startService(recIntent);
    }
}

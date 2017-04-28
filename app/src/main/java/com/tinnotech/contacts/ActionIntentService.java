package com.tinnotech.contacts;

import android.app.IntentService;
import android.app.Service;
import android.content.ComponentName;
import android.content.Intent;
import android.content.Context;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.os.IBinder;
import android.util.Log;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.serializer.IntegerCodec;

import cn.teemo.www.aidl.ISocketService;

public class ActionIntentService extends IntentService {
    public static final int ACTION_TYPE_LOGIN = 1;
    public static final int ACTION_TYPE_BIND = 2;
    public static final int ACTION_TYPE_UNBIND = 3;
    public static final int ACTION_TYPE_COMMON_SETTING = 16;
    public static final int ACTION_TYPE_UPDATE = 20;
    public static final int ACTION_TYPE_REFRESH = 21;
    public static final String SP_FILE_NAME = "settings";
    public static final String SP_KEY_CONTACT = "contact";
    public static final String SP_KEY_FAMILY = "family";
    public static final String SP_KEY_FRIEND = "friend";
    public static final String SP_KEY_WHITE_LIST_ENABLE = "white_list_enabled";
    private static final String TAG = "ActionIntentService";


    public ActionIntentService() {
        super("ActionIntentService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            final String action = intent.getAction();
            final int type = intent.getIntExtra("type", 0);
            final String json = intent.getStringExtra("json");
            switch(type){
                case ACTION_TYPE_LOGIN:
                    handleActionLogin(json);
                    break;

                case ACTION_TYPE_BIND:
                    handleActionBind(json);
                    break;

                case ACTION_TYPE_UNBIND:
                    handleActionUnBind(json);
                    break;

                case ACTION_TYPE_COMMON_SETTING:
                    handleActionCommonSetting(json);
                    break;

                case ACTION_TYPE_UPDATE:
                    handleActionContactUpdate(json);
                    break;

                case ACTION_TYPE_REFRESH:
                    handleActionContactRefresh(json);
                    break;

                default:
                    break;
            }
        }
    }

    private void handleActionLogin(String json_str) {
        JSONObject obj = JSON.parseObject(json_str);
        if (!obj.containsKey("status")) return;
        int status = obj.getInteger("status");
        if(status != 0) return;
        SharedPreferences sp = getSharedPreferences(SP_FILE_NAME, Context.MODE_PRIVATE);
        final int contact_version = sp.getInt(SP_KEY_CONTACT, 0);
        final int family_version = sp.getInt(SP_KEY_FAMILY, 0);
        final int friend_version = sp.getInt(SP_KEY_FRIEND, 0);
        obj = new JSONObject();
        obj.put(SP_KEY_CONTACT, contact_version);
        obj.put(SP_KEY_FAMILY, family_version);
        obj.put(SP_KEY_FRIEND, friend_version);
        Intent intent = new Intent(getApplicationContext(), NetworkService.class);
        intent.putExtra("type", ACTION_TYPE_UPDATE);
        intent.putExtra("json", obj.toString());
        startService(intent);
    }

    private void handleActionBind(String json_str) {

    }

    private void handleActionUnBind(String json_str) {

    }

    private void handleActionCommonSetting(String json_str) {
        JSONObject root_obj = JSON.parseObject(json_str);
        String module = root_obj.getString("module");
        if(!module.equals("system")) return;
        JSONObject param_obj = root_obj.getJSONObject("param");
        if(param_obj == null) return;
        int white_list_enabled = param_obj.getInteger(SP_KEY_WHITE_LIST_ENABLE);
        SharedPreferences sp = getSharedPreferences(SP_FILE_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        editor.putInt(SP_KEY_WHITE_LIST_ENABLE, white_list_enabled);
        editor.commit();
    }

    private void handleActionContactUpdate(String json_str) {

    }
    private void handleActionContactRefresh(String json_str) {
        JSONObject obj = JSON.parseObject(json_str);
        String update_type = obj.getString("type");

        SharedPreferences sp = getSharedPreferences(SP_FILE_NAME, Context.MODE_PRIVATE);
        int version = 0;
        if(update_type.equals(SP_KEY_CONTACT) || update_type.equals(SP_KEY_FAMILY) || update_type.equals(SP_KEY_FRIEND)){
            version = sp.getInt(update_type, 0);
            obj = new JSONObject();
            obj.put(update_type, version);
            Intent intent = new Intent(getApplicationContext(), NetworkService.class);
            intent.putExtra("type", ACTION_TYPE_UPDATE);
            intent.putExtra("json", obj.toString());
            startService(intent);
        }
        else{
            Log.e(TAG, "handleActionContactRefresh type = " + update_type);
        }
    }
}

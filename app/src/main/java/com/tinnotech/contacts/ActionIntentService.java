package com.tinnotech.contacts;

import android.app.IntentService;
import android.app.Service;
import android.content.ComponentName;
import android.content.ContentProviderOperation;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Intent;
import android.content.Context;
import android.content.OperationApplicationException;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.IBinder;
import android.os.RemoteException;
import android.provider.ContactsContract;
import android.util.Log;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.serializer.IntegerCodec;

import java.util.ArrayList;

import cn.teemo.www.aidl.ISocketService;

public class ActionIntentService extends IntentService {
    public static final int ACTION_TYPE_INIT = -1;  //内部操作 开机初始化检查
    public static final int ACTION_TYPE_LOGIN = 1;
    public static final int ACTION_TYPE_BIND = 2;
    public static final int ACTION_TYPE_UNBIND = 3;
    public static final int ACTION_TYPE_COMMON_SETTING = 16;
    public static final int ACTION_TYPE_UPDATE = 20;
    public static final int ACTION_TYPE_REFRESH = 21;

    private static final Uri RAW_CONTACTS_URI = ContactsContract.RawContacts.CONTENT_URI;
    private static final Uri DATA_URI = ContactsContract.Data.CONTENT_URI;
    private static final Uri GROUP_URI = ContactsContract.Groups.CONTENT_URI;

    private static final String GROUP_SOURCE_ID = ContactsContract.Groups.SOURCE_ID;

    private static final String ACCOUNT_TYPE = ContactsContract.RawContacts.ACCOUNT_TYPE;
    private static final String ACCOUNT_NAME = ContactsContract.RawContacts.ACCOUNT_NAME;

    private static final String RAW_CONTACT_ID = ContactsContract.Contacts.Data.RAW_CONTACT_ID;
    private static final String MIMETYPE = ContactsContract.Contacts.Data.MIMETYPE;

    private static final String NAME_ITEM_TYPE = ContactsContract.CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE;
    private static final String DISPLAY_NAME = ContactsContract.CommonDataKinds.StructuredName.DISPLAY_NAME;

    private static final String PHONE_ITEM_TYPE = ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE;
    private static final String PHONE_NUMBER = ContactsContract.CommonDataKinds.Phone.NUMBER;
    private static final String PHONE_TYPE = ContactsContract.CommonDataKinds.Phone.TYPE;
    private static final int PHONE_TYPE_MOBILE = ContactsContract.CommonDataKinds.Phone.TYPE_MOBILE;

    private static final String GROUP_ITEM_TYPE = ContactsContract.CommonDataKinds.GroupMembership.CONTENT_ITEM_TYPE;
    private static final String GROUP_ID = ContactsContract.CommonDataKinds.GroupMembership.GROUP_ROW_ID;

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
                case ACTION_TYPE_INIT:
                    handleActionInit();
                    break;
                case ACTION_TYPE_LOGIN:
                    handleActionLogin(json);
                    break;
                case ACTION_TYPE_BIND:
                    handleActionBind();
                    break;
                case ACTION_TYPE_UNBIND:
                    handleActionUnBind();
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

    private void deleteAllContacts(){
        Uri uri = Uri.parse("content://com.android.contacts/raw_contacts");
        getContentResolver().delete(uri,"_id!=-1", null);
        Log.e(TAG, "deleteAllContacts");
    }

    private void createDefaultGroups(){
        //查询内置群组是否已创建
        String group_source_id;
        long group_id_family_profile = 0, group_id_family = 0, group_id_contact = 0, group_id_friend = 0;
        Cursor groupCursor = getContentResolver().query(GROUP_URI,
                new String[]{GROUP_SOURCE_ID, ContactsContract.Groups._ID},
                null,
                null,
                null);
        if(groupCursor.getCount() > 0){
            while(groupCursor.moveToNext()){
                group_source_id = groupCursor.getString(0);
                if(group_source_id == null) continue;
                if(group_source_id.equals(Const.CONTACTS_GROUP_FAMILY_PROFILE)){
                    group_id_family_profile = groupCursor.getInt(1);
                }
                else if(group_source_id.equals(Const.CONTACTS_GROUP_CONTACT)){
                    group_id_contact = groupCursor.getInt(1);
                }
                else if(group_source_id.equals(Const.CONTACTS_GROUP_FAMILY)){
                    group_id_family = groupCursor.getInt(1);
                }
                else if(group_source_id.equals(Const.CONTACTS_GROUP_FRIEND)){
                    group_id_friend = groupCursor.getInt(1);
                }
                if((group_id_family_profile != 0) && (group_id_contact != 0) && (group_id_family != 0) && (group_id_friend != 0))
                    break;
            }
        }
        groupCursor.close();

        if(group_id_family_profile == 0) {
            ContentValues values = new ContentValues();
            values.put(GROUP_SOURCE_ID, Const.CONTACTS_GROUP_FAMILY_PROFILE);
            Uri uri = getContentResolver().insert(GROUP_URI, values);
            group_id_family_profile = ContentUris.parseId(uri);
            Log.e(TAG, "crate group_id_family_profile id:" + group_id_family_profile);
        }

        if(group_id_contact == 0) {
            ContentValues values = new ContentValues();
            values.put(GROUP_SOURCE_ID, Const.CONTACTS_GROUP_CONTACT);
            Uri uri = getContentResolver().insert(GROUP_URI, values);
            group_id_contact = ContentUris.parseId(uri);
            Log.e(TAG, "crate group_id_contact id:" + group_id_contact);
        }

        if(group_id_family == 0) {
            ContentValues values = new ContentValues();
            values.put(GROUP_SOURCE_ID, Const.CONTACTS_GROUP_FAMILY);
            Uri uri = getContentResolver().insert(GROUP_URI, values);
            group_id_family = ContentUris.parseId(uri);
            Log.e(TAG, "crate group_id_family id:" + group_id_family);
        }

        if(group_id_friend == 0) {
            ContentValues values = new ContentValues();
            values.put(GROUP_SOURCE_ID, Const.CONTACTS_GROUP_FRIEND);
            Uri uri = getContentResolver().insert(GROUP_URI, values);
            group_id_friend = ContentUris.parseId(uri);
            Log.e(TAG, "crate group_id_friend id:" + group_id_friend);
        }

    }
    private void batchAddContact(ArrayList<Person> list)
            throws RemoteException, OperationApplicationException {
        ArrayList<ContentProviderOperation> ops = new ArrayList<>();
        int rawContactInsertIndex = 0, i;
        String phone_num[];

        for (Person person : list) {
            rawContactInsertIndex = ops.size(); // 有了它才能给真正的实现批量添加

            ops.add(ContentProviderOperation.newInsert(RAW_CONTACTS_URI)
                    .withValue(ACCOUNT_TYPE, null)
                    .withValue(ACCOUNT_NAME, null)
                    .withYieldAllowed(true).build());

            // name
            ops.add(ContentProviderOperation
                    .newInsert(DATA_URI)
                    .withValueBackReference(RAW_CONTACT_ID, rawContactInsertIndex)
                    .withValue(MIMETYPE, NAME_ITEM_TYPE)
                    .withValue(DISPLAY_NAME, person.getName())
                    .withYieldAllowed(true).build());

            // phone
            phone_num = person.getPhone();
            if(phone_num != null) {
                for(i = 0; i < phone_num.length; i ++) {
                    ops.add(ContentProviderOperation
                            .newInsert(DATA_URI)
                            .withValueBackReference(RAW_CONTACT_ID, rawContactInsertIndex)
                            .withValue(MIMETYPE, PHONE_ITEM_TYPE)
                            .withValue(PHONE_NUMBER, phone_num[i])
                            .withValue(PHONE_TYPE, PHONE_TYPE_MOBILE)
                            .withYieldAllowed(true)
                            .build());
                }
            }

            // user_id

            //group
            ops.add(ContentProviderOperation
                    .newInsert(DATA_URI)
                    .withValueBackReference(RAW_CONTACT_ID, rawContactInsertIndex)
                    .withValue(MIMETYPE, GROUP_ITEM_TYPE)
                    .withValue(GROUP_ID, person.getGroup())
                    .withYieldAllowed(true).build());

            //device_type
            //spell
            //portrait_url
            //portrait_id
            //birthday
            //gender
            //auth

        }
        // 真正添加
        getContentResolver().applyBatch(ContactsContract.AUTHORITY, ops);
    }

    private void handleActionInit() {
        /* 查看电话本版本号， 如果所有的都为0， 确保系统电话本已清理 */
        SharedPreferences sp = getSharedPreferences(Const.SP_FILE_NAME, Context.MODE_PRIVATE);
        final int contact_version = sp.getInt(Const.SP_KEY_CONTACT, 0);
        final int family_version = sp.getInt(Const.SP_KEY_FAMILY, 0);
        final int friend_version = sp.getInt(Const.SP_KEY_FRIEND, 0);
        if(contact_version + family_version + friend_version == 0) {
            ContentResolver cr = getContentResolver();
            Cursor cur = cr.query(ContactsContract.Contacts.CONTENT_URI,
                    null, null, null, null);
            int count = cur.getCount();
            cur.close();
            if(count != 0)
                deleteAllContacts();
        }
        //创建默认群组
        createDefaultGroups();
    }

    private void handleActionLogin(String json_str) {
        JSONObject obj = JSON.parseObject(json_str);
        if (!obj.containsKey("status")) return;
        int status = obj.getInteger("status");
        if(status != 0) return;
        SharedPreferences sp = getSharedPreferences(Const.SP_FILE_NAME, Context.MODE_PRIVATE);
        final int contact_version = sp.getInt(Const.SP_KEY_CONTACT, 0);
        final int family_version = sp.getInt(Const.SP_KEY_FAMILY, 0);
        final int friend_version = sp.getInt(Const.SP_KEY_FRIEND, 0);
        obj = new JSONObject();
        obj.put(Const.SP_KEY_CONTACT, contact_version);
        obj.put(Const.SP_KEY_FAMILY, family_version);
        obj.put(Const.SP_KEY_FRIEND, friend_version);
        Intent intent = new Intent(getApplicationContext(), NetworkService.class);
        intent.putExtra("type", ACTION_TYPE_UPDATE);
        intent.putExtra("json", obj.toString());
        startService(intent);
    }

    private void handleActionBind() {
        /* 绑定之后 需要主动上传版本号 */
        //创建默认群组
        createDefaultGroups();
        JSONObject obj = new JSONObject();
        obj.put(Const.SP_KEY_CONTACT, 0);
        obj.put(Const.SP_KEY_FAMILY, 0);
        obj.put(Const.SP_KEY_FRIEND, 0);
        Intent intent = new Intent(getApplicationContext(), NetworkService.class);
        intent.putExtra("type", ACTION_TYPE_UPDATE);
        intent.putExtra("json", obj.toString());
        startService(intent);
    }

    private void handleActionUnBind() {
        deleteAllContacts();
        SharedPreferences sp = getSharedPreferences(Const.SP_FILE_NAME, Context.MODE_PRIVATE);
        if(sp != null){
            SharedPreferences.Editor editor = sp.edit();
            editor.clear();
            editor.commit();
        }

    }

    private void handleActionCommonSetting(String json_str) {
        JSONObject root_obj = JSON.parseObject(json_str);
        String module = root_obj.getString("module");
        if(!module.equals("system")) return;
        JSONObject param_obj = root_obj.getJSONObject("param");
        if(param_obj == null) return;
        int white_list_enabled = param_obj.getInteger(Const.SP_KEY_WHITE_LIST_ENABLE);
        SharedPreferences sp = getSharedPreferences(Const.SP_FILE_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        editor.putInt(Const.SP_KEY_WHITE_LIST_ENABLE, white_list_enabled);
        editor.commit();
    }

    private void handleActionContactUpdate(String json_str) {
        /* 目前只支持全量更新，暂不考虑start_version，type */
        SharedPreferences sp = getSharedPreferences(Const.SP_FILE_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        boolean write_flag = false;
        int sp_version, json_version;
        JSONObject root_obj = JSON.parseObject(json_str);
        JSONObject sub_obj, sub_obj2, person_obj;
        JSONArray data_array, person_array, phone_array;
        int i, j, k;

        deleteAllContacts(); //test
        //handle family
        while (root_obj.containsKey(Const.SP_KEY_CONTACT)){
            sub_obj = root_obj.getJSONObject(Const.SP_KEY_CONTACT);
            json_version = sub_obj.getInteger("to_version");
            sp_version = sp.getInt(Const.SP_KEY_CONTACT, 0);
            if(sp_version >= json_version){
                Log.e(TAG, "update contacts[contact] local_version:" + sp_version + ", server_version:" + json_version);
                break;
            }

            break;
        }
        //handle family
        while (root_obj.containsKey(Const.SP_KEY_FAMILY)){
            sub_obj = root_obj.getJSONObject(Const.SP_KEY_FAMILY);
            json_version = sub_obj.getInteger("to_version");
            sp_version = sp.getInt(Const.SP_KEY_FAMILY, 0);
            if(sp_version >= json_version){
                Log.e(TAG, "update contacts[family] local_version:" + sp_version + ", server_version:" + json_version);
                break;
            }
            ArrayList<Person> family_list = new ArrayList<Person>();
            //profile
            person_obj = sub_obj.getJSONObject("profile");
            if(person_obj != null){
                Person person = new Person();
                person.setGroup(Const.CONTACTS_GROUP_FAMILY_PROFILE);
                person.setUser_id(person_obj.getLong("family_id"));
                person.setName(person_obj.getString("name"));
                person.setSpell(person_obj.getString("spell"));
                person.setPortrait_id(person_obj.getInteger("portrait_id"));
                person.setPortrait_url(person_obj.getString("portrait_url"));
                family_list.add(person);
            }
            //memebers
            data_array = sub_obj.getJSONArray("data");
            if(data_array.size() == 0) break;
            for(i = 0; i < data_array.size(); i ++){
                person_array = data_array.getJSONObject(i).getJSONArray("person");
                if(person_array == null) break;
                for(j = 0; j < person_array.size(); j ++){
                    person_obj = person_array.getJSONObject(j);
                    Person person = new Person();
                    person.setGroup(Const.CONTACTS_GROUP_FAMILY);
                    person.setUser_id(person_obj.getLong("user_id"));
                    person.setName(person_obj.getString("name"));
                    person.setDevice_type(person_obj.getInteger("device_type"));
                    person.setSpell(person_obj.getString("spell"));
                    person.setPortrait_id(person_obj.getInteger("portrait_id"));
                    person.setPortrait_url(person_obj.getString("portrait_url"));
                    person.setBirthday(person_obj.getInteger("birthday"));
                    person.setGender(person_obj.getInteger("gender"));
                    person.setAuth(person_obj.getInteger("auth"));
                    phone_array = person_obj.getJSONArray("phone");
                    if(phone_array.size() > 0){
                        String phone[] = new String[phone_array.size()];
                        for(k = 0; k < phone_array.size(); k ++){
                            phone[k] = phone_array.getString(k);
                        }
                        person.setPhone(phone);
                    }

                    family_list.add(person);
                }
            }

            try {
                batchAddContact(family_list);
                //update version
                editor.putInt(Const.SP_KEY_FAMILY, json_version);
                write_flag = true;
            }catch(Exception e){
                e.printStackTrace();
            }

            break;
        }

        //handle friend
        if (root_obj.containsKey(Const.SP_KEY_FRIEND)){

        }

        //if(write_flag)
       //     editor.commit();
    }
    private void handleActionContactRefresh(String json_str) {
        JSONObject obj = JSON.parseObject(json_str);
        String update_type = obj.getString("type");

        SharedPreferences sp = getSharedPreferences(Const.SP_FILE_NAME, Context.MODE_PRIVATE);
        int version = 0;
        if(update_type.equals(Const.SP_KEY_CONTACT) || update_type.equals(Const.SP_KEY_FAMILY) || update_type.equals(Const.SP_KEY_FRIEND)){
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

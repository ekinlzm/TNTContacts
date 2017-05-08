package com.tinnotech.contacts;

import android.app.ActivityManager;
import android.app.IntentService;
import android.content.ContentProviderOperation;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.Context;
import android.content.OperationApplicationException;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.RemoteException;
import android.provider.ContactsContract;
import android.util.Log;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.Date;

public class ActionIntentService extends IntentService {
    public static final int ACTION_TYPE_INIT = -1;  //内部操作 开机初始化检查
    public static final int ACTION_TYPE_LOGIN = 1;
    public static final int ACTION_TYPE_BIND = 2;
    public static final int ACTION_TYPE_UNBIND = 3;
    public static final int ACTION_TYPE_COMMON_SETTING = 16;
    public static final int ACTION_TYPE_UPDATE = 20;
    public static final int ACTION_TYPE_REFRESH = 21;

    private static final String TAG = "ActionIntentService";
    private static final Long CACHE_CLEAN_INTERVAL = 7L * 24 * 3600 * 1000;

    public ActionIntentService() {
        super("ActionIntentService");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
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
        //raw id 没有重新开始计数, file下面 photos文件没有删除。 需要获取系统权限 清理用户数据
        Uri uri = Uri.parse("content://com.android.contacts/raw_contacts");
        getContentResolver().delete(uri,"_id!=-1", null);
        Log.e(TAG, "deleteAllContacts");
    }


    private void batchAddContact(ArrayList<Person> list)
            throws RemoteException, OperationApplicationException {
        ArrayList<ContentProviderOperation> ops = new ArrayList<>();
        int rawContactInsertIndex = 0, i;
        String phone_num[], portrait_url = null;


        for (Person person : list) {
            rawContactInsertIndex = ops.size(); // 有了它才能给真正的实现批量添加

            ops.add(ContentProviderOperation.newInsert(Const.RAW_CONTACTS_URI)
                    .withValue(Const.ACCOUNT_TYPE, null)
                    .withValue(Const.ACCOUNT_NAME, null)
                    .withYieldAllowed(true).build());

            // name
            ops.add(ContentProviderOperation
                    .newInsert(Const.DATA_URI)
                    .withValueBackReference(Const.RAW_CONTACT_ID, rawContactInsertIndex)
                    .withValue(Const.MIMETYPE, Const.NAME_ITEM_TYPE)
                    .withValue(Const.DISPLAY_NAME, person.getName())
                    .withYieldAllowed(true).build());

            // phone
            phone_num = person.getPhone();
            if(phone_num != null) {
                for(i = 0; i < phone_num.length; i ++) {
                    ops.add(ContentProviderOperation
                            .newInsert(Const.DATA_URI)
                            .withValueBackReference(Const.RAW_CONTACT_ID, rawContactInsertIndex)
                            .withValue(Const.MIMETYPE, Const.PHONE_ITEM_TYPE)
                            .withValue(Const.PHONE_NUMBER, phone_num[i])
                            .withValue(Const.PHONE_TYPE, Const.PHONE_TYPE_MOBILE)
                            .withYieldAllowed(true)
                            .build());
                }
            }

            // name, user_id, group, device_type, spell, portrait_url, portrait_id, birthday, gender, auth
            //所有的自定义字段都加到group MINE type里，方便查找
            ops.add(ContentProviderOperation
                    .newInsert(Const.DATA_URI)
                    .withValueBackReference(Const.RAW_CONTACT_ID, rawContactInsertIndex)
                    .withValue(Const.MIMETYPE, Const.CONTACT_ITEM_TYPE)
                    .withValue(Const.CONTACT_GROUP_ID, person.getGroup())
                    .withValue(Const.CONTACT_NAME, person.getName())
                    .withValue(Const.CONTACT_USER_ID, person.getUserId())
                    .withValue(Const.CONTACT_DEVICE_TYPE, person.getDeviceType())
                    .withValue(Const.CONTACT_SPELL, person.getSpell())
                    .withValue(Const.CONTACT_PORTRAIT_ID, person.getPortraitId())
                    .withValue(Const.CONTACT_PORTRAIT_URL, person.getPortraitUrl())
                    .withValue(Const.CONTACT_BIRTHDAY, person.getBirthday())
                    .withValue(Const.CONTACT_GENDER, person.getGender())
                    .withValue(Const.CONTACT_AUTH, person.getAuth())
                    .withYieldAllowed(true).build());

            //添加头像
            portrait_url = person.getPortraitUrl();
            if(portrait_url != null){
                String file_name = PortraitManagerService.getFileNameFromUrl(portrait_url);
                File portraitFile = new File(getDir(Const.portrait_cache_dir,Context.MODE_PRIVATE) + "/" + file_name);
                if(portraitFile.exists()){
                    byte[] photoData = null;
                    try {
                        FileInputStream io = new FileInputStream(portraitFile);
                        photoData = new byte[io.available()];
                        io.read(photoData);
                        io.close();
                        ops.add(ContentProviderOperation
                                .newInsert(Const.DATA_URI)
                                .withValueBackReference(Const.RAW_CONTACT_ID, rawContactInsertIndex)
                                .withValue(Const.MIMETYPE, Const.PORTRAIT_ITEM_TYPE)
                                .withValue(Const.PORTRAIT_PHOTO, photoData)
                                .withYieldAllowed(true).build());
                    }catch(Exception e){
                        e.printStackTrace();
                    }
                }
                else{

                    PortraitManagerService.addDownloadItem(portrait_url);
                }

            }

        }
        // 真正添加
        getContentResolver().applyBatch(ContactsContract.AUTHORITY, ops);
    }

    private void sendNetWorkMsg(int type, String json){
        Intent intent = new Intent(getApplicationContext(), NetworkService.class);
        intent.putExtra("type", type);
        intent.putExtra("json", json);
        startService(intent);
    }
    private void handleActionInit() {
        /* 查看电话本版本号， 如果所有的都为0， 清理电话本， 否则检查头像是否都已经下载完毕， 另外定期的清理一下头像缓存 */
        SharedPreferences sp = getSharedPreferences(Const.SP_FILE_NAME, Context.MODE_PRIVATE);
        final int contact_version = sp.getInt(Const.SP_KEY_CONTACT, 0);
        final int family_version = sp.getInt(Const.SP_KEY_FAMILY, 0);
        final int friend_version = sp.getInt(Const.SP_KEY_FRIEND, 0);
        if(contact_version + family_version + friend_version == 0) {
            ContentResolver cr = getContentResolver();
            Cursor cur = cr.query(ContactsContract.Contacts.CONTENT_URI,
                    null, null, null, null);
            if(cur != null) {
                int count = cur.getCount();
                cur.close();
                if (count != 0)
                    deleteAllContacts();
            }
        }else{
            //清理无用的头像，一周清理一次, 并下载缺失的
            boolean cleanUseless = false;
            Date dt = new Date();
            Long time = dt.getTime();
            final Long last_time = sp.getLong(Const.SP_KEY_PORTRAIT_CACHE_CLEAN, 0L);
            if(time - last_time >= CACHE_CLEAN_INTERVAL){
                SharedPreferences.Editor editor = sp.edit();
                editor.putLong(Const.SP_KEY_PORTRAIT_CACHE_CLEAN, time);
                editor.commit();
                cleanUseless = true;
            }
            PortraitManagerService.checkPortraitCache(getApplicationContext(), cleanUseless);
        }
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
        sendNetWorkMsg(ACTION_TYPE_UPDATE, obj.toString());
    }

    private void handleActionBind() {
        /* 绑定之后 需要主动上传版本号 */
        JSONObject obj = new JSONObject();
        obj.put(Const.SP_KEY_CONTACT, 0);
        obj.put(Const.SP_KEY_FAMILY, 0);
        obj.put(Const.SP_KEY_FRIEND, 0);
        sendNetWorkMsg(ACTION_TYPE_UPDATE, obj.toString());
        //创建头像cache目录
        File portraitCacheDir = getDir(Const.portrait_cache_dir,Context.MODE_PRIVATE);
    }

    private void handleActionUnBind() {
        //清理系统电话本
        deleteAllContacts();
        SharedPreferences sp = getSharedPreferences(Const.SP_FILE_NAME, Context.MODE_PRIVATE);
        if(sp != null){
            SharedPreferences.Editor editor = sp.edit();
            editor.clear();
            editor.commit();
        }
        //清空缓存目录；
        File cacheDir = getCacheDir();
        if (cacheDir.exists()) {
            for (File item : cacheDir.listFiles()) {
                item.delete();
            }
        }
        //清理头像缓存
        PortraitManagerService.clearDownloadList();
        File portraitCacheDir = getDir(Const.portrait_cache_dir,Context.MODE_PRIVATE);
        if (portraitCacheDir.exists()) {
            for (File item : portraitCacheDir.listFiles()) {
                item.delete();
            }
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
        //handle contact
        while (root_obj.containsKey(Const.SP_KEY_CONTACT)){
            sub_obj = root_obj.getJSONObject(Const.SP_KEY_CONTACT);
            json_version = sub_obj.getInteger("to_version");
            sp_version = sp.getInt(Const.SP_KEY_CONTACT, 0);
            if(sp_version >= json_version){
                Log.e(TAG, "update contacts[contact] local_version:" + sp_version + ", server_version:" + json_version);
                break;
            }

            ArrayList<Person> contact_list = new ArrayList<Person>();
            data_array = sub_obj.getJSONArray("data");
            if(data_array.size() == 0) break;
            for(i = 0; i < data_array.size(); i ++){
                person_array = data_array.getJSONObject(i).getJSONArray("person");
                if(person_array == null) break;
                for(j = 0; j < person_array.size(); j ++){
                    person_obj = person_array.getJSONObject(j);
                    Person person = new Person();
                    person.setGroup(Const.GROUP_ID_CONTACT);
                    person.setUserId(person_obj.getLong("user_id"));
                    person.setName(person_obj.getString("name"));
                    person.setDeviceType(person_obj.getInteger("device_type"));
                    person.setSpell(person_obj.getString("spell"));
                    person.setPortraitId(person_obj.getInteger("portrait_id"));
                    person.setPortraitUrl(person_obj.getString("portrait_url"));
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

                    contact_list.add(person);
                }
            }

            try {
                batchAddContact(contact_list);
                //update version
                editor.putInt(Const.SP_KEY_CONTACT, json_version);
                write_flag = true;
            }catch(Exception e){
                e.printStackTrace();
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
                person.setGroup(Const.GROUP_ID_FAMILY_PROFILE);
                person.setUserId(person_obj.getLong("family_id"));
                person.setName(person_obj.getString("name"));
                person.setSpell(person_obj.getString("spell"));
                person.setPortraitId(person_obj.getInteger("portrait_id"));
                person.setPortraitUrl(person_obj.getString("portrait_url"));
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
                    person.setGroup(Const.GROUP_ID_FAMILY);
                    person.setUserId(person_obj.getLong("user_id"));
                    person.setName(person_obj.getString("name"));
                    person.setDeviceType(person_obj.getInteger("device_type"));
                    person.setSpell(person_obj.getString("spell"));
                    person.setPortraitId(person_obj.getInteger("portrait_id"));
                    person.setPortraitUrl(person_obj.getString("portrait_url"));
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
        while (root_obj.containsKey(Const.SP_KEY_FRIEND)){
            sub_obj = root_obj.getJSONObject(Const.SP_KEY_FRIEND);
            json_version = sub_obj.getInteger("to_version");
            sp_version = sp.getInt(Const.SP_KEY_FRIEND, 0);
            if(sp_version >= json_version){
                Log.e(TAG, "update contacts[friend] local_version:" + sp_version + ", server_version:" + json_version);
                break;
            }

            ArrayList<Person> friend_list = new ArrayList<Person>();
            data_array = sub_obj.getJSONArray("data");
            if(data_array.size() == 0) break;
            for(i = 0; i < data_array.size(); i ++){
                person_array = data_array.getJSONObject(i).getJSONArray("person");
                if(person_array == null) break;
                for(j = 0; j < person_array.size(); j ++){
                    person_obj = person_array.getJSONObject(j);
                    Person person = new Person();
                    person.setGroup(Const.GROUP_ID_FRIEND);
                    person.setUserId(person_obj.getLong("user_id"));
                    person.setName(person_obj.getString("name"));
                    person.setDeviceType(person_obj.getInteger("device_type"));
                    person.setSpell(person_obj.getString("spell"));
                    person.setPortraitId(person_obj.getInteger("portrait_id"));
                    person.setPortraitUrl(person_obj.getString("portrait_url"));
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

                    friend_list.add(person);
                }
            }

            try {
                batchAddContact(friend_list);
                //update version
                editor.putInt(Const.SP_KEY_FRIEND, json_version);
                write_flag = true;
            }catch(Exception e){
                e.printStackTrace();
            }

            break;
        }

        if(write_flag) {
            editor.commit();
            //开始下载头像
            if(!PortraitManagerService.isDonwloadListEmpty()) {
                Intent intent = new Intent(this, PortraitManagerService.class);
                intent.putExtra("type", PortraitManagerService.ACTION_DOWNLAOD_PORTRAIT);
                startService(intent);
            }
        }
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
            sendNetWorkMsg(ACTION_TYPE_UPDATE, obj.toString());
        }
        else{
            Log.e(TAG, "handleActionContactRefresh type = " + update_type);
        }
    }

    //获取群组成员 for test
    public static void getContactsGroupMember(Context context, int[] groups){
        if((groups == null) ||(groups.length < 1)) return;
        String[] RAW_PROJECTION = new String[] {Const.RAW_CONTACT_ID, Const.CONTACT_NAME, Const.CONTACT_USER_ID};
        String RAW_CONTACTS_WHERE = Const.MIMETYPE
                + "='"
                + Const.CONTACT_ITEM_TYPE
                + "' and (" + Const.CONTACT_GROUP_ID
                + "=" + groups[0];
        for(int i = 1; i < groups.length; i ++){
            RAW_CONTACTS_WHERE += " or " + Const.CONTACT_GROUP_ID + "=" + groups[i];
         }
        RAW_CONTACTS_WHERE += ")";

        // 通过分组的id 查询得到RAW_CONTACT_ID, 并按姓名排序
        Cursor cursor = context.getContentResolver().query(
                Const.DATA_URI, RAW_PROJECTION,
                RAW_CONTACTS_WHERE, null, Const.CONTACT_NAME + " COLLATE LOCALIZED asc");

        // Second, query the corresponding name of the raw_contact_id
        if(cursor != null) {
            while (cursor.moveToNext()) {
                Log.e(TAG, "Member name is: " + cursor.getString(1) + ", user_id = " + cursor.getLong(2));
            }
            cursor.close();
        }
    }
}

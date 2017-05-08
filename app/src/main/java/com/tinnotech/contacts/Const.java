package com.tinnotech.contacts;

import android.net.Uri;
import android.provider.ContactsContract;

/**
 * Created by LZM on 2017/4/28.
 */

public class Const {
    /* SharedPreferences 文件名 */
    public static final String SP_FILE_NAME = "settings";

    /* 白名单群组版本号 str_key */
    public static final String SP_KEY_CONTACT = "contact";

    /* 家庭群组版本号 str_key */
    public static final String SP_KEY_FAMILY = "family";

    /* 好友群组版本号 str_key */
    public static final String SP_KEY_FRIEND = "friend";

    /* 运行陌生来电 str_key */
    public static final String SP_KEY_WHITE_LIST_ENABLE = "white_list_enabled";

    /* 头像缓存清理的时间戳 */
    public static final String SP_KEY_PORTRAIT_CACHE_CLEAN = "pottrait_cache_clean";

    /* 通讯录群组 家庭profile */
    public static final int GROUP_ID_FAMILY_PROFILE = 1;

    /* 通讯录群组 白名单成员 */
    public static final int GROUP_ID_CONTACT = 2;

    /* 通讯录群组 家庭成员 */
    public static final int GROUP_ID_FAMILY = 3;

    /* 通讯录群组 好友成员 */
    public static final int GROUP_ID_FRIEND = 4;

    /* 头像缓存目录 */
    public static final String portrait_cache_dir = "portrait_cache";

    /* database */
    public static final Uri RAW_CONTACTS_URI = ContactsContract.RawContacts.CONTENT_URI;
    public static final Uri DATA_URI = ContactsContract.Data.CONTENT_URI;

    public static final String ACCOUNT_TYPE = ContactsContract.RawContacts.ACCOUNT_TYPE;
    public static final String ACCOUNT_NAME = ContactsContract.RawContacts.ACCOUNT_NAME;

    public static final String RAW_CONTACT_ID = ContactsContract.Contacts.Data.RAW_CONTACT_ID;
    public static final String MIMETYPE = ContactsContract.Contacts.Data.MIMETYPE;

    public static final String NAME_ITEM_TYPE = ContactsContract.CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE;
    public static final String DISPLAY_NAME = ContactsContract.CommonDataKinds.StructuredName.DISPLAY_NAME;

    public static final String PHONE_ITEM_TYPE = ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE;
    public static final String PHONE_NUMBER = ContactsContract.CommonDataKinds.Phone.NUMBER;
    public static final String PHONE_TYPE = ContactsContract.CommonDataKinds.Phone.TYPE;
    public static final int PHONE_TYPE_MOBILE = ContactsContract.CommonDataKinds.Phone.TYPE_MOBILE;

    public static final String PORTRAIT_ITEM_TYPE = ContactsContract.CommonDataKinds.Photo.CONTENT_ITEM_TYPE;
    public static final String PORTRAIT_PHOTO = ContactsContract.CommonDataKinds.Photo.PHOTO;
    public static final String PORTRAIT_PHOTO_ID = ContactsContract.CommonDataKinds.Photo.PHOTO_FILE_ID;

    /* 所有自定义字段 除了电话号码都保存在系统group mine type里*/
    public static final String CONTACT_ITEM_TYPE = ContactsContract.CommonDataKinds.GroupMembership.CONTENT_ITEM_TYPE;
    public static final String CONTACT_GROUP_ID = ContactsContract.CommonDataKinds.GroupMembership.GROUP_ROW_ID;
    public static final String CONTACT_NAME = "data2";
    public static final String CONTACT_USER_ID = "data3";
    public static final String CONTACT_DEVICE_TYPE = "data4";
    public static final String CONTACT_SPELL = "data5";
    public static final String CONTACT_PORTRAIT_ID = "data6";
    public static final String CONTACT_PORTRAIT_URL = "data7";
    public static final String CONTACT_BIRTHDAY = "data8";
    public static final String CONTACT_GENDER = "data9";
    public static final String CONTACT_AUTH = "data10";

}

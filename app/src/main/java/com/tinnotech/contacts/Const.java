package com.tinnotech.contacts;

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

    /* 通讯录群组 家庭profile */
    public static final int CONTACTS_GROUP_FAMILY_PROFILE = 0;

    /* 通讯录群组 白名单成员 */
    public static final int CONTACTS_GROUP_CONTACT = 1;

    /* 通讯录群组 家庭成员 */
    public static final int CONTACTS_GROUP_FAMILY = 2;

    /* 通讯录群组 好友成员 */
    public static final int CONTACTS_GROUP_FRIEND = 3;

    /* 最多可以存储4个手机号 */
    public static final int MAX_PHONE_NUM = 4;

}

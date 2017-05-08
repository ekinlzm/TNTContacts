package com.tinnotech.contacts;

/**
 * Created by LZM on 2017/4/28.
 */

public class Person {
    private Long userId;// 唯一id
    private String name;// 姓名
    private int group; //所在群组
    private Integer deviceType;// 联系人的设备类型，家庭和好友群组必须携带比如 1:T1, 2:T2,3:M1,...1000:APP
    private String spell;// 姓名的拼音全拼，全大写,用来排序
    private String[] phone;// 号码数组,一个必填
    private String portraitUrl;// 头像url, 非必须
    private Integer portraitId;// 内置头像id，用于某些不支持头像下载的项目。
    private Integer birthday;// 生日，friend和family群组里的设备成员必须携带。用于交友显示。格式为YYYYMMDD
    private Integer gender;// 性别， friend和family群组里的设备成员必须携带。用于交友显示。0-女 1-男
    private Integer auth;// 操作权限mask，通过权限列表按位或获得

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getGroup() {
        return group;
    }

    public void setGroup(int group) {
        this.group = group;
    }

    public Integer getDeviceType() {
        return deviceType;
    }

    public void setDeviceType(Integer deviceType) {
        this.deviceType = deviceType;
    }

    public String getSpell() {
        return spell;
    }

    public void setSpell(String spell) {
        this.spell = spell;
    }

    public String[] getPhone() {
        return phone;
    }

    public void setPhone(String[] phone) {
        this.phone = phone;
    }

    public String getPortraitUrl() {
        return portraitUrl;
    }

    public void setPortraitUrl(String portraitUrl) {
        this.portraitUrl = portraitUrl;
    }

    public Integer getPortraitId() {
        return portraitId;
    }

    public void setPortraitId(Integer portraitId) {
        this.portraitId = (portraitId == null) ? 0 : portraitId;
    }

    public Integer getBirthday() {
        return birthday;
    }

    public void setBirthday(Integer birthday) {
        this.birthday = (birthday == null) ? 0 : birthday;
    }

    public Integer getGender() {
        return gender;
    }

    public void setGender(Integer gender) {
        this.gender = (gender == null) ? -1 : gender;
    }

    public Integer getAuth() {
        return auth;
    }

    public void setAuth(Integer auth) {
        this.auth = auth;
    }
}

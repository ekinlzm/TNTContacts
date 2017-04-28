package com.tinnotech.contacts;

/**
 * Created by LZM on 2017/4/28.
 */

public class Person {
    private Long user_id;// 唯一id
    private String name;// 姓名
    private Integer group; //所在群组
    private Integer device_type;// 联系人的设备类型，家庭和好友群组必须携带比如 1:T1, 2:T2,3:M1,...1000:APP
    private String spell;// 姓名的拼音全拼，全大写,用来排序
    private String[] phone;// 号码数组,一个必填
    private String portrait_url;// 头像url, 非必须
    private Integer portrait_id;// 内置头像id，用于某些不支持头像下载的项目。
    private Integer birthday;// 生日，friend和family群组里的设备成员必须携带。用于交友显示。格式为YYYYMMDD
    private Integer gender;// 性别， friend和family群组里的设备成员必须携带。用于交友显示。0-女 1-男
    private Integer auth;// 操作权限mask，通过权限列表按位或获得

    public Long getUser_id() {
        return user_id;
    }

    public void setUser_id(Long user_id) {
        this.user_id = user_id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getGroup() {
        return group;
    }

    public void setGroup(Integer group) {
        this.group = group;
    }

    public Integer getDevice_type() {
        return device_type;
    }

    public void setDevice_type(Integer device_type) {
        this.device_type = device_type;
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

    public String getPortrait_url() {
        return portrait_url;
    }

    public void setPortrait_url(String portrait_url) {
        this.portrait_url = portrait_url;
    }

    public Integer getPortrait_id() {
        return portrait_id;
    }

    public void setPortrait_id(Integer portrait_id) {
        this.portrait_id = portrait_id;
    }

    public Integer getBirthday() {
        return birthday;
    }

    public void setBirthday(Integer birthday) {
        this.birthday = birthday;
    }

    public Integer getGender() {
        return gender;
    }

    public void setGender(Integer gender) {
        this.gender = gender;
    }

    public Integer getAuth() {
        return auth;
    }

    public void setAuth(Integer auth) {
        this.auth = auth;
    }
}

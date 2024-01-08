package com.stockholmiot.proxyguide.ui.signin.models;

import com.google.firebase.firestore.IgnoreExtraProperties;

/**
 * User POJO.
 */
@IgnoreExtraProperties
public class User {

    public static final String FIELD_TOKEN = "token";
    public static final String FIELD_BIRTHDAY = "birthday";
    public static final String FIELD_USERNAME = "username";
    public static final String PROFILE_URL = "profile_url";
    public static final String PROFILE = "profile";

    private String token;
    private String birthday;
    private String username;
    private String profileUrl;
    private String profile;
    private String gender;
    private String address;
    private String phone;
    private String uid;

    private boolean isNearby;

    public User() {
    }

    public User(String uid) {
        this.uid = uid;
    }

    public User(String token, String birthday, String username,
                String profileUrl, String profile, String gender,
                String address, String phone, String uid, boolean isNearby) {

        this.token = token;
        this.birthday = birthday;
        this.username = username;
        this.profileUrl = profileUrl;
        this.profile = profile;
        this.gender = gender;
        this.address = address;
        this.phone = phone;
        this.uid = uid;
        this.isNearby = isNearby;
    }

    public User(String token, String birthday, String username,
                String profileUrl, String profile, String gender,
                String address, String phone, boolean isNearby) {

        this.token = token;
        this.birthday = birthday;
        this.username = username;
        this.profileUrl = profileUrl;
        this.profile = profile;
        this.gender = gender;
        this.address = address;
        this.phone = phone;
        this.isNearby = isNearby;
    }

    public User(String token, String birthday, String username, String profileUrl, String profile) {
        this.token = token;
        this.birthday = birthday;
        this.username = username;
        this.profileUrl = profileUrl;
        this.profile = profile;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getBirthday() {
        return birthday;
    }

    public void setBirthday(String birthday) {
        this.birthday = birthday;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getProfileUrl() {
        return profileUrl;
    }

    public void setProfileUrl(String profileUrl) {
        this.profileUrl = profileUrl;
    }

    public String getProfile() {
        return profile;
    }

    public void setProfile(String profile) {
        this.profile = profile;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public boolean isNearby() {
        return isNearby;
    }

    public void setNearby(boolean nearby) {
        isNearby = nearby;
    }
}

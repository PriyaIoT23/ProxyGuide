package com.stockholmiot.proxyguide.ui.home.models;

import com.google.firebase.firestore.GeoPoint;
import com.google.firebase.firestore.IgnoreExtraProperties;

import java.io.Serializable;

@IgnoreExtraProperties
public class PoIModel  implements Serializable {
    public static final String CiTY_TYPE = "city";
    public static final String COUNTRY = "country";
    public static final String TITTLE = "name";
    public static final String ADDRESS = "address";

    private String address;
    private String description;
    private GeoPoint latitude;
    private String photo_url;
    private String name;
    private String poiUid;
    private String country;
    private String city;
    private int number_views;

    public PoIModel() {
    }

    public PoIModel(String address, String description,  GeoPoint latitude,
                    String photo_url, String name, String poiUid,
                    String country, String city, int number_views) {

        this.address = address;
        this.description = description;
        this.latitude = latitude;
        this.photo_url = photo_url;
        this.name = name;
        this.poiUid = poiUid;
        this.country = country;
        this.city = city;
        this.number_views = number_views;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public GeoPoint getLatitude() {
        return latitude;
    }

    public void setLatitude(GeoPoint latitude) {
        this.latitude = latitude;
    }

    public String getPhoto_url() {
        return photo_url;
    }

    public void setPhoto_url(String photo_url) {
        this.photo_url = photo_url;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPoiUid() {
        return poiUid;
    }

    public void setPoiUid(String poiUid) {
        this.poiUid = poiUid;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public int getNumber_views() {
        return number_views;
    }

    public void setNumber_views(int number_views) {
        this.number_views = number_views;
    }


}

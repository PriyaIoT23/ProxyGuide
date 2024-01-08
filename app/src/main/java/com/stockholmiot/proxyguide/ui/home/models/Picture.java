package com.stockholmiot.proxyguide.ui.home.models;

import java.io.Serializable;

public class Picture implements Serializable {

    private String url;

    public Picture() {
    }

    public Picture(String url) {
        this.url = url;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }
}

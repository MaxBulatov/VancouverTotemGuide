package com.example.travelguide;

public class Totem {
    private String siteName;
    private String imageURL;
    private double latC;
    private double lonC;

    public Totem(String name, double lat, double lon){
        this.siteName = name;
        this.latC = lat;
        this.lonC = lon;
    }

    public Totem(String name, double lat, double lon, String url){
        this.siteName = name;
        this.latC = lat;
        this.lonC = lon;
        this.imageURL = url;
    }

    public String getSiteName() {
        return siteName;
    }

    public double getLatC() {
        return latC;
    }

    public double getLonC() {
        return lonC;
    }

    public void setSiteName(String siteName) {
        this.siteName = siteName;
    }

    public void setLatC(double latC) {
        this.latC = latC;
    }

    public void setLonC(double lonC) {
        this.lonC = lonC;
    }

    public String getImageURL() {
        return imageURL;
    }

    public void setImageURL(String imageURL) {
        this.imageURL = imageURL;
    }

}


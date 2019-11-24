package com.coolweather.android.gson.suggestion;

import com.google.gson.annotations.SerializedName;

public class Basic {

    @SerializedName("cid")
    public String weatherId;

    @SerializedName("location")
    public  String cityName;
}

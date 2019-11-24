package com.coolweather.android.gson.today;

import com.google.gson.annotations.SerializedName;

public class Now {

    public String cond_txt;//天气状况

    public String wind_dir;//风向

    public String wind_sc;//风力

    @SerializedName("tmp")
    public String temperature;



}

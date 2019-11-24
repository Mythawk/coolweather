package com.coolweather.android.gson.suggestion;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class HeWeatherSuggest {

    public Basic basic;

    public String status;

    @SerializedName("lifestyle")
    public List<Lifestyle> lifestyleList;

}

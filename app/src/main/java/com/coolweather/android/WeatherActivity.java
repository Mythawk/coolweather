package com.coolweather.android;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.coolweather.android.gson.suggestion.HeWeatherSuggest;
import com.coolweather.android.gson.suggestion.Lifestyle;
import com.coolweather.android.gson.today.HeWeatherNow;
import com.coolweather.android.service.AutoUpdateService;
import com.coolweather.android.util.HttpUtil;
import com.coolweather.android.util.Utility;

import java.io.IOException;
import java.net.HttpURLConnection;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.HttpUrl;
import okhttp3.Response;

public class WeatherActivity extends AppCompatActivity {

    private ScrollView weatherLayout;
    private TextView tittleCity;
    private TextView titleUpdateTime;
    private TextView degreeText;
    private TextView weatherInfoText;
    private LinearLayout forecastLayout;
    private TextView aqiText;
    private TextView pm25Text;
    private TextView comfortText;
    private TextView carWashText;
    private TextView sportText;
    private ImageView bingPicImg;
    public SwipeRefreshLayout switchRefresh;
    public DrawerLayout drawerLayout;
    private Button navButton;
    private String mWeatherId;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (Build.VERSION.SDK_INT >=21){//对版本进行判断，进行不同的处理方式
            View decorView =getWindow().getDecorView();
            decorView.setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_FULLSCREEN | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
            );
            getWindow().setStatusBarColor(Color.TRANSPARENT);
        }
        setContentView(R.layout.activity_weather);
        //初始化各控件
        bingPicImg = (ImageView)findViewById(R.id.bing_pic_img);
        weatherLayout = (ScrollView)findViewById(R.id.weather_layout);
        tittleCity = (TextView)findViewById(R.id.title_city);
        titleUpdateTime = (TextView)findViewById(R.id.title_update_time);
        degreeText = (TextView)findViewById(R.id.degree_text);
        weatherInfoText = (TextView)findViewById(R.id.weather_info_text);
        forecastLayout = (LinearLayout)findViewById(R.id.forecast_layout);
        aqiText = (TextView)findViewById(R.id.aqi_text);
        pm25Text = (TextView)findViewById(R.id.pm25_text);
        comfortText = (TextView)findViewById(R.id.comfor_text);
        carWashText = (TextView)findViewById(R.id.cat_wash_text);
        sportText = (TextView)findViewById(R.id.sport_text);
        switchRefresh = (SwipeRefreshLayout)findViewById(R.id.swipe_refresh);
        switchRefresh.setColorSchemeResources(R.color.colorPrimary);
        drawerLayout = (DrawerLayout)findViewById(R.id.drawer_layout);
        navButton = (Button)findViewById(R.id.nav_button);


//        Intent intent = new Intent(this, AutoUpdateService.class);
//        startService(intent);

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        String weatherString = prefs.getString("weather",null);
        String weatherIdForm = getIntent().getStringExtra("weather_id");
        String weatherSug = prefs.getString("suggestion",null);
        if (weatherString !=null && weatherIdForm !=null ){
            //有缓存时直接解析天气数据
            HeWeatherNow weather = Utility.handleWeatherResponse(weatherString);
            //HeWeatherSuggest suggest = Utility.handleWeatherSugResponse(weatherSug);
            mWeatherId = weather.basic.weatherId;
            showWeatherInfo(weather);
            //showSuggestion(suggest);
        }else{
            //没有缓存时去服务器查询天气
            mWeatherId = getIntent().getStringExtra("weather_id");
            weatherLayout.setVisibility(View.INVISIBLE);
            requestWeather(mWeatherId);
            //requestSugesttion(mWeatherId);

        }

        switchRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                mWeatherId = getIntent().getStringExtra("weather_id");
                requestWeather(mWeatherId);
                //requestSugesttion(mWeatherId);
            }
        });

        String bingPic = prefs.getString("bing_pic",null);
        if (bingPic != null){
            Glide.with(this).load(bingPic).into(bingPicImg);
        }else {
            loadBingPic();
        }

        navButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                drawerLayout.openDrawer(GravityCompat.START);
            }
        });

    }



    //根据天气id请求城市天气消息
    public void requestWeather(String weatherId) {
        String weatherUrl = "https://free-api.heweather.net/s6/weather/now?location=" + weatherId + "&key=275bf4c8500f4c41a2901261c58696f6";
        HttpUtil.sendOkHttpRequest(weatherUrl, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(WeatherActivity.this, "获取天气信息失败", Toast.LENGTH_SHORT).show();
                        switchRefresh.setRefreshing(false);
                    }
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                final String responseText = response.body().string();
                final HeWeatherNow weatherNow = Utility.handleWeatherResponse(responseText);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (weatherNow != null && "ok".equals(weatherNow.status)){
                            SharedPreferences.Editor editor = PreferenceManager.
                                    getDefaultSharedPreferences(WeatherActivity.this).edit();
                            editor.putString("weather",responseText);
                            editor.apply();
                            mWeatherId = weatherNow.basic.weatherId;
                            showWeatherInfo(weatherNow);
                        }else {
                            Toast.makeText(WeatherActivity.this, "获取天气信息失败", Toast.LENGTH_SHORT).show();
                        }
                        switchRefresh.setRefreshing(false);
                    }
                });
            }
        });
        loadBingPic();

    }


    public void requestSugesttion(String weatherId){

        String weatherUrlSug = "https://free-api.heweather.net/s6/weather/lifestyle?location=" + weatherId + "&key=275bf4c8500f4c41a2901261c58696f6";
        HttpUtil.sendOkHttpRequest(weatherUrlSug, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(WeatherActivity.this, "获取建议信息失败", Toast.LENGTH_SHORT).show();
                        switchRefresh.setRefreshing(false);
                    }
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                final String responseSugText = response.body().string();
                final HeWeatherSuggest heWeatherSuggest  = Utility.handleWeatherSugResponse(responseSugText);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (heWeatherSuggest != null && "ok".equals(heWeatherSuggest.status)){
                            SharedPreferences.Editor editor = PreferenceManager.
                                    getDefaultSharedPreferences(WeatherActivity.this).edit();
                            editor.putString("suggestion", responseSugText );
                            editor.apply();
                            showSuggestion(heWeatherSuggest);
                        }else {
                            Toast.makeText(WeatherActivity.this, "获取天气信息失败", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        });

    }


    /***
     * 在这里更新城市名称和现在温度
     * @param weatherNew
     */
    private void showWeatherInfo(HeWeatherNow weatherNew) {
        String cityName = weatherNew.basic.cityName;
        String updateTime = weatherNew.update.updateTime.split(" ")[1];
        String degree = weatherNew.now.temperature+"℃";
        String weatherInfo = weatherNew.now.cond_txt;
        tittleCity.setText(cityName);
        titleUpdateTime.setText(updateTime);
        degreeText.setText(degree);
        weatherInfoText.setText(weatherInfo);
        //forecastLayout.removeAllViews();
//        for (Forecast forecast : weatherNew.forecastList){
//            View view = LayoutInflater.from(this).inflate(R.layout.forecast_item,forecastLayout,false);
//            TextView dataText = (TextView)view.findViewById(R.id.date_text);
//            TextView infoText = (TextView)view.findViewById(R.id.info_text);
//            TextView maxText = (TextView)view.findViewById(R.id.max_text);
//            TextView minText = (TextView)view.findViewById(R.id.min_text);
//            dataText.setText(forecast.date);
//            infoText.setText(forecast.more.info);
//            maxText.setText(forecast.temperature.max);
//            minText.setText(forecast.temperature.min);
//            forecastLayout.addView(view);
//        }
//        if (weatherNew.aqi != null){
//            aqiText.setText(weatherNew.aqi.city.aqi);
//            pm25Text.setText(weatherNew.aqi.city.pm25);
//        }

        weatherLayout.setVisibility(View.VISIBLE);

    }

    /**
     * 在这里更新建议
     * @param weatherSug
     */
    private void  showSuggestion(HeWeatherSuggest weatherSug){
        String comfort = "" ;
        String carWash = "" ;
        String sport  = "";
        for (Lifestyle lifestyle :weatherSug.lifestyleList){
            while    (lifestyle.type.equals("comf")){
                 comfort = "舒适度:" + lifestyle.txt;
            }
            while (lifestyle.txt.equals("cw")){
                 carWash = "洗车指数:" + lifestyle.txt;
            }
            while (lifestyle.txt.equals("sport")){
                 carWash = "运动建议:" + lifestyle.txt;
            }

        }
        comfortText.setText(comfort);
        carWashText.setText(carWash);
        sportText.setText(sport);

    }




    /**
     * 加载必应每日一图
     */

    private void loadBingPic() {
        String requestBingPic = "http://guolin.tech/api/bing_pic";
        HttpUtil.sendOkHttpRequest(requestBingPic, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                final String bingPic = response.body().string();
                SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(WeatherActivity.this).edit();
                editor.putString("bing_pic",bingPic);
                editor.apply();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Glide.with(WeatherActivity.this).load(bingPic).into(bingPicImg);
                    }
                });
            }
        });
    }
}

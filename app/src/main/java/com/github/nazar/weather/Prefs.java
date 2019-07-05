package com.github.nazar.weather;

import android.content.Context;
import android.content.SharedPreferences;
import android.text.TextUtils;

import com.github.nazar.weather.data.WeatherResponse;
import com.google.gson.Gson;

public class Prefs {

    private static final String KEY_CITY = "city";
    private static final String KEY_CURRENT = "last_current";
    private static final String KEY_FORECAST = "last_forecast";

    private static Prefs instance;
    private SharedPreferences preferences;

    public static Prefs getInstance(Context context) {
        if (instance == null) {
            instance = new Prefs(context);
        }
        return instance;
    }

    private Prefs(Context context) {
        preferences = context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE);
    }

    public WeatherResponse getCurrentWeather() {
        String json = preferences.getString(KEY_CURRENT, "");
        if (TextUtils.isEmpty(json)) return null;
        try {
            return new Gson().fromJson(json, WeatherResponse.class);
        } catch (Exception e) {
            return null;
        }
    }

    public void saveCurrentWeater(WeatherResponse weatherResponse) {
        preferences.edit().putString(KEY_CURRENT, new Gson().toJson(weatherResponse)).commit();
    }

    public String lastCity() {
        return preferences.getString(KEY_CITY, "");
    }

    public void saveCity(String value) {
        preferences.edit().putString(KEY_CITY, value).commit();
    }
}

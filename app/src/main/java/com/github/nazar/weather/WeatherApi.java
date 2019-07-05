package com.github.nazar.weather;

public class WeatherApi {

    private static final String API_KEY = "579f16af42abe34db36f17877a3dbdfc";

    public static String nowUrl(String q) {
        return String.format("https://api.openweathermap.org/data/2.5/weather?q=%s&APPID=%s&units=metric", q, API_KEY);
    }

    public static String forecastUrl(String q) {
        return String.format("https://api.openweathermap.org/data/2.5/forecast?q=%s&APPID=%s&units=metric", q, API_KEY);
    }
}

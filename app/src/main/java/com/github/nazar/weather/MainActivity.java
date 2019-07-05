package com.github.nazar.weather;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import com.github.nazar.weather.data.Main;
import com.github.nazar.weather.data.Sys;
import com.github.nazar.weather.data.Weather;
import com.github.nazar.weather.data.WeatherResponse;
import com.github.nazar.weather.data.Wind;
import com.google.gson.Gson;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URI;

public class MainActivity extends Activity {

    private static final String TAG = "MainActivity";

    private View progressView;
    private EditText cityName;

    private TextView nameView;
    private TextView summaryView;
    private TextView temperatureView;
    private TextView humidityView;
    private TextView pressureView;
    private TextView windView;

    private final Handler handler = new Handler(Looper.getMainLooper());

    private Callback<WeatherResponse> mNowResponse = new Callback<WeatherResponse>() {
        @Override
        public void success(WeatherResponse weatherResponse) {
            if (weatherResponse != null) {
                Log.d(TAG, "onResponse: " + weatherResponse);
                Prefs.getInstance(MainActivity.this).saveCurrentWeater(weatherResponse);
                showWeather(weatherResponse);
            } else {
                showSavedWeather(true);
            }
        }

        @Override
        public void failure(int code, String message) {
            Log.d(TAG, "failure: " + code + ", " + message);
            showSavedWeather(true);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        nameView = (TextView) findViewById(R.id.nameView);
        summaryView = (TextView) findViewById(R.id.summaryView);
        temperatureView = (TextView) findViewById(R.id.temperatureView);
        humidityView = (TextView) findViewById(R.id.humidityView);
        pressureView = (TextView) findViewById(R.id.pressureView);
        windView = (TextView) findViewById(R.id.windView);

        cityName = (EditText) findViewById(R.id.cityName);
        cityName.setText(Prefs.getInstance(this).lastCity());

        ImageButton searchButton = (ImageButton) findViewById(R.id.searchButton);
        searchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                loadNowWeather();
            }
        });

        progressView = findViewById(R.id.progressView);
        progressView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
            }
        });

        updateProgress(false);
        showSavedWeather(false);
    }

    private void loadNowWeather() {
        String city = getCityName();
        if (TextUtils.isEmpty(city)) {
            return;
        }
        Prefs.getInstance(this).saveCity(city);
        makeRequest(WeatherApi.nowUrl(city), WeatherResponse.class, mNowResponse);
    }

    @Override
    protected void onStart() {
        super.onStart();
        loadNowWeather();
    }

    private void showSavedWeather(boolean showError) {
        WeatherResponse weatherResponse = Prefs.getInstance(this).getCurrentWeather();
        if (weatherResponse != null) {
            showWeather(weatherResponse);
        } else {
            if (showError) {
                nameView.setText(getCityName());
                summaryView.setText("Error");
            }
        }
    }

    private void showWeather(WeatherResponse weatherResponse) {
        if (weatherResponse != null) {
            Sys sys = weatherResponse.getSys();
            if (sys != null) {
                nameView.setText(weatherResponse.getName() + ", " + sys.getCountry());
            } else {
                nameView.setText(weatherResponse.getName());
            }
            Weather weather = null;
            if (weatherResponse.getWeather().size() > 0) {
                weather = weatherResponse.getWeather().get(0);
            }
            if (weather != null) {
                summaryView.setText(weather.getMain() + ", " + weather.getDescription());
            } else {
                summaryView.setText("N/a");
            }
            Main main = weatherResponse.getMain();
            if (main != null) {
                temperatureView.setText(main.getTemp() + "°C");
                humidityView.setText(String.valueOf(main.getHumidity()));
                pressureView.setText(String.valueOf(main.getPressure()));
            } else {
                temperatureView.setText("N/a");
                humidityView.setText("N/a");
                pressureView.setText("N/a");
            }
            Wind wind = weatherResponse.getWind();
            if (wind != null) {
                windView.setText(wind.getSpeed() + "m/s");
            } else {
                windView.setText("N/a");
            }
        }
    }

    private String getCityName() {
        return cityName.getText().toString().trim();
    }

    private void updateProgress(boolean visible) {
        if (visible) {
            progressView.setVisibility(View.VISIBLE);
        } else {
            progressView.setVisibility(View.GONE);
        }
    }

    private <T> void makeRequest(final String url, final Class<T> tClass, final Callback<T> callback) {
        Log.d(TAG, "makeRequest: " + url);
        updateProgress(true);
        new Thread(new Runnable() {
            @Override
            public void run() {
                BufferedReader in;
                try {
                    HttpClient client = new DefaultHttpClient();
                    URI website = new URI(url);
                    HttpGet request = new HttpGet();
                    request.setURI(website);
                    HttpResponse response = client.execute(request);
                    response.getStatusLine().getStatusCode();

                    in = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
                    StringBuilder sb = new StringBuilder("");
                    String l;
                    while ((l = in.readLine()) != null) {
                        sb.append(l);
                    }
                    in.close();
                    String json = sb.toString();
                    Log.d(TAG, "run: " + json);
                    final T t = new Gson().fromJson(json, tClass);

                    hideProgress();
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            if (callback != null) {
                                callback.success(t);
                            }
                        }
                    });
                } catch (final Exception e) {
                    hideProgress();
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            if (callback != null) {
                                callback.failure(500, e.getMessage());
                            }
                        }
                    });
                }

            }
        }).start();
    }

    private void hideProgress() {
        handler.post(new Runnable() {
            @Override
            public void run() {
                updateProgress(false);
            }
        });
    }

    private interface Callback<T> {
        void success(T t);

        void failure(int code, String message);
    }
}

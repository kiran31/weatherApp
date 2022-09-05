package com.example.weatherapplication;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.koushikdutta.async.future.FutureCallback;
import com.koushikdutta.ion.Ion;

import java.util.ArrayList;
import java.util.List;


public class MainActivity extends AppCompatActivity {

    private static final String API_KEY = "9bfd4bb468b4a199b9ec51050049eb2c";

    TextView tvTemp, tvCity;
    ListView listView;
    ImageView imgTemp;
    EditText etSearch;
    Button btnSearch;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        tvTemp = findViewById(R.id.tvTemp);
        tvCity = findViewById(R.id.tvCity);
        listView = findViewById(R.id.listView);
        imgTemp = findViewById(R.id.imgWhether);
        etSearch = findViewById(R.id.etCityName);
        btnSearch = findViewById(R.id.btnSearch);

        btnSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String city = etSearch.getText().toString();
                if(city.isEmpty()){
                    Toast.makeText(MainActivity.this, "Enter City name", Toast.LENGTH_SHORT).show();
                }else
                {
                    loadWeatherInfo(city);
                }
            }
        });
    }

    private void loadWeatherInfo(String city) {
        Ion.with(this)
                .load("http://api.openweathermap.org/data/2.5/weather?q="+city+"&&units=metric&appid="+API_KEY)
                .asJsonObject()
                .setCallback(new FutureCallback<JsonObject>() {
                    @Override
                    public void onCompleted(Exception e, JsonObject result) {


                        if(e != null){
                            e.printStackTrace();
                            Toast.makeText(MainActivity.this, "Server error", Toast.LENGTH_SHORT).show();
                        }else{
                            Log.d("weather>>>",result.toString());
                            JsonObject main = result.get("main").getAsJsonObject();
                            double temp = main.get("temp").getAsDouble();
                            tvTemp.setText(temp+" C");

                            JsonObject sys = result.get("sys").getAsJsonObject();
                            String country = sys.get("country").getAsString();
                            tvCity.setText(city+" ,"+country);

                            Ion.with(imgTemp)
                                    .placeholder(R.drawable.weather)
                                    .error(R.drawable.weather)
                                    .load("https://openweathermap.org/img/w/02d.png");

                            JsonObject coord = result.get("coord").getAsJsonObject();
                            double lon = coord.get("lon").getAsDouble();
                            double lat = coord.get("lat").getAsDouble();

                            loadDailyForecast(lon, lat);
                        }
                    }
                });
    }

    private void loadDailyForecast(double lon, double lat) {

        String apiUrl = "http://api.openweathermap.org/data/2.5/onecall?lat="+lat+"&lon="+lon+"&exclude=hourly,minutely,current&units=metric&appid="+API_KEY;
        Ion.with(this)
                .load(apiUrl)
                .asJsonObject()
                .setCallback(new FutureCallback<JsonObject>() {
                    @Override
                    public void onCompleted(Exception e, JsonObject result) {
                        if(e != null){
                            e.printStackTrace();
                            Toast.makeText(MainActivity.this, "Server error", Toast.LENGTH_SHORT).show();
                        }else{
                            Log.d("weather>>>",result.toString());

                            List<Weather> weatherList = new ArrayList<>();
                            String timeZone = result.get("timezone").getAsString();
                            JsonArray daily = result.get("daily").getAsJsonArray();

                            for(int i=0;i<daily.size();i++){
                                Long date = daily.get(i).getAsJsonObject().get("dt").getAsLong();
                                Double temp = daily.get(i).getAsJsonObject().get("temp").getAsJsonObject().get("day").getAsDouble();
                                String  icon = daily.get(i).getAsJsonObject().get("weather").getAsJsonArray().get(0).getAsJsonObject().get("icon").getAsString();
                                weatherList.add(new Weather(date, timeZone, temp, icon));
                            }

                            DailyWeatherAdapter adapter = new DailyWeatherAdapter(MainActivity.this, weatherList);
                            listView.setAdapter(adapter);
                        }
                    }
                });
    }
}
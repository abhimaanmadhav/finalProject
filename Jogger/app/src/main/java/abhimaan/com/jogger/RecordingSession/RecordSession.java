package abhimaan.com.jogger.RecordingSession;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.util.Pair;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Chronometer;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;

import abhimaan.com.jogger.Configration.Configuration;
import abhimaan.com.jogger.GoogleServices.GooglePlayConnection;
import abhimaan.com.jogger.HomeScreen;
import abhimaan.com.jogger.JogSessionService.JogSessionService;
import abhimaan.com.jogger.R;
import abhimaan.com.jogger.meter.Meter;
import abhimaan.com.jogger.utils.Logger;
import abhimaan.com.jogger.utils.Utils;

public class RecordSession extends AppCompatActivity
{
    TextView mDistance, mSpeed;
    UpdateReciver mUpdateReciver;
    private boolean displayWeather = false;

    Meter mHumidityMeter;


    @Override
    protected void onCreate(Bundle savedInstanceState)
        {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_record_session);
            ((Chronometer) findViewById(R.id.stop_watch)).start();
            mDistance = (TextView) findViewById(R.id.distance_covered);
            mDistance.setText(Utils.getDisplayDistance(this, 0));
            mSpeed = (TextView) findViewById(R.id.avg_speed);
            findViewById(R.id.stop).setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View v)
                    {
                        Intent serviceIntent = new Intent(RecordSession.this, JogSessionService
                                .class);
                        GooglePlayConnection.stopLocationUpdate(RecordSession.this);
                        serviceIntent.setAction(JogSessionService.END_SESSION);
                        startService(serviceIntent);
                        Intent intent = new Intent(RecordSession.this, HomeScreen.class);
                        ActivityOptionsCompat options = ActivityOptionsCompat
                                .makeSceneTransitionAnimation(
                                        RecordSession.this,
                                        new Pair<View, String>(v,
                                                getString(R.string.distance)));
                        ActivityCompat.startActivity(RecordSession.this, intent, options.toBundle
                                ());
                        finish();
                    }
            });
            GooglePlayConnection.LocationRequestState playConnectionState = GooglePlayConnection
                    .startLocationUpdate(RecordSession.this);
            if (playConnectionState == GooglePlayConnection.LocationRequestState.CONNECTION_ERROR)
                {
                    Logger.d(this, "google play error");
                    GooglePlayConnection.resolveError(this);
                } else
                {
                    Logger.d(this, "google play status" + playConnectionState);
                }
            mUpdateReciver = new UpdateReciver();
            mHumidityMeter = (Meter) findViewById(R.id.humidity);
            IntentFilter filter = new
                    IntentFilter(JogSessionService.ACTION_SESSION_INFO);
            filter.addAction(GooglePlayConnection.GOOGLE_PLAY_CONNECTED);
            LocalBroadcastManager.getInstance(this).registerReceiver(mUpdateReciver, filter);
        }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
        {
            GooglePlayConnection.onActivityResult(requestCode, resultCode);
            super.onActivityResult(requestCode, resultCode, data);
        }


    private class UpdateReciver extends BroadcastReceiver
    {

        @Override
        public void onReceive(Context context, Intent intent)
            {
                if (intent == null)
                    {
                        return;
                    }
                switch (intent.getAction())
                    {
                        case JogSessionService.ACTION_SESSION_INFO:
                        {
                            mDistance.setText(Utils.getDisplayDistance(context, intent.getFloatExtra
                                    (JogSessionService
                                            .SESSION_DISTANCE, 0)));
                            mSpeed.setText(Utils.getDisplayspeed(intent.getFloatExtra
                                    (JogSessionService
                                            .SESSION_SPEED, 0)));
                            if (!displayWeather && Utils.isOnline(context))
                                {
                                    Logger.d(this, "lat long for weather " + ((Location) intent
                                            .getParcelableExtra(JogSessionService
                                                    .LOCATION_LAST_KNOW)));
                                    new WeatherApi((Location) intent.getParcelableExtra
                                            (JogSessionService
                                                    .LOCATION_LAST_KNOW)).execute();
                                }
                            break;
                        }
                        case GooglePlayConnection.GOOGLE_PLAY_CONNECTED:
                            GooglePlayConnection
                                    .startLocationUpdate(RecordSession.this);
                            break;
                    }
            }
    }

    private class WeatherApi extends AsyncTask<Object, Object, WeatherModel>
    {
        private final String URL = "http://api.openweathermap.org/data/2" +
                ".5/weather?units=metric&" + "appid=" + Configuration.KEY;

        private Location location;


        public WeatherApi(Location location)
            {
                this.location = location;
            }


        @Override
        protected WeatherModel doInBackground(Object... params)
            {
                InputStream is = null;
                try
                    {
                        URL url = new URL(URL + "&lat=" + location.getLatitude() + "&lon=" +
                                location.getLongitude());
                        Logger.d(this, "url " + url.toString());
                        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                        conn.setReadTimeout(20000 /* milliseconds */);
                        conn.setConnectTimeout(30000 /* milliseconds */);
                        conn.setRequestMethod("GET");
                        conn.setDoInput(true);
                        conn.connect();
                        int response = conn.getResponseCode();
                        Logger.d(this, "The response is: " + response);
                        is = conn.getInputStream();
                        // Convert the InputStream into a string
                        BufferedReader r = new BufferedReader(new InputStreamReader(is));
                        StringBuilder responseString = new StringBuilder();
                        String line;
                        while ((line = r.readLine()) != null)
                            {
                                responseString.append(line).append('\n');
                            }
                        Logger.d(this, "The response is: " + responseString);
                        return new WeatherModel(responseString.toString());
                    } catch (ProtocolException e)
                    {
                        e.printStackTrace();
                    } catch (MalformedURLException e)
                    {
                        e.printStackTrace();
                    } catch (IOException e)
                    {
                        e.printStackTrace();
                    } finally
                    {
                        if (is != null)
                            {
                                try
                                    {
                                        is.close();
                                    } catch (IOException e)
                                    {
                                        e.printStackTrace();
                                    }
                            }
                    }
                return null;
            }


        @Override
        protected void onPostExecute(WeatherModel o)
            {
                if (!isFinishing() && o != null)
                    {
                        mHumidityMeter.setProgress((float) o
                                .humidity, true);
                        ((TextView) findViewById(R.id.temp)).setText(String.valueOf(o.temp) +
                                getString(R
                                        .string.degree));
                        ((TextView) findViewById(R.id.temp_range)).setText(String.valueOf(o
                                .temp_min) +
                                getString(R
                                        .string.degree) + "\n" + String.valueOf(o.temp_max) +
                                getString(R
                                        .string.degree));
                        displayWeather = true;
                    }
                super.onPostExecute(o);
            }

    }

    private class WeatherModel
    {
        public WeatherModel(String response)
            {
                try
                    {
                        JSONObject jResponse = new JSONObject(response);
                        jResponse = jResponse.getJSONObject("main");
                        temp = jResponse.getDouble("temp");
                        temp_max = jResponse.getDouble("temp_max");
                        temp_min = jResponse.getDouble("temp_min");
                        humidity = jResponse.getDouble("humidity");
                    } catch (JSONException e)
                    {
                        e.printStackTrace();
                    }
            }


        double temp;
        double humidity;
        double temp_min;
        double temp_max;
    }


    @Override
    protected void onDestroy()
        {
            GooglePlayConnection.stopLocationUpdate(RecordSession.this);
            LocalBroadcastManager.getInstance(this).unregisterReceiver(mUpdateReciver);
            super.onDestroy();
        }


    @Override
    public void onBackPressed()
        {
        }
}

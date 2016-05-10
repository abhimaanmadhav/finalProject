package abhimaan.com.jogger.GoogleServices;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResult;
import com.google.android.gms.location.LocationSettingsStates;
import com.google.android.gms.location.LocationSettingsStatusCodes;

import abhimaan.com.jogger.JogSessionService.JogSessionService;
import abhimaan.com.jogger.utils.Logger;

/**
 * @author Abhimaan Madhav
 */
public class GooglePlayConnection
{

    static private GoogleApiClient mGoogleApiClient;
    private final static int CONNECTION_RESOLUTION = 9000;
    private final static boolean debug = true;
    private final static int LAST_LOCATION_WINDOW = 1; // 1 min
    private final static int LOCATION_DEFUALT_INTERVAL = debug ? 2 * 1000 : 20 * 1000; // 20 sec
    private final static int LOCATION_DEFUALT_FASTEST_INTERVAL = debug ? 1500 : 15000; // 15 sec
    private final static int SMALLEST_DISPLACEMENT_METER = debug ? 0 : 10; // 10 meter
    public final static String LOCATION_ACTION = "abhimaan.action.location";
    public final static String GOOGLE_PLAY_CONNECTED = "abhimaan.google.play.connnected";

    private static GooglePlayConnectionState STATE = GooglePlayConnectionState.DISCONNECTED;
    private static LocationRequestState LOCATION_REQUEST_STATE = LocationRequestState
            .LOCATION_REQUEST_STOPPED;
    private static ConnectionResult CONNECTION_RESULT;
    private static GoogleApiClient.OnConnectionFailedListener FAILURE = new GoogleApiClient
            .OnConnectionFailedListener()
    {
        @Override
        public void onConnectionFailed(@NonNull ConnectionResult connectionResult)
            {
                STATE = GooglePlayConnectionState.CONNECTION_ERROR_REPAIRABLE;

                GooglePlayConnection.CONNECTION_RESULT = connectionResult;
                Logger.i(this, "onConnectionFailed");
            }
    };
    private static GoogleApiClient.ConnectionCallbacks Connection = new GoogleApiClient
            .ConnectionCallbacks()
    {
        @Override
        public void onConnected(@Nullable Bundle bundle)
            {
                STATE = GooglePlayConnectionState.CONNECTED;
                Logger.e(this, "onconnected");
                LocalBroadcastManager.getInstance(mGoogleApiClient.getContext()).sendBroadcast
                        (new Intent(GOOGLE_PLAY_CONNECTED));

            }


        @Override
        public void onConnectionSuspended(int i)
            {
                if (i == GoogleApiClient.ConnectionCallbacks.CAUSE_NETWORK_LOST)
                    {
                        STATE = GooglePlayConnectionState.SUSPENDED_DUE_TO_NETWORK_LOST;
                    } else
                    {
                        STATE = GooglePlayConnectionState.SUSPENDED_DUE_TO_SERVICE_DISCONNECTED;
                    }
            }
    };


    public static void connect(Context context)
        {
            if (mGoogleApiClient == null)
                {
                    mGoogleApiClient = new GoogleApiClient.Builder(context).addApi
                            (LocationServices.API)
                            .addConnectionCallbacks(Connection).addOnConnectionFailedListener
                                    (FAILURE)
                            .build();
                }
            connect();
        }


    private static void connect()
        {
            if (!mGoogleApiClient.isConnected())
                {
                    mGoogleApiClient.connect();
                    STATE = GooglePlayConnectionState.CONNECTING;
                } else
                {
                    STATE = GooglePlayConnectionState.CONNECTED;
                }
        }


    public static GooglePlayConnectionState isConnected()
        {
            return STATE;
        }


    public static void stop()
        {
            if (mGoogleApiClient != null && mGoogleApiClient.isConnected())
                {
                    mGoogleApiClient.disconnect();
                }
        }


    public Location getLastKnownLocation(Context context)
        {
            return null;
        }


    public long getAgeOfLocation(Location loc)
        {
            if (loc == null)
                return 2147483647;
            else
                return (System.currentTimeMillis() - loc.getTime()) / 1000;
        }


    public static LocationRequestState startLocationUpdate(final Activity activity)
        {
            if (LOCATION_REQUEST_STATE == LocationRequestState.INITIAL)
                {
                    LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder()
                            .addLocationRequest(createLocationRequest()).setAlwaysShow(true);
                    PendingResult<LocationSettingsResult> result =
                            LocationServices.SettingsApi.checkLocationSettings(mGoogleApiClient,
                                    builder.build());
                    result.setResultCallback(new ResultCallback<LocationSettingsResult>()
                    {
                        @Override
                        public void onResult(@NonNull LocationSettingsResult locationSettingsResult)
                            {
                                final Status status = locationSettingsResult.getStatus();
                                final LocationSettingsStates a = locationSettingsResult
                                        .getLocationSettingsStates();
                                switch (status.getStatusCode())
                                    {
                                        case LocationSettingsStatusCodes.SUCCESS:
                                            // All location settings are satisfied. The client can
                                            // initialize location requests here.
                                            Logger.i(GooglePlayConnection.class, "sucess");
                                            start(activity);
                                            break;
                                        case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                                            // Location settings are not satisfied, but this can
                                            // be fixed
                                            // by showing the user a dialog.
                                            Logger.i(this, "RESOLUTION_REQUIRED");
                                            try
                                                {
                                                    // Show the dialog by calling
                                                    // startResolutionForResult(),
                                                    // and check the result in onActivityResult().
                                                    status.startResolutionForResult(
                                                            activity,
                                                            34);

                                                } catch (IntentSender.SendIntentException e)
                                                {
                                                    // Ignore the error.
                                                }
                                            break;
                                        case LocationSettingsStatusCodes
                                                .SETTINGS_CHANGE_UNAVAILABLE:
                                            // Location settings are not satisfied. However, we
                                            // have no way
                                            // to fix the settings so we won't show the dialog.
                                            LOCATION_REQUEST_STATE = LocationRequestState
                                                    .CONNECTION_ERROR;
                                            Logger.i(this, "SETTINGS_CHANGE_UNAVAILABLE");
                                            break;
                                    }
                            }

                    });
                } else if (LOCATION_REQUEST_STATE != LocationRequestState.CONNECTION_ERROR)
                {
                    Logger.i(GooglePlayConnection.class, "start");
                    start(activity);
                }
            return LOCATION_REQUEST_STATE;
        }


    private static void start(Context mcontext)
        {
            if (LOCATION_REQUEST_STATE == LocationRequestState.INITIAL || LOCATION_REQUEST_STATE
                    == LocationRequestState
                    .LOCATION_REQUEST_STOPPED && mGoogleApiClient != null && mGoogleApiClient
                    .isConnected())
                {
                    LocationServices.FusedLocationApi.requestLocationUpdates(
                            mGoogleApiClient, createLocationRequest(), createPendingIntent
                                    (mcontext));
                    LOCATION_REQUEST_STATE = LocationRequestState
                            .REQUESTING_LOCATION;
                } else
                {
                    Logger.i(GooglePlayConnection.class, "LOCATION_REQUEST_STATE " +
                            LOCATION_REQUEST_STATE);
                }
        }


    private static LocationRequest createLocationRequest()
        {
            return LocationRequest.create()
                    .setInterval(LOCATION_DEFUALT_INTERVAL)
                    .setFastestInterval(LOCATION_DEFUALT_FASTEST_INTERVAL) // 500ms
                    .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                    .setSmallestDisplacement(SMALLEST_DISPLACEMENT_METER);
        }


    private static PendingIntent createPendingIntent(Context mContext)
        {
            Intent serviceIntent = new Intent(mContext, JogSessionService.class);
            serviceIntent.setAction(GooglePlayConnection.LOCATION_ACTION);
            return PendingIntent.getService(mContext, 100,
                    serviceIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        }


    public static void resolveError(Activity activity)
        {
            if (CONNECTION_RESULT != null)
                try
                    {
                        CONNECTION_RESULT.startResolutionForResult(activity, CONNECTION_RESOLUTION);
                    } catch (IntentSender.SendIntentException e)
                    {
                        e.printStackTrace();
                    }
        }


    public static Boolean onActivityResult(int requestCode, int result)
        {
            if (result == Activity.RESULT_OK)
                {
                    connect();
                    return true;
                }
            return false;
        }


    public static void stopLocationUpdate(Context mContext)
        {
            Logger.i(GooglePlayConnection.class, "stopLocationUpdate");
            if (mGoogleApiClient.isConnected() || mGoogleApiClient.isConnecting())
                {
                    LocationServices.FusedLocationApi.removeLocationUpdates(
                            mGoogleApiClient, createPendingIntent(mContext));
                    LOCATION_REQUEST_STATE = LocationRequestState.LOCATION_REQUEST_STOPPED;
                } else
                {
                    Logger.i(GooglePlayConnection.class, "stopLocationUpdate when client is " +
                            "dissconnected");
                }

        }


    public enum GooglePlayConnectionState
    {
        CONNECTED, DISCONNECTED,
        SUSPENDED_DUE_TO_NETWORK_LOST,
        SUSPENDED_DUE_TO_SERVICE_DISCONNECTED,
        CONNECTION_ERROR_REPAIRABLE, CONNECTING
    }

    public enum LocationRequestState
    {
        CONNECTION_ERROR, REQUESTING_LOCATION, LOCATION_REQUEST_STOPPED, INITIAL
    }
}
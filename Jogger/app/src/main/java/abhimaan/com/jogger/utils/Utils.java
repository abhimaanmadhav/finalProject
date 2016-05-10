package abhimaan.com.jogger.utils;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import java.io.File;
import java.text.Format;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import abhimaan.com.jogger.Configration.Configuration;
import abhimaan.com.jogger.preference.Preferences;

/**
 * Created by Abhimaan on 22/04/16.
 */
public class Utils
{
    public static String getDisplayDistance(Context context, float distance)
        {
            if (Preferences.getInstance(context).getUnit() == Unit.Km)
                {
                    Logger.d(context, "display distance" + NumberFormat.getInstance().format
                            (distance / 1000) + " Km");
                    return NumberFormat.getInstance().format(distance / 1000) + " Km";
                }
            //miles conversion
            Logger.d(context, "display distance" + NumberFormat.getInstance().format(0.621 *
                    (distance / 1000)) + " miles");
            return NumberFormat.getInstance().format(0.621 * (distance / 1000)) + " miles";
        }


    public static float getDistanceInUserPreferredFormat(Context context, float distance)
        {
            if (Preferences.getInstance(context).getUnit() == Unit.Km)
                {
                    return distance / 1000;
                }
            //miles conversion
            return (float) (0.621 * (distance / 1000));
        }


    public static String getDisplayspeed(float speed)
        {
            return NumberFormat.getInstance().format(speed) + " m/s";
        }


    public static String formatDateTime(long time)
        {
            Format format = new SimpleDateFormat("dd/MM/yy\nhh:mm a");
            return format.format(new Date(time));
        }


    public static String getLoggingDirectory(Context context)
        {
            return context.getCacheDir().getAbsolutePath() + File.separator + Configuration
                    .LOGGING_DIRECTORY;
        }


    public static boolean isOnline(Context context)
        {
            ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context
                    .CONNECTIVITY_SERVICE);
            NetworkInfo netInfo = cm.getActiveNetworkInfo();
            return netInfo != null && netInfo.isConnectedOrConnecting();
        }


    public enum Unit
    {
        Km,
        Miles;
    }
}

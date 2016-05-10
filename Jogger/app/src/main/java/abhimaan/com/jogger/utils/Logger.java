package abhimaan.com.jogger.utils;

import android.util.Log;

import abhimaan.com.jogger.Configration.Configuration;

public class Logger
{
    public static void d(Object object, String message)
        {
            if (Configuration.ENABLE_LOG)
                Log.d("" + object.getClass(), message);
        }


    public static void e(Object object, String string)
        {
            if (Configuration.ENABLE_LOG)
                {
                    Log.e("" + object.getClass(), string);
                }
        }


    public static void v(Object object, String message)
        {
            if (Configuration.ENABLE_LOG)
                Log.v("" + object.getClass(), message);
        }


    public static void i(Object object, String message)
        {
            if (Configuration.ENABLE_LOG)
                Log.i("" + object.getClass(), message);
        }
}

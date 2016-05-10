package abhimaan.com.jogger.JogSessionService;

import android.location.Location;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.util.Log;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import abhimaan.com.jogger.utils.Logger;

/**
 * @author Abhimaan Madhav
 */
public class DistanceCalculator
{

    public static int LOCATION_ACCURACY_LIMIT = 40;
    private float mDistance, tempDistance, tempSpeed;
    private boolean isrunnning = false;
    private Location mPreviousLocation;
    private long mTimeDiffrence;
    private final double MAX_SPEED = (70 * 1000) / (60 * 60);
    private long mPreviousLocationTime;


    public boolean isrunnning()
        {
            return isrunnning;
        }


    public void start()
        {
            if (!isrunnning)
                {
                    mDistance = 0;
                    isrunnning = true;
                }
        }


    public void start(Location location)
        {
            if (!isrunnning)
                {
                    mDistance = 0;
                    isrunnning = true;
                    mPreviousLocation = location;
                    mPreviousLocationTime = System.currentTimeMillis();
                }
        }


    public float stop()
        {
            isrunnning = false;
            mPreviousLocation = null;
            return mDistance;
        }


    public float getCurrentDistance()
        {
            return mDistance;
        }


    public boolean onLocationChanged(@NonNull Location location)
        {
            if (location.getAccuracy() > DistanceCalculator
                    .LOCATION_ACCURACY_LIMIT)
                {
                    return false;
                }
            if (mPreviousLocation == null)
                {
                    mPreviousLocation = location;
                    mPreviousLocationTime = System.currentTimeMillis();
                    return false;
                }
            tempDistance = mPreviousLocation.distanceTo(location);
            mTimeDiffrence = System.currentTimeMillis()
                    - mPreviousLocationTime;
            tempSpeed = tempDistance / (mTimeDiffrence / 1000);
            if (tempSpeed <= MAX_SPEED)
                {
                    mDistance += tempDistance;
                    mPreviousLocation = location;
                    mPreviousLocationTime = System.currentTimeMillis();
                    return true;
                } else
                {
                    Logger.e(this, "location speed greater than "
                            + MAX_SPEED);
                }
            return false;
        }


    void writeFile()
        {
            File file = new File(Environment.getExternalStorageDirectory().getPath() + File
                    .separator + "ATest");
            if (!file.exists())
                {
                    file.mkdirs();
                }
            Log.e("test", file.getAbsolutePath() + "exists " + file.exists());
            Log.e("test", file.getAbsolutePath() + "direc " + file.mkdirs());
            file = new File(file, "test.txt");
            try
                {
                    file.setWritable(true);
                    file.createNewFile();
                    Log.e("test", file.getAbsolutePath() + "exists " + file.exists());
                    FileWriter writer = new FileWriter(file);
                    writer.write("hi location2 / 2.9.6");
                    Thread.sleep(10000);
                    Log.e("test", "woke up1");
                    writer.write("hi location2 / 7.9.6");
                    Log.e("test", "sleeping 1");
                    Thread.sleep(10000);
                    Log.e("test", "woke up2");
                    writer.write("hi location2 / 2.9.6");
                    Log.e("test", "sleeping 1");
                    Thread.sleep(10000);
                    Log.e("test", "woke up3");
                    writer.write("hi location2 / 2.9.6");
                    Log.e("test", "sleeping 1");
                    Thread.sleep(10000);
                    Log.e("test", "woke up4");
                    writer.flush();
                    writer.close();
                } catch (IOException e)
                {
                    e.printStackTrace();
                } catch (InterruptedException e)
                {
                    e.printStackTrace();
                }
        }

}


package abhimaan.com.jogger.JogSessionService;

import android.app.Service;
import android.content.ContentValues;
import android.content.Intent;
import android.location.Location;
import android.os.IBinder;
import android.support.v4.content.LocalBroadcastManager;

import com.google.android.gms.location.LocationResult;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Calendar;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import abhimaan.com.jogger.ContentProvider.JoggerProviderHelper;
import abhimaan.com.jogger.GoogleServices.GooglePlayConnection;
import abhimaan.com.jogger.utils.Logger;
import abhimaan.com.jogger.utils.Utils;

public class JogSessionService extends Service
{
    public final static String START_SESSION = "abhimaan.jogger.session.start";
    public final static String END_SESSION = "abhimaan.jogger.session.end";
    public final static String ACTION_SESSION_INFO = "abhimaan.jogger.session.info";
    public final static String SESSION_DISTANCE = "abhimaan.jogger.session.info.distance";
    public final static String SESSION_SPEED = "abhimaan.jogger.session.info.speed";
    public final static String LOCATION_LAST_KNOW = "abhimaan.jogger.session.info.last.location";
    private DistanceCalculator calculator;
    private ExecutorService executor;
    private String fileName;
    private FileWriter mFileWriter;
    public final static String SEPERATOR = "#";
    private Boolean validLocation = false;
    private long startTime;


    public JogSessionService()
        {
        }


    @Override
    public void onCreate()
        {
            executor = Executors.newSingleThreadExecutor();

        }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId)
        {
            if (intent == null)
                {
                    return START_STICKY;
                }
            switch (intent.getAction())
                {
                    case GooglePlayConnection.LOCATION_ACTION:
                    {
                        Logger.i(this, "location recieved " + intent);
                        if(calculator==null){
                            return START_STICKY;
                        }
                        if (calculator.isrunnning())
                            {
                                parseLocation(intent);
                            } else
                            {
                                Logger.i(this, "not calculating  ");
                            }
                    }
                    break;
                    case START_SESSION:
                    {
                        Logger.i(this, "session started  ");
                        startSession();

                    }
                    break;
                    case END_SESSION:
                    {
                        stopSessionAndStoreSession();
                    }
                    break;

                }
            return START_STICKY;
        }


    private void startSession()
        {
            startTime = System.currentTimeMillis();
            calculator = new DistanceCalculator();
            calculator.start();
            fileName = System.currentTimeMillis() + ".txt";
            executor.submit(new FileIO(mFileWriter, true));
        }


    void parseLocation(Intent intent)
        {
            if (LocationResult.hasResult(intent))
                {
                    if (LocationResult.extractResult(intent) != null)
                        {
                            List<Location> locationList = LocationResult
                                    .extractResult(intent).getLocations();
                            for (Location location : locationList)
                                {
                                    validLocation = calculator
                                            .onLocationChanged(location);
                                    if (validLocation)
                                        {
                                            write(location);
                                            Logger.i(this, " valid location " +
                                                    "is  " + location);
                                            Intent data = new Intent(ACTION_SESSION_INFO);
                                            data.putExtra(SESSION_DISTANCE, calculator
                                                    .getCurrentDistance());
                                            data.putExtra(SESSION_SPEED, calculator
                                                    .getCurrentDistance() / ((System
                                                    .currentTimeMillis() - startTime) / 1000));
                                            data.putExtra(LOCATION_LAST_KNOW, location);
                                            LocalBroadcastManager.getInstance(this).sendBroadcast
                                                    (data);
                                        }
                                    Logger.e(this, " location is  " + location);
                                }

                        }
                } else
                {
                    Logger.i(this, " location doesnt have result  ");
                }

        }


    void write(Location location)
        {
            executor.submit(new writter(location, mFileWriter));
        }


    void stopSessionAndStoreSession()
        {
            float disance = calculator.stop();
            Logger.i(this, "distance is " + disance);
            executor.submit(new FileIO(mFileWriter, false));
            executor.submit(new SessionRecordCreator(fileName, startTime, disance));
        }


    @Override
    public void onDestroy()
        {
            Logger.e(this, "service destroyed");
            super.onDestroy();
        }


    @Override
    public IBinder onBind(Intent intent)
        {
            // TODO: Return the communication channel to the service.
            throw new UnsupportedOperationException("Not yet implemented");
        }


    class writter implements Runnable
    {
        private Location mLocation;
        private FileWriter fileWriter;


        public writter(Location location, FileWriter writter)
            {
                mLocation = location;
                fileWriter = writter;
            }


        @Override
        public void run()
            {
                StringBuilder locString = new StringBuilder();
                locString.append(mLocation.getLatitude());
                locString.append(SEPERATOR);
                locString.append(mLocation.getLongitude());
                locString.append(SEPERATOR);
                locString.append(mLocation.getAltitude());
                locString.append(SEPERATOR);
                locString.append(mLocation.getSpeed());
                locString.append("\n");
                try
                    {
                        fileWriter.write(locString.toString());
                    } catch (IOException e)
                    {
                        e.printStackTrace();
                    }
            }
    }

    class FileIO implements Runnable
    {
        private boolean toOpen;
        private FileWriter fileWriter;


        public FileIO(FileWriter writer, boolean open)
            {
                toOpen = open;
                fileWriter = writer;
            }


        @Override
        public void run()
            {
                if (toOpen)
                    {
                        File file = new File(Utils.getLoggingDirectory(JogSessionService.this));
                        if (!file.exists())
                            {
                                file.mkdirs();
                            }
                        file = new File(file, fileName);
                        Logger.i(this, file.getAbsolutePath());
                        file.setWritable(true);
                        try
                            {
                                file.createNewFile();
                                mFileWriter = new FileWriter(file);
                            } catch (IOException e)
                            {
                                e.printStackTrace();
                            }

                    } else
                    {
                        try
                            {
                                fileWriter.flush();
                                fileWriter.close();
                            } catch (IOException e)
                            {
                                e.printStackTrace();
                            }
                    }
            }
    }

    public class SessionRecordCreator implements Runnable
    {
        private String name;
        private long startTime, endTime;
        float d;


        public SessionRecordCreator(String fileName, long startTime, float distance)
            {
                endTime = System.currentTimeMillis();
                name = "" + fileName;
                this.startTime = startTime;
                d = distance;
                Logger.d(this, "SessionRecordCreator");
            }


        @Override
        public void run()
            {
                File file = new File(Utils.getLoggingDirectory(JogSessionService.this), name);
                try
                    {
                        FileInputStream fstream = new FileInputStream(file);
                        BufferedReader br = new BufferedReader(new InputStreamReader(fstream));
                        String line = br.readLine();
                        float sum_altitude = 0;
                        int vaildaltitude = 0;
                        String[] split;
                        while (line != null)
                            {
                                split = line.split(SEPERATOR);
                                if (Float.valueOf(split[2]) != 0)
                                    {
                                        vaildaltitude++;
                                        sum_altitude += Float.valueOf(split[2]);
                                    }
                                Logger.d(this, "sad " + line);
                                line = br.readLine();
                            }
                        br.close();
                        fstream.close();
                        if (vaildaltitude == 0)
                            {
                                //to avoid 0/0
                                vaildaltitude = 1;
                            }
                        ContentValues values = new ContentValues();
                        values.put(JoggerProviderHelper.JoggerEntry.DB_AVG_ALTITUDE, sum_altitude
                                / vaildaltitude);
                        Logger.d(this, "diff writter " + (endTime -
                                startTime) / 1000);
                        values.put(JoggerProviderHelper.JoggerEntry.DB_AVG_SPEED, d / ((endTime -
                                startTime) / 1000));
                        values.put(JoggerProviderHelper.JoggerEntry.DB_DISTANCE, d);
                        values.put(JoggerProviderHelper.JoggerEntry.DB_END_TIME, endTime);
                        Calendar c = Calendar.getInstance();
                        values.put(JoggerProviderHelper.JoggerEntry.DB_START_TIME, startTime);
                        values.put(JoggerProviderHelper.JoggerEntry.DB_PATH_ID, name);
                        Logger.d(this, "SessionRecordCreator inserting");
                        getContentResolver().insert(JoggerProviderHelper.JoggerEntry.CONTENT_URI,
                                values);
                    } catch (Exception e)
                    {
                        e.printStackTrace();

                    }
            }
    }
}

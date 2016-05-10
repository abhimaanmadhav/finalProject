package abhimaan.com.jogger.JoggingDetails;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.PolylineOptions;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import abhimaan.com.jogger.BaseActivity;
import abhimaan.com.jogger.JogSessionService.JogSessionService;
import abhimaan.com.jogger.R;
import abhimaan.com.jogger.utils.Logger;
import abhimaan.com.jogger.utils.Utils;

public class JoggingDetails extends BaseActivity implements OnMapReadyCallback, GoogleMap
        .SnapshotReadyCallback
{
    private final int DETAILS = 900;
    private GoogleMap mMap;
    public static final String PATH_INFO = "path";
    public static final String ELAPSED_TIME = "elapsed_time";
    public static final String DISTANCE = "distance";
    public static final String ALTITUDE = "altitude";
    private ProgressDialog mProgressDialog;


    @Override
    protected void onCreate(Bundle savedInstanceState)
        {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_jogging_details);
            // Obtain the SupportMapFragment and get notified when the map is ready to be used.
            SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                    .findFragmentById(R.id.map);
            mapFragment.getMapAsync(this);
            float distance = getIntent().getFloatExtra(DISTANCE, 0);
            long elapsed = getIntent().getLongExtra(ELAPSED_TIME, 0);
            ((TextView) findViewById(R.id.distance_covered)).setText(Utils.getDisplayDistance
                    (this, distance));
            ((TextView) findViewById(R.id.avg_altitude)).setText(String.valueOf(getIntent()
                    .getFloatExtra(ALTITUDE, 0)));
            ((TextView) findViewById(R.id.avg_speed)).setText(Utils.getDisplayspeed(distance / (
                    elapsed / 1000)));
            ((TextView) findViewById(R.id.time_taken)).setText(getElapsedTime(
                    (elapsed)));
            setSupportActionBar((Toolbar) findViewById(R.id.toolbar));
            findViewById(R.id.share).setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View v)
                    {
                        if (requestPermision(Manifest.permission.WRITE_EXTERNAL_STORAGE, DETAILS))
                            {
                                mProgressDialog = new ProgressDialog(JoggingDetails.this);
                                mProgressDialog.setCancelable(false);
                                mProgressDialog.setMessage(getString(R.string.share_preparing));
                                mProgressDialog.show();
                                mMap.snapshot(JoggingDetails.this);
                            }
                    }
            });
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }


    @Override
    public boolean onOptionsItemSelected(MenuItem item)
        {
            onBackPressed();
            return super.onOptionsItemSelected(item);
        }


    public String getElapsedTime(long different)
        {
            long hoursInMilli = 60 * 60 * 1000;
            long minutesInMilli = 60 * 1000;
            long secondsInMilli = 1000;
            long elapsedHours = different / hoursInMilli;
            different = different % hoursInMilli;
            long elapsedMinutes = different / minutesInMilli;
            different = different % minutesInMilli;
            long elapsedSeconds = different / secondsInMilli;
            return elapsedHours + ":" + elapsedMinutes + ":" + elapsedSeconds;
        }


    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap)
        {
            mMap = googleMap;
            // Polylines are useful for marking paths and routes on the map.
            new Thread(new Runnable()
            {
                @Override
                public void run()
                    {
                        parseFile();
                    }
            }
            ).start();
        }


    void parseFile()
        {
            File file = new File(Utils.getLoggingDirectory(JoggingDetails.this),
                    getIntent()
                            .getStringExtra(PATH_INFO));
            FileInputStream fstream = null;
            BufferedReader br = null;
            try
                {
                    fstream = new FileInputStream(file);
                    br = new BufferedReader(new InputStreamReader
                            (fstream));
                    String line = br.readLine();
                    String[] split;
                    final List<LatLng> list = new ArrayList<LatLng>();
                    while (line != null)
                        {
                            split = line.split(JogSessionService.SEPERATOR);
                            list.add(new LatLng(Double.valueOf(split[0]),
                                    Double.valueOf(split[1])));
                            Logger.d(this, "sad " + line);
                            line = br.readLine();
                        }
                    runOnUiThread(new Runnable()
                    {
                        @Override
                        public void run()
                            {
                                if (list.size() > 0)
                                    {
                                        PolylineOptions options = new PolylineOptions();
                                        options.clickable(false);
                                        options.geodesic(true);
                                        options.addAll(list).color(ResourcesCompat.getColor
                                                (getResources(), R.color.colorPrimary, null))
                                                .width(5 * getResources().getDisplayMetrics()
                                                        .density);
                                        mMap.addPolyline(options);
                                        ;
                                        mMap.animateCamera(CameraUpdateFactory.newLatLngBounds
                                                (LatLngBounds.builder().include(list.get(0))
                                                        .include(list.get(list.size() - 1)).build
                                                                (), 20));
                                    }
                            }
                    });
                } catch (FileNotFoundException e)
                {
                    e.printStackTrace();
                } catch (IOException e)
                {
                    e.printStackTrace();
                } finally
                {
                    try
                        {
                            if (br != null)
                                {
                                    br.close();
                                }
                            if (fstream != null)
                                {
                                    fstream.close();
                                }
                        } catch (IOException e)
                        {
                            e.printStackTrace();
                        }

                }
        }


    @Override
    public void onSnapshotReady(final Bitmap bitmap)
        {
            new Thread(new Runnable()
            {
                @Override
                public void run()
                    {
                        View mView = findViewById(R.id.root);
                        mView.setDrawingCacheEnabled(true);
                        Bitmap backBitmap = mView.getDrawingCache();
                        Bitmap bmOverlay = Bitmap.createBitmap(
                                backBitmap.getWidth(), backBitmap.getHeight(),
                                backBitmap.getConfig());
                        Canvas canvas = new Canvas(bmOverlay);
                        Paint paint = new Paint(Paint
                                .FILTER_BITMAP_FLAG);
                        canvas.drawBitmap(backBitmap, 0, 0, paint);
                        canvas.drawBitmap(bitmap, new Matrix(), paint);
                        FileOutputStream out = null;
                        boolean isFailure = false;
                        File f = new File(Environment.getExternalStorageDirectory()
                                + "/Jogger");
                        try
                            {
                                f.mkdirs();
                                f = new File(f.getAbsoluteFile() + File.separator
                                        + System.currentTimeMillis() + ".png");
                                out = new FileOutputStream(f.getAbsoluteFile()
                                );

                            } catch (IOException e)
                            {
                                Toast.makeText(JoggingDetails.this, getString(R.string
                                        .share_failed), Toast.LENGTH_LONG).show();
                                isFailure = true;
                                e.printStackTrace();
                            }
                        if (!isFailure)
                            {
                                bmOverlay.compress(Bitmap.CompressFormat.PNG, 90, out);
                                Intent shareIntent = new Intent();
                                shareIntent.setAction(Intent.ACTION_SEND);
                                shareIntent.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(f
                                        .getAbsoluteFile()));
                                shareIntent.setType("image/jpeg");
                                startActivity(Intent.createChooser(shareIntent, getResources
                                        ().getText(R.string.share_intent)));
                            }
                        runOnUiThread(new Runnable()
                        {
                            @Override
                            public void run()
                                {
                                    mProgressDialog.dismiss();
                                }
                        });
                    }
            }).start();
        }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults)
        {
            switch (requestCode)
                {
                    case DETAILS:
                    {
                        if (grantResults.length > 0)
                            {
                                findViewById(R.id.share).performClick();
                            } else
                            {
                                createDialogue(getString(R.string.permission_denied), this,
                                        getString(R.string.permission_denied_title), 0, new
                                                String[]{getString(R.string.ok)});
                            }
                    }
                    break;
                }
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }


    @Override
    protected void explainUserImportanceOfPermission(String Permission)
        {
            createDialogue(getString(R.string.external_storage_messgae), this, getString(R.string
                    .access_permission), DETAILS, new String[]{getString(R.string.change),
                    getString(R
                            .string.no_thanks)});
        }


    @Override
    public void positiveClicked(View v, int id)
        {
            switch (id)
                {
                    case DETAILS:
                        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                        Uri uri = Uri.fromParts("package", getPackageName(), null);
                        intent.setData(uri);
                        startActivity(intent);
                        mDialog.dismiss();
                        break;
                }
        }


    @Override
    public void negetiveClicked(View v, int id)
        {
            switch (id)
                {
                    case DETAILS:
                        mDialog.dismiss();
                }
        }
}

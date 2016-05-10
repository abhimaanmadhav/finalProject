package abhimaan.com.jogger;

import android.Manifest;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v4.util.Pair;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import com.github.mikephil.charting.charts.CombinedChart;
import com.github.mikephil.charting.charts.CombinedChart.DrawOrder;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.CombinedData;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;

import java.sql.Date;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import abhimaan.com.jogger.ContentProvider.JoggerProviderHelper;
import abhimaan.com.jogger.GoogleServices.GooglePlayConnection;
import abhimaan.com.jogger.JogSessionService.JogSessionService;
import abhimaan.com.jogger.JoggingDetails.JoggingDetails;
import abhimaan.com.jogger.RecordingSession.RecordSession;
import abhimaan.com.jogger.UserInfo.UserInfo;
import abhimaan.com.jogger.preference.Preferences;
import abhimaan.com.jogger.utils.Logger;
import abhimaan.com.jogger.utils.Utils;

public class HomeScreen extends BaseActivity
        implements NavigationView.OnNavigationItemSelectedListener, View.OnClickListener, android
        .app.LoaderManager.LoaderCallbacks<Cursor>, AdapterView.OnItemClickListener
{
    private final int ID = 89;
    private ListView mListView;

    private final int HOME_SCREEN = 100;


    @Override
    protected void onCreate(Bundle savedInstanceState)
        {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_home_screen);
            getLoaderManager().initLoader(ID, null, this);
            GooglePlayConnection.connect(HomeScreen.this);
            Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
            setSupportActionBar(toolbar);
            DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
            ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                    this, drawer, toolbar, R.string.navigation_drawer_open, R.string
                    .navigation_drawer_close);
            drawer.addDrawerListener(toggle);
            toggle.syncState();
            NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
            ((TextView) navigationView.getHeaderView(0).findViewById(R.id.name)).setText
                    (Preferences.getInstance(this).getName());
            navigationView.setNavigationItemSelectedListener(this);
            findViewById(R.id.start_jog).setOnClickListener(this);
            if (GooglePlayConnection.isConnected() != GooglePlayConnection
                    .GooglePlayConnectionState.CONNECTED)
                {
                    GooglePlayConnection.connect(this);
                } else
                {
                    Logger.i(this, "not connected");
                }
            mListView = (ListView) findViewById(R.id.history);
            mListView.setAdapter(new HistoryAdapter(this, null, false));
            mListView.setOnItemClickListener(this);
            mListView.setEmptyView(findViewById(R.id.no_history));
        }


    @Override
    public void onBackPressed()
        {
            DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
            if (drawer.isDrawerOpen(GravityCompat.START))
                {
                    drawer.closeDrawer(GravityCompat.START);
                } else
                {
                    GooglePlayConnection.stop();
                    super.onBackPressed();
                }
        }


    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item)
        {
            // Handle navigation view item clicks here.
            int id = item.getItemId();
            if (id == R.id.nav_about)
                {
                    startActivity(new Intent(this, aboutScreen.class));

                } else if (id == R.id.nav_info)
                {
                    Intent intent = new Intent(this, UserInfo.class);
                    intent.putExtra(UserInfo.NAME, Preferences.getInstance(this).getName());
                    startActivity(intent);
                    finish();
                }
            DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
            drawer.closeDrawer(GravityCompat.START);
            return true;
        }


    @Override
    public void onClick(View v)
        {
            if (requestPermision(Manifest.permission.ACCESS_FINE_LOCATION, HOME_SCREEN))
                {
                    Intent serviceIntent = new Intent(this, JogSessionService.class);
                    serviceIntent.setAction(JogSessionService.START_SESSION);
                    startService(serviceIntent);
                    Intent intent = new Intent(HomeScreen.this, RecordSession.class);
                    ActivityOptionsCompat options = ActivityOptionsCompat
                            .makeSceneTransitionAnimation(
                                    HomeScreen.this,
                                    new Pair<View, String>(findViewById(R.id.start_jog),
                                            getString(R.string.distance)));
                    ActivityCompat.startActivity(HomeScreen.this, intent, options.toBundle());
                    finish();
                }
        }


    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args)
        {
            return new CursorLoader(
                    this,
                    JoggerProviderHelper.JoggerEntry.CONTENT_URI,
                    null,
                    null,
                    null,
                    JoggerProviderHelper.JoggerEntry.DB_START_TIME + " desc");
        }


    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data)
        {
            ((HistoryAdapter) mListView.getAdapter()).swapCursor(data);
            CombinedChart mChart = (CombinedChart) findViewById(R.id.chart);
            mChart.clear();
            if (data.getCount() == 0)
                {
                    return;
                }
            mChart.setDescription("");
            mChart.setBackgroundColor(Color.WHITE);
            mChart.setDrawGridBackground(false);
            mChart.setDrawBarShadow(false);
            // draw bars behind lines
            mChart.setDrawOrder(new DrawOrder[]{
                    DrawOrder.BAR, DrawOrder.BUBBLE, DrawOrder.CANDLE, DrawOrder.LINE, DrawOrder
                    .SCATTER
            });
            YAxis rightAxis = mChart.getAxisRight();
            rightAxis.setDrawGridLines(false);
            rightAxis.setAxisMinValue(0f); // this replaces setStartAtZero(true)
            YAxis leftAxis = mChart.getAxisLeft();
            leftAxis.setDrawGridLines(false);
            leftAxis.setAxisMinValue(0f); // this replaces setStartAtZero(true)
            leftAxis.setPosition(YAxis.YAxisLabelPosition.OUTSIDE_CHART);
            XAxis xAxis = mChart.getXAxis();
            xAxis.setDrawGridLines(false);
            xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
            CombinedData combinedData = new CombinedData(getLabels(data));
            combinedData.setData(getLinedata(data));
            combinedData.setData(getBardata(data));
            mChart.setData(combinedData);
            mChart.invalidate();
        }


    private List<String> getLabels(Cursor data)
        {
            data.moveToFirst();
            Calendar c = Calendar.getInstance();
            Calendar c2 = Calendar.getInstance();
            if (data.getCount() <= 0)
                {
                    return new ArrayList<String>(0);
                }
            ArrayList<String> labels = new ArrayList<String>();
            SimpleDateFormat format = new SimpleDateFormat("dd MMM");
            int index = data.getColumnIndex
                    (JoggerProviderHelper
                            .JoggerEntry.DB_START_TIME);
            c.setTimeInMillis(data.getLong(index));
            c2.setTimeInMillis(data.getLong(index));
            labels.add(format.format(new Date(data.getLong(index))));
            while (data.moveToNext())
                {
                    c.setTimeInMillis(data.getLong(data.getColumnIndex
                            (JoggerProviderHelper
                                    .JoggerEntry.DB_START_TIME)));
                    if (!isSameDay(c, c2))
                        {
                            labels.add(format.format(new Date(data.getLong(index))));
                            c2.setTimeInMillis(data.getLong(index));
                            Logger.d(this, "change label" + format.format(new Date(data.getLong
                                    (index))));
                        }
                }
            return labels;
        }


    private LineData getLinedata(Cursor c)
        {
            LineData d = new LineData();
            ArrayList<Entry> list = new ArrayList<>(c.getCount());
            c.moveToFirst();
            Calendar iterator = Calendar.getInstance();
            Calendar previousMarker = Calendar.getInstance();
            int speedIndex = c.getColumnIndex(JoggerProviderHelper
                    .JoggerEntry.DB_AVG_SPEED), startTimeIndex = c.getColumnIndex
                    (JoggerProviderHelper
                            .JoggerEntry.DB_START_TIME);
            iterator.setTimeInMillis(c.getLong(startTimeIndex));
            previousMarker.setTimeInMillis(c.getLong(startTimeIndex));
            int i = 0, count = 0;
            float avgSpeed = c.getFloat(speedIndex);
            while (c.moveToNext())
                {
                    iterator.setTimeInMillis(c.getLong(startTimeIndex));
                    if (!isSameDay(iterator, previousMarker))
                        {
                            list.add(new Entry((avgSpeed / count), i));
                            i++;
                            count = 0;
                            avgSpeed = 0;
                            previousMarker.setTimeInMillis(c.getLong(startTimeIndex));
                        }
                    avgSpeed += c.getFloat(speedIndex);
                    count++;

                }
            //To add last
            list.add(new Entry((avgSpeed / count), i));
            LineDataSet set = new LineDataSet(list, getString(R.string.avg_speed_text));
            set.setColor(ResourcesCompat.getColor(getResources(), R.color.colorAccent, null));
            set.setValueTextColor(Color.parseColor("#B22222"));
            set.setCircleColor(Color.parseColor("#C71585"));
            d.addDataSet(set);
            return d;
        }


    boolean isSameDay(Calendar c, Calendar c2)
        {
            if (c.get(Calendar.DAY_OF_MONTH) != c2.get(Calendar.DAY_OF_MONTH) || c.get
                    (Calendar.MONTH) != c2.get(Calendar.MONTH) || c.get
                    (Calendar.YEAR) != c2.get(Calendar.YEAR))
                {
                    return false;
                }
            return true;
        }


    private BarData getBardata(Cursor c)
        {
            ArrayList<BarEntry> list = new ArrayList<>(c.getCount());
            c.moveToFirst();
            Calendar iterator = Calendar.getInstance();
            Calendar previousMarker = Calendar.getInstance();
            int distanceIndex = c.getColumnIndex(JoggerProviderHelper
                    .JoggerEntry.DB_DISTANCE), startTimeIndex = c.getColumnIndex
                    (JoggerProviderHelper
                            .JoggerEntry.DB_START_TIME);
            iterator.setTimeInMillis(c.getLong(startTimeIndex));
            previousMarker.setTimeInMillis(c.getLong(startTimeIndex));
            int i = 0;
            float totalDistance;
            totalDistance = c.getFloat(distanceIndex);
            while (c.moveToNext())
                {
                    iterator.setTimeInMillis(c.getLong(startTimeIndex));
                    if (!isSameDay(iterator, previousMarker))
                        {
                            list.add(new BarEntry(Utils.getDistanceInUserPreferredFormat(this,
                                    totalDistance), i));
                            Logger.d(this, "total distance" + totalDistance);
                            i++;
                            totalDistance = 0;
                            previousMarker.setTimeInMillis(c.getLong(startTimeIndex));
                        }
                    totalDistance += c.getFloat(distanceIndex);
                }
            list.add(new BarEntry(Utils.getDistanceInUserPreferredFormat(this,
                    totalDistance), i));
            BarDataSet set = new BarDataSet(list, getString(R.string.total_distance));
            set.setColor(Color.parseColor("#00BFFF"));
            set.setValueTextColor(ResourcesCompat.getColor(getResources(), android.R.color.black,
                    null));
            set.setValueTextSize(10f);
            set.setBarSpacePercent(25);
//            set.setAxisDependency(YAxis.AxisDependency.LEFT);
            BarData d = new BarData();
            d.addDataSet(set);
            return d;
        }


    @Override
    public void onLoaderReset(Loader<Cursor> loader)
        {
        }


    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id)
        {
            Cursor c = (Cursor) parent.getItemAtPosition(position);
            Intent intent = new Intent(this, JoggingDetails.class);
            intent.putExtra(JoggingDetails.PATH_INFO, c.getString(c.getColumnIndex
                    (JoggerProviderHelper.JoggerEntry.DB_PATH_ID)));
            intent.putExtra(JoggingDetails.DISTANCE, c.getFloat(c.getColumnIndex
                    (JoggerProviderHelper.JoggerEntry.DB_DISTANCE)));
            intent.putExtra(JoggingDetails.ELAPSED_TIME, c.getLong(c
                    .getColumnIndex
                            (JoggerProviderHelper.JoggerEntry.DB_END_TIME)) - (c
                    .getLong(c.getColumnIndex
                            (JoggerProviderHelper.JoggerEntry.DB_START_TIME))));
            intent.putExtra(JoggingDetails.ALTITUDE, c.getFloat(c.getColumnIndex
                    (JoggerProviderHelper.JoggerEntry.DB_AVG_ALTITUDE)));
            ActivityOptionsCompat options = ActivityOptionsCompat.makeSceneTransitionAnimation(
                    HomeScreen.this,
                    new Pair<View, String>(view.findViewById(R.id.distance),
                            getString(R.string.distance)));
            ActivityCompat.startActivity(HomeScreen.this, intent, options.toBundle());
        }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults)
        {
            switch (requestCode)
                {
                    case HOME_SCREEN:
                    {
                        if (grantResults.length > 0)
                            {
                                findViewById(R.id.start_jog).performClick();
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
            createDialogue(getString(R.string.location_messgae), this, getString(R.string
                    .access_permission), HOME_SCREEN, new String[]{getString(R.string.change),
                    getString(R
                            .string.no_thanks)});
        }


    @Override
    public void positiveClicked(View v, int id)
        {
            switch (id)
                {
                    case HOME_SCREEN:
                        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                        Uri uri = Uri.fromParts("package", getPackageName(), null);
                        intent.setData(uri);
                        startActivity(intent);
                        mDialog.dismiss();
                }
        }


    @Override
    public void negetiveClicked(View v, int id)
        {
            switch (id)
                {
                    case HOME_SCREEN:
                        mDialog.dismiss();
                }
        }
}

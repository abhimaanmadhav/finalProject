package abhimaan.com.jogger.Widget;

import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.database.Cursor;
import android.widget.RemoteViews;

import java.util.Calendar;
import java.util.Date;

import abhimaan.com.jogger.ContentProvider.JoggerProviderHelper;
import abhimaan.com.jogger.R;
import abhimaan.com.jogger.utils.Logger;
import abhimaan.com.jogger.utils.Utils;

/**
 * Implementation of App Widget functionality.
 */
public class WeeklyStats extends AppWidgetProvider
{

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds)
        {
            // There may be multiple widgets active, so update all of them
            for (int appWidgetId : appWidgetIds)
                {
                    updateAppWidget(context, appWidgetManager, appWidgetId);
                }
        }


    @Override
    public void onEnabled(Context context)
        {
            // Enter relevant functionality for when the first widget is created
        }


    @Override
    public void onDisabled(Context context)
        {
            // Enter relevant functionality for when the last widget is disabled
        }


    static void updateAppWidget(Context context, AppWidgetManager appWidgetManager,
                                int appWidgetId)
        {
            CharSequence widgetText = context.getString(R.string.appwidget_text);
            // Construct the RemoteViews object
            RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.weekly_stats);
            views.setTextViewText(R.id.appwidget_distance, widgetText);
            Calendar calendar = Calendar.getInstance();
            calendar.setTimeInMillis(new Date().getTime() - (7 * 24 * 60 * 60 * 1000));
            calendar.set(Calendar.MINUTE, 0);
            calendar.set(Calendar.HOUR, 0);
            calendar.set(Calendar.MILLISECOND, 00);
            calendar.set(Calendar.SECOND, 00);
            Cursor c = context.getContentResolver().query(JoggerProviderHelper.JoggerEntry
                    .buildJoggerHistoryURI(calendar.getTimeInMillis(), System.currentTimeMillis()
                    ), null, null, null, null);
            int index = c.getColumnIndex(JoggerProviderHelper.JoggerEntry.DB_DISTANCE);
            float avgDis = 0;
            while (c.moveToNext())
                {
                    avgDis += c.getFloat(index);
                }
            Logger.i(context, "avg distance widget" + avgDis);
            if (c.getCount() == 0)
                {
                    views.setTextViewText(R.id.appwidget_distance, Utils.getDisplayDistance
                            (context, 0));
                } else
                {
                    views.setTextViewText(R.id.appwidget_distance, Utils.getDisplayDistance
                            (context, avgDis / 7));
                }
            // Instruct the widget manager to update the widget
            appWidgetManager.updateAppWidget(appWidgetId, views);
        }

}


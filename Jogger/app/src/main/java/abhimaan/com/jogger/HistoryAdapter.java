package abhimaan.com.jogger;

import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.TextView;

import abhimaan.com.jogger.ContentProvider.JoggerProviderHelper;
import abhimaan.com.jogger.utils.Utils;

/**
 * Created by Abhimaan on 03/05/16.
 */
public class HistoryAdapter extends CursorAdapter
{
    public HistoryAdapter(Context context, Cursor c, boolean autoRequery)
        {
            super(context, c, autoRequery);
        }


    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent)
        {
            View v = LayoutInflater.from(context).inflate(R.layout.item_history, parent, false);
            Holder h = new Holder(v);
            v.setTag(h);
            return v;
        }


    @Override
    public void bindView(View view, Context context, Cursor cursor)
        {
            Holder h = (Holder) view.getTag();
            h.date.setText(Utils.formatDateTime(cursor.getLong(cursor.getColumnIndex
                    (JoggerProviderHelper.JoggerEntry
                            .DB_START_TIME))));
            h.distance.setText(Utils
                    .getDisplayDistance
                            (context, cursor.getFloat(cursor.getColumnIndex
                                    (JoggerProviderHelper.JoggerEntry
                                            .DB_DISTANCE))));
        }


    private class Holder
    {
        TextView date, distance;


        public Holder(View v)
            {
                date = (TextView) v.findViewById(R.id.time);
                distance = (TextView) v.findViewById(R.id.distance);
            }
    }
}

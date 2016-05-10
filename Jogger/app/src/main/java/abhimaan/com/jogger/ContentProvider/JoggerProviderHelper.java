package abhimaan.com.jogger.ContentProvider;

import android.net.Uri;
import android.provider.BaseColumns;

/**
 * Created by Abhimaan on 26/04/16.
 */
public class JoggerProviderHelper
{
    public static final String CONTENT_AUTHORITY = "abhimaan.com.jogger";
    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);
    public static final String PATH_JOGGER_HISTORY = "JOGGER_HISTORY";
    public static final String PATH_JOGGER_HISTORY_DATE = "JOGGER_HISTORY_DATE";

    public static final class JoggerEntry implements BaseColumns
    {
        public static final Uri CONTENT_URI =
                BASE_CONTENT_URI.buildUpon().appendPath(PATH_JOGGER_HISTORY).build();
        public static final String DB_START_TIME = "start_time";
        public static final String DB_ID = "_id";
        public static final String DB_END_TIME = "end_time";
        public static final String DB_AVG_SPEED = "avg_speed";
        public static final String DB_AVG_ALTITUDE = "avg_altitude";
        public static final String DB_DISTANCE = "distance";
        public static final String DB_PATH_ID = "path_id";


        public static Uri buildJoggerHistoryURI(long startDate, long endDate)
            {
                return BASE_CONTENT_URI.buildUpon().appendPath(PATH_JOGGER_HISTORY_DATE).appendQueryParameter(JoggerEntry.DB_START_TIME,
                        Long.toString
                        (startDate)).appendQueryParameter(JoggerEntry.DB_END_TIME, Long.toString
                        (endDate)).build();
            }


        public static Uri buildJoggerdetailsURI(int id)
            {
                return CONTENT_URI.buildUpon().appendQueryParameter(DB_ID, Integer.toString(id))
                        .build();
            }
    }
}

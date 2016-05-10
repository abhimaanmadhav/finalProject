package abhimaan.com.jogger.ContentProvider;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * @author Abhimaan
 */
public class JoggerDb extends SQLiteOpenHelper
{
    private static final String DB_START_TIME = "start_time";
    private static final String DB_ID = "_id";
    private static final String DB_END_TIME = "end_time";
    private static final String DB_AVG_SPEED = "avg_speed";
    private static final String DB_AVG_ALTITUDE = "avg_altitude";
    private static final String DB_DISTANCE = "distance";
    private static final String DB_PATH_ID = "path_id";
    private static final String SPACE = " ";
    private static final String DATABASE_NAME = "Jogger";

    private final static int DATABASE_VERSION = 1;
    private final String TABLE_NAME = "JOGGER_HISTORY";


    public JoggerDb(Context context)
        {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);

        }


    @Override
    public void onCreate(SQLiteDatabase db)
        {
            try
                {
                    db.beginTransaction();
                    String query = "CREATE TABLE " +
                            TABLE_NAME +
                            SPACE + "( " + DB_ID + SPACE + "INTEGER PRIMARY KEY AUTOINCREMENT," +
                            DB_START_TIME + " REAL NOT NULL, " + DB_DISTANCE + " REAL NOT NULL, " +
                            DB_AVG_ALTITUDE + " REAL NOT NULL, " + DB_AVG_SPEED + " REAL NOT " +
                            "NULL, " +
                            DB_END_TIME + " REAL NOT NULL, " + DB_PATH_ID + " TEXT NOT NULL)";
                    db.execSQL(query);
                    db.setTransactionSuccessful();
                } catch (Exception e)
                {
                    e.printStackTrace();
                } finally
                {
                    db.endTransaction();
                }

        }


    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion)
        {
        }
}
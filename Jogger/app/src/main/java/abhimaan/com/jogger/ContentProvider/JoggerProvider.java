package abhimaan.com.jogger.ContentProvider;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.support.annotation.Nullable;

import java.security.InvalidParameterException;

import abhimaan.com.jogger.utils.Logger;

/**
 * Created by Abhimaan on 25/04/16.
 */
public class JoggerProvider extends ContentProvider
{
    JoggerDb dbHelper;

    static final int JOGGERHISTORY = 100;
    static final int JOGGERHISTORY_WITH_ID = 101;
    static final int JOGGERHISTORY_WITH_DATE = 102;
    static UriMatcher uriMatcher = joggerMatcher();
    SQLiteDatabase db;


    @Override
    public boolean onCreate()
        {
            dbHelper = new JoggerDb(getContext());
            return true;
        }


    private SQLiteDatabase getDb()
        {
            if (db == null)
                {
                    db = dbHelper.getWritableDatabase();
                }
            return db;
        }


    @Nullable
    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs,
                        String sortOrder)
        {
            if (uriMatcher.match(uri) == JOGGERHISTORY)
                {
                    return getDb().query(JoggerProviderHelper
                                    .PATH_JOGGER_HISTORY, projection, selection, selectionArgs,
                            null,
                            null, sortOrder);
                } else
                {
                    Logger.d(this,"return "+uriMatcher.match(uri));
                    if (uriMatcher.match(uri) == JOGGERHISTORY_WITH_DATE)
                        {
                            String startDate = uri.getQueryParameter(JoggerProviderHelper
                                    .JoggerEntry.DB_START_TIME);
                            String endDate = uri.getQueryParameter(JoggerProviderHelper
                                    .JoggerEntry.DB_END_TIME);
                            if (startDate == null || endDate == null)
                                {
                                    throw new InvalidParameterException("expecting start and end " +
                                            "date but recived" + startDate + " " + endDate);
                                }
                            String where =   JoggerProviderHelper.JoggerEntry
                                    .DB_START_TIME +
                                    ">=? and " + JoggerProviderHelper.JoggerEntry.DB_END_TIME +
                                    "<=?";
                            Logger.d(this,where);
                            return getDb().query(JoggerProviderHelper
                                            .PATH_JOGGER_HISTORY, projection, where, new
                                            String[]{startDate, endDate},
                                    null,
                                    null,
                                    null, sortOrder);
                        } else
                        {
                            throw new UnsupportedOperationException(uri.toString()+" this URI is not supported ");
                        }
                }
        }


    @Nullable
    @Override
    public String getType(Uri uri)
        {
            return null;
        }


    @Nullable
    @Override
    public Uri insert(Uri uri, ContentValues values)
        {
            Logger.i(this, "inserted " + uri);
            Logger.i(this, "matcher " + uriMatcher.match(uri));
            if (uriMatcher.match(uri) == JOGGERHISTORY)
                {
                    try
                        {
                            getDb().beginTransaction();
                            long id = getDb().insert(JoggerProviderHelper
                                    .PATH_JOGGER_HISTORY, null, values);
                            Logger.i(this, "inserted id " + id);
                            getDb().setTransactionSuccessful();
                        } catch (Exception e)
                        {
                            e.printStackTrace();
                        } finally
                        {
                            getDb().endTransaction();
                        }

                } else
                {
                    Logger.i(this, "error ");
                    throw new UnsupportedOperationException("URI is not suppored");
                }
            return null;
        }


    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs)
        {
            return 0;
        }


    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs)
        {
            return 0;
        }


    private static UriMatcher joggerMatcher()
        {
            final UriMatcher matcher = new UriMatcher(UriMatcher.NO_MATCH);
            final String authority = JoggerProviderHelper.CONTENT_AUTHORITY;
            // For each type of URI you want to add, create a corresponding code.
            matcher.addURI(authority, JoggerProviderHelper.PATH_JOGGER_HISTORY, JOGGERHISTORY);
            matcher.addURI(authority, JoggerProviderHelper.PATH_JOGGER_HISTORY_DATE,
                    JOGGERHISTORY_WITH_DATE);
            matcher.addURI(authority, JoggerProviderHelper.PATH_JOGGER_HISTORY + "/" +
                    JoggerProviderHelper.JoggerEntry.DB_ID + "/#", JOGGERHISTORY_WITH_ID);
            return matcher;
        }

}

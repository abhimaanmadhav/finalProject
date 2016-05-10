package abhimaan.com.jogger.preference;

import android.content.Context;
import android.content.SharedPreferences;

import abhimaan.com.jogger.utils.Logger;
import abhimaan.com.jogger.utils.Utils;

/**
 * @author Shweta
 */
public class Preferences
{

    private final String NAME = "name";
    private final String UNIT = "unit";
    private static Preferences celebInstance;
    private final SharedPreferences prefs;


    private Preferences(Context mContext)
        {
            prefs = mContext.getSharedPreferences(mContext.getPackageName(),
                    Context.MODE_PRIVATE);
        }


    public synchronized static Preferences getInstance(Context mContext)
        {
            if (celebInstance == null)
                {
                    celebInstance = new Preferences(mContext);
                }
            return celebInstance;
        }


    public String getName()
        {
            return prefs.getString(NAME, "");
        }


    public void setUserInfo(String name, Utils.Unit unit)
        {
            setUnit(unit);
            setName(name);
        }


    public void logOut()
        {
            prefs.edit().clear().apply();
        }


    public void setUnit(Utils.Unit unit)
        {
            SharedPreferences.Editor editor = prefs.edit();
            editor.putBoolean(UNIT, ((unit == Utils.Unit.Km) ? false : true));
            Logger.d(this, "units stored" + ((unit == Utils.Unit.Km) ? false : true));
            editor.commit();
            getUnit();
        }


    public Utils.Unit getUnit()
        {
            Logger.d(this, "units " + prefs.getBoolean(UNIT, false));
            return (prefs.getBoolean(UNIT, false) ? Utils.Unit.Miles : Utils.Unit.Km);
        }


    public void setName(String name)
        {
            SharedPreferences.Editor editor = prefs.edit();
            editor.putString(NAME, name);
            editor.apply();
        }


    public boolean isUserConfigured()
        {
            return (!getName().isEmpty());
        }

}

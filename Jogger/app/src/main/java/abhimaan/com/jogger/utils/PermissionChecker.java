package abhimaan.com.jogger.utils;

import android.app.Activity;
import android.content.pm.PackageManager;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;

/**
 * Created by Abhimaan on 28/04/16.
 */

public class PermissionChecker
{
    boolean checkPermission(Activity activity, @NonNull String permission, int
            permissionRequestCode)
        {
            if (ContextCompat.checkSelfPermission(activity, permission) == PackageManager
                    .PERMISSION_GRANTED)
                {
                    return true;
                } else
                {
                    ActivityCompat.requestPermissions(activity,
                            new String[]{permission},
                            permissionRequestCode);
                }
            return false;
        }
}

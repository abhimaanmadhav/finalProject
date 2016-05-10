package abhimaan.com.jogger;

import android.app.Dialog;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import abhimaan.com.jogger.utils.Logger;

/**
 * Created by Abhimaan on 10/05/16.
 */
public class BaseActivity extends AppCompatActivity
{
    public Dialog mDialog;


    protected boolean requestPermision(String permissions, int requstCode)
        {
            if (ContextCompat.checkSelfPermission(this,
                    permissions)
                    != PackageManager.PERMISSION_GRANTED)
                {
                    Logger.d(this, "not granted permission");
                    if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                            permissions))
                        {
                            Logger.d(this, "explainUserImportanceOfPermission");
                            explainUserImportanceOfPermission(permissions);
                        } else
                        {
                            ActivityCompat.requestPermissions(this, new String[]{permissions},
                                    requstCode);
                        }
                    return false;
                }
            return true;
        }


    protected void explainUserImportanceOfPermission(String Permission)
        {
            new Throwable("implement this method");
        }


    public void createDialogue(String message, Context context, String title, int id, String[]
            buttonLabel)
        {
            if (mDialog == null)
                {
                    mDialog = new Dialog(context);
                    mDialog.setContentView(R.layout.request_permission_dialogue);
                    mDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                    Button dialogCancelButton = (Button) mDialog.findViewById(R.id.cancel_btn);
                    Button dialogOKButton = (Button) mDialog.findViewById(R.id.ok_btn);
                    dialogOKButton.setOnClickListener(new View.OnClickListener()
                    {
                        @Override
                        public void onClick(View v)
                            {
                                positiveClicked(v, (int) v.getTag());
                            }

                    });
                    dialogCancelButton.setOnClickListener(new View.OnClickListener()
                    {
                        @Override
                        public void onClick(View v)
                            {
                                negetiveClicked(v, (int) v.getTag());

                            }
                    });
                }
            TextView tile = (TextView) mDialog.findViewById(R.id.title);
            tile.setText(title);
            TextView text = (TextView) mDialog.findViewById(R.id.msg);
            text.setText(message);
            Button dialogOKButton = (Button) mDialog.findViewById(R.id.ok_btn);
            dialogOKButton.setTag(id);
            dialogOKButton.setText(buttonLabel[0]);
            Button dialogCancelButton = (Button) mDialog.findViewById(R.id.cancel_btn);
            if (buttonLabel.length != 1)
                {
                    dialogCancelButton.setTag(id);
                    dialogCancelButton.setText(buttonLabel[1]);
                } else
                {
                    dialogCancelButton.setVisibility(View.GONE);
                }
            if (!mDialog.isShowing())
                {
                    mDialog.show();
                }
        }


    public void positiveClicked(View v, int id)
        {
        }


    public void negetiveClicked(View v, int id)
        {
        }
}

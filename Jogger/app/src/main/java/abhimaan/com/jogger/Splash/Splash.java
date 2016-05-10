package abhimaan.com.jogger.Splash;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

import abhimaan.com.jogger.HomeScreen;
import abhimaan.com.jogger.R;
import abhimaan.com.jogger.UserInfo.UserInfo;
import abhimaan.com.jogger.preference.Preferences;

public class Splash extends Activity
{
    final long TIME = 3000;


    @Override
    protected void onCreate(Bundle savedInstanceState)
        {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_splash);
            new Handler().postDelayed(new Runnable()
                                      {
                                          @Override
                                          public void run()
                                              {
                                                  if (Preferences.getInstance(Splash.this)
                                                          .isUserConfigured())
                                                      {
                                                          startActivity(new Intent(Splash
                                                                  .this, HomeScreen.class));

                                                      } else
                                                      {
                                                          startActivity(new Intent(Splash.this,
                                                                  UserInfo.class));
                                                      }
                                                  finish();
                                              }
                                      }
                    , TIME);
        }


    @Override
    public void onBackPressed()
        {
        }
}

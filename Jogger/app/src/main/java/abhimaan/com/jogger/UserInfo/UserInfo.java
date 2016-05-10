package abhimaan.com.jogger.UserInfo;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.Spinner;

import abhimaan.com.jogger.GoogleServices.GooglePlayConnection;
import abhimaan.com.jogger.HomeScreen;
import abhimaan.com.jogger.R;
import abhimaan.com.jogger.preference.Preferences;
import abhimaan.com.jogger.utils.Utils;

public class UserInfo extends AppCompatActivity
{
    EditText name;
    Spinner unit;
    public static final String NAME = "name", UNIT = "unit";


    @Override
    protected void onCreate(Bundle savedInstanceState)
        {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_user_info);
            Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
            setSupportActionBar(toolbar);
            GooglePlayConnection.connect(this);
            name = (EditText) findViewById(R.id.name);
            unit = (Spinner) findViewById(R.id.unit);
            name.setText(getIntent().getStringExtra(NAME));
            unit.setSelection(Preferences.getInstance(this).getUnit() == Utils.Unit.Km ? 0 : 1);
            if (getIntent().getStringExtra(NAME) != null)
                {
                    getSupportActionBar().setDisplayHomeAsUpEnabled(true);
                }
            findViewById(R.id.save).setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View v)
                    {
                        name.clearFocus();
                        if (name.length() <= 2)
                            {
                                name.setError(getString(R.string.error_name));
                                name.requestFocus();
                                return;
                            }
                        Preferences.getInstance(UserInfo.this).setUserInfo(name.getText()
                                .toString(), unit.getSelectedItemPosition() == 0 ? Utils.Unit.Km
                                : Utils.Unit.Miles);
                        startActivity(new Intent(UserInfo.this, HomeScreen.class));
                        finish();
                    }
            });

        }


    @Override
    public boolean onOptionsItemSelected(MenuItem item)
        {
            switch (item.getItemId())
                {
                    case android.R.id.home:
                        Intent intent = new Intent(this, HomeScreen.class);
                        startActivity(intent);
                        finish();
                        return true;
                    default:
                        return super.onOptionsItemSelected(item);
                }
        }


    @Override
    public void onBackPressed()
        {
            Intent intent = new Intent(this, HomeScreen.class);
            startActivity(intent);
            finish();
        }
}

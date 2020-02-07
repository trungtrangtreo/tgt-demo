package ca.TransCanadaTrail.TheGreatTrail.MenuTool;


import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import ca.TransCanadaTrail.TheGreatTrail.R;


public class SettingsActivity extends AppCompatActivity implements
        ListAdapter.customButtonListener {

    private ImageView backBtn;

    private ListView listView;
    ListAdapter adapter;
    ArrayList<String> dataItems = new ArrayList<String>();

    SharedPreferences preferences;
    SharedPreferences.Editor editor;

    boolean restrictToWifi = false;
    boolean includePhoto = true;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_settings);


        Toolbar toolbar = (Toolbar) findViewById(R.id.main_activity_tb);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        ListView settingsListView = (ListView) findViewById(R.id.settingsListView);
        settingsListView.setOnTouchListener(new View.OnTouchListener() {

            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_MOVE) {
                    return true; // Indicates that this has been handled by you and will not be forwarded further.
                }
                return false;
            }
        });

        backBtn = (ImageView) findViewById(R.id.backBtn);
        backBtn.setImageResource(R.drawable.ic_arrow_back);
        backBtn.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                finish();
                return false;
            }
        });



        preferences = PreferenceManager.getDefaultSharedPreferences(getApplication());
        editor = preferences.edit();

        restrictToWifi = false;
        includePhoto = true;

        if (preferences.contains("RestrictToWifi") && preferences.contains("IncludePhoto"))
        {
             restrictToWifi = preferences.getBoolean("RestrictToWifi", false);
             includePhoto = preferences.getBoolean("IncludePhoto", true);

        }
        else {
            editor.putBoolean("RestrictToWifi", false); // value to store
            editor.putBoolean("IncludePhoto", true); // value to store
            editor.commit();
        }


        String[] dataArray = getResources().getStringArray(R.array.Settings);
        List<String> dataTemp = Arrays.asList(dataArray);
        dataItems.addAll(dataTemp);
        listView = (ListView) findViewById(R.id.settingsListView);
        adapter = new ListAdapter(SettingsActivity.this, dataItems, restrictToWifi, includePhoto);
        adapter.setCustomButtonListner(SettingsActivity.this);
        listView.setAdapter(adapter);

    }

    @Override
    public void onButtonClickListner(int position, String value) {
        if (position == 3){
            Intent intent = new Intent(this,SettingsActivity2.class);
            startActivity(intent);
        }

        if (position == 1){
            restrictToWifi = ! restrictToWifi;
            editor.putBoolean("RestrictToWifi", restrictToWifi); // value to store
            editor.commit();
        }

        if (position == 2){
            includePhoto = ! includePhoto;
            editor.putBoolean("IncludePhoto", includePhoto); // value to store
            editor.commit();
        }


       // Toast.makeText(SettingsActivity.this, value,Toast.LENGTH_SHORT).show();

    }
}



package ca.TransCanadaTrail.TheGreatTrail.MenuTool;


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


public class SettingsActivity2 extends AppCompatActivity implements
        ListAdapter2.customButtonListener {

    private ImageView backBtn;

    private ListView listView;
    ListAdapter2 adapter;
    ArrayList<String> dataItems = new ArrayList<String>();
    ArrayList<String> subtitlesItems = new ArrayList<String>();

    SharedPreferences preferences;
    SharedPreferences.Editor editor;

    int accuracy = 2 ;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_settings2);

        ListView settingsListView = (ListView) findViewById(R.id.settingsListView2);
        settingsListView.setOnTouchListener(new View.OnTouchListener() {

            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_MOVE) {
                    return true; // Indicates that this has been handled by you and will not be forwarded further.
                }
                return false;
            }
        });


        Toolbar toolbar = (Toolbar) findViewById(R.id.main_activity_tb);

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



        if (preferences.contains("Accuracy"))
        {
            accuracy = preferences.getInt("Accuracy", 2);

        }
        else {
            editor.putInt("Accuracy", 2); // value to store
            editor.commit();
        }



        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);




        String[] dataArray = getResources().getStringArray(R.array.Settings2);
        List<String> dataTemp = Arrays.asList(dataArray);
        dataItems.addAll(dataTemp);

        String[] dataArray2 = getResources().getStringArray(R.array.Settings3);
        List<String> dataTemp2 = Arrays.asList(dataArray2);
        subtitlesItems.addAll(dataTemp2);


        listView = (ListView) findViewById(R.id.settingsListView2);
        adapter = new ListAdapter2(SettingsActivity2.this, dataItems, subtitlesItems, accuracy);
        adapter.setCustomButtonListner(SettingsActivity2.this);
        listView.setAdapter(adapter);

    }

    @Override
    public void onButtonClickListner(int position, String value) {

        editor.putInt("Accuracy", position); // value to store
        editor.commit();

       // Toast.makeText(SettingsActivity2.this,value,Toast.LENGTH_SHORT).show();

    }
}



package ca.TransCanadaTrail.TheGreatTrail.MenuTool;

import android.content.Intent;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.method.LinkMovementMethod;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import ca.TransCanadaTrail.TheGreatTrail.AppController;
import ca.TransCanadaTrail.TheGreatTrail.R;

public class KeenActivity extends AppCompatActivity {
    private Tracker mTracker;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_keen);

        // [START shared_tracker]
        // Obtain the shared Tracker instance.
        AppController application = (AppController) getApplication();
        mTracker = application.getDefaultTracker();
        // [END shared_tracker]


        Toolbar toolbar = (Toolbar) findViewById(R.id.main_activity_tb);

        ImageView backBtn = (ImageView) findViewById(R.id.backBtn);
        backBtn.setImageResource(R.drawable.ic_arrow_back);
        backBtn.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                finish();
                return false;
            }
        });


        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        TextView quality_title=(TextView)findViewById(R.id.quality_title);
        Typeface typeFace= Typeface.createFromAsset(getAssets(), "fonts/ProximaNova-Bold.ttf");
        quality_title.setTypeface(typeFace);

        TextView quality_text=(TextView)findViewById(R.id.quality_text);
        typeFace= Typeface.createFromAsset(getAssets(),  "fonts/ProximaNova-Light.ttf");
        quality_text.setTypeface(typeFace);


        TextView integrity_title=(TextView)findViewById(R.id.integrity_title);
        typeFace= Typeface.createFromAsset(getAssets(), "fonts/ProximaNova-Bold.ttf");
        integrity_title.setTypeface(typeFace);

        TextView integrity_text=(TextView)findViewById(R.id.integrity_text);
        typeFace= Typeface.createFromAsset(getAssets(), "fonts/ProximaNova-Light.ttf");
        integrity_text.setTypeface(typeFace);

        TextView health_title=(TextView)findViewById(R.id.health_title);
        typeFace= Typeface.createFromAsset(getAssets(), "fonts/ProximaNova-Bold.ttf");
        health_title.setTypeface(typeFace);

        TextView health_text=(TextView)findViewById(R.id.health_text);
        typeFace= Typeface.createFromAsset(getAssets(), "fonts/ProximaNova-Light.ttf");
        health_text.setTypeface(typeFace);

        TextView caring_title=(TextView)findViewById(R.id.caring_title);
        typeFace= Typeface.createFromAsset(getAssets(), "fonts/ProximaNova-Bold.ttf");
        caring_title.setTypeface(typeFace);

        TextView caring_text=(TextView)findViewById(R.id.caring_text);
        typeFace= Typeface.createFromAsset(getAssets(), "fonts/ProximaNova-Light.ttf");
        caring_text.setTypeface(typeFace);


        TextView linkTxt = (TextView) findViewById(R.id.link);
        typeFace= Typeface.createFromAsset(getAssets(), "fonts/ProximaNova-Bold.ttf");
        linkTxt.setMovementMethod(LinkMovementMethod.getInstance());
        linkTxt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String  url = "http://www.keenfootwear.com/?utm_medium=App&utm_source=Keen_Android";
                /*if (Locale.getDefault().getLanguage().equals("fr")) {
                    url = "http://www.keenfootwear.com/fr-ca/?utm_medium=app&utm_source=keen_android";
                }*/

                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setData(Uri.parse(url));
                startActivity(intent);
            }
        });



    }


    @Override
    public void onResume() {
        super.onResume();

        // [START screen_view_hit]
        mTracker.setScreenName("About Keen");
        mTracker.send(new HitBuilders.ScreenViewBuilder().build());
        // [END screen_view_hit]

    }



}



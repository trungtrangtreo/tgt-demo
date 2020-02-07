package ca.TransCanadaTrail.TheGreatTrail.MapView;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.widget.ImageView;

import ca.TransCanadaTrail.TheGreatTrail.R;

public class FullImageActivity extends AppCompatActivity {


    public static AppCompatActivity activity;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.full_image);


        // Tool bar with arrow and personnalized title

        Toolbar toolbar = (Toolbar) findViewById(R.id.offlineTrailToolbar);
        toolbar.setTitle("");
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        activity = FullImageActivity.this;
        activity.getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        ((AppCompatActivity) FullImageActivity.this).getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_arrow_back);

        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);     //  Fixed Portrait orientation

        // get intent data
        Intent i = getIntent();
        Intent intent = getIntent();
        int num = intent.getIntExtra("num" ,0) ;
        Bitmap bitmap = SegmentDetailsFragment.images.get(num);

        ImageView imageView = (ImageView) findViewById(R.id.full_image_view);
        imageView.setImageBitmap(bitmap);
        // Selected image id
       /* int position = i.getExtras().getInt("id");
        ImageAdapter imageAdapter = new ImageAdapter(this);

        ImageView imageView = (ImageView) findViewById(R.id.full_image_view);
        imageView.setImageResource(imageAdapter.mThumbIds[position]);*/
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }

    }

    @Override
    public void onBackPressed() {
        this.finish();
    }

}
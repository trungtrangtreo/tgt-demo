package ca.TransCanadaTrail.TheGreatTrail.OfflineMap;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import com.baoyz.swipemenulistview.SwipeMenu;
import com.baoyz.swipemenulistview.SwipeMenuCreator;
import com.baoyz.swipemenulistview.SwipeMenuItem;
import com.baoyz.swipemenulistview.SwipeMenuListView;
import com.mapbox.mapboxsdk.MapboxAccountManager;
import com.mapbox.mapboxsdk.location.LocationServices;
import com.mapbox.mapboxsdk.offline.OfflineManager;
import com.mapbox.mapboxsdk.offline.OfflineRegion;
import com.mapbox.mapboxsdk.offline.OfflineRegionStatus;

import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import ca.TransCanadaTrail.TheGreatTrail.R;
import ca.TransCanadaTrail.TheGreatTrail.database.ActivityDBHelper;
import ca.TransCanadaTrail.TheGreatTrail.item.OfflineItem;
import ca.TransCanadaTrail.TheGreatTrail.item.OfflineItemAdapter;
import ca.TransCanadaTrail.TheGreatTrail.utils.Utility;

import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import ca.TransCanadaTrail.TheGreatTrail.AppController;

import static android.view.View.OnClickListener;

public class OfflineTrailsActivity extends AppCompatActivity {

    private static final String TAG = "LocationService";



    private ProgressDialog progressBar;

    private String mbglOffline = "/data/data/ca.TransCanadaTrail.TheGreatTrail/files/mbgl-offline.db";

    private FloatingActionButton floatingActionButton;
    private LocationServices locationServices;


    private SwipeMenuListView  offlineTrailsListView;
    private FloatingActionButton selectAreaBtn;
    private TextView sizeTxt;
    private TextView avertissementMessage;

    private List<OfflineItem> offlineTrailsList = new ArrayList<OfflineItem>();
    private HashMap<String,String>  offlineTrailsHashMap = new HashMap<String,String>();
    private String  regionName;



    // JSON encoding/decoding
    public static final String JSON_CHARSET = "UTF-8";
    public static final String JSON_FIELD_REGION_NAME = "FIELD_REGION_NAME";

    // Offline objects
    private OfflineManager offlineManager;

   // private  ArrayList<String> offlineRegionsNames;
   // private  ArrayAdapter<String> modeAdapter;
   private OfflineItemAdapter adapter;

    private ArrayList<OfflineItem> offlineItems = new  ArrayList<OfflineItem>();

    private ActivityDBHelper dbHelper = new ActivityDBHelper(OfflineTrailsActivity.this);

    private Tracker mTracker;



    static Context context;
    public static AppCompatActivity activity;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Mapbox access token is configured here. This needs to be called either in your application
        // object or in the same activity which contains the mapview.
        MapboxAccountManager.start(this, getString(R.string.access_token));

        context = this;

        // This contains the MapView in XML and needs to be called after the account manager
        setContentView(R.layout.activity_offline_trails);

        // [START shared_tracker]
        // Obtain the shared Tracker instance.
        AppController application = (AppController) getApplication();
        mTracker = application.getDefaultTracker();
        // [END shared_tracker]


        sizeTxt = (TextView) findViewById(R.id.sizeTxt);
        //progressBar = (ProgressBar) findViewById(R.id.progress_bar);
        avertissementMessage = (TextView) findViewById(R.id.avertissementMessage);



        selectAreaBtn = (FloatingActionButton) findViewById(R.id.selectAreaBtn);
        selectAreaBtn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {

                if(isNetworkAvailable()){
                    Intent i = new Intent(OfflineTrailsActivity.this, AreaSelectionActivity.class);
                    startActivity(i);
                }
                else {
                    AlertDialog.Builder builder = new AlertDialog.Builder(context);
                    builder.setTitle("Error");
                    builder.setMessage(R.string.need_online);
                    builder.setCancelable(true);
                    builder.setPositiveButton(android.R.string.ok,
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {

                                    dialog.cancel();
                                }
                            });

                    AlertDialog alert = builder.create();
                    alert.show();
            }


            }
        });



       // offlineTrailsListView = (ListView) findViewById(R.id.offlineTrailsList);

        offlineManager = OfflineManager.getInstance(this);
        offlineItems = new  ArrayList<OfflineItem>();


        // Tool bar with arrow and personnalized title

        Toolbar toolbar = (Toolbar) findViewById(R.id.offlineTrailToolbar);
        toolbar.setTitle("");
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        activity = OfflineTrailsActivity.this;
        activity.getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        ((AppCompatActivity) OfflineTrailsActivity.this).getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_arrow_back);

        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);     //  Fixed Portrait orientation





        offlineTrailsListView = (SwipeMenuListView)findViewById(R.id.offlineTrailsList);

        offlineTrailsListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent i = new Intent(OfflineTrailsActivity.this, DisplayOfflineTrailsActivity.class);
                i.putExtra("regionSelected", position);
                i.putExtra("regionName",offlineItems.get(position).getName());
                startActivity(i);
            }
        });

        adapter = new OfflineItemAdapter(this, R.layout.list_item_offline_trails, offlineItems);
        offlineTrailsListView.setAdapter(adapter); //  offlineTrailsList.setAdapter(modeAdapter)
        SwipeMenuCreator creator = new SwipeMenuCreator() {

            @Override
            public void create(SwipeMenu menu) {


                SwipeMenuItem item = new SwipeMenuItem(
                        getApplicationContext());
                // set item background
                item.setBackground(new ColorDrawable(Color.RED));
                item.setWidth(dpToPx(90));
                item.setTitle("Delete");
                item.setTitleSize(18);
                item.setTitleColor(Color.WHITE);
                menu.addMenuItem(item);
            }
        };
        //set MenuCreator
        offlineTrailsListView.setMenuCreator(creator);
        // set SwipeListener
        offlineTrailsListView.setOnSwipeListener(new SwipeMenuListView.OnSwipeListener() {

            @Override
            public void onSwipeStart(int position) {
                // swipe start
            }

            @Override
            public void onSwipeEnd(int position) {
                // swipe end
            }
        });

        offlineTrailsListView.setOnMenuItemClickListener(new SwipeMenuListView.OnMenuItemClickListener() {

            @Override
            public boolean onMenuItemClick(int position, SwipeMenu menu, int index) {
                final int regionSelected = position;
                OfflineItem value = adapter.getItem(position);
                regionName = value.getName();
                switch (index) {
                    case 0:
                        offlineManager.listOfflineRegions(new OfflineManager.ListOfflineRegionsCallback() {
                            @Override
                            public void onList(final OfflineRegion[] offlineRegions) {
                                // Check result. If no regions have been
                                // downloaded yet, notify user and return
                                if (offlineRegions == null || offlineRegions.length == 0) {
                                   // Toast.makeText(OfflineTrailsActivity.this, "You have no regions yet.", Toast.LENGTH_SHORT).show();
                                    return;
                                }

                                progressBar = new ProgressDialog(OfflineTrailsActivity.context);
                                progressBar.setIndeterminate(true);
                                progressBar.setProgressNumberFormat(null);
                                progressBar.setProgressPercentFormat(null);
                                progressBar.setMessage(getResources().getString(R.string.take_while));
                                progressBar.setTitle("Deleting");
                                progressBar.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
                                progressBar.show();


                                // Begin the deletion process
                                offlineRegions[regionSelected].delete(new OfflineRegion.OfflineRegionDeleteCallback() {
                                    @Override
                                    public void onDelete() {
                                        // Once the region is deleted, remove the
                                        // progressBar and display a toast
                                        progressBar.dismiss();
                                        progressBar.setIndeterminate(false);
                                        Utility.displayToast(OfflineTrailsActivity.this, "  Region deleted  ", 1);
                                       // Toast.makeText(OfflineTrailsActivity.this, "Region deleted", Toast.LENGTH_LONG).show();
                                         dbHelper.deleteOfflineMapInDB(regionName);


                                        downloadedRegionList();
                                    }

                                    @Override
                                    public void onError(String error) {
                                        progressBar.dismiss();
                                        progressBar.setIndeterminate(false);
                                        Log.e(TAG, "Error: " + error);
                                    }
                                });

                            }

                            @Override
                            public void onError(String error) {
                                Log.e(TAG, "Error: " + error);
                            }
                        });

                        break;
                   /* case 1:
                        Toast.makeText(getApplicationContext(), "Action 2 for "+ value , Toast.LENGTH_SHORT).show();
                        break;*/
                }
                return false;
            }});

    }


    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    @Override
    public void onResume() {
        super.onResume();
         downloadedRegionList();

        // [START screen_view_hit]
        mTracker.setScreenName("Offline Maps");
        mTracker.send(new HitBuilders.ScreenViewBuilder().build());


    }

    @Override
    public void onPause() {
        super.onPause();


    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
     }

    @Override
    protected void onDestroy() {
        super.onDestroy();
     }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
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



    private void downloadedRegionList() {


        offlineTrailsList = dbHelper.getOfflineTrails();
        offlineTrailsHashMap = dbHelper.getOfflineTrailsAsHashMap();



        // Query the DB asynchronously
        offlineManager.listOfflineRegions(new OfflineManager.ListOfflineRegionsCallback() {
            @Override
            public void onList(final OfflineRegion[] offlineRegions) {
                // Check result. If no regions have been
                // downloaded yet, notify user and return
                if (offlineRegions == null || offlineRegions.length == 0) {

                    avertissementMessage.setVisibility(View.VISIBLE);
                    offlineTrailsListView.setVisibility(View.GONE);
                    return;
                }

                avertissementMessage.setVisibility(View.GONE);
                offlineTrailsListView.setVisibility(View.VISIBLE);

                // Add all of the region names to a list
                offlineItems.clear();
                for (final OfflineRegion offlineRegion : offlineRegions) {
                    String regionName = getRegionName(offlineRegion);
                    offlineItems.add(new OfflineItem(regionName,offlineTrailsHashMap.get(regionName),true));

                }
                adapter = new OfflineItemAdapter(OfflineTrailsActivity.this, R.layout.list_item_offline_trails, offlineItems);
                offlineTrailsListView.setAdapter(adapter);
                adapter.notifyDataSetChanged();

            }

            @Override
            public void onError(String error) {
                Log.e(TAG, "Error: " + error);
            }
        });

        File file = new File(mbglOffline);
        if(file.exists() && file.length()>100000) {

            long sizeMb = (file.length()*100 / 1048576 );
            float dispalyedSize = (float) sizeMb /100;
            sizeTxt.setText(getResources().getString(R.string.total_cache_size)+dispalyedSize+" MB");

            Log.i(TAG,"if : file.length() = "+file.length());
        }
        else{
            sizeTxt.setText(getResources().getString(R.string.total_cache_size)+"0 MB");
            Log.i(TAG,"else : file.length() = "+file.length());
        }

    }





    private String getRegionName(OfflineRegion offlineRegion) {
        // Get the retion name from the offline region metadata
        String regionName;

        try {
            byte[] metadata = offlineRegion.getMetadata();
            String json = new String(metadata, JSON_CHARSET);
            JSONObject jsonObject = new JSONObject(json);  Log.e(TAG, "Failed to decode metadata: --------------------------------" + jsonObject.toString());
            regionName = jsonObject.getString(JSON_FIELD_REGION_NAME);
        } catch (Exception exception) {
            Log.e(TAG, "Failed to decode metadata: " + exception.getMessage());
            regionName = "Region " + offlineRegion.getID();
        }
        return regionName;
    }


    public static int dpToPx(int dp)
    {
        return (int) (dp * Resources.getSystem().getDisplayMetrics().density);
    }



}
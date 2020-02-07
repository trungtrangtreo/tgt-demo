package ca.TransCanadaTrail.TheGreatTrail.OfflineMap;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.net.ConnectivityManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.mapbox.mapboxsdk.MapboxAccountManager;
import com.mapbox.mapboxsdk.annotations.PolylineOptions;
import com.mapbox.mapboxsdk.camera.CameraUpdateFactory;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.geometry.LatLngBounds;
import com.mapbox.mapboxsdk.location.LocationListener;
import com.mapbox.mapboxsdk.location.LocationServices;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;
import com.mapbox.mapboxsdk.offline.OfflineManager;
import com.mapbox.mapboxsdk.offline.OfflineRegion;
import com.mapbox.mapboxsdk.offline.OfflineRegionError;
import com.mapbox.mapboxsdk.offline.OfflineRegionStatus;
import com.mapbox.mapboxsdk.offline.OfflineTilePyramidRegionDefinition;
import com.mapbox.mapboxsdk.style.layers.LineLayer;
import com.mapbox.mapboxsdk.style.layers.Property;
import com.mapbox.mapboxsdk.style.layers.PropertyFactory;
import com.mapbox.mapboxsdk.style.sources.GeoJsonSource;
import com.mapbox.mapboxsdk.style.sources.Source;
import com.mapbox.services.commons.geojson.Feature;
import com.mapbox.services.commons.geojson.FeatureCollection;
import com.mapbox.services.commons.geojson.LineString;
import com.mapbox.services.commons.models.Position;

import org.json.JSONObject;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.TimeZone;

import ca.TransCanadaTrail.TheGreatTrail.AppController;
import ca.TransCanadaTrail.TheGreatTrail.Constants;
import ca.TransCanadaTrail.TheGreatTrail.MainActivity;
import ca.TransCanadaTrail.TheGreatTrail.R;
import ca.TransCanadaTrail.TheGreatTrail.database.ActivityDBHelper;
import ca.TransCanadaTrail.TheGreatTrail.utils.Utility;

import static ca.TransCanadaTrail.TheGreatTrail.utils.Utility.displayToast;


public class AreaSelectionActivity extends AppCompatActivity implements OnMapReadyCallback {

    private static final String TAG = "LocationService";

    private ImageView downloadBtn;
    private ImageView backBtn;
    private FloatingActionButton floatingActionButton;

    private Tracker mTracker;

    // JSON encoding/decoding
    public static final String JSON_CHARSET = "UTF-8";
    public static final String JSON_FIELD_REGION_NAME = "FIELD_REGION_NAME";

    // UI elements
    private MapView mapView;
    private MapboxMap map;
    private ProgressDialog progressBar;

    private static final int PERMISSIONS_LOCATION = 0;

    private LocationServices locationServices;

    private boolean isEndNotified;

    // Offline objects
    private OfflineManager offlineManager;
    private OfflineRegion offlineRegion;
    private ProgressDialog mProgressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Mapbox access token is configured here. This needs to be called either in your application
        // object or in the same activity which contains the mapview.
        MapboxAccountManager.start(this, getString(R.string.access_token));

        // This contains the MapView in XML and needs to be called after the account manager
        setContentView(R.layout.activity_area_selection);


        backBtn = (ImageView) findViewById(R.id.backBtn);
        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });


        // Tool bar with arrow and personnalized title
        Toolbar toolbar = (Toolbar) findViewById(R.id.selectAreaToolbar);
        toolbar.setTitle("");
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
         //  activity.getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        // ((AppCompatActivity) AreaSelectionActivity.this).getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_arrow_back);

        AppController application = (AppController) getApplication();
        mTracker = application.getDefaultTracker();


        //  Fixed Portrait orientation
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        locationServices = LocationServices.getLocationServices(this);

        // Set up the MapView
        mapView = (MapView) findViewById(R.id.mapViewOffline);
        mapView.onCreate(savedInstanceState);
        mapView.getMapAsync(this);

        // Assign progressBar for later use
        //progressBar = (ProgressDialog) findViewById(R.id.progress_bar);

        // Set up the offlineManager
        offlineManager = OfflineManager.getInstance(this);

        // Bottom navigation bar button clicks are handled here.
        // Download offline button
        downloadBtn = (ImageView) findViewById(R.id.downloadBtn);
        downloadBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (isNetworkAvailable(AreaSelectionActivity.this)) {
                    downloadRegionDialog();
                } else {
                    //Toast.makeText(AreaSelectionActivity.this, "Check please your Internet connection", Toast.LENGTH_SHORT).show();
                    new AlertDialog.Builder(AreaSelectionActivity.this)
                            .setTitle(R.string.no_internet)
                            .setMessage(R.string.need_online)
                            .setNeutralButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.dismiss();
                                }
                            })
                            .setIcon(android.R.drawable.ic_dialog_alert)
                            .show();

                }


            }
        });


        floatingActionButton = (FloatingActionButton) findViewById(R.id.location_toggle_fab);
        floatingActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (map != null) {
                    toggleGps(true);
                 }
            }
        });



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

    @Override
    public void onMapReady(MapboxMap mapboxMap) {
        map = mapboxMap;
        if (map != null) {
            goToUserLocation();
           // new DrawAllTrails(AreaSelectionActivity.this).execute();
            drawTrails();
        }
    }

    // Override Activity lifecycle methods
    @Override
    public void onResume() {
        super.onResume();
        mapView.onResume();
        mTracker.setScreenName("Select area (offline maps)");
        mTracker.send(new HitBuilders.ScreenViewBuilder().build());
    }

    @Override
    public void onPause() {
        super.onPause();
        mapView.onPause();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        mapView.onSaveInstanceState(outState);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mapView.onDestroy();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mapView.onLowMemory();
    }



    private void toggleGps(boolean enableGps) {
        if (enableGps) {
            enableLocation(true);
        }
    }

    private void enableLocation(boolean enabled) {
        if (enabled) {
            // If we have the last location of the user, we can move the camera to that position.
            Location lastLocation = locationServices.getLastLocation();
            if (lastLocation != null) {
                map.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(lastLocation), 11));
            }

            locationServices.addLocationListener(new LocationListener() {
                @Override
                public void onLocationChanged(Location location) {
                    if (location != null) {
                        // Move the map camera to where the user location is and then remove the
                        // listener so the camera isn't constantly updating when the user location
                        // changes. When the user disables and then enables the location again, this
                        // listener is registered again and will adjust the camera once again.
                        map.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(location), 11));
                        locationServices.removeLocationListener(this);
                    }
                }
            });
            // floatingActionButton.setImageResource(R.drawable.ic_location_disabled_24dp);
        } /*else {
            floatingActionButton.setImageResource(R.drawable.ic_my_location_24dp);
        }*/
        // Enable or disable the location layer on the map
        // map.setMyLocationEnabled(enabled);
    }

    @Override
    public void onRequestPermissionsResult(
            int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == PERMISSIONS_LOCATION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                enableLocation(true);
            }
        }
    }



    private void goToUserLocation() {
        Location lastLocation = locationServices.getLastLocation();
        if (lastLocation != null) {
            map.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(lastLocation), 11));

            locationServices.addLocationListener(new LocationListener() {
                @Override
                public void onLocationChanged(Location location) {
                    if (location != null) {
                        // Move the map camera to where the user location is and then remove the
                        // listener so the camera isn't constantly updating when the user location
                        // changes. When the user disables and then enables the location again, this
                        // listener is registered again and will adjust the camera once again.
                        map.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(location), 11));
                        locationServices.removeLocationListener(this);
                    }
                }
            });

        }
        // Enable or disable the location layer on the map
        map.setMyLocationEnabled(true);
    }





    protected void downloadRegionDialog() {
        // get prompts.xml view
        LayoutInflater layoutInflater = LayoutInflater.from(AreaSelectionActivity.this);
        View promptView = layoutInflater.inflate(R.layout.input_dialog_select_area, null);
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(AreaSelectionActivity.this);
        alertDialogBuilder.setView(promptView);

        final EditText editText = (EditText) promptView.findViewById(R.id.edittext);
        // setup a dialog window

        AlertDialog.Builder builder = new AlertDialog.Builder(AreaSelectionActivity.this);


        builder.setView(promptView);

        //builder.setTitle("Hey");
        builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String regionName = editText.getText().toString();
                // Require a region name to begin the download.
                // If the user-provided string is empty, display
                // a toast message and do not begin download.
                if (regionName.length() == 0) {
                    //Toast.makeText(AreaSelectionActivity.this, "Region name cannot be empty.", Toast.LENGTH_SHORT).show();
                    displayToast(AreaSelectionActivity.this, " Region name cannot be empty. ", 1);
                } else {
                    // Begin download process
                    downloadRegion(regionName);

                    // formatted for mysql datetime format
                    DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm");
                    dateFormat.setTimeZone(TimeZone.getDefault());
                    Date date = new Date();

                    // Add to Database
                    ActivityDBHelper db =  new ActivityDBHelper(AreaSelectionActivity.this);
                    db.addOfflineMapInDB(regionName, dateFormat.format(date) ,0);
                }

            }
        });
        builder.setNegativeButton(android.R.string.cancel, null);

        final AlertDialog alertDialog = builder.create();
        alertDialog.show();

        Button nbutton = alertDialog.getButton(alertDialog.BUTTON_NEGATIVE);
        nbutton.setTextColor(Color.parseColor("#37647D"));
        alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(false);
        editText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {

                if(s.length()>=1)
                {
                    alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(true);
                    Button pbutton = alertDialog.getButton(alertDialog.BUTTON_POSITIVE);
                    pbutton.setTextColor(Color.parseColor("#37647D"));
                }
                else {
                    alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(false);
                    Button pbutton = alertDialog.getButton(alertDialog.BUTTON_POSITIVE);
                    pbutton.setTextColor(Color.LTGRAY);

                }

            }
        });

    }




    private void downloadRegionDialogOriginal() {
        // Set up download interaction. Display a dialog
        // when the user clicks download button and require
        // a user-provided region name
        AlertDialog.Builder builder = new AlertDialog.Builder(AreaSelectionActivity.this);

        final EditText regionNameEdit = new EditText(AreaSelectionActivity.this);
        regionNameEdit.setHint("Enter name");

        // Build the dialog box
        builder.setTitle("Name new region")
                .setView(regionNameEdit)
                .setMessage("Downloads the map region you currently are viewing")
                .setPositiveButton("Download", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String regionName = regionNameEdit.getText().toString();
                        // Require a region name to begin the download.
                        // If the user-provided string is empty, display
                        // a toast message and do not begin download.
                        if (regionName.length() == 0) {
                            displayToast(AreaSelectionActivity.this, " Region name cannot be empty. ", 1);
                        } else {
                            // Begin download process
                            downloadRegion(regionName);

                            // formatted for mysql datetime format
                            DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm");
                            dateFormat.setTimeZone(TimeZone.getDefault());
                            Date date = new Date();

                            // Add to Database
                            ActivityDBHelper db =  new ActivityDBHelper(AreaSelectionActivity.this);
                            db.addOfflineMapInDB(regionName, dateFormat.format(date) ,0);
                        }
                    }
                })
                .setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });

        // Display the dialog
        builder.show();
    }

    private void downloadRegion(final String regionName) {
        // Define offline region parameters, including bounds,
        // min/max zoom, and metadata

        /*downloadBtn.setEnabled(false);
        downloadBtn.setClickable(false);*/

        // Start the progressBar
        startProgress();

        // Create offline definition using the current
        // style and boundaries of visible map area
        String styleUrl = map.getStyleUrl();
        LatLngBounds bounds = map.getProjection().getVisibleRegion().latLngBounds;
        double minZoom = map.getCameraPosition().zoom;  //10
        double maxZoom = 17 ; // map.getMaxZoom();  //21
        if(minZoom > 14) {
            maxZoom = 20;
        }

        //   Toast.makeText(this," Min ="+minZoom+" , Max ="+maxZoom,Toast.LENGTH_LONG).show();

        float pixelRatio = this.getResources().getDisplayMetrics().density;
        OfflineTilePyramidRegionDefinition definition = new OfflineTilePyramidRegionDefinition(
                styleUrl, bounds, minZoom, maxZoom, pixelRatio);

        // Build a JSONObject using the user-defined offline region title,
        // convert it into string, and use it to create a metadata variable.
        // The metadata varaible will later be passed to createOfflineRegion()
        byte[] metadata;
        try {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put(JSON_FIELD_REGION_NAME, regionName);
            String json = jsonObject.toString();
            metadata = json.getBytes(JSON_CHARSET);
        } catch (Exception exception) {
            Log.e(TAG, "Failed to encode metadata: " + exception.getMessage());
            metadata = null;
        }

        // Create the offline region and launch the download
        offlineManager.createOfflineRegion(definition, metadata, new OfflineManager.CreateOfflineRegionCallback() {
            @Override
            public void onCreate(OfflineRegion offlineRegion) {
                Log.d(TAG, "Offline region created: " + regionName);
                AreaSelectionActivity.this.offlineRegion = offlineRegion;


                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);


                launchDownload();
            }

            @Override
            public void onError(String error) {
                Log.e(TAG, "Error: " + error);
                endProgress(" Failed to download, choose a short map size");
            }
        });
    }

    private void launchDownload() {
        // Set up an observer to handle download progress and
        // notify the user when the region is finished downloading
        offlineRegion.setObserver(new OfflineRegion.OfflineRegionObserver() {
            @Override
            public void onStatusChanged(OfflineRegionStatus status) {
                // Compute a percentage
                double percentage = status.getRequiredResourceCount() >= 0
                        ? (100.0 * status.getCompletedResourceCount() / status.getRequiredResourceCount()) :
                        0.0;

                if (status.isComplete()) {
                    // Download complete
                    endProgress(" Region downloaded successfully. ");
                    return;
                } else if (status.isRequiredResourceCountPrecise()) {
                    // Switch to determinate state
                    setPercentage((int) Math.round(percentage));
                }

                // Log what is being currently downloaded
                Log.d(TAG, String.format("%s/%s resources; %s bytes downloaded.",
                        String.valueOf(status.getCompletedResourceCount()),
                        String.valueOf(status.getRequiredResourceCount()),
                        String.valueOf(status.getCompletedResourceSize())));
            }

            @Override
            public void onError(OfflineRegionError error) {
                Utility.displayToast(AreaSelectionActivity.this,"Error, try again",Toast.LENGTH_LONG);
                Log.e(TAG, "onError reason: " + error.getReason());
                Log.e(TAG, "onError message: " + error.getMessage());

            }

            @Override
            public void mapboxTileCountLimitExceeded(long limit) {
                Log.e(TAG, "Mapbox tile count limit exceeded: " + limit);
                Utility.displayToast(AreaSelectionActivity.this,"You exceed the limit size file",Toast.LENGTH_LONG);
                endProgress(" End with errors");
            }
        });

        // Change the region state
        offlineRegion.setDownloadState(OfflineRegion.STATE_ACTIVE);
    }




    private String getRegionName(OfflineRegion offlineRegion) {
        // Get the retion name from the offline region metadata
        String regionName;

        try {
            byte[] metadata = offlineRegion.getMetadata();
            String json = new String(metadata, JSON_CHARSET);
            JSONObject jsonObject = new JSONObject(json);
            regionName = jsonObject.getString(JSON_FIELD_REGION_NAME);
        } catch (Exception exception) {
            Log.e(TAG, "Failed to decode metadata: " + exception.getMessage());
            regionName = "Region " + offlineRegion.getID();
        }
        return regionName;
    }

    // Progress bar methods
    private void startProgress() {
        // Disable buttons
        downloadBtn.setEnabled(false);
        downloadBtn.setClickable(false);
        downloadBtn.setImageResource(R.drawable.ic_get_app_24dp_grey);

        //chooseBtn.setEnabled(false);

        // Start and show the progress bar
        isEndNotified = false;

        progressBar = new ProgressDialog(this);
        progressBar.setIndeterminate(true);
        progressBar.setProgressNumberFormat(null);
        progressBar.setProgressPercentFormat(null);
       // progressBar.setMax(100);
        progressBar.setMessage(getResources().getString(R.string.take_while));
        progressBar.setTitle(R.string.saving);
        progressBar.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        progressBar.show();


    }

    private void setPercentage(final int percentage) {
        progressBar.setIndeterminate(false);
        progressBar.setProgress(percentage);
    }

    private void endProgress(final String message) {
        // Don't notify more than once
        if (isEndNotified) {
            return;
        }

        // Enable buttons
        downloadBtn.setEnabled(true);
        downloadBtn.setClickable(true);
        downloadBtn.setImageResource(R.drawable.ic_get_app_white);
        //chooseBtn.setEnabled(true);

        // Stop and hide the progress bar
        isEndNotified = true;
        progressBar.setIndeterminate(true);
        /*progressBar.setProgressNumberFormat(null);
        progressBar.setProgressPercentFormat(null);*/
        progressBar.dismiss();

        // Show a toast
        displayToast(AreaSelectionActivity.this, message, 1);
        //Toast.makeText(AreaSelectionActivity.this, message, Toast.LENGTH_LONG).show();
    }


    private void drawTrails(){
        if( MainActivity.listSegments == null){
            return;
        }

        try{

            int size = MainActivity.listSegments.size();
            for(int i = 0; i< size; i++)
            {
                int color = Constants.land;  // Land
                int statusCode = MainActivity.listSegments.get(i).statusCode;
                int categoryCode = MainActivity.listSegments.get(i).categoryCode;

                if(statusCode == 1) {
                    if(categoryCode == 2) {
                        color = Constants.water ; // water
                    }
                    else {
                        color = Constants.land; // land
                    }
                }
                else if (statusCode == 2) {
                    color = Constants.gap;   // gap2017-04-04
                }

                if(categoryCode == 5) {
                    color = Constants.water ; // water
                }

                drawPath(MainActivity.listPoints.get(MainActivity.listSegments.get(i).objectId), color, categoryCode, MainActivity.listSegments.get(i).objectId, MainActivity.listSegments.get(i).trailId );

            }


        }
        catch (Exception e){

        }
    }


    private void drawPath(ArrayList<com.google.android.gms.maps.model.LatLng> points , int color, int categoryCode, int objectId, String trailId)  {


        try {

            if(categoryCode == 5) {
                ArrayList<Position> pointsArray = new ArrayList<Position>();
                for (int i = 0; i < points.size(); i++) {
                    pointsArray.add(Position.fromCoordinates(points.get(i).longitude, points.get(i).latitude));
                }

                // Create the LineString from the list of coordinates and then make a GeoJSON
                // FeatureCollection so we can add the line to our map as a layer.
                LineString lineString = LineString.fromCoordinates(pointsArray);

                FeatureCollection featureCollection =
                        FeatureCollection.fromFeatures(new Feature[]{Feature.fromGeometry(lineString)});

                Source geoJsonSource = new GeoJsonSource("layer-"+objectId+"-"+trailId, featureCollection);

                map.addSource(geoJsonSource);

                LineLayer lineLayer = new LineLayer( "layer-"+objectId+"-"+trailId, "layer-"+objectId+"-"+trailId);

                // The layer properties for our line. This is where we make the line dotted, set the
                // color, etc.
                lineLayer.setProperties(
                        PropertyFactory.lineDasharray(new Float[]{2f, 2f}),
                        PropertyFactory.lineCap(Property.LINE_CAP_ROUND),
                        PropertyFactory.lineJoin(Property.LINE_JOIN_ROUND),
                        PropertyFactory.lineWidth(5f),
                        PropertyFactory.lineColor(Constants.water)
                );

                map.addLayer(lineLayer);


            }
            else {
                LatLng[] pointsArray = new LatLng[points.size()];
                for (int i = 0; i < points.size(); i++) {
                    pointsArray[i] = new LatLng(points.get(i).latitude, points.get(i).longitude);
                }

                map.addPolyline(new PolylineOptions()
                        .add(pointsArray)
                        .color(color)
                        .width(5));

            }


        }
        catch(Exception e){}


    }




    public boolean isNetworkAvailable(final Context context) {
        final ConnectivityManager connectivityManager = ((ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE));
        return connectivityManager.getActiveNetworkInfo() != null && connectivityManager.getActiveNetworkInfo().isConnected();
    }



    private class DrawAllTrails extends AsyncTask<String, Integer, String> {

        private Context context;

        public DrawAllTrails(Context context) {
            this.context = context;
        }

        @Override
        protected String doInBackground(String... param) {

            drawTrails();

            return null;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            mProgressDialog = ProgressDialog.show(AreaSelectionActivity.this, getResources().getString(R.string.rendering), getResources().getString(R.string.beautifing), true);

            mProgressDialog.setCancelable(false);
            mProgressDialog.show();
        }

        @Override
        protected void onProgressUpdate(Integer... progress) {
            super.onProgressUpdate(progress);
            // if we get here, length is known, now set indeterminate to false
            /*mProgressDialog.setIndeterminate(false);
            mProgressDialog.setMax(100);
            mProgressDialog.setProgress(progress[0]);*/
        }

        @Override
        protected void onPostExecute(String result) {

            mProgressDialog.dismiss();


        }

    }


}


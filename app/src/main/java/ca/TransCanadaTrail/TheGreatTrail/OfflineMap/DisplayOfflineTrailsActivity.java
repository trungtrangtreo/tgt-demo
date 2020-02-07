package ca.TransCanadaTrail.TheGreatTrail.OfflineMap;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.database.Cursor;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.widget.TextView;

import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.mapbox.mapboxsdk.MapboxAccountManager;
import com.mapbox.mapboxsdk.annotations.Icon;
import com.mapbox.mapboxsdk.annotations.IconFactory;
import com.mapbox.mapboxsdk.annotations.MarkerViewOptions;
import com.mapbox.mapboxsdk.annotations.PolylineOptions;
import com.mapbox.mapboxsdk.camera.CameraPosition;
import com.mapbox.mapboxsdk.camera.CameraUpdateFactory;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.geometry.LatLngBounds;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;
import com.mapbox.mapboxsdk.offline.OfflineManager;
import com.mapbox.mapboxsdk.offline.OfflineRegion;
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

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;

import ca.TransCanadaTrail.TheGreatTrail.AppController;
import ca.TransCanadaTrail.TheGreatTrail.Constants;
import ca.TransCanadaTrail.TheGreatTrail.MainActivity;
import ca.TransCanadaTrail.TheGreatTrail.R;
import ca.TransCanadaTrail.TheGreatTrail.database.AmenityDBHelperTrail;

//import com.google.android.gms.maps.model.LatLngBounds;

public class DisplayOfflineTrailsActivity extends AppCompatActivity {

    private static final String TAG = "LocationService";
    public static AppCompatActivity activity;



    // UI elements
    private MapView mapView;
    private MapboxMap map;
    private ProgressDialog mProgressDialog;

    private Icon parkingIcon;
    private Icon accessPointIcon;
    private Icon restAreaIcon;

    private Tracker mTracker;

    ArrayList<OfflineMarker> accessPoints = new  ArrayList<OfflineMarker>();
    ArrayList<OfflineMarker> parking = new  ArrayList<OfflineMarker>();
    ArrayList<OfflineMarker> restAreas = new  ArrayList<OfflineMarker>();


    private int regionSelected;

    // Offline objects
    private OfflineManager offlineManager;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Mapbox access token is configured here. This needs to be called either in your application
        // object or in the same activity which contains the mapview.
        MapboxAccountManager.start(this, getString(R.string.access_token));

        // This contains the MapView in XML and needs to be called after the account manager
        setContentView(R.layout.activity_display_offline_trails);

        // Set up the offlineManager
        offlineManager = OfflineManager.getInstance(this);
        regionSelected = getIntent().getIntExtra("regionSelected",0);
        String title = getIntent().getStringExtra("regionName");

        AppController application = (AppController) getApplication();
        mTracker = application.getDefaultTracker();


        // Set up the MapView
        mapView = (MapView) findViewById(R.id.displayMapViewOffline);
        mapView.onCreate(savedInstanceState);



       /* mapView.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(MapboxMap mapboxMap) {
                map = mapboxMap;

               // addMarkers();
                drawTrails();
                new DisplayOfflineTrailsActivity.PutMarkers(DisplayOfflineTrailsActivity.this).execute();

                *//*map.setOnCameraChangeListener(new MapboxMap.OnCameraChangeListener() {
                    @Override
                    public void onCameraChange(CameraPosition position) {
                       drawTrails();
                       addMarkers();
                      //  Log.e("LocationService", "Zoom max = " + map.getMaxZoom()+"  zoom min = "+ map.getMinZoom()+" Camera Position= "+position.zoom  );

                    }
                });*//*

            }
        });
*/


        TextView display_toolbar_title = (TextView) findViewById(R.id.display_toolbar_title);
        display_toolbar_title.setText(title);


        // Tool bar with arrow and personnalized title

        Toolbar toolbar = (Toolbar) findViewById(R.id.displayOfflineTrailToolbar);
        toolbar.setTitle("");
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        activity = DisplayOfflineTrailsActivity.this;
        activity.getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        ((AppCompatActivity) DisplayOfflineTrailsActivity.this).getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_arrow_back);

        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);     //  Fixed Portrait orientation


        IconFactory iconFactory = IconFactory.getInstance(this);
        Drawable iconDrawable = ContextCompat.getDrawable(this, R.drawable.parking_marker);
        parkingIcon = iconFactory.fromDrawable(iconDrawable);

        iconDrawable = ContextCompat.getDrawable(this, R.drawable.access_point_marker);
        accessPointIcon = iconFactory.fromDrawable(iconDrawable);

        iconDrawable = ContextCompat.getDrawable(this, R.drawable.rest_area_marker);
        restAreaIcon = iconFactory.fromDrawable(iconDrawable);




        mapView.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(MapboxMap mapboxMap) {

                // Customize map with markers, polylines, etc.

                map = mapboxMap;
                displayRegion();

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



    // Override Activity lifecycle methods
    @Override
    public void onResume() {
        super.onResume();
        mapView.onResume();

        mTracker.setScreenName("Display offline maps");
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


    private void displayRegion() {

        // Query the DB asynchronously
        offlineManager.listOfflineRegions(new OfflineManager.ListOfflineRegionsCallback() {
            @Override
            public void onList(final OfflineRegion[] offlineRegions) {
                // Get the region bounds and zoom
                LatLngBounds bounds = ((OfflineTilePyramidRegionDefinition)
                        offlineRegions[regionSelected].getDefinition()).getBounds();
                double regionZoom = ((OfflineTilePyramidRegionDefinition)
                        offlineRegions[regionSelected].getDefinition()).getMinZoom();


                // Create new camera position
                CameraPosition cameraPosition = new CameraPosition.Builder()
                        .target(bounds.getCenter())
                        .zoom(regionZoom)
                        .build();

                // Move camera to new position

                try {
                    if (CameraUpdateFactory.newCameraPosition(cameraPosition) != null ) {
                        map.moveCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));

                        new DisplayOfflineTrailsActivity.PutMarkers(DisplayOfflineTrailsActivity.this).execute();

                    }
                }
                catch(Exception E){}; // Toast.makeText(DisplayOfflineTrailsActivity.this,"Error to download, check your connecxion",Toast.LENGTH_LONG).show();


            }

            @Override
            public void onError(String error) {
                Log.e(TAG, "Error: " + error);
            }
        });
    }



    private void drawTrails(){
        if( MainActivity.listSegments == null){
            return;
        }

        try{

            if(MainActivity.listSegments != null && MainActivity.listPoints != null ) {
                for(int i = 0; i< MainActivity.listSegments.size(); i++)
                {

                    int lenghtListPoints = MainActivity.listPoints.get(MainActivity.listSegments.get(i).objectId).size();

                    LatLngBounds bounds = map.getProjection().getVisibleRegion().latLngBounds;
                    com.google.android.gms.maps.model.LatLngBounds myBounds = new  com.google.android.gms.maps.model.LatLngBounds
                            (new com.google.android.gms.maps.model.LatLng(bounds.getLatSouth()-3, bounds.getLonWest()-3) ,
                                    new com.google.android.gms.maps.model.LatLng(bounds.getLatNorth()+3, bounds.getLonEast()+3));

                    if (	(lenghtListPoints < 8)   && (   myBounds.contains(MainActivity.listPoints.get(MainActivity.listSegments.get(i).objectId).get(0))   ||
                            myBounds.contains(MainActivity.listPoints.get(MainActivity.listSegments.get(i).objectId).get(lenghtListPoints-1)))
                            ||
                            ((lenghtListPoints >= 8) &&   (	myBounds.contains(MainActivity.listPoints.get(MainActivity.listSegments.get(i).objectId).get(0)) 						||
                                    myBounds.contains(MainActivity.listPoints.get(MainActivity.listSegments.get(i).objectId).get(lenghtListPoints/6)) 	||
                                    myBounds.contains(MainActivity.listPoints.get(MainActivity.listSegments.get(i).objectId).get(2*lenghtListPoints/6)) 	||
                                    myBounds.contains(MainActivity.listPoints.get(MainActivity.listSegments.get(i).objectId).get(3*lenghtListPoints/6))  	||
                                    myBounds.contains(MainActivity.listPoints.get(MainActivity.listSegments.get(i).objectId).get(4*lenghtListPoints/6))  	||
                                    myBounds.contains(MainActivity.listPoints.get(MainActivity.listSegments.get(i).objectId).get(5*lenghtListPoints/6)) 	||
                                    myBounds.contains(MainActivity.listPoints.get(MainActivity.listSegments.get(i).objectId).get(lenghtListPoints-1))      )  )    ){



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
                            color = Constants.gap;   // gap
                        }

                        if(categoryCode == 5) {
                            color = Constants.water ; // water
                        }

                        drawPath(MainActivity.listPoints.get(MainActivity.listSegments.get(i).objectId), color, categoryCode, MainActivity.listSegments.get(i).objectId, MainActivity.listSegments.get(i).trailId );


                    }

                }
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




    private void addMarkers(){
        AmenityDBHelperTrail db =  AmenityDBHelperTrail.getInstance(this);
        Cursor cursor = null;
        cursor = db.getAmenityCoordinatesAndType();

        String previousGeometry = "";

        if (cursor != null && cursor.moveToFirst()) {
            do {
                int amenityType = cursor.getInt(cursor.getColumnIndex("type_amenity"));
                String geometry =  cursor.getString(cursor.getColumnIndex("geometry"));
                String nameAmenity = cursor.getString(cursor.getColumnIndex("name_amenity"));  //  name_amenity

                if(geometry.equals(previousGeometry)){
                    previousGeometry = new String(geometry);
                    continue;
                }

                previousGeometry = new String(geometry);
                LatLng coordinate = decodeJSON(geometry);

                LatLngBounds bounds = map.getProjection().getVisibleRegion().latLngBounds;
                com.google.android.gms.maps.model.LatLngBounds myBounds = new  com.google.android.gms.maps.model.LatLngBounds
                        (new com.google.android.gms.maps.model.LatLng(bounds.getLatSouth()-2, bounds.getLonWest()-2) ,
                                new com.google.android.gms.maps.model.LatLng(bounds.getLatNorth()+2, bounds.getLonEast()+2));

                if(myBounds.contains(new com.google.android.gms.maps.model.LatLng(coordinate.getLatitude(),coordinate.getLongitude()))) {
                    switch(amenityType) {
                        case 1:
                            parking.add(new OfflineMarker(coordinate,nameAmenity));
                            break;
                        case 2:
                            accessPoints.add(new OfflineMarker(coordinate,nameAmenity));
                            break;
                        case 3:
                            restAreas.add(new OfflineMarker(coordinate,nameAmenity));
                            break;
                    }
                 }


            }
            while (cursor.moveToNext());
            cursor.close();
        }

        db.close();

        addAllMarkers(parking, parkingIcon);
        addAllMarkers(accessPoints, accessPointIcon);
        addAllMarkers(restAreas, restAreaIcon);

    }




    private void addAllMarkers(ArrayList<OfflineMarker> pointsList, Icon icon){

        for(OfflineMarker point : pointsList){
            map.addMarker(new MarkerViewOptions()
                    .title(point.getTitle())
                    .position(point.getCoordinate())
                    .icon(icon));
        }

    }


    private LatLng decodeJSON(String stringJSON) {
        LatLng point = new LatLng(0.0,0.0);
        if( !stringJSON.equals("") ) {
            try {
                JSONArray jsonArray = new JSONArray(stringJSON);
                if (jsonArray != null) {
                    point = new LatLng(Double.parseDouble(jsonArray.get(1).toString()), Double.parseDouble(jsonArray.get(0).toString()));
                }

            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return point;
    }






    private class PutMarkers extends AsyncTask<String, Integer, String> {

        private Context context;

        public PutMarkers(Context context) {
            this.context = context;
        }

        @Override
        protected String doInBackground(String... param) {

            //  drawTrails();
            addMarkers();
            drawTrails();


            return null;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            mProgressDialog = ProgressDialog.show(DisplayOfflineTrailsActivity.this, getResources().getString(R.string.rendering), getResources().getString(R.string.beautifing), true);

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

            try {
                mProgressDialog.dismiss();
            }
            catch (Exception e){};


        }

    }


    private class OfflineMarker {

        public OfflineMarker(LatLng coordinate, String title) {
            this.coordinate = coordinate;
            this.title = title;
        }

        public LatLng getCoordinate() {
            return coordinate;
        }

        public void setCoordinate(LatLng coordinate) {
            this.coordinate = coordinate;
        }

        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            this.title = title;
        }

        private LatLng coordinate;
        private String title;
    }

}



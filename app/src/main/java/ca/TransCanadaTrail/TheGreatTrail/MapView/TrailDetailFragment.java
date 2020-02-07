package ca.TransCanadaTrail.TheGreatTrail.MapView;

import android.content.Context;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.database.Cursor;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.text.Html;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.Dash;
import com.google.android.gms.maps.model.Gap;
import com.google.android.gms.maps.model.JointType;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PatternItem;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.maps.model.RoundCap;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import ca.TransCanadaTrail.TheGreatTrail.Constants;
import ca.TransCanadaTrail.TheGreatTrail.MainActivity;
import ca.TransCanadaTrail.TheGreatTrail.R;
import ca.TransCanadaTrail.TheGreatTrail.database.ActivityDBHelperTrail;

import static android.content.Context.LOCATION_SERVICE;
import static ca.TransCanadaTrail.TheGreatTrail.ActivityTracker.ActivityTrackerFragment.trackerfragStack;
import static ca.TransCanadaTrail.TheGreatTrail.ActivityTracker.ActivityTrackerFragment.trackerfragTagStack;
import static ca.TransCanadaTrail.TheGreatTrail.MainActivity.REQUEST_ID_ACCESS_COURSE_FINE_LOCATION;
import static ca.TransCanadaTrail.TheGreatTrail.MainActivity.currentTab;
import static ca.TransCanadaTrail.TheGreatTrail.MapView.MapFragment.mapfragStack;
import static ca.TransCanadaTrail.TheGreatTrail.MapView.MapFragment.mapfragTagStack;
import static ca.TransCanadaTrail.TheGreatTrail.MeasureTool.MeasureFragment.measurefragStack;
import static ca.TransCanadaTrail.TheGreatTrail.MeasureTool.MeasureFragment.measurefragTagStack;
import static ca.TransCanadaTrail.TheGreatTrail.SearchListFragment.resourceID;


public class TrailDetailFragment extends Fragment implements NavigationView.OnNavigationItemSelectedListener {

    public static TrailDetailFragment instance = null;
    private static GoogleMap myMap;
    protected MapView mMapView;
    Location myLocation;
    private TextView infoTxt,lengthTxt;
    LinearLayout linearlayout;
    int objectId;
    String trailName;
    String trailID;
    private float currentZoom = 0f;
    private float lastZoom = 0f;
    private FloatingActionButton floatingActionButton;
    private GoogleApiClient mGoogleApiClient;
    public static HashMap<Integer, Polyline> listVisibleSegments = null;
    LatLng focusPoint = new LatLng(0,0);
    TrailSegment trail;
    MainActivity activity;

    private static final int POLYLINE_STROKE_WIDTH_PX = 12;
    private static final int PATTERN_DASH_LENGTH_PX = 40;
    private static final int PATTERN_GAP_LENGTH_PX = 20;
    private static final PatternItem DASH = new Dash(PATTERN_DASH_LENGTH_PX);
    private static final PatternItem GAP = new Gap(PATTERN_GAP_LENGTH_PX);

    // Create a stroke pattern of a gap followed by a dot.
    private static final List<PatternItem> PATTERN_POLYLINE_DOTTED = Arrays.asList(GAP, DASH);


    private LatLng startPoint;
    private LatLng endPoint ;


    public static TrailDetailFragment getInstance() {
        if (instance == null) {
            instance = new TrailDetailFragment();
        }
        return instance;
    }

    private static TrailDetailFragment newInstance() {
        TrailDetailFragment trailFragment = getInstance();

        return trailFragment;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        activity=(MainActivity)context;

    }

//    @Override
//    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
//        super.onCreateOptionsMenu(menu, inflater);
//        menu.clear();
//    }
//
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view=inflater.inflate(R.layout.trail_detail,container,false);

        activity.getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        activity.getSupportActionBar().setDisplayShowHomeEnabled(true);

        mMapView = (MapView) view.findViewById(R.id.mapview1);
        mMapView.onCreate(savedInstanceState);
        //setHasOptionsMenu(true);
        infoTxt = (TextView)  view.findViewById(R.id.infoTxt);
        lengthTxt = (TextView) view.findViewById(R.id.trail_lengthTxt);
        linearlayout=(LinearLayout)view.findViewById(R.id.linearlayout);
        linearlayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                pushTrailFragmentToStack();
                FragmentManager fragmentManager = activity.getSupportFragmentManager();

                SegmentDetailsFragment segmentDetailsFragment = SegmentDetailsFragment.getInstance();

                if (fragmentManager.findFragmentByTag("SegmentDetailsFragment")== null){
                    segmentDetailsFragment.setObjectId(objectId);
                    Bundle args = new Bundle();
                    args.putString("trailId", trailID);
                    args.putString("trail_name",trailName);
                    segmentDetailsFragment.setArguments(args);
                }
                replaceFragment(resourceID,segmentDetailsFragment);
            }
        });

        trailID=getArguments().getString("trailid");

        ActivityDBHelperTrail db = new ActivityDBHelperTrail(getContext());
        Cursor cursor = null;

        cursor = db.getTrailByID(trailID);
        if (cursor != null && cursor.moveToFirst()) {
            trail= new TrailSegment(cursor);
        }
        objectId=trail.getObjectId();
        trailName=trail.getTrailName();
        activity.getSupportActionBar().setTitle(Html.fromHtml("<small>"+trailName+"</small>"));


        Log.d("LocationService",trail.getTrailId()+":"+trail.getTrailName()+trail.getGeometry());

        ArrayList<LatLng> listeCoordinates = decodeJSON(trail.getGeometry());
        LatLng coordinateCenter = listeCoordinates.get(listeCoordinates.size()/2);
        focusPoint = new LatLng(coordinateCenter.latitude,coordinateCenter.longitude);

        if (mGoogleApiClient == null) {
            mGoogleApiClient = new GoogleApiClient.Builder(activity)
                    .addApi(LocationServices.API)
                    .build();
        }
        floatingActionButton = (FloatingActionButton) view.findViewById(R.id.location_toggle_fab);
        floatingActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (Build.VERSION.SDK_INT >= 23) {
                    int accessCoarsePermission
                            = ContextCompat.checkSelfPermission(activity, android.Manifest.permission.ACCESS_COARSE_LOCATION);
                    int accessFinePermission
                            = ContextCompat.checkSelfPermission(activity, android.Manifest.permission.ACCESS_FINE_LOCATION);

                    if (accessCoarsePermission != PackageManager.PERMISSION_GRANTED
                            || accessFinePermission != PackageManager.PERMISSION_GRANTED) {
                        // The Permissions to ask user.
                        String[] permissions = new String[]{android.Manifest.permission.ACCESS_COARSE_LOCATION,
                                android.Manifest.permission.ACCESS_FINE_LOCATION};
                        // Show a dialog asking the user to allow the above permissions.
                        ActivityCompat.requestPermissions(activity, permissions,
                                REQUEST_ID_ACCESS_COURSE_FINE_LOCATION);

                        return;
                    }
                }

                Location mLastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);

                if (mLastLocation != null) {
                    /*LatLng latLng = new LatLng(mLastLocation.latitude, mLastLocation.longitude);
                    myMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 13));*/

                    LatLng firstPoint = new LatLng(mLastLocation.getLatitude(), mLastLocation.getLongitude());
                    LatLng secondePoint = nearestPoint(firstPoint);
                    if(secondePoint == null) {
                        return;
                    }
                    LatLngBounds.Builder builder = new LatLngBounds.Builder();

                    builder.include(firstPoint);
                    builder.include(secondePoint);

                    LatLngBounds bounds = builder.build();

                    CameraUpdate cu = CameraUpdateFactory.newLatLngBounds(bounds, mMapView.getWidth(), mMapView.getHeight(), mMapView.getWidth()*15 / 100);
                    myMap.moveCamera(cu);
                    float zoom = myMap.getCameraPosition().zoom;
                    cu = CameraUpdateFactory.newLatLngZoom(firstPoint, zoom-0.5f);     //newLatLngBounds(bounds, mMapView.getWidth(), mMapView.getHeight(), mMapView.getWidth()*45 / 100);
                    myMap.moveCamera(cu);

                    zoomOut();
                }


            }
        });



        listVisibleSegments =  new HashMap<Integer,Polyline>();

        return view;
    }

    private void replaceFragment(int resourceID, Fragment trailFragment) {
        FragmentTransaction mFragmentTransaction = activity.getSupportFragmentManager().beginTransaction();
        mFragmentTransaction
                .replace(resourceID, trailFragment)
                .commit();
    }

    private void pushTrailFragmentToStack() {
        String actlogtag = "TrailDetailFragment";
        FragmentManager fragmentManager = activity.getSupportFragmentManager();
        Fragment fsegment=null;

        switch (currentTab) {
            case "MapFragment":
                actlogtag="Map"+actlogtag;
                fsegment=fragmentManager.findFragmentById(R.id.searchLayout);
                if (fsegment instanceof TrailDetailFragment) {
                    mapfragStack.put(actlogtag, fsegment);
                    mapfragTagStack.push(actlogtag);
                }
                break;
            case "MeasureFragment":
                actlogtag="Measure"+actlogtag;
                fsegment=fragmentManager.findFragmentById(R.id.measureSearchLayout);
                if (fsegment instanceof TrailDetailFragment) {
                    measurefragStack.put(actlogtag, fsegment);
                    measurefragTagStack.push(actlogtag);
                }
                break;
            case "ActivityTrackerFragment":
                actlogtag="Tracker"+actlogtag;
                fsegment=fragmentManager.findFragmentById(R.id.trackerSearchLayout);
                if (fsegment instanceof TrailDetailFragment) {
                    trackerfragStack.put(actlogtag, fsegment);
                    trackerfragTagStack.push(actlogtag);
                }
                break;
        }

    }


    private  ArrayList<LatLng> decodeJSON(String stringJSON) {
        ArrayList<LatLng> listPoints = new ArrayList<LatLng>();

        if( !stringJSON.equals("") ) {

            try {

                JSONArray jsonArray = new JSONArray(stringJSON);
                if (jsonArray != null) {
                    int len = jsonArray.length();
                    for (int i=0;i<len;i++){
                        JSONArray jsonArrayPoint = new JSONArray(jsonArray.get(i).toString());
                        listPoints.add( new LatLng(jsonArrayPoint.getDouble(1),jsonArrayPoint.getDouble(0)));
                    }
                }

            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return listPoints;
    }


    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {

        super.onCreateOptionsMenu(menu, inflater);

        if(currentTab.equals("MapFragment") && mapfragStack.containsKey("MapSearchFragment")){
            menu.clear();
        }
        else if(currentTab.equals("MeasureFragment") && measurefragStack.containsKey("MeasureSearchFragment")){
            menu.clear();
        }
        else if(currentTab.equals("ActivityTrackerFragment") && trackerfragStack.containsKey("TrackerSearchFragment")){
            menu.clear();
        }
    }


    private void askPermission() {

        // With API> = 23, you have to ask the user for permission to view their location.
        if (Build.VERSION.SDK_INT >= 23) {
            int accessCoarsePermission = ContextCompat.checkSelfPermission(activity, android.Manifest.permission.ACCESS_COARSE_LOCATION);
            int accessFinePermission = ContextCompat.checkSelfPermission(activity, android.Manifest.permission.ACCESS_FINE_LOCATION);

            if (accessCoarsePermission != PackageManager.PERMISSION_GRANTED
                    || accessFinePermission != PackageManager.PERMISSION_GRANTED) {
                // The Permissions to ask user.
                String[] permissions = new String[]{android.Manifest.permission.ACCESS_COARSE_LOCATION,android.Manifest.permission.ACCESS_FINE_LOCATION};
                // Show a dialog asking the user to allow the above permissions.
                ActivityCompat.requestPermissions(activity, permissions, REQUEST_ID_ACCESS_COURSE_FINE_LOCATION);

            }
        }
    }



    @Override
    public void onStart() {
        askPermission();
        // Set callback listener, on Google Map ready.
        mGoogleApiClient.connect();
        mMapView.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(GoogleMap googleMap) {
                onMyMapReady(googleMap);
                zoomIn();
                retriveTrail();

            }
        });
        super.onStart();
    }

    @Override
    public void onStop() {
        //  getActivity().unregisterReceiver(locationReceiver);
        mGoogleApiClient.disconnect();
        super.onStop();
        if (mMapView != null) {
            mMapView.onStop();
        }
    }

    private void retriveTrail() {
        try
        {

            infoTxt.setText(trail.getTrailName());
            String sumLengthKm=trail.getSumLengthKm()+" Km";
            lengthTxt.setText(sumLengthKm);

            //listVisibleSegments.get(selectedSegmentId).setWidth(21);
            selectSegment();
            focusPoint=listVisibleSegments.get(objectId).getPoints().get(0);



            LatLngBounds.Builder builder = new LatLngBounds.Builder();
           /* builder.include(listVisibleSegments.get(selectedSegmentId).getPoints().get(0));
            builder.include(listVisibleSegments.get(selectedSegmentId).getPoints().get(listVisibleSegments.get(selectedSegmentId).getPoints().size()-1));*/
            startPoint =listVisibleSegments.get(objectId).getPoints().get(0);
            endPoint = listVisibleSegments.get(objectId).getPoints().get(listVisibleSegments.get(objectId).getPoints().size()-1);
            centerTrail(objectId);
            builder.include(startPoint);
            builder.include(endPoint);

            LatLngBounds bounds = builder.build();
            int padding = dpToPx(110) ; // offset from edges of the map in pixels
            //CameraUpdate cu = CameraUpdateFactory.newLatLngBounds(bounds, padding);
            myMap.moveCamera(CameraUpdateFactory.newLatLngBounds(bounds, padding));
            zoomOut();

            MapFragment mapFragment = ((MainActivity) getActivity()).getMapFragment();

            if(mapFragment == null)
                return;
            //mapFragment.myMap.moveCamera(CameraUpdateFactory.newLatLngZoom(myMap.getCameraPosition().target, myMap.getCameraPosition().zoom));
            //center = focusPoint;
            //zoom = myMap.getCameraPosition().zoom;
            mapFragment.myMap.moveCamera(CameraUpdateFactory.newLatLngBounds(bounds, padding));
            mapFragment.syncMapSearch();
          //  mapFragment.goInMap(center, 11);
        }
        catch(Exception e)
        {
            e.printStackTrace();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mMapView != null) {
            mMapView.onResume();
        }
    }

    @Override
    public void onPause() {
        if (mMapView != null) {
            mMapView.onPause();
        }
        super.onPause();
    }

    @Override
    public void onDestroy()
    {
        if (mMapView != null) {
            try {
                mMapView.onDestroy();
            } catch (NullPointerException e) {
                Log.e("LocationService", "Error while attempting MapView.onDestroy(), ignoring exception", e);
            }
        }
        super.onDestroy();
    }

    @Override
    public void onLowMemory()
    {
        super.onLowMemory();
        if (mMapView != null) {
            mMapView.onLowMemory();
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState)
    {
        super.onSaveInstanceState(outState);
        if (mMapView != null) {
            mMapView.onSaveInstanceState(outState);
        }
    }

    public GoogleMap.OnCameraIdleListener getCameraChangeListener()
    {
        return  new GoogleMap.OnCameraIdleListener()
        {

            @Override
            public void onCameraIdle() {

                currentZoom = myMap.getCameraPosition().zoom;

                if (currentZoom < 14) {
                    addPolylineToMap();
                    lastZoom = currentZoom ;
                }


            }
        };
    }

    private void zoomOut(){

        if(myMap != null)
        {

            //This is the current user-viewable region of the map
            LatLngBounds bounds = myMap.getProjection().getVisibleRegion().latLngBounds;

            //Loop through all the items that are available to be placed on the map
            for(int i=0; i<MainActivity.listSegments.size(); i++)
            {


                int lenghtListPoints = MainActivity.listPoints.get(MainActivity.listSegments.get(i).objectId).size();

                if((lenghtListPoints < 8 && (bounds.contains(MainActivity.listPoints.get(MainActivity.listSegments.get(i).objectId).get(0))  || bounds.contains(MainActivity.listPoints.get(MainActivity.listSegments.get(i).objectId).get(lenghtListPoints-1)))) ||
                        ( (lenghtListPoints >= 8) && (bounds.contains(MainActivity.listPoints.get(MainActivity.listSegments.get(i).objectId).get(0))  || bounds.contains(MainActivity.listPoints.get(MainActivity.listSegments.get(i).objectId).get(lenghtListPoints/6)) || bounds.contains(MainActivity.listPoints.get(MainActivity.listSegments.get(i).objectId).get(2*lenghtListPoints/6)) ||
                                bounds.contains(MainActivity.listPoints.get(MainActivity.listSegments.get(i).objectId).get(3*lenghtListPoints/6))  || bounds.contains(MainActivity.listPoints.get(MainActivity.listSegments.get(i).objectId).get(4*lenghtListPoints/6))  || bounds.contains(MainActivity.listPoints.get(MainActivity.listSegments.get(i).objectId).get(5*lenghtListPoints/6)) ||
                                bounds.contains(MainActivity.listPoints.get(MainActivity.listSegments.get(i).objectId).get(lenghtListPoints-1)))  )     ){


                    //If the trail isn't already being displayed
                    if(!listVisibleSegments.containsKey(MainActivity.listSegments.get(i).objectId))
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
                            color = Constants.gap;   // gap
                        }

                        if(categoryCode == 5) {
                            color = Constants.water ; // water
                        }

                        //Add Polyline to the Map and keep track of it with the HashMap
                        if(currentZoom >= 6) {
                            Polyline polyline = drawPath3(myMap,MainActivity.listPoints.get(MainActivity.listSegments.get(i).objectId), color, categoryCode ) ;
                            listVisibleSegments.put(MainActivity.listSegments.get(i).objectId, polyline);


                        }
                        else {
                            Polyline polyline = drawPath33(myMap,MainActivity.listPoints.get(MainActivity.listSegments.get(i).objectId), color, categoryCode ) ;
                            listVisibleSegments.put(MainActivity.listSegments.get(i).objectId, polyline);


                        }

                    }
                }

                //If the marker is off screen
                else
                {
                    //If the course was previously on screen
                    if(listVisibleSegments.containsKey(MainActivity.listSegments.get(i).objectId))
                    {
                        listVisibleSegments.get(MainActivity.listSegments.get(i).objectId).remove();
                        listVisibleSegments.remove(MainActivity.listSegments.get(i).objectId);
                    }
                }


            }
        }
        //addSelectedPolylines();
        selectSegment();
    }



    private void zoomIn(){

        if(myMap != null)
        {
            //This is the current user-viewable region of the map
            LatLngBounds bounds = myMap.getProjection().getVisibleRegion().latLngBounds;

            for(int objectId : new ArrayList<Integer> (listVisibleSegments.keySet()) )  // listSegments remplaced by listVisibleSegments
            {
                int lenListPoints = MainActivity.listPoints.get(objectId).size();

                boolean containsCoordinate = false;
                for(int j=0; j<lenListPoints ; j++) {

                    if(bounds.contains(MainActivity.listPoints.get(objectId).get(j)) )
                    {
                        containsCoordinate = true;
                        break;
                    }

                }

                if (!containsCoordinate) {
                    listVisibleSegments.get(objectId).remove();
                    listVisibleSegments.remove(objectId);
                }

            }
        }
    }

    private void addPolylineToMap()
    {
        if (currentZoom >= 6) {

            if (currentZoom == lastZoom ) {
                // zoomIn();
                zoomOut();

                // persistant large trail if selected
                /*if(selectedSegmentId != 0){
                    if(currentZoom >= 6) {
                        Polyline polyline = drawPath3(myMap, MainActivity.listPoints.get(selectedSegmentId), Constants.land ) ;
                        listVisibleSegments.put(selectedSegmentId, polyline);
                    }
                    else {
                        Polyline polyline = drawPath33(myMap, MainActivity.listPoints.get(selectedSegmentId), Constants.land ) ;
                        listVisibleSegments.put(selectedSegmentId, polyline);
                    }
                }*/



            }
            else  if (currentZoom > lastZoom) {
                if (lastZoom < 6){
                    listVisibleSegments.clear();
                    myMap.clear();
                    zoomOut();
                    addtrailWarnings();
                    if(objectId != 0 && listVisibleSegments.get(objectId) != null) {
                        //listVisibleSegments.get(selectedSegmentId).setWidth(21);
                        selectSegment();
                    }

                }
                else {
                    zoomIn();
                }

            }

            else  if (currentZoom < lastZoom ) {
                zoomOut();
            }
        }
        else if (lastZoom >= 6){
            listVisibleSegments.clear();
            myMap.clear();
            zoomOut();
            addtrailWarnings();
            Polyline line =listVisibleSegments.get(objectId);
            if(objectId != 0 && line!=null)  {
                //line.setWidth(21);
                selectSegment();
            }
        }
        else if (lastZoom < 6){
            zoomOut();
        }

    }


    private Polyline drawPath3(GoogleMap googleMap, List<LatLng> points, int colorLine, int categoryCode) {
        int lineWidth = 9;

        if(categoryCode == 5) {
            Polyline polyline = googleMap.addPolyline(new PolylineOptions()
                    //  .clickable(true)
                    .addAll(points));

            stylePolyline(polyline);
            polyline.setPattern(PATTERN_POLYLINE_DOTTED);
            return polyline;
        }
        else{
            PolylineOptions polylineOptions = new PolylineOptions().width(lineWidth).color(colorLine);
            polylineOptions.addAll(points);
            Polyline polyline = myMap.addPolyline(polylineOptions);
            //  Log.i(TrackService.TAG, "I draw  -------------------------------------------------------------------------------Id = "+polyline.getId());
            return polyline;
        }

    }


    /**
     * Styles the polyline, based on type.
     * @param polyline The polyline object that needs styling.
     */
    private void stylePolyline(Polyline polyline) {
        polyline.setStartCap(new RoundCap());
        polyline.setEndCap(new RoundCap());
        polyline.setWidth(POLYLINE_STROKE_WIDTH_PX);
        polyline.setColor(Constants.water);
        polyline.setJointType(JointType.ROUND);
    }


    private Polyline drawPath33(GoogleMap googleMap, List<LatLng> points, int colorLine, int categoryCode) {
        int lineWidth = 9;

        if(categoryCode == 5) {
            Polyline polyline = googleMap.addPolyline(new PolylineOptions()
                    //  .clickable(true)
                    .addAll(points));

            stylePolyline(polyline);
            polyline.setPattern(PATTERN_POLYLINE_DOTTED);
            return polyline;
        }
        else{
            PolylineOptions polylineOptions = new PolylineOptions().width(lineWidth).color(colorLine);
            polylineOptions.add(points.get(0));
            for(int i = 1; i< points.size()-1 ; i+= 50) {
                polylineOptions.add(points.get(i));
            }
            polylineOptions.add(points.get(points.size() - 1));
            Polyline polyline = myMap.addPolyline(polylineOptions);
            return polyline;
        }

    }
    /*private List<LatLng>  coordinateToLatlng (List<Coordinate> oldPoints) {

        List<LatLng>  newPoints = new ArrayList<LatLng>();
        for(int  i = 0 ; i < oldPoints.size() ; i++){
            newPoints.add(oldPoints.get(i));
        }

        return newPoints;
    }*/

    private void addtrailWarnings(){

        if( MainActivity.trailWarnings != null) {
            for(int i = 0 ; i < MainActivity.trailWarnings.size() ; i++) {
                myMap.addMarker(new MarkerOptions()
                        .position(MainActivity.trailWarnings.get(i).getGeometry())
                        .title(MainActivity.trailWarnings.get(i).getLocation())
                        .snippet(MainActivity.trailWarnings.get(i).getMessage())
                        .icon(BitmapDescriptorFactory.fromResource(R.drawable.warning_marker)));
            }
        }

    }

    private void onMyMapReady(GoogleMap googleMap) {
        myMap = googleMap;
        myMap.getUiSettings().setMapToolbarEnabled(false);
        if (MainActivity.listSegments == null) {
            myMap.setOnCameraIdleListener(null);
        } else {
            myMap.setOnCameraIdleListener(getCameraChangeListener());
        }

        // Set OnMapLoadedCallback Listener.
        myMap.setOnMapLoadedCallback(new GoogleMap.OnMapLoadedCallback() {
            @Override
            public void onMapLoaded() {
             }
        });
        myMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        myMap.getUiSettings().setZoomControlsEnabled(false);

        myMap.getUiSettings().setMyLocationButtonEnabled(false);

        if (Build.VERSION.SDK_INT >= 23) {
            if (ActivityCompat.checkSelfPermission(activity, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(activity, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                return;
            }
        }

        myMap.setMyLocationEnabled(true);

    }

    private void askPermissionsAndShowMyLocation() {

        // With API> = 23, you have to ask the user for permission to view their location.
        if (Build.VERSION.SDK_INT >= 23) {
            int accessCoarsePermission = ContextCompat.checkSelfPermission(activity, android.Manifest.permission.ACCESS_COARSE_LOCATION);
            int accessFinePermission = ContextCompat.checkSelfPermission(activity, android.Manifest.permission.ACCESS_FINE_LOCATION);

            if (accessCoarsePermission != PackageManager.PERMISSION_GRANTED || accessFinePermission != PackageManager.PERMISSION_GRANTED) {
                // The Permissions to ask user.
                String[] permissions = new String[]{android.Manifest.permission.ACCESS_COARSE_LOCATION,android.Manifest.permission.ACCESS_FINE_LOCATION};
                // Show a dialog asking the user to allow the above permissions.
                ActivityCompat.requestPermissions(activity, permissions,REQUEST_ID_ACCESS_COURSE_FINE_LOCATION);

                return;
            }
        }

        // Show current location on Map.
     //   this.showTrailLocation();
    }


    // Call this method only when you have the permissions to view a user's location.


    private String getEnabledLocationProvider() {
        LocationManager locationManager = (LocationManager) activity.getSystemService(LOCATION_SERVICE);

        // Criteria to find location provider.
        Criteria criteria = new Criteria();

        // Returns the name of the provider that best meets the given criteria.
        // ==> "gps", "network",...
        String bestProvider = locationManager.getBestProvider(criteria, true);

        boolean enabled = locationManager.isProviderEnabled(bestProvider);

        if (!enabled) {
         //   Toast.makeText(activity, "No location provider enabled!", Toast.LENGTH_LONG).show();
            Log.i("ActivityTrackerFragment", "No location provider enabled!");
            return null;
        }
        return bestProvider;
    }


    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        return false;
    }



    private void selectSegment() {

        if(!trailID.equals("")){
            for(int i = 0; i< MainActivity.listSegments.size(); i++)
            {
                if (MainActivity.listSegments.get(i).trailId.equals(trailID) ) {

                    if(listVisibleSegments.get(MainActivity.listSegments.get(i).objectId) == null){
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

                        Polyline polyline = drawPath3(myMap, MainActivity.listPoints.get(MainActivity.listSegments.get(i).objectId), color, categoryCode ) ;
                        polyline.setWidth(21);
                        listVisibleSegments.put(MainActivity.listSegments.get(i).objectId, polyline);

                    }
                    else {
                        listVisibleSegments.get(MainActivity.listSegments.get(i).objectId).setWidth(21);
                    }

                }
            }
        }

    }






    private void unselectSegment() {

        for (int objectId : listVisibleSegments.keySet())  // listSegments remplaced by listVisibleSegments
        {
            listVisibleSegments.get(objectId).setWidth(9);
        }


    }


    public int dpToPx(int dp) {
        //Context context = MainActivity.context;
        Resources resources = activity.getResources();
        DisplayMetrics metrics = resources.getDisplayMetrics();
        return (int) (dp * ((float) metrics.densityDpi / DisplayMetrics.DENSITY_DEFAULT));
    }


    public void centerTrail(int objectId) {

        String trailId = "";
        int newObjectId = objectId ;
        for(int i = 0; i< MainActivity.listSegments.size(); i++)
        {
            if(MainActivity.listSegments.get(i).objectId == objectId){
                trailId = MainActivity.listSegments.get(i).trailId ;
                break;
            }
        }


        for(int i = 0; i< MainActivity.listSegments.size(); i++) {
            if (MainActivity.listSegments.get(i).trailId.equals(trailId)) {
                startPoint = new LatLng(MainActivity.listPoints.get(MainActivity.listSegments.get(i).objectId).get(0).latitude, MainActivity.listPoints.get(MainActivity.listSegments.get(i).objectId).get(0).longitude);
                break;
            }
        }

        for(int i = 0; i< MainActivity.listSegments.size(); i++) {
            if (MainActivity.listSegments.get(i).trailId.equals(trailId)) {
                newObjectId = MainActivity.listSegments.get(i).objectId;

            }
        }

        if(objectId != 0) {
            endPoint = new LatLng(MainActivity.listPoints.get(objectId).get(MainActivity.listPoints.get(objectId).size()-1).latitude,
                    MainActivity.listPoints.get(newObjectId).get(MainActivity.listPoints.get(newObjectId).size()-1).longitude);
        }
    }


    private LatLng nearestPoint(LatLng point){
        if(this.myMap != null && MainActivity.listSegmentsByTrailId!=null) {
            LatLng nearestPoint = new LatLng(0,0);
            float[] currentDistance =  new float[1];
            float minDistance = Float.MAX_VALUE;


            for(String trailId : MainActivity.listPointsByTrailId.keySet())  {
                for(int i = 0; i < MainActivity.listPointsByTrailId.get(trailId).size() ; i=i+10 ) {
                    Location.distanceBetween(point.latitude, point.longitude, MainActivity.listPointsByTrailId.get(trailId).get(i).latitude, MainActivity.listPointsByTrailId.get(trailId).get(i).longitude, currentDistance);

                    if (currentDistance[0] < minDistance  /*&&  currentDistance[0]<Constants.TOLLERANCE_DISTANCE*/ ) {
                        // If distance is less than 100 meters, this is your polyline
                        minDistance = currentDistance[0];
                        nearestPoint = new LatLng(MainActivity.listPointsByTrailId.get(trailId).get(i).latitude, MainActivity.listPointsByTrailId.get(trailId).get(i).longitude);
                    }

                }
            }

            Log.i("LocationService"," The Nearest Point---------------------------------------------------------------------------"+nearestPoint.toString());

            return nearestPoint;
        }

        return null;
    }


}

package ca.TransCanadaTrail.TheGreatTrail;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Rect;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.VisibleRegion;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import butterknife.ButterKnife;
import ca.TransCanadaTrail.TheGreatTrail.fragments.BaseTrailDrawingFragment;

import static android.content.Context.LOCATION_SERVICE;
import static ca.TransCanadaTrail.TheGreatTrail.ActivityTracker.ActivityTrackerFragment.trackerfragStack;
import static ca.TransCanadaTrail.TheGreatTrail.MapView.MapFragment.mapfragStack;
import static ca.TransCanadaTrail.TheGreatTrail.MeasureTool.MeasureFragment.measurefragStack;

/**
 * Created by hardikfumakiya on 2017-01-03.
 */

public class GooglePlaceCategoryFragment extends BaseTrailDrawingFragment implements GoogleMap.OnInfoWindowClickListener, GoogleMap.OnMarkerClickListener, GoogleApiClient.ConnectionCallbacks {

    public static GooglePlaceCategoryFragment instance = null;

    public ArrayList<Marker> markerlist = new ArrayList<Marker>();
    private TextView infoTxt;
    private Marker mSelectedMarker;
    private HashMap<String, String> selectedPlace;
    String categoryType;
    SingleScrollableHeaderListView search_placeresult;
    Location myLocation = null;
    public static List<HashMap<String, String>> placeList =new ArrayList<>();

    PlaceAdapter mAdapter;
    MainActivity activity;
    float radius;
    boolean isFirstTime;
    String next_page_token = "";

    public GooglePlaceCategoryFragment() {
        // Required empty public constructor
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        activity = (MainActivity) context;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        isFirstTime = true;
    }

    public static GooglePlaceCategoryFragment getInstance() {
        if (instance == null) {
            instance = new GooglePlaceCategoryFragment();
        }
        return instance;
    }

    private static GooglePlaceCategoryFragment newInstance() {
        GooglePlaceCategoryFragment mapFragment = getInstance();
        return mapFragment;
    }

    int currentFirstVisibleItem;
    int currentVisibleItemCount;
    int currentTotalItemCount;

    @Nullable
    @Override
    public View onCreateView(final LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.category_places, container, false);
        ButterKnife.bind(this, view);

        activity.getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        activity.getSupportActionBar().setDisplayShowHomeEnabled(true);
        activity.getSupportActionBar().setTitle(Html.fromHtml("<small>" + categoryType + "</small>"));

        search_placeresult = (SingleScrollableHeaderListView) view.findViewById(R.id.search_placeresult);

        infoTxt = (TextView) view.findViewById(R.id.infoTxt);

        View headerView = inflater.inflate(R.layout.header, null, false);
        mMapView = (MapView) headerView.findViewById(R.id.mapview);
        mMapView.onCreate(savedInstanceState);

        search_placeresult.addHeaderView(headerView);

        ImageView poweredBygoogle = new ImageView(getContext());
        AbsListView.LayoutParams lp = new AbsListView.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        poweredBygoogle.setLayoutParams(lp);
        poweredBygoogle.setPadding(20, 30, 20, 30);
        poweredBygoogle.setImageResource(R.drawable.powered_by_google_light);
        search_placeresult.addFooterView(poweredBygoogle);

        search_placeresult.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return false;
            }
        });

        mAdapter = new PlaceAdapter(activity, placeList);
        search_placeresult.setAdapter(mAdapter);

        search_placeresult.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {
                parallelx(mMapView);
                isScrollCompleted();
            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                currentFirstVisibleItem = firstVisibleItem;
                currentVisibleItemCount = visibleItemCount;
                currentTotalItemCount = totalItemCount;

                parallelx(mMapView);

            }
        });

        infoTxt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                scrollResult();

            }
        });

        if (mGoogleApiClient == null) {
            mGoogleApiClient = new GoogleApiClient.Builder(activity)
                    .addConnectionCallbacks(this)
                    .addApi(LocationServices.API)
                    .build();
        }
        FloatingActionButton floatingActionButton = (FloatingActionButton) view.findViewById(R.id.location_toggle_fab);
        floatingActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (Build.VERSION.SDK_INT >= 23) {
                    int accessCoarsePermission
                            = ContextCompat.checkSelfPermission(activity, android.Manifest.permission.ACCESS_COARSE_LOCATION);
                    int accessFinePermission
                            = ContextCompat.checkSelfPermission(activity, android.Manifest.permission.ACCESS_FINE_LOCATION);

                    if (accessCoarsePermission != PackageManager.PERMISSION_GRANTED || accessFinePermission != PackageManager.PERMISSION_GRANTED) {
                        // The Permissions to ask user.
                        String[] permissions = new String[]{android.Manifest.permission.ACCESS_COARSE_LOCATION,
                                android.Manifest.permission.ACCESS_FINE_LOCATION};
                        // Show a dialog asking the user to allow the above permissions.
                        ActivityCompat.requestPermissions(activity, permissions,
                                MainActivity.REQUEST_ID_ACCESS_COURSE_FINE_LOCATION);

                        return;
                    }
                }

                Location mLastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);

                if (mLastLocation != null) {
                    LatLng latLng = new LatLng(mLastLocation.getLatitude(), mLastLocation.getLongitude());
                    myMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 10));
                }
            }
        });

        setupMapView();
        return view;
    }

    private void scrollResult() {
        final int pos= placeList.size()>4?4:(placeList.size()+1);
        //search_placeresult.smoothScrollToPosition(5);
        search_placeresult.post(new Runnable() {
            @Override
            public void run() {
                search_placeresult.smoothScrollToPosition(pos);
                isScrollCompleted();
                //search_placeresult.setSelection(pos);
            }
        });
    }

    private void isScrollCompleted() {
        int visiblePos=search_placeresult.getLastVisiblePosition();
        if(visiblePos>1){
            infoTxt.setVisibility(View.GONE);
        }
        else
            infoTxt.setVisibility(View.VISIBLE);
    }

    @Override
    protected void initializeMap(GoogleMap googleMap) {
        myMap = googleMap;


        if (MainActivity.listSegments == null) {
            myMap.setOnCameraIdleListener(null);
        } else {
            myMap.setOnCameraIdleListener(getCameraChangeListener());
        }

        // Set OnMapLoadedCallback Listener.
        myMap.setOnMapLoadedCallback(new GoogleMap.OnMapLoadedCallback() {

            @Override
            public void onMapLoaded() {
                askPermissionsAndShowMyLocation();
            }
        });
        myMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        myMap.getUiSettings().setZoomControlsEnabled(false);
        myMap.getUiSettings().setMapToolbarEnabled(false);
        myMap.setOnMarkerClickListener(this);


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

        myMap.setOnInfoWindowClickListener(this);

        myMap.getUiSettings().setMyLocationButtonEnabled(false);

        showMyLocation();

    }

    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) activity.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    public GoogleMap.OnCameraIdleListener getCameraChangeListener() {
        return new GoogleMap.OnCameraIdleListener() {

            @Override
            public void onCameraIdle() {
                GooglePlaceCategoryFragment.super.onCameraIdle();
            }
        };
    }

    private float calculateRadius() {

        VisibleRegion vr = myMap.getProjection().getVisibleRegion();
        Location center = new Location("center");
        center.setLatitude(vr.latLngBounds.southwest.latitude);
        center.setLongitude(vr.latLngBounds.southwest.longitude);
        Log.d("center ", center.toString());

        Location farLeftCornerLocation = new Location("farPoint");
        farLeftCornerLocation.setLatitude(vr.latLngBounds.northeast.latitude);
        farLeftCornerLocation.setLongitude(vr.latLngBounds.northeast.longitude);
        Log.d("farLeftCornerLocation ", farLeftCornerLocation.toString());

        float radius = center.distanceTo(farLeftCornerLocation);
        return radius;
    }

    private int lastTop = 0;

    public void parallelx(final View v) {
        final Rect r = new Rect();
        v.getLocalVisibleRect(r);
        if (lastTop != r.top) {
            lastTop = r.top;
            v.post(new Runnable() {
                @Override
                public void run() {
                    v.setY((float) r.top / 2);
                }
            });
        }


    }

    public StringBuilder sbMethod() {
        StringBuilder sb = null;
        if (myLocation != null) {

            double mLatitude = myLocation.getLatitude();
            double mLongitude = myLocation.getLongitude();

            //float radius = topLeft.distanceTo(bottomRight);
            String category = FetchCategory(categoryType);
            radius = calculateRadius();
            Log.d("radius", "" + radius);


            sb = new StringBuilder("https://maps.googleapis.com/maps/api/place/nearbysearch/json?");
            sb.append("location=" + mLatitude + "," + mLongitude);
            sb.append("&radius="+radius);
            sb.append("&type=&name=" + category);
            sb.append("&key="+SearchListFragment.API_KEY);
            sb.append("&pagetoken=").append(next_page_token);

            Log.d("Map", "api: " + sb.toString());

        }

        return sb;
    }

    private String FetchCategory(String categoryType) {
        if (categoryType.equals("Restaurant"))
            return "restaurant";
        else if (categoryType.equals("Campground") || categoryType.equals("Camping"))
            return "campground";
        else if (categoryType.equals("Café"))
            return "cafe";
        else if (categoryType.equals("ATM") || categoryType.equals("Guichets automatiques"))
            return "atm";
        else if (categoryType.equals("Lodging") || categoryType.equals("Hébergement"))
            return "lodging";
        else if (categoryType.equals("Bakery") || categoryType.equals("Boulangerie"))
            return "bakery";
        else if (categoryType.equals("Bicycle Store") || categoryType.equals("Bicycletterie"))
            return "bicycle_store";
        else if (categoryType.equals("Convenience Store") || categoryType.equals("Dépanneur"))
            return "convenience_store";
        else if (categoryType.equals("Hardware Store") || categoryType.equals("Quincaillerie"))
            return "hardware_store";
        else if (categoryType.equals("Hospital") || categoryType.equals("Hôpital"))
            return "hospital";
        else if (categoryType.equals("Museum") || categoryType.equals("Musée"))
            return "museum";
        else if (categoryType.equals("Park") || categoryType.equals("Parc"))
            return "park";
        else
            return "pharmacy";
    }

    @Override
    public void onInfoWindowClick(Marker marker) {

        LatLng myPosition = marker.getPosition();
        for (int i = 0; i < placeList.size(); i++) {

            double lat = Double.parseDouble(placeList.get(i).get("lat"));

            double lng = Double.parseDouble(placeList.get(i).get("lng"));

            LatLng latLng = new LatLng(lat, lng);
            if (latLng.equals(myPosition))
                selectedPlace = placeList.get(i);
        }

        if (selectedPlace != null) {
            pushCategoryFragmentToStack();

            PlaceDetailFragment placeFrag = new PlaceDetailFragment();
            Bundle bundle = new Bundle();
            bundle.putString("place_name", selectedPlace.get("place_name"));
            bundle.putString("place_id", selectedPlace.get("place_id"));
            bundle.putString("vicinity", selectedPlace.get("vicinity"));
            if (selectedPlace.get("photo_reference") != null)
                bundle.putString("photo_reference", selectedPlace.get("photo_reference"));
            placeFrag.setArguments(bundle);

            replaceFragment(SearchListFragment.resourceID, placeFrag);

        }

    }

    private void replaceFragment(int resourceID, Fragment replaceFragment) {
        FragmentTransaction mFragmentTransaction = activity.getSupportFragmentManager().beginTransaction();
        mFragmentTransaction
                .replace(resourceID, replaceFragment)
                .commit();
    }

    private void pushCategoryFragmentToStack() {
        FragmentManager fragmentManager = activity.getSupportFragmentManager();
        Fragment fgooglePOI = null;
        String fragmentTag = "GooglePlaceCategory";
        switch (MainActivity.currentTab) {
            case "MapFragment":
                fragmentTag = "Map" + fragmentTag;
                fgooglePOI = fragmentManager.findFragmentById(R.id.searchLayout);

                break;
            case "MeasureFragment":
                fragmentTag = "Measure" + fragmentTag;
                fgooglePOI = fragmentManager.findFragmentById(R.id.measureSearchLayout);
                break;

            case "ActivityTrackerFragment":
                fragmentTag = "Tracker" + fragmentTag;
                fgooglePOI = fragmentManager.findFragmentById(R.id.trackerSearchLayout);
                break;
        }

        if (fgooglePOI instanceof GooglePlaceCategoryFragment) {
            SearchListFragment.fragStack.put(fragmentTag, fgooglePOI);
            SearchListFragment.fragTagStack.push(fragmentTag);
        }
    }

    @Override
    public boolean onMarkerClick(Marker marker) {

        if (mSelectedMarker != null) {
            mSelectedMarker.setIcon(BitmapDescriptorFactory.fromResource(R.drawable.orange_poi_marker));
        }

        setSelection(marker);
        return false;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);

        if(MainActivity.currentTab.equals("MapFragment") && mapfragStack.containsKey("MapSearchFragment")){
            menu.clear();
        }
        else if(MainActivity.currentTab.equals("MeasureFragment") && measurefragStack.containsKey("MeasureSearchFragment")){
            menu.clear();
        }
        else if(MainActivity.currentTab.equals("ActivityTrackerFragment") && trackerfragStack.containsKey("TrackerSearchFragment")){
            menu.clear();
        }

    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        if (ActivityCompat.checkSelfPermission(activity, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(activity, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        myLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);

        if (myLocation != null) {
            LatLng latLng = new LatLng(myLocation.getLatitude(), myLocation.getLongitude());
            myMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 10));
        }
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    protected boolean hasClickableSegments() {
        return false;
    }

    private void setSelection(Marker m) {
        mSelectedMarker = m;
        mSelectedMarker.showInfoWindow();
        mSelectedMarker.setIcon(BitmapDescriptorFactory.fromResource(R.drawable.yellow_poi_marker));
    }

    private void retrivePlacesByCategory() {
        try {

            //markerlist.clear();
            StringBuilder sbValue = sbMethod();

            //PlacesTask placesTask = new PlacesTask();
            //Log.d("url:", sbValue.toString());

            final ProgressDialog pDialog = new ProgressDialog(activity);
            pDialog.setMessage("Loading...");
            pDialog.show();

            if(sbValue!=null) {
                String url = sbValue.toString();
                RequestQueue queue = Volley.newRequestQueue(activity);  // this = context

                StringRequest postRequest = new StringRequest(Request.Method.GET, url,
                        new Response.Listener<String>() {
                            @Override
                            public void onResponse(String response) {
                                // response
                                try{
                                    Log.i("request", "Response " + response);
                                    if (response != null) {
                                        ParserTask parserTask = new ParserTask();
                                        parserTask.execute(response);
                                    }
                                }
                                catch (Exception e){

                                }finally {
                                    pDialog.dismiss();
                                }


                            }
                        },
                        new Response.ErrorListener() {
                            @Override
                            public void onErrorResponse(VolleyError error) {
                                // error
                                //  Log.d("Error.Response", error.getMessage());
                                // TODO handle the error
                                //  error.printStackTrace();
                                Toast.makeText(activity, "error on Server, Try in while", Toast.LENGTH_LONG).show();
                                pDialog.dismiss();
                            }
                        }
                ) ;

                queue.add(postRequest);
            }
            //placesTask.execute(sbValue.toString());
        }
        catch(Exception e)
        {
            e.printStackTrace();
        }
    }

    private void askPermissionsAndShowMyLocation() {

        // With API> = 23, you have to ask the user for permission to view their location.
        if (Build.VERSION.SDK_INT >= 23) {
            int accessCoarsePermission = ContextCompat.checkSelfPermission(activity, android.Manifest.permission.ACCESS_COARSE_LOCATION);
            int accessFinePermission = ContextCompat.checkSelfPermission(activity, android.Manifest.permission.ACCESS_FINE_LOCATION);


            if (accessCoarsePermission != PackageManager.PERMISSION_GRANTED
                    || accessFinePermission != PackageManager.PERMISSION_GRANTED) {
                // The Permissions to ask user.
                String[] permissions = new String[]{android.Manifest.permission.ACCESS_COARSE_LOCATION,
                        android.Manifest.permission.ACCESS_FINE_LOCATION};
                // Show a dialog asking the user to allow the above permissions.
                ActivityCompat.requestPermissions(activity, permissions, MainActivity.REQUEST_ID_ACCESS_COURSE_FINE_LOCATION);

                return;
            }
        }

        // Show current location on Map.
        this.showMyLocation();

    }

    private String getEnabledLocationProvider() {
        LocationManager locationManager = (LocationManager) activity.getSystemService(LOCATION_SERVICE);

        // Criteria to find location provider.
        Criteria criteria = new Criteria();

        // Returns the name of the provider that best meets the given criteria.
        // ==> "gps", "network",...
        String bestProvider = locationManager.getBestProvider(criteria, true);

        boolean enabled = locationManager.isProviderEnabled(bestProvider);

        if (!enabled) {
            Toast.makeText(activity, "No location provider enabled!", Toast.LENGTH_LONG).show();
            Log.i("ActivityTrackerFragment", "No location provider enabled!");
            return null;
        }
        return bestProvider;
    }

    @Override
    protected void onMapReady() {
        super.onMapReady();

        if (isNetworkAvailable())
            retrivePlacesByCategory();
        else
            infoTxt.setText(activity.getResources().getString(R.string.blank_category));
    }

    private class ParserTask extends AsyncTask<String, Integer, List<HashMap<String, String>>> {

        JSONObject jObject;

        // Invoked by execute() method of this object
        @Override
        protected List<HashMap<String, String>> doInBackground(String... jsonData) {
            //placeList.clear();
            List<HashMap<String, String>> places = null;
            Place_JSON placeJson = new Place_JSON();
            if (jsonData != null) {
                try
                {
                    jObject = new JSONObject(jsonData[0]);
                    places = placeJson.parse(jObject);

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            return places;
        }

        @Override
        protected void onPostExecute(final List<HashMap<String, String>> list) {
            placeList = list;

            if (list != null && list.size() > 0)
            {
                Log.d("Map", "places size: " + list.size());

                infoTxt.setText(activity.getResources().getString(R.string.show_result));

                for (int i = 0; i < list.size(); i++) {
                    // Creating a marker
                    HashMap<String, String> hmPlace = list.get(i);

                    // Getting latitude of the place
                    double lat = Double.parseDouble(hmPlace.get("lat"));

                    // Getting longitude of the place
                    double lng = Double.parseDouble(hmPlace.get("lng"));
                    String name = hmPlace.get("place_name");


                    // Getting vicinity
                    String vicinity = hmPlace.get("vicinity");

                    LatLng latLng = new LatLng(lat, lng);

                    // Setting the position for the marker

                    MarkerOptions markerOptions = new MarkerOptions();
                    markerOptions.position(latLng);
                    markerOptions.title(name);
                    markerOptions.icon(BitmapDescriptorFactory.fromResource(R.drawable.orange_poi_marker));
                    markerOptions.snippet(vicinity);
                    // Placing a marker on the touched position
                    Marker m = myMap.addMarker(markerOptions);
                    markerlist.add(m);

                    //Log.d("marker: " + m.getPosition().toString(), "place: " + name);


                }
                mAdapter = new PlaceAdapter(activity, placeList);

                search_placeresult.setAdapter(mAdapter);

                search_placeresult.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                        //Toast.makeText(getContext(),"Clicked item: " + position, Toast.LENGTH_LONG).show();

                        HashMap<String, String> place = mAdapter.getItem(position - 1);
                        //selectedPlace=place;
                        double lat = Double.parseDouble(place.get("lat"));
                        double lng = Double.parseDouble(place.get("lng"));
                        LatLng latLng = new LatLng(lat, lng);
                        String vicinity = place.get("vicinity");
                        String place_name = place.get("place_name");

                        //Log.d("Clicked on " + place_name, vicinity);

                        // Setting the position for the marker
                        MarkerOptions markerOptions = new MarkerOptions();

                        markerOptions.position(latLng);

                        markerOptions.title(place_name);

                        markerOptions.icon(BitmapDescriptorFactory.fromResource(R.drawable.orange_poi_marker));
                        markerOptions.snippet(vicinity);
                        // Placing a marker on the touched position
                        Marker m = myMap.addMarker(markerOptions);

                        if (mSelectedMarker != null) {
                            mSelectedMarker.setIcon(BitmapDescriptorFactory.fromResource(R.drawable.orange_poi_marker));
                        }

                        setSelection(m);

                        myMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 11));

                    }
                });
            }
            else{
                infoTxt.setText(activity.getResources().getString(R.string.blank_category));
            }

        }

    }

    private void showMyLocation() {

        try {
            LocationManager locationManager = (LocationManager) activity.getSystemService(LOCATION_SERVICE);

            String locationProvider = this.getEnabledLocationProvider();

            if (locationProvider == null) {
                return;
            }

            myLocation = locationManager.getLastKnownLocation(locationProvider);

            if (myLocation != null)
            {
                LatLng latLng = new LatLng(myLocation.getLatitude(), myLocation.getLongitude());
                myMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng,11));
            }
            else
            {
                //Toast.makeText(activity, "Location not found!", Toast.LENGTH_LONG).show();
                infoTxt.setText(activity.getResources().getString(R.string.blank_category));
                Log.i("ActivityTrackerFragment", "Location not found");
            }
        }
        catch (SecurityException e)
        {
            Log.e("ActivityTackeSegment", "Show My Location Error:" + e.getMessage());
            infoTxt.setText(activity.getResources().getString(R.string.blank_category));
            e.printStackTrace();
            return;
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
    public void onDestroy() {
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
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (mMapView != null) {
            mMapView.onSaveInstanceState(outState);
        }
    }

    private class Place_JSON {

        /**
         * Receives a JSONObject and returns a list
         */
        public List<HashMap<String, String>> parse(JSONObject jObject) {

            JSONArray jPlaces = null;
            try {
                /** Retrieves all the elements in the 'places' array */
                jPlaces = jObject.getJSONArray("results");
//                if(jObject.has("next_page_token"))
//                {
//                    next_page_token=jObject.getString("next_page_token");
//                    retrivePlacesByCategory();
//                }
                    //"next_page_token" -> "CpQCBgEAAFmTpdHgYjswl2YTFJTpJ7qDp94emmjYugOv5KD4utUEcx3D4zs1fIFhp34cjJ5Fs5PQ7XST2kAS08gofO1Q2vxcxX9MZXr60Dyz8dOcOvVA6kpnFXgC_yp1tWuBlGNTCxVkP8UjD9djp6M5PgwTbZm-cs8lNfa-Z7pRJCMCuHBh64TG6CEGnkTK7LWBVyBMzyBpT__nImJxeAkY2lsyWMh5k6A1GHXA4AJnA2XbcHdk3b3OItvhTscaK7iKPtEEKFhFRFmuCRgzCIHE2p2mkYDmDbJkGUfnPGewxueno_cMDl-jLWVXO3dIsPvxfNyQ4Mr3aRNy5vFw6q2fZjdIIJhU4SRQsxfMfexOUsoOmDvwEhD1NmIauXd8weYuEdpbD1GAGhQlmLnO7PIEH2ILB7C2UxZYZgSauA"
            } catch (Exception e) {
                e.printStackTrace();
            }
            /** Invoking getPlaces with the array of json object
             * where each json object represent a place
             */
            return getPlaces(jPlaces);
        }

        private List<HashMap<String, String>> getPlaces(JSONArray jPlaces) {
            int placesCount = jPlaces.length();
            List<HashMap<String, String>> placesList = new ArrayList<HashMap<String, String>>();
            HashMap<String, String> place = null;

            /** Taking each place, parses and adds to list object */
            for (int i = 0; i < placesCount; i++) {
                try {
                    /** Call getPlace with place JSON object to parse the place */
                    place = getPlace((JSONObject) jPlaces.get(i));
                    placesList.add(place);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            return placesList;
        }

        /**
         * Parsing the Place JSON object
         */
        private HashMap<String, String> getPlace(JSONObject jPlace) {

            HashMap<String, String> place = new HashMap<String, String>();
            String placeName = "-NA-";
            String vicinity = "-NA-";
            String latitude = "";
            String longitude = "";
            String reference = "";
            String placeid="";


            try {
                // Extracting Place name, if available
                if (!jPlace.isNull("name")) {
                    placeName = jPlace.getString("name");
                }

                // Extracting Place Vicinity, if available
                if (!jPlace.isNull("vicinity")) {
                    vicinity = jPlace.getString("vicinity");
                }

                latitude = jPlace.getJSONObject("geometry").getJSONObject("location").getString("lat");
                longitude = jPlace.getJSONObject("geometry").getJSONObject("location").getString("lng");
                Log.d("name: "+placeName+"lat: "+latitude,"lng: "+longitude);
                reference = jPlace.getString("reference");
                placeid=jPlace.getString("place_id");

                if(jPlace.has("photos"))
                {
                    JSONArray parray= jPlace.getJSONArray("photos");
                    String photo_ref=parray.getJSONObject(0).getString("photo_reference");
                    place.put("photo_reference",photo_ref);
                }

                place.put("place_name", placeName);
                place.put("vicinity", vicinity);
                place.put("lat", latitude);
                place.put("lng", longitude);
                place.put("place_id",placeid);
                place.put("reference", reference);

            } catch (Exception e) {
                e.printStackTrace();
                return place;
            }
            return place;
        }
    }
}

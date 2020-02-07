package ca.TransCanadaTrail.TheGreatTrail.MeasureTool;


import android.app.SearchManager;
import android.app.SearchableInfo;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.Typeface;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.SearchView;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;
import com.github.mikephil.charting.listener.ChartTouchListener;
import com.github.mikephil.charting.listener.OnChartGestureListener;
import com.github.mikephil.charting.listener.OnChartValueSelectedListener;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Stack;

import butterknife.BindDimen;
import butterknife.BindView;
import butterknife.ButterKnife;
import ca.TransCanadaTrail.TheGreatTrail.AppController;
import ca.TransCanadaTrail.TheGreatTrail.Constants;
import ca.TransCanadaTrail.TheGreatTrail.MainActivity;
import ca.TransCanadaTrail.TheGreatTrail.MapView.TrailSegmentLight;
import ca.TransCanadaTrail.TheGreatTrail.R;
import ca.TransCanadaTrail.TheGreatTrail.SearchListFragment;
import ca.TransCanadaTrail.TheGreatTrail.fragments.HomeTabMapFragment;
import ca.TransCanadaTrail.TheGreatTrail.utils.ElevationClass;
import ca.TransCanadaTrail.TheGreatTrail.utils.TrailUtility;
import ca.TransCanadaTrail.TheGreatTrail.utils.Utility;

import static android.content.Context.LOCATION_SERVICE;
import static android.content.Context.SEARCH_SERVICE;
import static ca.TransCanadaTrail.TheGreatTrail.MainActivity.REQUEST_ID_ACCESS_COURSE_FINE_LOCATION;

public class MeasureFragment extends HomeTabMapFragment implements OnChartGestureListener, OnChartValueSelectedListener {

    private static final String EXTRA_CENTER_KEY = "center";
    private static final String EXTRA_ZOOM_KEY = "zoom";
    private static final String EXTRA_DELETE_MEASURE = "delete-measure";
    private static final String TAG = "MeasureFragment";
    private static final int TRAIL_MEASUREMENT_DROPPED_POINTS_COUNT = 25;
    private static final double LOW_ZOOM_LEVEL = 7.5;
    private static final double MID_ZOOM_LEVEL = 10;
    private static final double HIGH_ZOOM_LEVEL = 12.5;
    private static final double DROP_PIN_TOLERANCE_DISTANCE_COEFFICIENT = 100 * 1000;   // an emprical values means that zoom * tolerance = 100,000 meters

    public static HashMap<String, Fragment> measurefragStack = new HashMap<String, Fragment>();
    public static Stack<String> measurefragTagStack = new Stack<String>();

    public LatLng center;
    public float zoom;
    public boolean deleteMeasure;
    public SearchView searchView;
    public int isSearchOpened = 0;
    public FrameLayout measureSearchLayout;

    @BindDimen(R.dimen.fragment_measure_text_view_height)
    int infoTvHeight;
    @BindDimen(R.dimen.animated_camera_padding)
    int animatedCameraPadding;
    @BindView(R.id.drop_another_pin_tv)
    TextView dropAnotherPinTv;
    @BindView(R.id.drop_first_pin_tv)
    TextView dropFirstPinTv;

    private FloatingActionButton floatingActionButton;
    private ImageView upBtn;
    private ImageView deleteBtn;
    private TextView distanceTxt;
    private TextView elevationTxt;
    private LineChart lineChart;
    private LinearLayout layoutMeasure;
    private boolean layoutVisible = false;
    private boolean putFirstMarker = true;
    private boolean putSecondMarker = true;
    private Marker startMarker, endMarker;
    private Menu menu;
    private Tracker mTracker;
    private LatLng startMarkerPoint;
    private LatLng endMarkerPoint;
    private int TIMEOUT = 20000;

    public MeasureFragment() {
        // Required empty public constructor
        searchText = "";
    }

    public static MeasureFragment newInstance() {
        return new MeasureFragment();
    }

    public static int getPixelValue(Context context, int dimenId) {
        Resources resources = context.getResources();
        return (int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP,
                dimenId,
                resources.getDisplayMetrics()
        );
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        activity = (MainActivity) context;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);

        // [START shared_tracker]
        // Obtain the shared Tracker instance.
        AppController application = (AppController) getActivity().getApplication();
        mTracker = application.getDefaultTracker();
        // [END shared_tracker]
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_measure, container, false);
        ButterKnife.bind(this, view);
        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        if (savedInstanceState != null) {

            if (savedInstanceState.containsKey(EXTRA_DELETE_MEASURE)) {
                deleteMeasure = savedInstanceState.getBoolean(EXTRA_DELETE_MEASURE);
            }

            if (savedInstanceState.containsKey(EXTRA_CENTER_KEY)) {
                center = (LatLng) savedInstanceState.get(EXTRA_CENTER_KEY);
            }

            if (savedInstanceState.containsKey(EXTRA_ZOOM_KEY)) {
                zoom = savedInstanceState.getFloat(EXTRA_ZOOM_KEY);
            }
        }
        setUiValues(savedInstanceState, getView());
    }

    @Override
    public void onResume() {
        super.onResume();
        // [START screen_view_hit]
        mTracker.setScreenName("Measure Tool");
        mTracker.send(new HitBuilders.ScreenViewBuilder().build());
        // [END screen_view_hit]

    }

    @Override
    public void onDestroy() {
        measurefragStack = null;
        measurefragTagStack = null;

        if (mMapView != null) {
            try {
                mMapView.onDestroy();
            } catch (NullPointerException e) {
                Log.e("TAG", "Error while attempting MapView.onDestroy(), ignoring exception", e);
            }
        }
        super.onDestroy();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelable(EXTRA_CENTER_KEY, center);
        outState.putFloat(EXTRA_ZOOM_KEY, zoom);
        outState.putBoolean(EXTRA_DELETE_MEASURE, deleteMeasure);

        if (mMapView != null) {
            mMapView.onSaveInstanceState(outState);
        }
    }

    @Override
    protected boolean hasClickableSegments() {
        return false;
    }

    @Override
    public void onChartGestureStart(MotionEvent me, ChartTouchListener.ChartGesture lastPerformedGesture) {
        Log.i("Gesture", "START, x: " + me.getX() + ", y: " + me.getY());
    }

    @Override
    public void onChartGestureEnd(MotionEvent me, ChartTouchListener.ChartGesture lastPerformedGesture) {
        Log.i("Gesture", "END, lastGesture: " + lastPerformedGesture);

        // un-highlight values after the gesture is finished and no single-tap
        if (lastPerformedGesture != ChartTouchListener.ChartGesture.SINGLE_TAP)
            lineChart.highlightValues(null); // or highlightTouch(null) for callback to onNothingSelected(...)
    }

    @Override
    public void onChartLongPressed(MotionEvent me) {
        Log.i("LongPress", "Chart longpressed.");
    }

    @Override
    public void onChartDoubleTapped(MotionEvent me) {
        Log.i("DoubleTap", "Chart double-tapped.");
    }

    @Override
    public void onChartSingleTapped(MotionEvent me) {
        Log.i("SingleTap", "Chart single-tapped.");
    }

    @Override
    public void onChartFling(MotionEvent me1, MotionEvent me2, float velocityX, float velocityY) {
        Log.i("Fling", "Chart flinged. VeloX: " + velocityX + ", VeloY: " + velocityY);
    }

    @Override
    public void onChartScale(MotionEvent me, float scaleX, float scaleY) {
        Log.i("Scale / Zoom", "ScaleX: " + scaleX + ", ScaleY: " + scaleY);
    }

    @Override
    public void onChartTranslate(MotionEvent me, float dX, float dY) {
        Log.i("Translate / Move", "dX: " + dX + ", dY: " + dY);
    }

    @Override
    public void onValueSelected(Entry e, Highlight h) {
        Log.i("Entry selected", e.toString());
        Log.i("MIN MAX", "xmin: " + lineChart.getXChartMin() + ", xmax: " + lineChart.getXChartMax() + ", ymin: " + lineChart.getYChartMin() + ", ymax: " + lineChart.getYChartMax());
    }

    @Override
    public void onNothingSelected() {
        Log.i("Nothing selected", "Nothing selected.");
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        this.menu = menu;
        menu.clear();
        inflater.inflate(R.menu.main, menu);
        super.onCreateOptionsMenu(menu, inflater);

        SearchManager searchManager = (SearchManager) getContext().getSystemService(Context.SEARCH_SERVICE);
        MenuItem searchItem = menu.findItem(R.id.search);
        searchView = (SearchView) MenuItemCompat.getActionView(searchItem);
        searchView.setOnQueryTextListener(this);
        // Assumes current activity is the searchable activity
        SearchableInfo searchableInfo = searchManager.getSearchableInfo(activity.getComponentName());
        searchView.setSearchableInfo(searchableInfo);
//        searchView.clearFocus();
        searchView.setFocusable(false);
        // Override the hint with whatever you like
        searchView.setQueryHint(getResources().getString(R.string.search_hint));

        ImageView closeButton = (ImageView) searchView.findViewById(R.id.search_close_btn);

        TextView searchTextView = (TextView) searchView.findViewById(android.support.v7.appcompat.R.id.search_src_text);
        try {
            Field mCursorDrawableRes = TextView.class.getDeclaredField("mCursorDrawableRes");
            mCursorDrawableRes.setAccessible(true);
            mCursorDrawableRes.set(searchTextView, R.drawable.cursor); //This sets the cursor resource ID to 0 or @null which will make it visible on white background
        } catch (Exception e) {
        }

        searchView.setOnCloseListener(new SearchView.OnCloseListener() {
            @Override
            public boolean onClose() {
                if (isSearchOpened == 1) {
                    activity.getSupportActionBar().setDisplayHomeAsUpEnabled(true);
                    activity.getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_arrow_back);
                }
                return false;
            }
        });

        closeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                searchText = "";
                searchView.setQuery(searchText, false);
                searchListFragment.refreshData(activity, searchText);
            }
        });

        MenuItemCompat.setOnActionExpandListener(menu.findItem(R.id.search), new MenuItemCompat.OnActionExpandListener() {
            @Override
            public boolean onMenuItemActionExpand(MenuItem item) {

                if (!isNetworkAvailable()) {
                    showNoInternetDialog();
                    return false;
                }

                isSearchOpened = 1;
                ShowSearchMenu();
                if (searchText != null) {
                    searchView.setQuery(searchText, false);
                    searchListFragment.refreshData(activity, searchText);
                }
                return true;
            }

            @Override
            public boolean onMenuItemActionCollapse(MenuItem item) {
                isSearchOpened = 0;
                activity.onBackPressed();
                return true;
            }
        });
    }

    @Override
    protected void onMapReady() {
        goInMap(center, zoom);
        super.onMapReady();
        drawMarkers();

        center = myMap.getCameraPosition().target;
        zoom = myMap.getCameraPosition().zoom;
    }

    @Override
    protected void initializeMap(GoogleMap googleMap) {
        myMap = googleMap;
        myMap.getUiSettings().setRotateGesturesEnabled(false);

        myMap.getUiSettings().setMapToolbarEnabled(false);
        if (MainActivity.listSegments == null) {
            myMap.setOnCameraIdleListener(null);
        } else {
            myMap.setOnCameraIdleListener(getCameraChangeListener3());
        }
        myMap.setOnMapClickListener(onMapClick());

        // Set OnMapLoadedCallback Listener.
        myMap.setOnMapLoadedCallback(new GoogleMap.OnMapLoadedCallback() {

            @Override
            public void onMapLoaded() {

                askPermissionsAndShowMyLocation();

            }
        });
        myMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        myMap.getUiSettings().setZoomControlsEnabled(false);
        if (Build.VERSION.SDK_INT >= 23) {
            if (ActivityCompat.checkSelfPermission(activity, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(activity, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                return;
            }
        }
        myMap.setMyLocationEnabled(true);
        myMap.getUiSettings().setMyLocationButtonEnabled(false);

        myMap.setInfoWindowAdapter(new GoogleMap.InfoWindowAdapter() {

            @Override
            public View getInfoWindow(Marker arg0) {
                return null;
            }

            @Override
            public View getInfoContents(Marker marker) {

                Context context = getActivity(); //or getActivity(), YourActivity.this, etc.

                LinearLayout info = new LinearLayout(context);
                info.setOrientation(LinearLayout.VERTICAL);


                TextView title = new TextView(context);
                title.setTextColor(Color.BLACK);
                title.setGravity(Gravity.CENTER);
                title.setTypeface(null, Typeface.BOLD);
                title.setText(marker.getTitle());

                TextView snippet = new TextView(context);
                snippet.setTextColor(Color.GRAY);
                snippet.setText(marker.getSnippet());

                info.addView(title);
                info.addView(snippet);

                return info;
            }
        });

        myMap.setMyLocationEnabled(true);
    }

    public LatLng getCenter() {
        return center;
    }

    public void setCenter(LatLng center) {
        this.center = center;
    }

    public float getZoom() {
        return zoom;
    }

    public void setZoom(float zoom) {
        this.zoom = zoom;
    }

    public LatLng getEndMarkerPoint() {
        return endMarkerPoint;
    }

    public LatLng getStartMarkerPoint() {
        return startMarkerPoint;
    }

    public void expandSearchView() {
        SearchManager searchManager = (SearchManager) activity.getSystemService(SEARCH_SERVICE);
        searchView.setSearchableInfo(searchManager.getSearchableInfo(activity.getComponentName()));
        searchView.setIconifiedByDefault(false);

        searchView.setQuery(searchText, false);
        searchView.setFocusable(false);
    }

    public int dpToPx(float dp) {
        Resources resources = activity.getResources();
        DisplayMetrics metrics = resources.getDisplayMetrics();
        int px = (int) (dp * ((float) metrics.densityDpi / DisplayMetrics.DENSITY_DEFAULT));
        return px;
    }

    public GoogleMap.OnCameraIdleListener getCameraChangeListener3() {
        return new GoogleMap.OnCameraIdleListener() {

            @Override
            public void onCameraIdle() {
                MeasureFragment.super.onCameraIdle();
                center = myMap.getCameraPosition().target;
                zoom = myMap.getCameraPosition().zoom;
            }
        };
    }

    public GoogleMap.OnMapClickListener onMapClick() {
        return new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {
                if (!isNetworkAvailable()) {
                    showNoInternetDialog();
                    return;
                }

                //FIXME should be check here, maker won't showing if distance too long
                if (startMarkerPoint == null && putFirstMarker && putSecondMarker) {
                    startMarkerPoint = Utility.nearestPoint(latLng);
                    if (startMarkerPoint != null
                            && withinReasonableTolerance(TrailUtility.distanceTo(startMarkerPoint, latLng))) {
                        startMarker = myMap.addMarker(new MarkerOptions()
                                .position(startMarkerPoint)
                                .icon(BitmapDescriptorFactory
                                        .fromResource(R.drawable.purple_marker)));
                        myMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
                            @Override
                            public boolean onMarkerClick(Marker marker) {
                                return true;
                            }
                        });

                        dropFirstPinTv.setVisibility(View.GONE);
                        dropAnotherPinTv.setVisibility(View.VISIBLE);

                        putFirstMarker = false;
                    } else {
                        startMarkerPoint = null;
                        Toast.makeText(getActivity(), R.string.drop_pin_note, Toast.LENGTH_SHORT).show();
                    }
                } else {
                    endMarkerPoint = Utility.nearestPoint(latLng);
                    if (endMarkerPoint == null
                            || !withinReasonableTolerance(TrailUtility.distanceTo(endMarkerPoint, latLng))) {
                        Toast.makeText(getActivity(), R.string.drop_pin_note, Toast.LENGTH_SHORT).show();
                        return;
                    }

                    if (putSecondMarker) {
                        myMap.setOnMapClickListener(null);
                        endMarker = myMap.addMarker(new MarkerOptions()
                                .position(endMarkerPoint)
                                .icon(BitmapDescriptorFactory.fromResource(R.drawable.purple_marker)));
                        endMarker.hideInfoWindow();

                        dropFirstPinTv.setVisibility(View.GONE);
                        dropAnotherPinTv.setVisibility(View.GONE);

                        layoutMeasure.setVisibility(View.VISIBLE);
                        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                                LinearLayout.LayoutParams.MATCH_PARENT,
                                getPixelValue(activity, 95)
                        );
                        layoutMeasure.setLayoutParams(params);

                        calculateDistanceFromAPI(startMarkerPoint, endMarkerPoint);

                        layoutMeasure.setVisibility(View.VISIBLE);

                        putFirstMarker = false;
                        putSecondMarker = false;
                        moveCameraToMeasuredArea();
                    }
                }
            }
        };

    }

    public void moveCameraToMeasuredArea() {
        if (startMarker == null || endMarker == null) {
            return;
        }

        LatLngBounds measuredBounds = new LatLngBounds.Builder()
                .include(startMarker.getPosition())
                .include(endMarker.getPosition())
                .build();

        myMap.animateCamera(CameraUpdateFactory.newLatLngBounds(measuredBounds, animatedCameraPadding));
    }

    public void replaceFragment(int resourceID, Fragment mFragment) {
        if (mFragment != null) {
            measureSearchLayout.setVisibility(View.VISIBLE);
            FragmentTransaction mFragmentTransaction = activity.getSupportFragmentManager().beginTransaction();
            mFragmentTransaction
                    .replace(resourceID, mFragment)
                    .commit();
        }
    }

    public void showFragment(Fragment f) {
        replaceFragment(R.id.measureSearchLayout, f);
        searchView.setIconified(true);
    }

    public void goInMap(LatLng center, float zoom) {
        if (myMap != null && center != null) {
            myMap.moveCamera(CameraUpdateFactory.newLatLngZoom(center, zoom));
        }
    }

    public void showSearchIcon() {
        if (menu != null)
            onCreateOptionsMenu(menu, activity.getMenuInflater());
    }

    private void drawMarkers() {
        if (startMarkerPoint != null) {

            startMarker = myMap.addMarker(new MarkerOptions()
                    .position(startMarkerPoint)
                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.purple_marker)));
            myMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
                @Override
                public boolean onMarkerClick(Marker marker) {
                    return true;
                }
            });

            if (endMarkerPoint != null) {
                myMap.setOnMapClickListener(null);
                endMarker = myMap.addMarker(new MarkerOptions()
                        .position(endMarkerPoint)
                        .icon(BitmapDescriptorFactory.fromResource(R.drawable.purple_marker)));
                endMarker.hideInfoWindow();
            }
        }
    }

    private void showNoInternetDialog() {
        new AlertDialog.Builder(getActivity())
                .setTitle(R.string.no_internet)
                .setMessage(R.string.must_online_measure)
                .setNeutralButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                })
                .setIcon(android.R.drawable.ic_dialog_alert)
                .show();
    }

    private LatLng findNearestPointOnTrail(LatLng clickPoint) {
        LatLng nearestPoint = null;
        double minDistance = Double.MAX_VALUE;
        List<TrailSegmentLight> nearbySegments = TrailUtility.nearbySegments(clickPoint);

        for (TrailSegmentLight segment : nearbySegments) {
            for (LatLng point : TrailUtility.compressedSegment(MainActivity.listPoints.get(segment.objectId), TRAIL_MEASUREMENT_DROPPED_POINTS_COUNT)) {

                double distance = TrailUtility.distanceTo(clickPoint, point);
                if (distance < minDistance && withinReasonableTolerance(distance)) {
                    minDistance = distance;
                    nearestPoint = new LatLng(point.latitude, point.longitude);
                }
            }
        }
        Log.d(TAG, minDistance + "");

        return nearestPoint;
    }

    private boolean withinReasonableTolerance(double distance) {
        int divisor = 1;
        double zoomLevel = myMap.getCameraPosition().zoom;

        if (zoomLevel <= LOW_ZOOM_LEVEL) {
            divisor = 1;
        } else if (zoomLevel <= MID_ZOOM_LEVEL) {
            divisor = 2;
        } else if (zoomLevel <= HIGH_ZOOM_LEVEL) {
            divisor = 4;
        } else {
            divisor = 8;
        }

        return distance < DROP_PIN_TOLERANCE_DISTANCE_COEFFICIENT / zoomLevel / divisor;
    }

    private void askPermissionsAndShowMyLocation() {

        // With API> = 23, you have to ask the user for permission to view their location.
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

        // Show current location on Map.
        this.showMyLocation();
    }

    // Call this method only when you have the permissions to view a user's location.
    private void showMyLocation() {

        LocationManager locationManager = (LocationManager) activity.getSystemService(LOCATION_SERVICE);
        String locationProvider = this.getEnabledLocationProvider();

        Location myLocation = null;
        try {

            myLocation = locationManager
                    .getLastKnownLocation(locationProvider);
        }
        // With Android API >= 23, need to catch SecurityException.
        catch (SecurityException e) {
            Log.e("ActivityTackeSegment", "Show My Location Error:" + e.getMessage());
            e.printStackTrace();
            return;
        }
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
            Log.i("ActivityTrackerFragment", "No location provider enabled!");
            return null;
        }
        return bestProvider;
    }

    private void calculateDistanceFromAPI(final LatLng startDistance, final LatLng endDistance) {

        String toleranceEncodeUrl = "%7B%22distance%22:100.0,%22units%22:%22esriMeters%22%7D&" +
                "Stops=%7B%22geometryType%22:%22esriGeometryPoint%22,%22features%22:%5B%7B%22" +
                "geometry%22:%7B%22x%22:" + startDistance.longitude + ",%22y%22:" + startDistance.latitude + ",%22spatialReference%22:%7B%22wkid%22:" +
                "4326%7D%7D%7D,%7B%22geometry%22:%7B%22x%22:" + endDistance.longitude + ",%22y%22:" + endDistance.latitude + ",%22spatialReference%22" +
                ":%7B%22wkid%22:4326%7D%7D%7D%5D,%22sr%22:%7B%22wkid%22:4326%7D%7D";
//
//        String urlAPI = "https://devmap.thegreattrail.ca/arcgis/rest/services/TCT/Tools/GPServer/GetRouteGen/execute?f=json&env:outSR=4326&Tolerance={\"distance\":\100," +
//                "\"units\":\"esriMeters\"}&Stops=" +
//                "{\"geometryType\":\"esriGeometryPoint\",\"features\":[{\"geometry\":{\"x\":" + startDistance.longitude + ",\"y\":" + startDistance.latitude + ",\"spatialReference\":{\"wkid\":4326}}}," +
//                "{\"geometry\":{\"x\":" + endDistance.longitude + ",\"y\":" + endDistance.latitude + ",\"spatialReference\":{\"wkid\":4326}}}],\"sr\":{\"wkid\":4326}}";


        String urlAPI = "https://devmap.thegreattrail.ca/arcgis/rest/services/TCT/Tools/GPServer/GetRouteGen/execute?f=json&env:outSR=4326&Tolerance=" + toleranceEncodeUrl;

        RequestQueue queue = Volley.newRequestQueue(activity);  // this = context


        StringRequest postRequest = new StringRequest(Request.Method.GET, urlAPI,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {

                        Log.i("LocationService", " Reponse api calculateDistanceFromAPI ........................................................................  = " + response.toString());

                        try {

                            JSONObject jsonObj = new JSONObject(response);
                            JSONArray results = jsonObj.getJSONArray("results");
                            JSONObject value = results.getJSONObject(0).getJSONObject("value");
                            JSONArray features = value.getJSONArray("features");
                            JSONObject attributes = features.getJSONObject(0).getJSONObject("attributes");

                            double totalLength = attributes.getDouble("Total_Length");
                            String distance = String.format("%.2f", totalLength / 1000);
                            distanceTxt.setText(distance.toString() + " km");
                            JSONObject geometry = features.getJSONObject(0).getJSONObject("geometry");


                            ArrayList<LatLng> coordinatesList = new ArrayList<LatLng>();

                            for (int j = 0; j < geometry.getJSONArray("paths").length(); j++) {

                                JSONArray paths = geometry.getJSONArray("paths").getJSONArray(j);

                                for (int i = 0; i < paths.length(); i++) {
                                    JSONArray tempCoordinate = paths.getJSONArray(i);
                                    double laltitude = tempCoordinate.getDouble(1);
                                    double longitude = tempCoordinate.getDouble(0);
                                    LatLng coordinate = new LatLng(laltitude, longitude);
                                    coordinatesList.add(coordinate);
                                }

                            }

                            int coordinatesListSize = coordinatesList.size();
                            String pathString = coordinatesList.get(0).latitude + "," + coordinatesList.get(0).longitude;

                            if (coordinatesListSize > Constants.maxElevationCoordinatePairs) {
                                int bound = calculateBound(coordinatesListSize);

                                for (int i = 1; i < coordinatesListSize - 1; i = i + bound) {
                                    pathString += "|" + coordinatesList.get(i).latitude + "," + coordinatesList.get(i).longitude;
                                }

                            } else {
                                for (int i = 1; i < coordinatesListSize - 1; i++) {
                                    pathString += "|" + coordinatesList.get(i).latitude + "," + coordinatesList.get(i).longitude;
                                }
                            }

                            pathString += "|" + coordinatesList.get(coordinatesListSize - 1).latitude + "," + coordinatesList.get(coordinatesListSize - 1).longitude;


                            int samples = calculateSamples(coordinatesList.size());


                            calculateElevationFromAPI(pathString, samples, totalLength);

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        // error
//                        Log.d("Error.Response", error.getMessage());
                        // TODO handle the error
                        error.printStackTrace();
                    }
                }
        );

        postRequest.setRetryPolicy(new DefaultRetryPolicy(
                TIMEOUT,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        queue.add(postRequest);
    }

    private int calculateSamples(int size) {
        int sample = size / 10;
        if (sample <= Constants.minElevationSamples) {
            sample = Constants.minElevationSamples;
        } else if (sample > Constants.maxElevationSamples) {
            sample = Constants.maxElevationSamples;

        }


        return sample;
    }

    private int calculateBound(int size) {
        int bound = 1;
        if (size > Constants.maxElevationCoordinatePairs) {
            bound = size / Constants.maxElevationCoordinatePairs;
            if (size % Constants.maxElevationCoordinatePairs != 0) {
                bound++;
            }
        }

        return bound;
    }

    private void calculateElevationFromAPI(String path, int samples, final double totalDistance) {

        Log.i("LocationService", " Reponse api ........................................................................ path = " + path.toString());

        String urlAPI = "https://maps.googleapis.com/maps/api/elevation/json?path=" + path + "&samples=" + samples + "&key=" + "AIzaSyDZL37pIbCDiGjHdPQv2pWNQKOIunX8WWA";  // "AIzaSyDZL37pIbCDiGjHdPQv2pWNQKOIunX8WWA"

        int tempLenght = urlAPI.length();

        RequestQueue queue = Volley.newRequestQueue(activity);  // this = context


        StringRequest postRequest = new StringRequest(Request.Method.GET, urlAPI,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {

                        Log.i("LocationService", " Reponse api calculateElevationFromAPI  ........................................................................  = " + response.toString());


                        ArrayList<ElevationClass> elevationsList = new ArrayList<ElevationClass>();

                        try {

                            JSONObject jsonObj = new JSONObject(response);

                            JSONArray results = jsonObj.getJSONArray("results");

                            float boundInMeter = (float) totalDistance / (results.length() - 1);

                            float distance = 0;

                            for (int i = 0; i < results.length(); i++) {

                                double elevation = results.getJSONObject(i).getDouble("elevation");
                                // double  resolution = results.getJSONObject(i).getDouble("resolution");
                                JSONObject location = results.getJSONObject(i).getJSONObject("location");
                                double latitude = location.getDouble("lat");
                                double longitude = location.getDouble("lng");

                                distance = i * boundInMeter;


                                ElevationClass elevationClass = new ElevationClass((float) elevation, latitude, longitude, distance);
                                elevationsList.add(elevationClass);
                            }

                            double elevationDisplayed = 0;
                            for (int i = 0; i < elevationsList.size() - 1; i++) {
                                if (elevationsList.get(i).getElevation() < elevationsList.get(i + 1).getElevation()) {
                                    elevationDisplayed = elevationDisplayed + elevationsList.get(i + 1).getElevation() - elevationsList.get(i).getElevation();
                                }
                            }
                            String distanceString = String.format("%.2f", elevationDisplayed);

                            elevationTxt.setText(distanceString + " m");

                            ArrayList<Entry> listPointsDiagramme = new ArrayList<Entry>();


                            for (int i = 0; i < elevationsList.size(); i++) {
                                listPointsDiagramme.add(new Entry((float) elevationsList.get(i).getDistance() / 1000, (float) elevationsList.get(i).getElevation()));
                            }

                            drawChart(listPointsDiagramme);


                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        // error
                       /* Log.d("Error.Response", error.getMessage());
                        // TODO handle the error
                        error.printStackTrace();*/
                    }
                }
        );
        queue.add(postRequest);


    }

    private void drawChart(ArrayList<Entry> listPointsDiagramme) {


        lineChart.setOnChartGestureListener(this);
        lineChart.setOnChartValueSelectedListener(this);
        lineChart.setDrawGridBackground(false);
        lineChart.setBackgroundColor(Color.rgb(55, 100, 125));
        Description desc = new Description();
        desc.setText("");
        lineChart.setDescription(desc);

        // add data
        setData(listPointsDiagramme);


        Legend legend = lineChart.getLegend();
        legend.setEnabled(false);

        lineChart.setTouchEnabled(true);

        // enable scaling and dragging
        lineChart.setDragEnabled(true);
        lineChart.setScaleEnabled(true);


        lineChart.setExtraBottomOffset(dpToPx(4));

        lineChart.setClipToPadding(true);

        YAxis leftAxis = lineChart.getAxisLeft();
        leftAxis.removeAllLimitLines(); // reset all limit lines to avoid overlapping lines

        leftAxis.enableGridDashedLine(10f, 10f, 0f);
        leftAxis.setDrawZeroLine(false);

        // limit lines are drawn behind data (and not on top)
        leftAxis.setDrawLimitLinesBehindData(true);

        lineChart.getAxisRight().setEnabled(false);


        //********************************

        XAxis xAxis = lineChart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setTextSize(11f);
        xAxis.setTextColor(Color.YELLOW);
        xAxis.setDrawAxisLine(true);
        xAxis.setDrawGridLines(false);  //MyYAxisValueFormatter
        // set a custom value formatter
        xAxis.setValueFormatter(new MyYAxisValueFormatter(true));


        YAxis yAxis = lineChart.getAxisLeft();
        yAxis.setDrawAxisLine(true);
        yAxis.setDrawGridLines(false);
        yAxis.setTextSize(11f); // set the text size
        yAxis.setTextColor(Color.YELLOW);
        yAxis.setValueFormatter(new MyYAxisValueFormatter(false));

        //  dont forget to refresh the drawing
        lineChart.invalidate();
    }

    private void setData(ArrayList<Entry> listPointsDiagramme) {
        ArrayList<Entry> yVals = listPointsDiagramme; // setYAxisValues();

        LineDataSet set1;

        // create a dataset and give it a type
        set1 = new LineDataSet(yVals, "Distance/Elevation");

        set1.setFillColor(Color.rgb(55, 100, 125));


        set1.setColor(Color.YELLOW);
        set1.setCircleColor(Color.YELLOW);
        set1.setLineWidth(3f);
        set1.setCircleRadius(7f);
        set1.setCircleColorHole(Color.LTGRAY);
        set1.setCircleHoleRadius(4f);
        set1.setDrawCircleHole(true);
        set1.setValueTextSize(5f);
        set1.setValueTextColor(Color.YELLOW);
        set1.setColors(Color.YELLOW);
        set1.setDrawFilled(true);


        ArrayList<ILineDataSet> dataSets = new ArrayList<ILineDataSet>();
        dataSets.add(set1); // add the datasets

        // create a data object with the datasets
        LineData data = new LineData(dataSets);

        // set data
        lineChart.setData(data);

    }

    private void ShowSearchMenu() {
        pushMeasureFragmentToStack();
        if (!measurefragStack.containsKey("MeasureSearchFragment")) {
            searchListFragment = createSearchListFragment();
            searchListFragment.resourceID = R.id.measureSearchLayout;
            searchListFragment.fragTagStack = measurefragTagStack;
            searchListFragment.fragStack = measurefragStack;
        } else {
            searchListFragment = (SearchListFragment) measurefragStack.get("MeasureSearchFragment");
        }
        replaceFragment(R.id.measureSearchLayout, searchListFragment);
    }

    private void pushMeasureFragmentToStack() {
        String segmentTag = "MeasureFragment";
        MeasureFragment measureFragment = ((MainActivity) getActivity()).getMeasureFragment();

        if (measureFragment instanceof MeasureFragment) {
            measurefragStack.put(segmentTag, measureFragment);
            measurefragTagStack.push(segmentTag);
        }
    }

    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    private void setUiValues(Bundle savedInstanceState, View view) {
        mMapView = (MapView) view.findViewById(R.id.mapview1);
        mMapView.onCreate(savedInstanceState);
        dropAnotherPinTv.setVisibility(View.GONE);

        if (mGoogleApiClient == null) {
            mGoogleApiClient = new GoogleApiClient.Builder(activity)
                    .addApi(LocationServices.API)
                    .build();
        }

        floatingActionButton = (FloatingActionButton) view.findViewById(R.id.location_toggle_fab);
        floatingActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showMyFusedLocation();
            }
        });

        upBtn = (ImageView) view.findViewById(R.id.upBtn);
        upBtn.setImageResource(R.drawable.arrow_up);
        measureSearchLayout = (FrameLayout) view.findViewById(R.id.measureSearchLayout);

        upBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (layoutVisible == false) {

                    lineChart.setVisibility(View.VISIBLE);


                    layoutVisible = true;


                    LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.MATCH_PARENT,
                            getPixelValue(activity, 95)
                    );
                    params.setMargins(0, 0, 0, getPixelValue(activity, 15));
                    layoutMeasure.setLayoutParams(params);
                    upBtn.setImageResource(R.drawable.arrow_down);
                } else {
                    lineChart.setVisibility(View.GONE);

                    layoutVisible = false;
                    LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.MATCH_PARENT,
                            getPixelValue(activity, 95)
                    );
                    layoutMeasure.setLayoutParams(params);
                    upBtn.setImageResource(R.drawable.arrow_up);
                }

            }
        });

        deleteBtn = (ImageView) view.findViewById(R.id.deleteBtn);

        deleteBtn.setImageResource(R.drawable.trash);

        deleteBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DeleteConfirmationDialog();

            }
        });
        distanceTxt = (TextView) view.findViewById(R.id.txtDistanceMeasure);
        elevationTxt = (TextView) view.findViewById(R.id.txtElevationMeasure);
        layoutMeasure = (LinearLayout) view.findViewById(R.id.layoutMeasure);
        lineChart = (LineChart) view.findViewById(R.id.chart);

        isViewCreated = true;
        loadUi();
    }

    private void showMyFusedLocation() {

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

            LatLng secondePoint = Utility.nearestPoint(firstPoint);
//            LatLng secondePoint = findNearestPointOnTrail(firstPoint);
            if (secondePoint == null) {
                return;
            }
            LatLngBounds.Builder builder = new LatLngBounds.Builder();

            builder.include(firstPoint);
            builder.include(secondePoint);

            LatLngBounds bounds = builder.build();

            int padding = 0; // offset from edges of the map in pixels  //  mMapView.getWidth()*45 / 100
            CameraUpdate cu = CameraUpdateFactory.newLatLngBounds(bounds, mMapView.getWidth(), mMapView.getHeight(), mMapView.getWidth() * 15 / 100);
            myMap.moveCamera(cu);
//            cu = CameraUpdateFactory.newLatLngZoom(firstPoint, INITIAL_ZOOM_LEVEL);
            cu = CameraUpdateFactory.newLatLngZoom(firstPoint, zoom - 0.5f);
            myMap.moveCamera(cu);
        }
        ///
    }

    private void DeleteConfirmationDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);

        builder.setTitle(R.string.clear_measurement_title);
        builder.setMessage(R.string.clear_measurement_message);

        builder.setPositiveButton(getResources().getText(R.string.yes), new DialogInterface.OnClickListener() {

            public void onClick(DialogInterface dialog, int which) {

                putFirstMarker = true;
                putSecondMarker = true;

                startMarkerPoint = null;
                endMarkerPoint = null;


                if (startMarker != null) {
                    startMarker.remove();
                }

                if (endMarker != null) {
                    endMarker.remove();
                }

                myMap.setOnMapClickListener(onMapClick());
                layoutVisible = false;
                layoutMeasure.setVisibility(View.GONE);
                lineChart.setVisibility(View.GONE);
                dropFirstPinTv.setVisibility(View.VISIBLE);
                dropAnotherPinTv.setVisibility(View.GONE);
                upBtn.setImageResource(R.drawable.arrow_up);

                distanceTxt.setText("0 Km");
                elevationTxt.setText("0 m");
                deleteMeasure = true;

                dialog.dismiss();
            }
        });

        builder.setNegativeButton(getResources().getText(R.string.no), new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {

                // Do nothing
                dialog.dismiss();
            }
        });

        AlertDialog alert = builder.create();
        alert.show();
    }

}



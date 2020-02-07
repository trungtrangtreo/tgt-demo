package ca.TransCanadaTrail.TheGreatTrail.MapView;


import android.app.SearchManager;
import android.app.SearchableInfo;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.Typeface;
import android.location.Location;
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
import android.text.Html;
import android.text.Spanned;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.Polyline;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Stack;

import butterknife.BindView;
import butterknife.ButterKnife;
import ca.TransCanadaTrail.TheGreatTrail.AppController;
import ca.TransCanadaTrail.TheGreatTrail.MainActivity;
import ca.TransCanadaTrail.TheGreatTrail.R;
import ca.TransCanadaTrail.TheGreatTrail.SearchListFragment;
import ca.TransCanadaTrail.TheGreatTrail.database.ActivityDBHelperTrail;
import ca.TransCanadaTrail.TheGreatTrail.fragments.HomeTabMapFragment;
import ca.TransCanadaTrail.TheGreatTrail.utils.Utility;

import static android.content.Context.SEARCH_SERVICE;
import static ca.TransCanadaTrail.TheGreatTrail.MainActivity.REQUEST_ID_ACCESS_COURSE_FINE_LOCATION;


public class MapFragment extends HomeTabMapFragment {

    private static final String TAG = "MapFragment";
    private static final int INITIAL_ZOOM_LEVEL = 13;
    private static final String EXTRA_CENTER_KEY = "center";
    private static final String EXTRA_ZOOM_KEY = "zoom";

    public static HashMap<String, Fragment> mapfragStack = new HashMap<>();
    public static Stack<String> mapfragTagStack = new Stack<>();

    private static boolean shownForTheFirstTime;

    public LatLng center;
    public float zoom;
    public Menu menu;
    public SearchView searchView;
    public int isSearchOpened = 0;
    public int lastSelectedSegmentId = 0;
    public int lastObjectIdMeasureTool = 0;
    public int selectedSegmentId = 0;
    @BindView(R.id.searchLayout) public FrameLayout searchLayout;

    TrailSegment selectedTrail;
    MenuItem searchItem;
    @BindView(R.id.infoTxt) TextView infoTxt;
    @BindView(R.id.segment_info_tv) TextView segmantInfoTv;

    private FloatingActionButton floatingActionButton;
    private boolean displayMap = true;
    private Tracker mTracker;
    private Polyline lastSelectedPolyline;

    public MapFragment() {
        // Required empty public constructor
        searchText = "";
    }

    public static MapFragment newInstance() {
        return new MapFragment();
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
        View view = inflater.inflate(R.layout.fragment_map, container, false);
        ButterKnife.bind(this, view);
        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        if (savedInstanceState != null) {

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
    public void onAttach(Context context) {
        super.onAttach(context);
        this.activity = (MainActivity) context;
    }

    @Override
    public void onResume() {
        super.onResume();
        if (!shownForTheFirstTime && mMapView != null) {
            shownForTheFirstTime = true;
            mMapView.onResume();
        }
        // [START screen_view_hit]
        mTracker.setScreenName("Main Trail Map View");
        mTracker.send(new HitBuilders.ScreenViewBuilder().build());
        // [END screen_view_hit]

    }

    @Override
    public void onDestroy() {
        mapfragStack = null;
        mapfragTagStack = null;
        shownForTheFirstTime = false;

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
        outState.putParcelable(EXTRA_CENTER_KEY, center);
        outState.putFloat(EXTRA_ZOOM_KEY, zoom);

        if (mMapView != null) {
            mMapView.onSaveInstanceState(outState);
        }
    }

    @Override
    protected boolean hasClickableSegments() {
        return true;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        this.menu = menu;
        menu.clear();

        inflater.inflate(R.menu.main, menu);
        super.onCreateOptionsMenu(menu, inflater);

        SearchManager searchManager = (SearchManager) activity.getSystemService(SEARCH_SERVICE);
        searchItem = menu.findItem(R.id.search);

        searchView = (SearchView) MenuItemCompat.getActionView(searchItem);
        searchView.setOnQueryTextListener(this);
        // Assumes current activity is the searchable activity
        SearchableInfo searchableInfo = searchManager.getSearchableInfo(activity.getComponentName());
        searchView.setSearchableInfo(searchableInfo);
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

        searchTextView.setTextSize(16);

        searchView.setOnCloseListener(new SearchView.OnCloseListener() {
            @Override
            public boolean onClose() {
                //Toast.makeText(activity, "map search onClose", Toast.LENGTH_SHORT).show();
                activity.getSupportActionBar().setDisplayHomeAsUpEnabled(true);
                activity.getSupportActionBar().setDisplayShowHomeEnabled(true);
                activity.getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_arrow_back);
                //  activity.onBackPressed();
                return true;
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
                    //Toast.makeText(AreaSelectionActivity.this, "Check please your Internet connection", Toast.LENGTH_SHORT).show();
                    new AlertDialog.Builder(getActivity())
                            .setTitle(R.string.no_internet)
                            .setMessage(R.string.must_online_search)
                            .setNeutralButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.dismiss();
                                }
                            })
                            .setIcon(android.R.drawable.ic_dialog_alert)
                            .show();
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
        super.onMapReady();
        showMyFusedLocation();

        center = myMap.getCameraPosition().target;
        zoom = myMap.getCameraPosition().zoom;
    }

    @Override
    protected void initializeMap(GoogleMap googleMap) {
        myMap = googleMap;
        myMap.getUiSettings().setRotateGesturesEnabled(false);
        myMap.getUiSettings().setCompassEnabled(false);

        if (MainActivity.listSegments == null) {
            myMap.setOnCameraIdleListener(null);
        } else {
            myMap.setOnCameraIdleListener(getCameraChangeListener());
        }
        myMap.setOnPolylineClickListener(polyLineClickListner());
        myMap.setOnMapClickListener(mapClickListner());

        // Set OnMapLoadedCallback Listener.
        myMap.setOnMapLoadedCallback(new GoogleMap.OnMapLoadedCallback() {

            @Override
            public void onMapLoaded() {
                if (displayMap) {
                    showMyFusedLocation();
                    displayMap = false;
                }
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

                LinearLayout info = new LinearLayout(activity);
                info.setOrientation(LinearLayout.VERTICAL);


                TextView title = new TextView(activity);
                title.setTextColor(Color.BLACK);
                title.setGravity(Gravity.CENTER);
                title.setTypeface(null, Typeface.BOLD);
                title.setText(marker.getTitle());

                TextView snippet = new TextView(activity);
                snippet.setTextColor(Color.GRAY);
                snippet.setText(marker.getSnippet());

                info.addView(title);
                info.addView(snippet);

                return info;
            }
        });

        showMyFusedLocation();
    }

    public LatLng getCenter() {
        return center;
    }

    public float getZoom() {
        return zoom;
    }

    public int getLastObjectIdMeasureTool() {
        return lastObjectIdMeasureTool;
    }

    public void setLastObjectIdMeasureTool(int lastObjectIdMeasureTool) {
        this.lastObjectIdMeasureTool = lastObjectIdMeasureTool;
    }

    public int getSelectedSegmentId() {
        return selectedSegmentId;
    }

    public GoogleMap.OnCameraIdleListener getCameraChangeListener() {
        return new GoogleMap.OnCameraIdleListener() {

            @Override
            public void onCameraIdle() {
                MapFragment.super.onCameraIdle();

                center = myMap.getCameraPosition().target;
                zoom = myMap.getCameraPosition().zoom;
            }
        };
    }

    public void showSearchIcon() {
        if (menu != null)
            onCreateOptionsMenu(menu, activity.getMenuInflater());
    }

    public void replaceFragment(int resourceID, Fragment rFragment) {
        if (rFragment != null) {
            searchLayout.setVisibility(View.VISIBLE);
            FragmentTransaction mFragmentTransaction = activity.getSupportFragmentManager().beginTransaction();
            mFragmentTransaction
                    .replace(resourceID, rFragment)
                    .commit();
        }

    }

    public void showFragment(Fragment f) {
        replaceFragment(R.id.searchLayout, f);
        searchView.setIconified(true);
    }

    public void goInMap(LatLng center, float zoom) {
        if (myMap != null) {
            myMap.moveCamera(CameraUpdateFactory.newLatLngZoom(center, zoom));
        }
    }

    public void syncMapSearch() {
        infoTxt.setVisibility(View.VISIBLE);
        segmantInfoTv.setVisibility(View.GONE);
    }

    protected void ShowSearchMenu() {
        pushMapFragmentToStack();
        if (!mapfragStack.containsKey("MapSearchFragment")) {
            searchListFragment = createSearchListFragment();
            searchListFragment.resourceID = R.id.searchLayout;
            searchListFragment.fragTagStack = mapfragTagStack;
            searchListFragment.fragStack = mapfragStack;
        } else {
            searchListFragment = (SearchListFragment) mapfragStack.get("MapSearchFragment");
        }
        replaceFragment(R.id.searchLayout, searchListFragment);
    }

    private void setUiValues(Bundle savedInstanceState, View view) {
        segmantInfoTv.setVisibility(View.GONE);
        segmantInfoTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!isNetworkAvailable()) {
                    //Toast.makeText(AreaSelectionActivity.this, "Check please your Internet connection", Toast.LENGTH_SHORT).show();
                    new AlertDialog.Builder(getActivity())
                            .setTitle(R.string.no_internet)
                            .setMessage(R.string.must_online_trail)
                            .setNeutralButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.dismiss();
                                }
                            })
                            .setIcon(android.R.drawable.ic_dialog_alert)
                            .show();
                    return;
                }
                pushMapFragmentToStack();

                SegmentDetailsFragment segmentDetailsFragment = new SegmentDetailsFragment();
                segmentDetailsFragment.setObjectId(lastSelectedSegmentId);
                Bundle args = new Bundle();
                args.putString("trailId", selectedTrail.getTrailId());
                args.putString("trail_name", selectedTrail.getTrailName());
                segmentDetailsFragment.setArguments(args);

                replaceFragment(R.id.searchLayout, segmentDetailsFragment);

                activity.getSupportActionBar().setDisplayHomeAsUpEnabled(true);
                activity.getSupportActionBar().setDisplayShowHomeEnabled(true);
                activity.getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_arrow_back);
            }
        });

        mMapView = (MapView) view.findViewById(R.id.mapview1);
        mMapView.onCreate(savedInstanceState);

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

        displayMap = true;

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
            cu = CameraUpdateFactory.newLatLngZoom(firstPoint, INITIAL_ZOOM_LEVEL);
            myMap.moveCamera(cu);
        }

    }

    private void pushMapFragmentToStack() {
        String segmentTag = "MapFragment";
        MapFragment mapFragment = ((MainActivity) getActivity()).getMapFragment();

        if (mapFragment == null)
            return;

        if (mapFragment instanceof MapFragment) {
            mapfragStack.put(segmentTag, mapFragment);
            mapfragTagStack.push(segmentTag);
        }

    }

    private GoogleMap.OnPolylineClickListener polyLineClickListner() {
        return new GoogleMap.OnPolylineClickListener() {
            @Override
            public void onPolylineClick(Polyline polyline) {
                if (lastSelectedPolyline != null)
                    lastSelectedPolyline.setWidth(unSelectedPolylineWidth);

                lastSelectedPolyline = polyline;
                polyline.setWidth(selectedPolylineWidth);


                List<LatLng> points = polyline.getPoints();
                selectedSegmentId = findSegmentIdWithPoints(points);

                lastObjectIdMeasureTool = lastSelectedSegmentId;
                lastSelectedSegmentId = selectedSegmentId;

                infoTxt.setVisibility(View.GONE);
                segmantInfoTv.setVisibility(View.VISIBLE);

                ActivityDBHelperTrail db = ActivityDBHelperTrail.getInstance(activity);
                Cursor cursor = db.getSpecificSegments(selectedSegmentId);
                if (cursor != null && cursor.moveToFirst()) {
                    TrailSegment segment = new TrailSegment(cursor);
                    selectedTrail = segment;

                    showSegmentInfo(segment);
                }
                cursor.close();
            }
        };

    }

    private GoogleMap.OnMapClickListener mapClickListner() {
        return new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {
                if (lastSelectedPolyline != null)
                    lastSelectedPolyline.setWidth(unSelectedPolylineWidth);

                segmantInfoTv.setVisibility(View.GONE);
                infoTxt.setVisibility(View.VISIBLE);
            }
        };
    }

    private int findSegmentIdWithPoints(List<LatLng> points) {

        if (points == null || points.size() < 2)
            return -1;

        LatLng firstPoint = points.get(0);
        LatLng lastPoint = points.get(points.size() - 1);

        ArrayList<LatLng> segmentPoints;
        for (int id : MainActivity.listPoints.keySet()) {
            segmentPoints = MainActivity.listPoints.get(id);
            if (segmentPoints.get(0).equals(firstPoint) &&
                    segmentPoints.get(segmentPoints.size() - 1).equals(lastPoint))
                return id;
        }

        return -1;                      // Not found
    }

    private void showSegmentInfo(TrailSegment segment) {
        if (segment == null)
            return;

        String text = getString(R.string.segmentInfo, segment.trailName, segment.sumLengthKm);
        Spanned formattedText;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
            formattedText = Html.fromHtml(text, Html.FROM_HTML_MODE_LEGACY);
        } else {
            formattedText = Html.fromHtml(text); // for 24 api and more
        }
        segmantInfoTv.setText(formattedText);
    }

    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

}
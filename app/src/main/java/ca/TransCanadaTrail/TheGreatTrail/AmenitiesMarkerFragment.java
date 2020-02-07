package ca.TransCanadaTrail.TheGreatTrail;

import android.content.Context;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.text.Html;
import android.util.Log;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.maps.android.clustering.Cluster;
import com.google.maps.android.clustering.ClusterManager;
import com.google.maps.android.clustering.view.DefaultClusterRenderer;
import com.google.maps.android.ui.IconGenerator;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import ca.TransCanadaTrail.TheGreatTrail.database.AmenityDBHelperTrail;
import ca.TransCanadaTrail.TheGreatTrail.fragments.BaseTrailDrawingFragment;
import ca.TransCanadaTrail.TheGreatTrail.utils.AmenitySegment;

import static ca.TransCanadaTrail.TheGreatTrail.ActivityTracker.ActivityTrackerFragment.trackerfragStack;
import static ca.TransCanadaTrail.TheGreatTrail.MapView.MapFragment.mapfragStack;
import static ca.TransCanadaTrail.TheGreatTrail.MeasureTool.MeasureFragment.measurefragStack;

/**
 * Created by hardikfumakiya on 2016-12-22.
 */

public class AmenitiesMarkerFragment extends BaseTrailDrawingFragment implements ClusterManager.OnClusterItemClickListener<MyItem> {

    private static final String ARGUMENT_AMENITY_TYPE = "ARGUMENT_AMENITY_TYPE";
    private static final String ARGUMENT_CAMERA_POSITION = "ARGUMENT_CAMERA_POSITION";
    public ArrayList<AmenitySegment> amenitySegments = new ArrayList<AmenitySegment>();

    MyClusterRenderer renderer;
    String amenityType;
    ClusterManager<MyItem> clusterManager;
    MainActivity activity;

    @BindView(R.id.infoTxt) TextView infoTxt;
    @BindView(R.id.rl) LinearLayout rl;
    @BindView(R.id.linearlayout) LinearLayout linearlayout;

    private Marker mSelectedMarker;
    private CameraPosition mCameraPosition;

    public AmenitiesMarkerFragment() {
        // Required empty public constructor
    }

    public static AmenitiesMarkerFragment newInstance(String amenityType, CameraPosition cameraPosition) {
        Bundle args = new Bundle();
        args.putString(ARGUMENT_AMENITY_TYPE, amenityType);
        args.putParcelable(ARGUMENT_CAMERA_POSITION, cameraPosition);

        AmenitiesMarkerFragment fragment = new AmenitiesMarkerFragment();
        fragment.setArguments(args);
        return fragment;
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

        if (getArguments() == null)
            return;

        amenityType = getArguments().getString(ARGUMENT_AMENITY_TYPE);
        mCameraPosition = getArguments().getParcelable(ARGUMENT_CAMERA_POSITION);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_amenity_marker, container, false);
        ButterKnife.bind(this, view);

        mMapView = (MapView) view.findViewById(R.id.mapview1);
        mMapView.onCreate(savedInstanceState);

        activity.getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        activity.getSupportActionBar().setDisplayShowHomeEnabled(true);
        activity.getSupportActionBar().setTitle(Html.fromHtml("<small>" + amenityType + "</small>"));

        if (mGoogleApiClient == null) {
            mGoogleApiClient = new GoogleApiClient.Builder(activity)
                    .addApi(LocationServices.API)
                    .build();
        }

        linearlayout.setVisibility(View.GONE);

        FloatingActionButton floatingActionButton = (FloatingActionButton) view.findViewById(R.id.location_toggle_fab);
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

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:

                break;
            default:
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void retrieveAmenities() {

        try {
            int amenity_type = 0;

            switch (amenityType) {

                case "Access Point":
                    amenity_type = 2;
                    break;
                case "Parking":
                    amenity_type = 1;
                    break;
                case "Rest Areas":
                    amenity_type = 3;
                    break;
                case "Points d'accès":
                    amenity_type = 2;
                    break;
                case "Stationnement":
                    amenity_type = 1;
                    break;
                case "Aires de repos":
                    amenity_type = 3;
                    break;

            }

            AmenityDBHelperTrail db = AmenityDBHelperTrail.getInstance(activity);

            amenitySegments.clear();
            Cursor cursor = null;
            cursor = db.getAmenitiesByType(amenity_type);

            if (cursor != null && cursor.moveToFirst()) {
                do {
                    AmenitySegment segment = new AmenitySegment(cursor);
                    amenitySegments.add(segment);
                }
                while (cursor.moveToNext());
            }
            moveCameraToLatestCameraPosition();
            initMarkers();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void moveCameraToLatestCameraPosition() {
        if (myMap == null || mCameraPosition == null)
            return;

        myMap.moveCamera(CameraUpdateFactory.newCameraPosition(mCameraPosition));
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);

        if (MainActivity.currentTab.equals("MapFragment") && mapfragStack.containsKey("MapSearchFragment")) {
            menu.clear();
        } else if (MainActivity.currentTab.equals("MeasureFragment") && measurefragStack.containsKey("MeasureSearchFragment")) {
            menu.clear();
        } else if (MainActivity.currentTab.equals("ActivityTrackerFragment") && trackerfragStack.containsKey("TrackerSearchFragment")) {
            menu.clear();
        }

    }

    private void askPermission() {

        // With API> = 23, you have to ask the user for permission to view their location.
        if (Build.VERSION.SDK_INT >= 23) {
            int accessCoarsePermission = ContextCompat.checkSelfPermission(getActivity(), android.Manifest.permission.ACCESS_COARSE_LOCATION);
            int accessFinePermission = ContextCompat.checkSelfPermission(getActivity(), android.Manifest.permission.ACCESS_FINE_LOCATION);

            if (accessCoarsePermission != PackageManager.PERMISSION_GRANTED
                    || accessFinePermission != PackageManager.PERMISSION_GRANTED) {
                // The Permissions to ask user.
                String[] permissions = new String[]{android.Manifest.permission.ACCESS_COARSE_LOCATION, android.Manifest.permission.ACCESS_FINE_LOCATION};
                // Show a dialog asking the user to allow the above permissions.
                ActivityCompat.requestPermissions(getActivity(), permissions, MainActivity.REQUEST_ID_ACCESS_COURSE_FINE_LOCATION);
                return;
            }
        }
    }

    private void initMarkers() {
        clusterManager = new ClusterManager<MyItem>(activity, myMap);

        MultiListener ml = new MultiListener();
        ml.registerListener(getCameraChangeListener());
        ml.registerListener(clusterManager);
        myMap.setOnCameraIdleListener(ml);
        myMap.getUiSettings().setCompassEnabled(false);

        renderer = new MyClusterRenderer(activity, myMap, clusterManager);

        double lat, lng;

        int size = amenitySegments.size();
        MyItem offsetItem = null;
        for (int i = 0; i < size; i++) {
            AmenitySegment amenitySegment = amenitySegments.get(i);
            lat = Double.parseDouble(amenitySegment.getLat());
            lng = Double.parseDouble(amenitySegment.getLng());
            offsetItem = new MyItem(lat, lng, amenitySegment.getObjectid());

            clusterManager.addItem(offsetItem);
        }
        clusterManager.setOnClusterItemClickListener(this);
        myMap.setOnMarkerClickListener(clusterManager);
        clusterManager.setRenderer(renderer);
    }

    private void ShowFacilities(Cursor c) {
        rl.removeAllViews();
        boolean information, picnic_table, restroom, water;

        information = (c.getInt(c.getColumnIndex("information")) != 0);
        picnic_table = (c.getInt(c.getColumnIndex("picnic_table")) != 0);
        restroom = (c.getInt(c.getColumnIndex("restroom")) != 0);
        water = (c.getInt(c.getColumnIndex("water")) != 0);

        if (information) {
            ImageView img = new ImageView(activity);
            LinearLayout.LayoutParams viewParamsCenter = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            viewParamsCenter.setMargins(5, 5, 5, 5);
            img.setLayoutParams(viewParamsCenter);
            img.setImageResource(R.drawable.info_centre);
            img.setVisibility(View.GONE);
            rl.addView(img);
        }
        if (picnic_table) {
            ImageView img = new ImageView(activity);
            LinearLayout.LayoutParams viewParamsCenter = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            viewParamsCenter.setMargins(5, 5, 5, 5);
            img.setLayoutParams(viewParamsCenter);
            img.setImageResource(R.drawable.picnic_area);
            rl.addView(img);
        }
        if (restroom) {
            ImageView img = new ImageView(activity);
            LinearLayout.LayoutParams viewParamsCenter = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            viewParamsCenter.setMargins(5, 5, 5, 5);
            img.setLayoutParams(viewParamsCenter);
            img.setImageResource(R.drawable.restroom);
            rl.addView(img);
        }
        if (water) {
            ImageView img = new ImageView(activity);
            LinearLayout.LayoutParams viewParamsCenter = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            viewParamsCenter.setMargins(5, 5, 5, 5);
            img.setLayoutParams(viewParamsCenter);
            img.setImageResource(R.drawable.water);
            rl.addView(img);
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
        super.onPause();
        if (mMapView != null) {
            mMapView.onPause();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mMapView != null) {
            try {
                mMapView.onDestroy();
            } catch (NullPointerException e) {
                Log.e("LocationService", "Error while attempting MapView.onDestroy(), ignoring exception", e);
            }
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (mMapView != null) {
            mMapView.onSaveInstanceState(outState);
        }
    }

    @Override
    protected boolean hasClickableSegments() {
        return false;
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        MenuInflater inflater = getActivity().getMenuInflater();
        inflater.inflate(R.menu.contextual_menu, menu);
    }

    @Override
    protected void initializeMap(GoogleMap googleMap) {
        myMap = googleMap;
        myMap.getUiSettings().setMapToolbarEnabled(false);

        myMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        myMap.getUiSettings().setZoomControlsEnabled(false);

        if (Build.VERSION.SDK_INT >= 23) {
            if (ActivityCompat.checkSelfPermission(getActivity(), android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getActivity(), android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                return;
            }
        }

        myMap.setMyLocationEnabled(true);

        myMap.getUiSettings().setMyLocationButtonEnabled(false);
        retrieveAmenities();
    }

    public GoogleMap.OnCameraIdleListener getCameraChangeListener() {
        return new GoogleMap.OnCameraIdleListener() {

            @Override
            public void onCameraIdle() {
                AmenitiesMarkerFragment.super.onCameraIdle();
            }
        };
    }

    @Override
    public boolean onClusterItemClick(MyItem myItem) {
        try {
            if (mSelectedMarker != null && mSelectedMarker.isVisible()) {

                mSelectedMarker.setIcon(BitmapDescriptorFactory.fromResource(R.drawable.blue_poi_marker));
            }

            mSelectedMarker = renderer.getMarker(myItem);

            mSelectedMarker.setIcon(BitmapDescriptorFactory.fromResource(R.drawable.yellow_poi_marker));

            AmenityDBHelperTrail db = AmenityDBHelperTrail.getInstance(activity);
            Cursor c = db.getAmenitiesByObjectId(myItem.getObjectid());
            infoTxt.setText(c.getString(c.getColumnIndex("name_amenity")));

            ShowFacilities(c);

            if (linearlayout.getVisibility() == View.GONE) {
                linearlayout.setVisibility(View.VISIBLE);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    class MyClusterRenderer extends DefaultClusterRenderer<MyItem> {

        private final IconGenerator mClusterIconGenerator = new IconGenerator(activity);

        public MyClusterRenderer(Context context, GoogleMap map,
                                 ClusterManager<MyItem> clusterManager) {
            super(context, map, clusterManager);
        }

        @Override
        protected void onBeforeClusterItemRendered(MyItem item, MarkerOptions markerOptions) {

            markerOptions.icon(BitmapDescriptorFactory.fromResource(R.drawable.blue_poi_marker));
            markerOptions.snippet("").title("");
        }


        @Override
        protected void onClusterItemRendered(MyItem clusterItem, Marker marker) {
            super.onClusterItemRendered(clusterItem, marker);
        }

        @Override
        protected void onBeforeClusterRendered(Cluster<MyItem> cluster, MarkerOptions markerOptions) {
            mClusterIconGenerator.setColor(Color.WHITE);
            Bitmap icon = mClusterIconGenerator.makeIcon(String.valueOf(cluster.getSize()));
            markerOptions.icon(BitmapDescriptorFactory.fromBitmap(icon));
        }
    }

    public class MultiListener implements GoogleMap.OnCameraIdleListener {
        private List<GoogleMap.OnCameraIdleListener> mListeners = new ArrayList<GoogleMap.OnCameraIdleListener>();

        public void registerListener(GoogleMap.OnCameraIdleListener listener) {
            mListeners.add(listener);
        }


        @Override
        public void onCameraIdle() {
            for (GoogleMap.OnCameraIdleListener ccl : mListeners) {
                ccl.onCameraIdle();
            }
        }
    }

}

package ca.TransCanadaTrail.TheGreatTrail;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.net.Uri;
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
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import butterknife.ButterKnife;
import ca.TransCanadaTrail.TheGreatTrail.fragments.BaseTrailDrawingFragment;

/**
 * Created by hardikfumakiya on 2016-12-22.
 */

public class GooglePlaceFragment extends BaseTrailDrawingFragment implements GoogleApiClient.ConnectionCallbacks {

    Location myLocation;
    LinearLayout linearlayout;
    String placeID, place_name, vicinity, photo_reference, phone_number;
    boolean isCity = false;
    ImageView callPlace;
    MainActivity activity;
    private TextView namePlace, addressPlace;

    private static GooglePlaceFragment newInstance() {
        return new GooglePlaceFragment();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        activity = (MainActivity) context;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.google_place, container, false);
        ButterKnife.bind(this, view);

        activity.getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        activity.getSupportActionBar().setDisplayShowHomeEnabled(true);

        mMapView = (MapView) view.findViewById(R.id.mapview1);
        mMapView.onCreate(savedInstanceState);
        namePlace = (TextView) view.findViewById(R.id.namePlace);
        addressPlace = (TextView) view.findViewById(R.id.addressPlace);
        callPlace = (ImageView) view.findViewById(R.id.callPlace);

        callPlace.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (phone_number != null) {

                    Uri call = Uri.parse("tel:" + phone_number);
                    if (Build.VERSION.SDK_INT >= 23) {
                        int accessCallPermission
                                = ContextCompat.checkSelfPermission(activity, Manifest.permission.CALL_PHONE);

                        if (accessCallPermission != PackageManager.PERMISSION_GRANTED) {
                            // The Permissions to ask user.
                            String[] permissions = new String[]{Manifest.permission.CALL_PHONE};
                            // Show a dialog asking the user to allow the above permissions.
                            ActivityCompat.requestPermissions(activity, permissions, MainActivity.REQUEST_ID_ACCESS_COURSE_FINE_LOCATION);
                            return;
                        }
                    }
                    Intent surf = new Intent(Intent.ACTION_CALL, call);
                    activity.startActivity(surf);
                } else {
                    Toast.makeText(activity, "Sorry, couldn't place a call", Toast.LENGTH_SHORT).show();
                }
            }
        });
        linearlayout = (LinearLayout) view.findViewById(R.id.linearlayout);


        if (mGoogleApiClient == null) {
            mGoogleApiClient = new GoogleApiClient.Builder(activity)
                    .addApi(LocationServices.API)
                    .addConnectionCallbacks(this)
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

        placeID = getArguments().getString("placeid");
        setupMapView();
        return view;
    }

    public StringBuilder sbMethod() {
        StringBuilder sb;
        sb = new StringBuilder("https://maps.googleapis.com/maps/api/place/details/json?");
        sb.append("placeid=" + placeID);
        sb.append("&key=" + SearchListFragment.API_KEY);
        return sb;
    }

    private void retrievePlace() {

        try {
            StringBuilder sbValue = new StringBuilder(sbMethod());

            PlacesDetailTask placesTask = new PlacesDetailTask();
            placesTask.execute(sbValue.toString());
        } catch (Exception e) {
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

    @Override
    protected void initializeMap(GoogleMap googleMap) {
        myMap = googleMap;

        if (MainActivity.listSegments == null) {
            myMap.setOnCameraIdleListener(null);
        } else {
            myMap.setOnCameraIdleListener(getCameraChangeListener());
        }

        myMap.getUiSettings().setMapToolbarEnabled(false);


        // Set OnMapLoadedCallback Listener.
        myMap.setOnMapLoadedCallback(new GoogleMap.OnMapLoadedCallback() {
            @Override
            public void onMapLoaded() {
                askPermissionsAndShowMyLocation();
            }
        });
        myMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        myMap.getUiSettings().setZoomControlsEnabled(false);
        myMap.setOnInfoWindowClickListener(new GoogleMap.OnInfoWindowClickListener() {
            @Override
            public void onInfoWindowClick(Marker marker) {

                if (!isCity) {
                    pushPlaceFragmentToStack();

                    PlaceDetailFragment frag = new PlaceDetailFragment();
                    Bundle bundle = new Bundle();
                    bundle.putString("place_name", place_name);
                    bundle.putString("place_id", placeID);
                    bundle.putString("vicinity", vicinity);
                    if (photo_reference != null)
                        bundle.putString("photo_reference", photo_reference);

                    frag.setArguments(bundle);

                    replaceFragment(SearchListFragment.resourceID, frag);

                }

            }
        });

        if (Build.VERSION.SDK_INT >= 23) {
            if (ActivityCompat.checkSelfPermission(getActivity(), android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getActivity(), android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                return;
            }
        }

        myMap.setMyLocationEnabled(true);
        myMap.getUiSettings().setMyLocationButtonEnabled(false);
    }

    @Override
    protected boolean hasClickableSegments() {
        return false;
    }

    private void replaceFragment(int resourceID, Fragment replaceFragment) {
        FragmentTransaction mFragmentTransaction = activity.getSupportFragmentManager().beginTransaction();

        mFragmentTransaction
                .replace(resourceID, replaceFragment)
                .commit();
    }

    private void pushPlaceFragmentToStack() {
        FragmentManager fragmentManager = activity.getSupportFragmentManager();
        Fragment gplace = null;
        String fragmentTag = "MoreListFragment";

        switch (MainActivity.currentTab) {
            case "MapFragment":
                fragmentTag = "Map" + fragmentTag;
                gplace = fragmentManager.findFragmentById(R.id.searchLayout);

                break;
            case "MeasureFragment":
                fragmentTag = "Measure" + fragmentTag;
                gplace = fragmentManager.findFragmentById(R.id.measureSearchLayout);

                break;
            case "ActivityTrackerFragment":
                fragmentTag = "Tracker" + fragmentTag;
                gplace = fragmentManager.findFragmentById(R.id.trackerSearchLayout);
                break;
        }

        if (gplace instanceof GooglePlaceFragment) {
            SearchListFragment.fragStack.put(fragmentTag, gplace);
            SearchListFragment.fragTagStack.push(fragmentTag);
        }
    }

    public GoogleMap.OnCameraIdleListener getCameraChangeListener() {
        return new GoogleMap.OnCameraIdleListener() {
            @Override
            public void onCameraIdle() {
                GooglePlaceFragment.super.onCameraIdle();
            }
        };
    }

    private void askPermissionsAndShowMyLocation() {

        // With API> = 23, you have to ask the user for permission to view their location.
        if (Build.VERSION.SDK_INT >= 23) {
            int accessCoarsePermission = ContextCompat.checkSelfPermission(getActivity(), android.Manifest.permission.ACCESS_COARSE_LOCATION);
            int accessFinePermission = ContextCompat.checkSelfPermission(getActivity(), android.Manifest.permission.ACCESS_FINE_LOCATION);

            if (accessCoarsePermission != PackageManager.PERMISSION_GRANTED || accessFinePermission != PackageManager.PERMISSION_GRANTED) {
                // The Permissions to ask user.
                String[] permissions = new String[]{android.Manifest.permission.ACCESS_COARSE_LOCATION, android.Manifest.permission.ACCESS_FINE_LOCATION};
                // Show a dialog asking the user to allow the above permissions.
                ActivityCompat.requestPermissions(getActivity(), permissions, MainActivity.REQUEST_ID_ACCESS_COURSE_FINE_LOCATION);

                return;
            }
        }

        // Show current location on Map.

    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        if (ActivityCompat.checkSelfPermission(activity, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(activity, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        myLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
        retrievePlace();
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    private class PlacesDetailTask extends AsyncTask<String, Integer, String> {

        String datas = null;

        @Override
        protected String doInBackground(String... url) {
            try {
                InputStream iStream = null;
                HttpURLConnection urlConnection = null;
                try {
                    URL urls = new URL(url[0]);

                    // Creating an http connection to communicate with url
                    urlConnection = (HttpURLConnection) urls.openConnection();

                    // Connecting to url
                    urlConnection.connect();

                    // Reading data from url
                    iStream = urlConnection.getInputStream();

                    BufferedReader br = new BufferedReader(new InputStreamReader(iStream));

                    StringBuffer sb = new StringBuffer();

                    String line = "";

                    while ((line = br.readLine()) != null) {
                        sb.append(line);
                    }

                    datas = sb.toString();

                    br.close();

                } catch (Exception e) {
                    Log.d("Exception", e.toString());
                } finally {
                    iStream.close();
                    urlConnection.disconnect();
                }

            } catch (Exception e) {
                Log.d("Background Task", e.toString());
            }

            return datas;
        }

        // Executed after the complete execution of doInBackground() method
        @Override
        protected void onPostExecute(String result) {
            try {
                JSONObject jresult = new JSONObject(result);
                JSONObject placeDetails = new JSONObject(jresult.getString("result"));

                place_name = placeDetails.get("name").toString();

                String latitude, longitude;
                latitude = placeDetails.getJSONObject("geometry").getJSONObject("location").getString("lat");
                longitude = placeDetails.getJSONObject("geometry").getJSONObject("location").getString("lng");

                if (placeDetails.has("address_components")) {
                    JSONObject address_type = placeDetails.getJSONArray("address_components").getJSONObject(0);
                    JSONArray aarray = address_type.getJSONArray("types");

                    for (int i = 0; i < aarray.length(); i++) {
                        if (aarray.get(i).equals("locality") || aarray.get(i).equals("sublocality")) {
                            isCity = true;
                        }
                    }
                }
                vicinity = placeDetails.getString("formatted_address");

                if (placeDetails.has("international_phone_number"))
                    phone_number = placeDetails.getString("international_phone_number");

                if (placeDetails.has("photos")) {
                    JSONArray parray = placeDetails.getJSONArray("photos");
                    photo_reference = parray.getJSONObject(0).getString("photo_reference");
                }

                namePlace.setText(place_name);
                addressPlace.setText(vicinity);

                LatLng latLng = new LatLng(Double.parseDouble(latitude), Double.parseDouble(longitude));
                MarkerOptions markerOptions = new MarkerOptions();
                markerOptions.position(latLng);

                markerOptions.title(place_name);


                markerOptions.icon(BitmapDescriptorFactory.fromResource(R.drawable.yellow_poi_marker));

                markerOptions.snippet(vicinity);
                Marker m = myMap.addMarker(markerOptions);

                myMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 8));

                m.showInfoWindow();


            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }
}

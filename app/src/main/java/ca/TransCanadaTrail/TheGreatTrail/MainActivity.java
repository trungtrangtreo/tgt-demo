package ca.TransCanadaTrail.TheGreatTrail;


import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.PowerManager;
import android.support.annotation.IdRes;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;

import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.mikepenz.materialdrawer.Drawer;
import com.mikepenz.materialdrawer.DrawerBuilder;
import com.mikepenz.materialdrawer.model.SecondaryDrawerItem;
import com.mikepenz.materialdrawer.model.interfaces.IDrawerItem;
import com.roughike.bottombar.BottomBar;
import com.roughike.bottombar.OnTabReselectListener;
import com.roughike.bottombar.OnTabSelectListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;
import ca.TransCanadaTrail.TheGreatTrail.ActivityTracker.ActivityLogFragment;
import ca.TransCanadaTrail.TheGreatTrail.ActivityTracker.ActivityTrackerFragment;
import ca.TransCanadaTrail.TheGreatTrail.MapView.MapFragment;
import ca.TransCanadaTrail.TheGreatTrail.MapView.SegmentDetailsFragment;
import ca.TransCanadaTrail.TheGreatTrail.MapView.TrailSegmentLight;
import ca.TransCanadaTrail.TheGreatTrail.MapView.TrailSegmentLightLight;
import ca.TransCanadaTrail.TheGreatTrail.MeasureTool.MeasureFragment;
import ca.TransCanadaTrail.TheGreatTrail.MenuTool.AboutActivity;
import ca.TransCanadaTrail.TheGreatTrail.MenuTool.KeenActivity;
import ca.TransCanadaTrail.TheGreatTrail.MenuTool.SettingsActivity;
import ca.TransCanadaTrail.TheGreatTrail.OfflineMap.OfflineTrailsActivity;
import ca.TransCanadaTrail.TheGreatTrail.activities.AchievementDetailsActivity;
import ca.TransCanadaTrail.TheGreatTrail.adapters.HomeViewPagerAdapter;
import ca.TransCanadaTrail.TheGreatTrail.controllers.AchievementsManager;
import ca.TransCanadaTrail.TheGreatTrail.fragments.AchievementsFragment;
import ca.TransCanadaTrail.TheGreatTrail.fragments.BaseTrailDrawingFragment;
import ca.TransCanadaTrail.TheGreatTrail.models.Achievement;
import ca.TransCanadaTrail.TheGreatTrail.realmdoas.AchievementsDao;
import ca.TransCanadaTrail.TheGreatTrail.utils.ApplicationData;
import ca.TransCanadaTrail.TheGreatTrail.utils.DownloadedAppBadgeBroadcastReceiver;

import static ca.TransCanadaTrail.TheGreatTrail.ActivityTracker.ActivityTrackerFragment.trackerfragStack;
import static ca.TransCanadaTrail.TheGreatTrail.ActivityTracker.ActivityTrackerFragment.trackerfragTagStack;
import static ca.TransCanadaTrail.TheGreatTrail.MapView.MapFragment.mapfragStack;
import static ca.TransCanadaTrail.TheGreatTrail.MapView.MapFragment.mapfragTagStack;
import static ca.TransCanadaTrail.TheGreatTrail.MeasureTool.MeasureFragment.measurefragStack;
import static ca.TransCanadaTrail.TheGreatTrail.MeasureTool.MeasureFragment.measurefragTagStack;

public class MainActivity extends AppCompatActivity implements AchievementsFragment.GetStartedDialogIF {

    public static final int REQUEST_ID_ACCESS_COURSE_FINE_LOCATION = 100;
    public static final int DOWNLOAD_BADGE_ELPASED_TIME = 10000;
    public static GoogleApiClient client;
    public static ArrayList<TrailSegmentLightLight> listSegmentsByTrailId = null;
    public static HashMap<String, ArrayList<LatLng>> listPointsByTrailId = null;
    public static ArrayList<TrailSegmentLight> listSegments = null;
    public static HashMap<Integer, ArrayList<LatLng>> listPoints = null;
    public static ArrayList<TrailWarning> trailWarnings = null;
    public static FragmentManager manager;
    public static ActionBarDrawerToggle toggle;
    public static String currentTab = "MapFragment";
    public Drawer drawer;
    @BindView(R.id.viewpager_main_activity)
    public ViewPager homeTabsViewPager;
    PowerManager.WakeLock mWakeLock;
    @BindView(R.id.main_acitivity_bb)
    BottomBar bottomBar;
    @BindView(R.id.main_activity_tb)
    Toolbar toolbar;
    @BindView(R.id.main_activity_dl)
    DrawerLayout navigationDrawer;
    private Marker startMarker = null;
    private Marker endMarker = null;
    private boolean callMeasure = false;
    private Tracker mTracker;
    private HomeViewPagerAdapter homeTabsAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        Bundle extras = getIntent().getExtras();
        checkExtras(extras);

        PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
        mWakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK,
                getClass().getName());
        mWakeLock.acquire();

        showToolbar();

        AppController application = (AppController) getApplication();
        mTracker = application.getDefaultTracker();
        manager = getSupportFragmentManager();


        showNavigationDrawer();
        toggle = new ActionBarDrawerToggle(this, drawer.getDrawerLayout(), R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        MainActivity.toggle.syncState();

        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);     //  Fixed Portrait orientation

        homeTabsAdapter = new HomeViewPagerAdapter(getSupportFragmentManager());
        homeTabsViewPager.setAdapter(homeTabsAdapter);
        homeTabsViewPager.setOffscreenPageLimit(HomeViewPagerAdapter.FRAGMENTS_COUNT - 1);
        homeTabsViewPager.addOnPageChangeListener(onPageChangeListner());

        setBottomBar();

        client = new GoogleApiClient.Builder(this)
                .addApi(Places.GEO_DATA_API)
                .build();

        if (Build.VERSION.SDK_INT >= 21) {
            Window window = getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            window.setStatusBarColor(getResources().getColor(R.color.colorPrimaryDark));
        }

        ApplicationData applicationData = ApplicationData.getInstance(this);

        if (applicationData.isFirstRun()) {
            applicationData.setFirstRun();
            AchievementsDao.getInstance().updateUnlockedField(this, Achievement.DOWNLOAD_BADGE_ID, true);
            setDownloadedBadgeAlarm();
        }
    }

    private void checkExtras(Bundle extras) {
        if (extras != null) {
            int notificationID = extras.getInt(AchievementDetailsActivity.EXTRA_ACHIEVEMENT_ID);
            if (notificationID != 0) {
                bottomBar.selectTabWithId(R.id.tab_achievements);
                homeTabsViewPager.setCurrentItem(HomeViewPagerAdapter.ACHIEVMENETS_FRAGMENT_INDEX, false);

                Intent intent = AchievementDetailsActivity.newIntent(this, notificationID);
                startActivity(intent);
            }
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        client.connect();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mTracker.setScreenName("Main Trail Map View");
        mTracker.send(new HitBuilders.ScreenViewBuilder().build());
        AchievementsManager.getInstance().checkCelebrateAchievement(this);
        updateMapState(true);
    }

    private void updateMapState(boolean isVisible) {
        int currentFragmentIndex = homeTabsViewPager.getCurrentItem();
        Fragment fragment = getFragmentByPosition(currentFragmentIndex);
        if (fragment instanceof BaseTrailDrawingFragment) {
            ((BaseTrailDrawingFragment) fragment).resumeMapView(isVisible);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        updateMapState(false);
    }

    @Override
    public void onNewIntent(Intent intent) {
        Bundle extras = intent.getExtras();
        checkExtras(extras);
    }

    @Override
    public void onStop() {
        super.onStop();
        client.disconnect();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        clearData();
    }

    public void clearData() {
        mWakeLock.release();

        client = null;
        listSegmentsByTrailId = null;
        listPointsByTrailId = null;
        listSegments = null;
        listPoints = null;
        trailWarnings = null;
        manager = null;
    }

    public void setDownloadedBadgeAlarm() {
        Intent intent = new Intent(this, DownloadedAppBadgeBroadcastReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, 234324243, intent, 0);
        AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
        alarmManager.set(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + DOWNLOAD_BADGE_ELPASED_TIME, pendingIntent);
    }

    private void removeAllViews() {
        MapFragment mapFragment = getMapFragment();
        MeasureFragment measureFragment = getMeasureFragment();
        ActivityTrackerFragment activityTrackerFragment = getActivityTrackerFragment();

        if (currentTab.equals("MapFragment")) {
            mapfragStack.clear();
            mapfragTagStack.clear();
            mapFragment.searchLayout.setVisibility(View.GONE);
            toggle.syncState();
            mapFragment.isSearchOpened = 0;
            mapFragment.showSearchIcon();
            getSupportActionBar().setTitle("");
        } else if (currentTab.equals("MeasureFragment")) {
            measurefragStack.clear();
            measurefragTagStack.clear();
            measureFragment.measureSearchLayout.setVisibility(View.GONE);
            toggle.syncState();
            measureFragment.isSearchOpened = 0;
            measureFragment.showSearchIcon();
            getSupportActionBar().setTitle("");
        } else {
            trackerfragStack.clear();
            trackerfragTagStack.clear();
            activityTrackerFragment.trackerSearchLayout.setVisibility(View.GONE);
            toggle.syncState();
            activityTrackerFragment.isSearchOpened = 0;
            activityTrackerFragment.showSearchIcon();
            getSupportActionBar().setTitle("");
        }
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        return false;
    }

    public void showToolbar() {
        toolbar.setTitle("");
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
    }


    public void showNavigationDrawer() {
        String about = getResources().getString(R.string.about);
        DrawerBuilder builder;
        builder = new DrawerBuilder()
                .withActivity(this)
                .addDrawerItems(
                        new SecondaryDrawerItem().withIcon(R.drawable.ic_menu_about).withName(getResources().getString(R.string.about)),  // 0
                        new SecondaryDrawerItem().withIcon(R.drawable.ic_menu_privacy).withName(getResources().getString(R.string.about_keen)),  // 1
                        new SecondaryDrawerItem().withIcon(R.drawable.ic_menu_donate).withName(getResources().getString(R.string.donate)), // 2
                        new SecondaryDrawerItem().withIcon(R.drawable.ic_menu_help).withName(getResources().getString(R.string.help)), // 3
                        new SecondaryDrawerItem().withIcon(R.drawable.ic_map).withName(getResources().getString(R.string.offline_maps)),  // 4
                        new SecondaryDrawerItem().withIcon(R.drawable.ic_menu_faq).withName(getResources().getString(R.string.faq)),  // 5
                        new SecondaryDrawerItem().withIcon(R.drawable.ic_settings).withName(getResources().getString(R.string.settings)),  // 6
                        new SecondaryDrawerItem().withIcon(R.drawable.ic_menu_exit).withName(getResources().getString(R.string.exit))  // 7
                )
                .withOnDrawerItemClickListener(new Drawer.OnDrawerItemClickListener() {
                    @Override
                    public boolean onItemClick(AdapterView<?> parent, View view, int position, long id, IDrawerItem drawerItem) {

                        switch (position) {
                            case 0:
                                Intent intent = new Intent(MainActivity.this, AboutActivity.class);
                                startActivity(intent);
                                break;

                            case 1:
                                //  About KEEN
                                intent = new Intent(MainActivity.this, KeenActivity.class);
                                startActivity(intent);
                                break;

                            case 2:
                                //  https://thegreattrail.ca/give/form/
                                String url = "https://thegreattrail.ca/give/";
                                if (Locale.getDefault().getLanguage().equals("fr")) {
                                    url = "https://thegreattrail.ca/fr/give/";
                                }
                                Intent i = new Intent(Intent.ACTION_VIEW);
                                i.setData(Uri.parse(url));
                                startActivity(i);
                                break;

                            case 3:
                                Intent emailIntent = new Intent(Intent.ACTION_SEND);
                                emailIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                //  emailIntent.setType("text/plain");
                                emailIntent.setType("vnd.android.cursor.item/email");
                                emailIntent.putExtra(Intent.EXTRA_EMAIL, new String[]{"Android-support@tctrail.ca"});
                                emailIntent.putExtra(Intent.EXTRA_SUBJECT, "Help & Feedback");

                                String versionName = BuildConfig.VERSION_NAME;
                                String deviceName = Build.MODEL;
                                String deviceMan = Build.MANUFACTURER;
                                String androidOS = Build.VERSION.RELEASE;

                                String messageEnd = "\n\n\n\n\n\n\n\n\n---------------------------------------\nApplication version :  " + versionName + "\n" + "Phone Model :  " + deviceMan + "  " + deviceName + "\n" + "Android Version :  " + androidOS + "\n\n";

                                emailIntent.putExtra(Intent.EXTRA_TEXT, messageEnd);

                                startActivity(Intent.createChooser(emailIntent, "Send mail using..."));

                                break;


                            case 5:
                                //  https://thegreattrail.ca/about-us/faq/
                                url = "https://thegreattrail.ca/about-us/faq/";
                                i = new Intent(Intent.ACTION_VIEW);
                                i.setData(Uri.parse(url));
                                startActivity(i);

                                break;

                            case 4:
                                //  Offline Map

                                intent = new Intent(MainActivity.this, OfflineTrailsActivity.class);
                                startActivity(intent);
                                break;

                            case 6:
                                //  Settings
                                intent = new Intent(MainActivity.this, SettingsActivity.class);
                                startActivity(intent);
                                break;


                            case 7:
                                // Exit the whole process to remove any static data
                                finish();
                                break;
                        }

                        return false;

                    }
                });

        drawer = builder.build();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
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
        FragmentManager fragmentManager = getSupportFragmentManager();
        MapFragment mapFragment = getMapFragment();
        MeasureFragment measureFragment = getMeasureFragment();
        ActivityTrackerFragment activityTrackerFragment = getActivityTrackerFragment();

        if (currentTab.equals("MapFragment")) {
            int visible = mapFragment.searchLayout.getVisibility();
            if (visible == View.VISIBLE) {

                if (mapFragment.myMap != null && mapfragTagStack.size() < 0) {
                    if (startMarker != null)
                        startMarker.remove();

                    if (endMarker != null)
                        endMarker.remove();

                    if (measureFragment.getStartMarkerPoint() != null) {
                        if (mapFragment.myMap != null && measureFragment.deleteMeasure) {
                            measureFragment.deleteMeasure = false;
                        }
                        startMarker = mapFragment.myMap.addMarker(new MarkerOptions()
                                .position(measureFragment.getStartMarkerPoint())
                                .icon(BitmapDescriptorFactory.fromResource(R.drawable.purple_marker)));
                        mapFragment.myMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
                            @Override
                            public boolean onMarkerClick(Marker marker) {
                                return true;
                            }
                        });


                        if (measureFragment.getEndMarkerPoint() != null) {
                            //mapFragment.myMap.setOnMapClickListener(null);
                            endMarker = mapFragment.myMap.addMarker(new MarkerOptions()
                                    .position(measureFragment.getEndMarkerPoint())
                                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.purple_marker)));
                            mapFragment.myMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
                                @Override
                                public boolean onMarkerClick(Marker marker) {
                                    return true;
                                }
                            });
                        }
                    }
                }

                String lastTag;
                if (mapfragTagStack.size() > 0) {
                    lastTag = mapfragTagStack.pop();
                    Fragment mapStackFragment = mapfragStack.get(lastTag);
                    if (mapStackFragment instanceof MapFragment) {
                        mapfragStack.remove(lastTag);
                        mapFragment.searchLayout.setVisibility(View.GONE);
                        toggle.syncState();
                        mapFragment.isSearchOpened = 0;

                        mapFragment.showSearchIcon();
                        getSupportActionBar().setTitle("");

                        Fragment fragment = fragmentManager.findFragmentById(R.id.searchLayout);
                        if (fragment instanceof SegmentDetailsFragment) {
                            SegmentDetailsFragment segmentDetailsFragment = (SegmentDetailsFragment) fragmentManager.findFragmentById(R.id.searchLayout);
                            fragmentManager.beginTransaction()
                                    .remove(segmentDetailsFragment)
                                    .commit();
                        } else if (fragment instanceof SearchListFragment) {
                            SearchListFragment searchListFragment = (SearchListFragment) fragmentManager.findFragmentById(R.id.searchLayout);
                            fragmentManager.beginTransaction()
                                    .remove(searchListFragment)
                                    .commit();
                        }


                        return;
                    } else if (mapStackFragment instanceof SearchListFragment) {
                        mapfragStack.remove(lastTag);
                        ((SearchListFragment) mapStackFragment).searchData = mapFragment.searchText;

                        mapFragment.replaceFragment(R.id.searchLayout, mapStackFragment);
                        toggle.syncState();
                        getSupportActionBar().setTitle("");
                    } else if (mapStackFragment instanceof SegmentDetailsFragment) {
                        mapfragStack.remove(lastTag);
                        mapFragment.replaceFragment(R.id.searchLayout, mapStackFragment);
                    } else {

                        mapfragStack.remove(lastTag);
                        mapFragment.showFragment(mapStackFragment);

                        getSupportActionBar().setTitle("");
                        Log.d("map lastFragTag: " + lastTag, mapfragTagStack.size() + ":" + mapfragStack.size());
                    }
                }
                Log.d("MapFragment", "map tab track back stack  from here");
            } else {
                drawer.openDrawer();
            }
        } else if (currentTab.equals("MeasureFragment")) {
            int visible = measureFragment.measureSearchLayout.getVisibility();
            if (visible == View.VISIBLE) {
                String lastTag;
                if (measurefragTagStack.size() > 0) {
                    lastTag = measurefragTagStack.pop();
                    Fragment measureStackFragment = measurefragStack.get(lastTag);
                    if (measureStackFragment instanceof MeasureFragment) {
                        measurefragStack.remove(lastTag);
                        measureFragment.measureSearchLayout.setVisibility(View.GONE);
                        toggle.syncState();
                        measureFragment.isSearchOpened = 0;
                        measureFragment.showSearchIcon();
                        getSupportActionBar().setTitle("");
                        return;
                    } else if (measureStackFragment instanceof SearchListFragment) {
                        measurefragStack.remove(lastTag);
                        ((SearchListFragment) measureStackFragment).searchData = measureFragment.searchText;
                        measureFragment.replaceFragment(R.id.measureSearchLayout, measureStackFragment);
                        toggle.syncState();
                    } else {

                        measurefragStack.remove(lastTag);
                        measureFragment.showFragment(measureStackFragment);
                        Log.d("measure lastFragTag: " + lastTag, measurefragTagStack.size() + ":" + measurefragStack.size());
                    }
                }
                Log.d("MeasureFragment", "measure tab track back stack  from here");
            } else {
                drawer.openDrawer();
            }
        } else if (currentTab.equals("ActivityTrackerFragment")) {
            getSupportActionBar().show();
            int visible = activityTrackerFragment.trackerSearchLayout.getVisibility();
            if (visible == View.VISIBLE) {
                String lastTag;
                if (trackerfragTagStack.size() > 0) {
                    lastTag = trackerfragTagStack.pop();
                    Fragment trackerStackFragment = trackerfragStack.get(lastTag);
                    if (trackerStackFragment instanceof ActivityTrackerFragment) {
                        trackerfragStack.remove(lastTag);
                        activityTrackerFragment.trackerSearchLayout.setVisibility(View.GONE);
                        toggle.syncState();
                        activityTrackerFragment.isSearchOpened = 0;
                        activityTrackerFragment.showSearchIcon();
                        getSupportActionBar().setTitle("");
                        return;
                    } else if (trackerStackFragment instanceof ActivityLogFragment) {
                        trackerfragStack.remove(lastTag);
                        activityTrackerFragment.replaceFragment(R.id.trackerSearchLayout, trackerStackFragment);
                        toggle.syncState();
                    } else if (trackerStackFragment instanceof SearchListFragment) {
                        trackerfragStack.remove(lastTag);
                        ((SearchListFragment) trackerStackFragment).searchData = activityTrackerFragment.searchText;

                        activityTrackerFragment.replaceFragment(R.id.trackerSearchLayout, trackerStackFragment);
                        toggle.syncState();
                    } else {
                        trackerfragStack.remove(lastTag);
                        activityTrackerFragment.showFragment(trackerStackFragment);
                        Log.d("tracker lastFragTag: " + lastTag, trackerfragTagStack.size() + ":" + trackerfragStack.size());
                    }
                }
                Log.d("activityTrackerFragment", "activity tracker tab track back stack from here");
            } else {
                drawer.openDrawer();
            }
        } else {
            drawer.openDrawer();
        }
    }

    private void setBottomBar() {
        bottomBar.setBackgroundResource(R.color.pur_white);
        bottomBar.setOnTabReselectListener(new OnTabReselectListener() {
            @Override
            public void onTabReSelected(@IdRes int tabId) {
                if (tabId != R.id.tab_achievements) {
                    removeAllViews();
                }
            }
        });
        bottomBar.setOnTabSelectListener(new OnTabSelectListener() {
            @Override
            public void onTabSelected(@IdRes int tabId) {
                MapFragment mapFragment = getMapFragment();
                MeasureFragment measureFragment = getMeasureFragment();
                ActivityTrackerFragment activityTrackerFragment = getActivityTrackerFragment();

                toggle.syncState();

                if (tabId == R.id.tab_map) {
                    if (mapFragment == null || measureFragment == null)
                        return;

                    if (callMeasure) {
                        LatLng center = measureFragment.getCenter();
                        float zoom = measureFragment.getZoom();
                        mapFragment.goInMap(center, zoom);
                        callMeasure = false;
                    }

                    getSupportActionBar().show();
                    homeTabsViewPager.setCurrentItem(HomeViewPagerAdapter.MAP_FRAGMENT_INDEX, false);

                    mapFragment.showSearchIcon();
                    currentTab = "MapFragment";

                    if (startMarker != null)
                        startMarker.remove();

                    if (endMarker != null)
                        endMarker.remove();

                    if (measureFragment.getStartMarkerPoint() != null) {
                        if (mapFragment.myMap != null && measureFragment.deleteMeasure) {
                            measureFragment.deleteMeasure = false;
                        }
                        startMarker = mapFragment.myMap.addMarker(new MarkerOptions()
                                .position(measureFragment.getStartMarkerPoint())
                                .icon(BitmapDescriptorFactory.fromResource(R.drawable.purple_marker)));
                        mapFragment.myMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
                            @Override
                            public boolean onMarkerClick(Marker marker) {
                                return true;
                            }
                        });

                        if (measureFragment.getEndMarkerPoint() != null) {
                            endMarker = mapFragment.myMap.addMarker(new MarkerOptions()
                                    .position(measureFragment.getEndMarkerPoint())
                                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.purple_marker)));
                            mapFragment.myMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
                                @Override
                                public boolean onMarkerClick(Marker marker) {
                                    return true;
                                }
                            });
                        }

                    } else {

                        if (mapFragment.myMap != null && measureFragment.mapChanged) {
                            measureFragment.mapChanged = false;
                        }
                    }
                    mapFragment.showSearchIcon();
                } else if (tabId == R.id.tab_measure) {
                    if (mapFragment == null || measureFragment == null)
                        return;

                    LatLng center = mapFragment.getCenter();
                    float zoom = mapFragment.getZoom();
                    measureFragment.setZoom(zoom);
                    measureFragment.setCenter(center);

                    if (measureFragment.myMap != null) {
                        measureFragment.goInMap(center, zoom);
                    }

                    getSupportActionBar().show();
                    homeTabsViewPager.setCurrentItem(HomeViewPagerAdapter.MEASURE_FRAGMENT_INDEX, false);

                    measureFragment.showSearchIcon();
                    callMeasure = false;
                    measureFragment.deleteMeasure = false;

                    if (mapFragment.mapChanged) {

                        if (measureFragment.myMap != null) {
                            mapFragment.mapChanged = false;
                        }
                    }

                    currentTab = "MeasureFragment";

                    callMeasure = true;

                    if (mapFragment.getLastObjectIdMeasureTool() != 0 && mapFragment.getLastObjectIdMeasureTool() != mapFragment.getSelectedSegmentId()) {
                        mapFragment.setLastObjectIdMeasureTool(0);
                    }

                    if (measureFragment.isSearchOpened == 1) {
                        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
                        getSupportActionBar().setDisplayShowHomeEnabled(true);
                        getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_arrow_back);
                        measureFragment.expandSearchView();
                    } else {
                        measureFragment.showSearchIcon();
                        getSupportActionBar().setTitle("");
                    }
                    measureFragment.showSearchIcon();
                } else if (tabId == R.id.tab_tracker) {
                    currentTab = "ActivityTrackerFragment";
                    homeTabsViewPager.setCurrentItem(HomeViewPagerAdapter.TRACKER_FRAGMENT_INDEX, false);

                    if (activityTrackerFragment != null && activityTrackerFragment.isSearchOpened == 1) {
                        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
                        getSupportActionBar().setDisplayShowHomeEnabled(true);
                        getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_arrow_back);
                    } else {

                        if (activityTrackerFragment != null)
                            activityTrackerFragment.showSearchIcon();
                        getSupportActionBar().setTitle("");
                    }
                } else if (tabId == R.id.tab_achievements) {
                    getSupportActionBar().show();
                    homeTabsViewPager.setCurrentItem(HomeViewPagerAdapter.ACHIEVMENETS_FRAGMENT_INDEX, false);
                    getSupportActionBar().setTitle(null);
                    currentTab = "AchievementsFragment";
                }
            }
        });
    }

    public MapFragment getMapFragment() {
        MapFragment mapFragment = null;
        Fragment fragment = getFragmentByPosition(HomeViewPagerAdapter.MAP_FRAGMENT_INDEX);

        if (fragment instanceof MapFragment)
            mapFragment = (MapFragment) fragment;
        return mapFragment;
    }

    public MeasureFragment getMeasureFragment() {
        MeasureFragment measureFragment = null;
        Fragment fragment = getFragmentByPosition(HomeViewPagerAdapter.MEASURE_FRAGMENT_INDEX);

        if (fragment instanceof MeasureFragment)
            measureFragment = (MeasureFragment) fragment;
        return measureFragment;
    }

    public ActivityTrackerFragment getActivityTrackerFragment() {
        ActivityTrackerFragment activityTrackerFragment = null;
        Fragment fragment = getFragmentByPosition(HomeViewPagerAdapter.TRACKER_FRAGMENT_INDEX);

        if (fragment instanceof ActivityTrackerFragment)
            activityTrackerFragment = (ActivityTrackerFragment) fragment;
        return activityTrackerFragment;
    }

    private Fragment getFragmentByPosition(int position) {
        return getSupportFragmentManager().findFragmentByTag(
                "android:switcher:" + homeTabsViewPager.getId() + ":"
                        + homeTabsAdapter.getItemId(position));
    }

    @Override
    public void onClickGetStarted() {
        bottomBar.selectTabWithId(R.id.tab_tracker);
        homeTabsViewPager.setCurrentItem(HomeViewPagerAdapter.TRACKER_FRAGMENT_INDEX, false);
    }

    private ViewPager.OnPageChangeListener onPageChangeListner() {
        return new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                // No-op
            }

            @Override
            public void onPageSelected(int position) {
                bottomBar.selectTabAtPosition(position);
            }

            @Override
            public void onPageScrollStateChanged(int state) {
                // No-op
            }
        };
    }
}
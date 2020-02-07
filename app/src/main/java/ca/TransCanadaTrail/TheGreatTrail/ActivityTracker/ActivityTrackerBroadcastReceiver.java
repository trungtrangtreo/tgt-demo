package ca.TransCanadaTrail.TheGreatTrail.ActivityTracker;

import android.app.ActivityManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.google.android.gms.location.LocationResult;


/**
 * Created by Dev1 on 10/4/2016.
 */


public class ActivityTrackerBroadcastReceiver extends BroadcastReceiver {

    private SharedPreferences mPrefs;
    public static final String TAG = "LLoggerServiceManager";

    // private String TAG = "ActivityTrackerBroadcastReceiver" ; //this.getClass().getSimpleName();
    private LocationResult mLocationResult;
    Context ctx;


    @Override
    public void onReceive(Context contextt, Intent intent) {
        ctx=contextt;
        SharedPreferences preferences;
        preferences = PreferenceManager.getDefaultSharedPreferences(contextt);
        Boolean writeOnBD = preferences.getBoolean("WriteOnBD", false);
        String state =  preferences.getString("State", "Stop");

        if (writeOnBD && !state.equals("Stop") && ! isMyServiceRunning(TrackService.class)){
            Intent intentService = new Intent(contextt, TrackService.class);
            contextt.startService(intentService);
        }

    }

    private boolean isMyServiceRunning(Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) ctx.getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }
}

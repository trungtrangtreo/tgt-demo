package ca.TransCanadaTrail.TheGreatTrail.utils;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Created by Islam Salah on 7/26/17.
 */

public class ApplicationData {

    private static final String fileName = "application-shared-preferences-file-name";
    private static ApplicationData sInstance;
    private static SharedPreferences sharedPreferences;

    private static final String IS_FIRST_RUN_KEY = "is-first-run";
    private static final String IS_ON_TRAIL_BADGE_GAINED = "on-trail-badge";
    private static final String SHOWN_GET_STARTED_DIALOG = "show-get-started-dialog";

    private static final String DISTANCE_COVERED = "distance-covered";
    private static final String TIME_COVERED = "time-covered";
    private static final String ELEVATION_COVERED = "elevation-covered";

    private ApplicationData(Context context) {
        sharedPreferences = context.getSharedPreferences(fileName, Context.MODE_PRIVATE);
    }

    public static synchronized ApplicationData getInstance(Context context) {
        if (sInstance == null)
            sInstance = new ApplicationData(context);

        return sInstance;
    }

    private static void putString(String key, String value) {
        sharedPreferences.edit()
                .putString(key, value)
                .commit();
    }

    private static void putFloat(String key, float value) {
        sharedPreferences.edit()
                .putFloat(key, value)
                .commit();
    }

    private static void putBoolean(String key, boolean value) {
        sharedPreferences.edit()
                .putBoolean(key, value)
                .commit();
    }

    private static String getString(String key) {
        return sharedPreferences.getString(key, "");
    }

    private static float getFloat(String key) {
        return sharedPreferences.getFloat(key, 0f);
    }

    private static boolean getBoolean(String key) {
        return sharedPreferences.getBoolean(key, false);
    }

    public static boolean isFirstRun() {
        return !getBoolean(IS_FIRST_RUN_KEY);
    }

    public static void setFirstRun() {
        putBoolean(IS_FIRST_RUN_KEY, true);
    }


    public static boolean isOnTrailBadgeGained() {
        return getBoolean(IS_ON_TRAIL_BADGE_GAINED);
    }

    public static void setOnTrailBadgeGained() {
        putBoolean(IS_ON_TRAIL_BADGE_GAINED, true);
    }

    public static boolean showGetStartedDialog() {
        if (!getBoolean(SHOWN_GET_STARTED_DIALOG)) {
            putBoolean(SHOWN_GET_STARTED_DIALOG, true);
            return true;
        }
        return false;
    }

    public void addDistanceCovered(float distance) {
        float oldDistance = getDistanceCovered();
        putFloat(DISTANCE_COVERED, oldDistance + distance);
    }

    public void addElevation(float elevation) {
        float oldElevation = getElevationCovered();
        putFloat(ELEVATION_COVERED, oldElevation + elevation);
    }


    public void addTimeCovered(float time) {
        float oldTime = getTimeCovered();
        putFloat(TIME_COVERED, oldTime + time);
    }

    public float getDistanceCovered() {
        return getFloat(DISTANCE_COVERED);
    }

    public float getElevationCovered() {
        return getFloat(ELEVATION_COVERED);
    }

    public float getTimeCovered() {
        return getFloat(TIME_COVERED);
    }

}

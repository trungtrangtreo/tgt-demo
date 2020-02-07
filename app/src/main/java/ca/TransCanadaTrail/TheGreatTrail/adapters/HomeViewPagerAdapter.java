package ca.TransCanadaTrail.TheGreatTrail.adapters;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import ca.TransCanadaTrail.TheGreatTrail.ActivityTracker.ActivityTrackerFragment;
import ca.TransCanadaTrail.TheGreatTrail.MapView.MapFragment;
import ca.TransCanadaTrail.TheGreatTrail.MeasureTool.MeasureFragment;
import ca.TransCanadaTrail.TheGreatTrail.fragments.AchievementsFragment;

/**
 * Created by Islam Salah on 7/17/17.
 */

public class HomeViewPagerAdapter extends FragmentPagerAdapter {

    public static final int MAP_FRAGMENT_INDEX = 0;
    public static final int MEASURE_FRAGMENT_INDEX = 1;
    public static final int TRACKER_FRAGMENT_INDEX = 2;
    public static final int ACHIEVMENETS_FRAGMENT_INDEX = 3;

    public static final int FRAGMENTS_COUNT = 4;

    public HomeViewPagerAdapter(FragmentManager fm) {
        super(fm);
    }

    @Override
    public Fragment getItem(int position) {
        switch (position) {
            case MAP_FRAGMENT_INDEX:
                return MapFragment.newInstance();
            case MEASURE_FRAGMENT_INDEX:
                return MeasureFragment.newInstance();
            case TRACKER_FRAGMENT_INDEX:
                return ActivityTrackerFragment.newInstance();
            case ACHIEVMENETS_FRAGMENT_INDEX:
                return AchievementsFragment.newInstance();
            default:
                return null;
        }
    }

    @Override
    public int getCount() {
        return FRAGMENTS_COUNT;
    }
}

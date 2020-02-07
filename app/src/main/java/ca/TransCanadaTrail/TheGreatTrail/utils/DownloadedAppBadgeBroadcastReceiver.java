package ca.TransCanadaTrail.TheGreatTrail.utils;


import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import java.util.ArrayList;

import ca.TransCanadaTrail.TheGreatTrail.models.Achievement;
import ca.TransCanadaTrail.TheGreatTrail.realmdoas.AchievementsDao;

/**
 * Created by tarekAshraf on 7/31/17.
 */

public class DownloadedAppBadgeBroadcastReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        ArrayList<Achievement> achievements = AchievementsDao.getInstance().findWithId(context, Achievement.ID_FIELD_NAME, Achievement.DOWNLOAD_BADGE_ID);

        if (achievements.size() > 0)
            NotificationsManager.showNotification(context, achievements.get(0));
    }
}
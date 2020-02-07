package ca.TransCanadaTrail.TheGreatTrail.controllers;

import android.content.Context;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import ca.TransCanadaTrail.TheGreatTrail.models.Achievement;
import ca.TransCanadaTrail.TheGreatTrail.models.AchievementFilter;
import ca.TransCanadaTrail.TheGreatTrail.models.UserTrackingInfo;
import ca.TransCanadaTrail.TheGreatTrail.realmdoas.AchievementsDao;
import ca.TransCanadaTrail.TheGreatTrail.utils.NotificationsManager;

/**
 * Created by tarekAshraf on 7/31/17.
 */

public class AchievementsManager {

    public static final int TRAIL_DISTANCE_THRESHOLD = 100;

    private List<Achievement> achievements;

    private NotificationsManager notificationsManager;
    private TrackingManager trackingManager;

    private static AchievementsManager sInstance;
    private AchievementsDao achievementsDao;

    public static AchievementsManager getInstance() {

        if (sInstance == null)
            sInstance = new AchievementsManager();
        return sInstance;
    }

    private AchievementsManager() {
        achievementsDao = AchievementsDao.getInstance();
        trackingManager = TrackingManager.getInstance();
        notificationsManager = NotificationsManager.getInstance();
    }

    public void checkUnlockOfAchievements(Context context) {
        loadAllAchievements(context);

        if (achievements == null)
            return;
        UserTrackingInfo currentUserTrackingInfo = trackingManager.getCurrentUserTrackingInfo();

        for (Achievement achievement : achievements) {
            isAchievementFulfilled(context, currentUserTrackingInfo, achievement);
        }
    }

    private void isAchievementFulfilled(Context context, UserTrackingInfo currentUserTrackingInfo, Achievement achievement) {

        if (achievement.isUnlocked())
            return;
        AchievementFilter achievementFilter = achievement.getAchievementFilter();
        boolean filterPassed = achievementFilter.isFilterPassed(currentUserTrackingInfo);

        if (filterPassed) {
            unlockAchievement(context, achievement);
            displayUnlockNotification(context, achievement);
        }
    }

    public void checkCelebrateAchievement(Context context) {
        ArrayList<Achievement> achievements = AchievementsDao.getInstance().findWithId(context, Achievement.ID_FIELD_NAME, Achievement.CELEBRATE_BADGE_ID);

        if (achievements == null || achievements.isEmpty())
            return;
        Achievement celebrateAchievement = achievements.get(0);
        AchievementFilter achievementFilter = celebrateAchievement.getAchievementFilter();

        if(celebrateAchievement.isUnlocked())
            return;
        Date now = Calendar.getInstance().getTime();
        boolean celebrateAchievementAchieved = achievementFilter.achievedDateBadge(now);

        if (!celebrateAchievementAchieved)
            return;
        unlockAchievement(context, celebrateAchievement);
        displayUnlockNotification(context, celebrateAchievement);
    }


    private void loadAllAchievements(Context context) {
        // Always fetch it before checking to get latest state of the badge
        achievements = achievementsDao.findAll(context);
    }

    private void unlockAchievement(Context context, Achievement achievement) {
        achievementsDao.updateUnlockedField(context, achievement.getId(), true);
    }

    private void displayUnlockNotification(Context context, Achievement achievement) {
        notificationsManager.showNotification(context, achievement);
    }
}

package ca.TransCanadaTrail.TheGreatTrail.services;

import android.app.IntentService;
import android.content.Intent;

import ca.TransCanadaTrail.TheGreatTrail.controllers.AchievementsManager;

/**
 * Created by Ayman Mahgoub on 8/8/17.
 */

public class AchievementsGrantingService extends IntentService {

    public AchievementsGrantingService() {
        super(AchievementsGrantingService.class.getName());
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        AchievementsManager achievementsManager = AchievementsManager.getInstance();
        achievementsManager.checkUnlockOfAchievements(this);
    }
}

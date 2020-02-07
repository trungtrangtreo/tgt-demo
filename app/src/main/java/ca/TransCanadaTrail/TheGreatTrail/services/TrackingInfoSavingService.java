package ca.TransCanadaTrail.TheGreatTrail.services;

import android.app.IntentService;
import android.content.Intent;

import ca.TransCanadaTrail.TheGreatTrail.controllers.TrackingManager;

/**
 * Created by Ayman Mahgoub on 8/8/17.
 */

public class TrackingInfoSavingService extends IntentService {

    public TrackingInfoSavingService() {
        super(TrackingInfoSavingService.class.getName());
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        TrackingManager trackingManager = TrackingManager.getInstance();
        trackingManager.persistTrackingInfo(this);
    }
}

package ca.TransCanadaTrail.TheGreatTrail.utils;

import android.app.Notification;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;

import ca.TransCanadaTrail.TheGreatTrail.MainActivity;
import ca.TransCanadaTrail.TheGreatTrail.R;
import ca.TransCanadaTrail.TheGreatTrail.activities.AchievementDetailsActivity;
import ca.TransCanadaTrail.TheGreatTrail.models.Achievement;

/**
 * Created by tarekAshraf on 7/27/17.
 */

public class NotificationsManager {

    public static final int REQUEST_CODE_BASE = 10000;
    public static final int NOTIFICATION_ID_BASE = 1000;

    private static NotificationsManager sInstance;

    public static NotificationsManager getInstance() {
        if (sInstance == null)
            sInstance = new NotificationsManager();

        return sInstance;
    }

    public static void showNotification(Context context, Achievement achievement) {
        Notification notification = createNotification(context, achievement.getId(), context.getString(R.string.badge_notification_title), achievement.getAchievementDescription(context));
        NotificationManagerCompat.from(context).notify(NOTIFICATION_ID_BASE + achievement.getId(), notification);
    }

    private static Notification createNotification(Context context, int notificationId, String notificationTitle, String notificationMsg) {
        Intent intent = new Intent(context, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        intent.putExtra(AchievementDetailsActivity.EXTRA_ACHIEVEMENT_ID, notificationId);
        // Creates the PendingIntent
        PendingIntent notifyPendingIntent =
                PendingIntent.getActivity(
                        context,
                        REQUEST_CODE_BASE + notificationId,
                        intent,
                        PendingIntent.FLAG_UPDATE_CURRENT
                );

        NotificationCompat.InboxStyle inboxStyle = getInboxStyle(notificationMsg);
        return new NotificationCompat.Builder(context)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle(notificationTitle)
                .setContentText(notificationMsg)
                .setContentIntent(notifyPendingIntent)
                .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
                .setDefaults(Notification.DEFAULT_ALL)
                .setStyle(new NotificationCompat.BigTextStyle()
                        .bigText(notificationMsg))
                .setPriority(Notification.PRIORITY_HIGH)
                .setAutoCancel(true)
                .build();
    }

    private static NotificationCompat.InboxStyle getInboxStyle(String note) {
        NotificationCompat.InboxStyle inboxStyle = new NotificationCompat.InboxStyle();

        String[] notes = note.split("\n");
        for (String singleNote : notes) {
            inboxStyle.addLine(singleNote);
        }

        return inboxStyle;
    }

    private static PendingIntent defaultAction(Context context) {
        return null;
    }
}

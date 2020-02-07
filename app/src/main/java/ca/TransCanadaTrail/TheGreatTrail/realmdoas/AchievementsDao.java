package ca.TransCanadaTrail.TheGreatTrail.realmdoas;

import android.content.Context;

import java.util.Date;

import ca.TransCanadaTrail.TheGreatTrail.models.Achievement;
import io.realm.Realm;
import io.realm.RealmQuery;


/**
 * Created by Ayman Mahgoub on 3/10/17.
 */

public class AchievementsDao extends GenericDao<Achievement> {

    private static AchievementsDao achievementsDao;

    private AchievementsDao() {
        super();
    }

    public static AchievementsDao getInstance() {

        if (achievementsDao == null)
            achievementsDao = new AchievementsDao();
        return achievementsDao;
    }

    public void updateUnlockedField(final Context context, final int achievementId, final boolean isUnlocked) {
        Realm realm = getRealmInstance(context);

        realm.executeTransactionAsync(new Realm.Transaction() {
            @Override
            public void execute(Realm bgRealm) {
                RealmQuery<Achievement> query = bgRealm.where(Achievement.class).equalTo(Achievement.ID_FIELD_NAME, achievementId);
                final Achievement achievement = query.findFirst();

                if (achievement != null) {
                    achievement.setUnlocked(isUnlocked);
                    achievement.setUnlockDate(new Date());
                }
            }
        });
    }

    public void updateSeenField(final Context context, final int achievementId, final boolean isSeen) {
        Realm realm = getRealmInstance(context);

        realm.executeTransactionAsync(new Realm.Transaction() {
            @Override
            public void execute(Realm bgRealm) {
                RealmQuery<Achievement> query = bgRealm.where(Achievement.class).equalTo(Achievement.ID_FIELD_NAME, achievementId);
                final Achievement achievement = query.findFirst();

                if (achievement != null) {
                    achievement.setSeenAchievement(isSeen);
                }
            }
        });
    }
}

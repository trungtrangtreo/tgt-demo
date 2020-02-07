package ca.TransCanadaTrail.TheGreatTrail.models;

import android.content.Context;
import android.graphics.drawable.Drawable;

import java.util.Date;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

/**
 * Created by Islam Salah on 7/12/17.
 */

public class Achievement extends RealmObject {

    public static final int DOWNLOAD_BADGE_ID = 1;
    public static final int CELEBRATE_BADGE_ID = 3;

    public static final String ID_FIELD_NAME = "id";

    @PrimaryKey
    private int id;
    private String title;
    private String description;
    private String imageNameInactive;
    private String imageNameActive;
    private Date unlockDate;
    private boolean isUnlocked;
    private boolean seenAchievement;

    private AchievementFilter achievementFilter;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public String getImageUrlInactive() {
        return imageNameInactive;
    }

    public AchievementFilter getAchievementFilter() {
        return achievementFilter;
    }

    public String getImageUrlActive() {
        return imageNameActive;
    }

    public boolean isUnlocked() {
        return isUnlocked;
    }

    public Date getUnlockDate() {
        return unlockDate;
    }

    public boolean isSeenAchievement() {
        return seenAchievement;
    }

    public void setImageUrlInactive(String mImageUrlInactive) {
        this.imageNameInactive = mImageUrlInactive;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setImageUrlActive(String mImageUrl) {
        this.imageNameActive = mImageUrl;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setAchievementFilter(AchievementFilter achievementFilter) {
        this.achievementFilter = achievementFilter;
    }

    public void setUnlockDate(Date unlockDate) {
        this.unlockDate = unlockDate;
    }

    public void setUnlocked(boolean unlocked) {
        isUnlocked = unlocked;
    }

    public void setSeenAchievement(boolean seenAchievement) {
        this.seenAchievement = seenAchievement;
    }

    public Drawable getAchievementImage(Context context) {
        if (isUnlocked())
            return context.getDrawable(getDrawableIdFromName(context, getImageUrlActive()));
        else
            return context.getDrawable(getDrawableIdFromName(context, getImageUrlInactive()));
    }
    
    public int getDrawableIdFromName(Context context, String name) {
        return context.getResources().getIdentifier(name, "drawable", context.getPackageName());
    }

    public String getAchievementDescription(Context context) {
        return context.getString(context.getResources().getIdentifier(getDescription(), "string", context.getPackageName()));
    }

    public String getAchievementTitle(Context context) {
        return context.getString(context.getResources().getIdentifier(getTitle(), "string", context.getPackageName()));
    }
}

package ca.TransCanadaTrail.TheGreatTrail.models;

import android.support.annotation.Nullable;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import io.realm.RealmList;
import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

/**
 * Created by tarekAshraf on 8/2/17.
 */

public class AchievementFilter extends RealmObject {

    @PrimaryKey
    private int id;

    private int distance;
    private int elevation;
    private int time;
    private String date;
    private int numberOfProvinces;
    private int numberOfVisits;
    private int period;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getDistance() {
        return distance;
    }

    public void setDistance(int distance) {
        this.distance = distance;
    }

    public int getElevation() {
        return elevation;
    }

    public void setElevation(int elevation) {
        this.elevation = elevation;
    }

    public int getTime() {
        return time;
    }

    public void setTime(int time) {
        this.time = time;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public int getNumberOfProvinces() {
        return numberOfProvinces;
    }

    public void setNumberOfProvinces(int numberOfProvinces) {
        this.numberOfProvinces = numberOfProvinces;
    }

    public int getNumberOfVisits() {
        return numberOfVisits;
    }

    public void setNumberOfVisits(int numberOfVisits) {
        this.numberOfVisits = numberOfVisits;
    }

    public int getPeriod() {
        return period;
    }

    public void setPeriod(int period) {
        this.period = period;
    }

    public boolean isFilterPassed(UserTrackingInfo userTrackingInfo) {

        if (userTrackingInfo == null)
            return false;
        float totalAchievedElevation = userTrackingInfo.getTotalAchievedElevation();
        float totalCoveredDistance = userTrackingInfo.getTotalCoveredDistance();
        float totalTrackedTime = userTrackingInfo.getTotalTrackedTime();
        RealmList<Province> provinces = userTrackingInfo.getVisitedProvinces();

        Date now = Calendar.getInstance().getTime();

        boolean achieved = false;

        achieved |= achievedDistanceBadge(totalCoveredDistance);
        achieved |= achievedElevationBadge(totalAchievedElevation);
        achieved |= achievedTimeBadge(totalTrackedTime);
        achieved |= achievedDateBadge(now);
        achieved |= achievedNumProvincesBadge(provinces);
        achieved |= achievedNumVisitsBadge(userTrackingInfo);

        return achieved;
    }

    @Nullable
    public boolean achievedDateBadge(Date now) {
        if (date != null) {
            // 26/8
            try {
                SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd/MM");
                Date unlockedAt = simpleDateFormat.parse(date);
                Date nowDate = simpleDateFormat.parse(simpleDateFormat.format(now));

                // Check if we need to normalize hours, minutes and seconds
                return nowDate.compareTo(unlockedAt) >= 0;
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }
        return false;
    }

    private boolean achievedNumVisitsBadge(UserTrackingInfo userTrackingInfo) {
        if (numberOfVisits != 0) {
            // It is a valid field in the filter
            return userTrackingInfo.visitTrailInPeriod(getPeriod()) || userTrackingInfo.visitedProvinceMoreThanOne();
        }
        return false;
    }

    private boolean achievedNumProvincesBadge(RealmList<Province> provinces) {
        if (numberOfProvinces != 0) {
            // It is a valid field in the filter
            int visitedProvincesNumber = provinces != null ? provinces.size() : 0;

            if (visitedProvincesNumber >= numberOfProvinces)
                return true;
        }
        return false;
    }

    private boolean achievedTimeBadge(float totalTrackedTime) {
        if (time != 0) {
            // It is a valid field in the filter

            if (totalTrackedTime >= time)
                return true;
        }
        return false;
    }

    private boolean achievedElevationBadge(float totalAchievedElevation) {
        if (elevation != 0) {
            // It is a valid field in the filter

            if (totalAchievedElevation >= elevation)
                return true;
        }
        return false;
    }

    private boolean achievedDistanceBadge(float totalCoveredDistance) {
        if (distance != 0) {
            // It is a valid field in the filter

            if (totalCoveredDistance >= distance)
                return true;
        }
        return false;
    }
}

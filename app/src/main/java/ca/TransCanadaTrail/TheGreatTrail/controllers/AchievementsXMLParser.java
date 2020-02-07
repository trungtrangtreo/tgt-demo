package ca.TransCanadaTrail.TheGreatTrail.controllers;

import android.content.Context;
import android.support.annotation.NonNull;
import android.text.TextUtils;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.io.InputStream;
import java.util.ArrayList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import ca.TransCanadaTrail.TheGreatTrail.models.Achievement;
import ca.TransCanadaTrail.TheGreatTrail.models.AchievementFilter;

/**
 * Created by tarekAshraf on 8/2/17.
 */

public class AchievementsXMLParser {

    public static final String ACHIEVEMENT_FILTER_ID_TAG = "id";
    public static final String ACHIEVEMENT_FILTER_DISTANCE_TAG = "distance";
    public static final String ACHIEVEMENT_FILTER_ELEVATION_TAG = "elevation";
    public static final String ACHIEVEMENT_FILTER_TIME_TAG = "time";
    public static final String ACHIEVEMENT_FILTER_DATE_TAG = "date";
    public static final String ACHIEVEMENT_FILTER_NUMBER_OF_PROVINCE_TAG = "number_of_provinces";
    public static final String ACHIEVEMENT_FILTER_NUMBER_OF_VISITS_TAG = "number_of_visits";
    public static final String ACHIEVEMENT_FILTER_PERIOD_TAG = "period";
    public static final String ACHIEVEMENT_FILTER_TAG = "achievement-filter";

    public static final String ID_TAG = "id";
    public static final String TITLE_TAG = "title";
    public static final String DESCRIPTION_TAG = "description";
    public static final String INACTIVE_IMAGE_TAG = "image-name-inactive";
    public static final String ACTIVE_IMAGE_TAG = "image-name-active";
    public static final String ACHIEVEMENT_TAG = "achievement";
    public static final String ACHIEVEMENTS_TAG = "achievements";

    public static final String ACHIEVEMENTS_FILE_NAME = "achievements.xml";

    public static ArrayList<Achievement> parseAchievements(Context context) throws Exception {
        InputStream raw = context.getApplicationContext().getAssets().open(ACHIEVEMENTS_FILE_NAME);
        DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
        Document doc = dBuilder.parse(raw);
        doc.getDocumentElement().normalize();

        NodeList nList = doc.getElementsByTagName(ACHIEVEMENT_TAG);

        ArrayList<Achievement> achievements = new ArrayList<>();
        for (int count = 0; count < nList.getLength(); count++) {
            Achievement achievement = new Achievement();
            Node nNode = nList.item(count);

            if (nNode.getNodeType() == Node.ELEMENT_NODE) {
                Element achievementElement = (Element) nNode;
                String id = achievementElement.getElementsByTagName(ID_TAG).item(0).getTextContent();

                setAchievementParameters(achievement, achievementElement, id);

                AchievementFilter achievementFilter = setAchievementFilterParameters(achievementElement);
                achievement.setAchievementFilter(achievementFilter);

                achievements.add(achievement);
            }
        }
        return achievements;
    }

    @NonNull
    private static AchievementFilter setAchievementFilterParameters(Element achievementElement) {
        AchievementFilter achievementFilter = new AchievementFilter();

        if (achievementElement.getElementsByTagName(ACHIEVEMENT_FILTER_TAG).item(0) != null) {
            Element badgeFilter = (Element) achievementElement.getElementsByTagName(ACHIEVEMENT_FILTER_TAG).item(0);

            setBadgeID(achievementFilter, badgeFilter);
            setBadgeDistance(achievementFilter, badgeFilter);
            setBadgeElevation(achievementFilter, badgeFilter);
            setBadgeTime(achievementFilter, badgeFilter);
            setBadgeDate(achievementFilter, badgeFilter);
            setBadgeNumProvinces(achievementFilter, badgeFilter);
            setBagdeNumVisits(achievementFilter, badgeFilter);
            setBadgePeriod(achievementFilter, badgeFilter);
        }
        return achievementFilter;
    }

    private static void setBadgePeriod(AchievementFilter achievementFilter, Element badgeFilter) {
        Node achievementFilterPeriodNode = badgeFilter.getElementsByTagName(ACHIEVEMENT_FILTER_PERIOD_TAG).item(0);

        if (achievementFilterPeriodNode != null)
            achievementFilter.setPeriod(Integer.parseInt(achievementFilterPeriodNode.getTextContent()));
    }

    private static void setBagdeNumVisits(AchievementFilter achievementFilter, Element badgeFilter) {
        Node achievementFilterNumberOfVisitsNode = badgeFilter.getElementsByTagName(ACHIEVEMENT_FILTER_NUMBER_OF_VISITS_TAG).item(0);
        if (achievementFilterNumberOfVisitsNode != null)
            achievementFilter.setNumberOfVisits(Integer.parseInt(achievementFilterNumberOfVisitsNode.getTextContent()));
    }

    private static void setBadgeNumProvinces(AchievementFilter achievementFilter, Element badgeFilter) {
        Node achievementFilterNumberOfProvincesNode = badgeFilter.getElementsByTagName(ACHIEVEMENT_FILTER_NUMBER_OF_PROVINCE_TAG).item(0);
        if (achievementFilterNumberOfProvincesNode != null)
            achievementFilter.setNumberOfProvinces(Integer.parseInt(achievementFilterNumberOfProvincesNode.getTextContent()));
    }

    private static void setBadgeDate(AchievementFilter achievementFilter, Element badgeFilter) {
        Node achievementFilterDateNode = badgeFilter.getElementsByTagName(ACHIEVEMENT_FILTER_DATE_TAG).item(0);
        if (achievementFilterDateNode != null)
            achievementFilter.setDate(achievementFilterDateNode.getTextContent());
    }

    private static void setBadgeTime(AchievementFilter achievementFilter, Element badgeFilter) {
        Node achievementFilterTimeNode = badgeFilter.getElementsByTagName(ACHIEVEMENT_FILTER_TIME_TAG).item(0);
        if (achievementFilterTimeNode != null)
            achievementFilter.setTime(Integer.parseInt(achievementFilterTimeNode.getTextContent()));
    }

    private static void setBadgeElevation(AchievementFilter achievementFilter, Element badgeFilter) {
        Node achievementFilterElevationNode = badgeFilter.getElementsByTagName(ACHIEVEMENT_FILTER_ELEVATION_TAG).item(0);
        if (achievementFilterElevationNode != null)
            achievementFilter.setElevation(Integer.parseInt(achievementFilterElevationNode.getTextContent()));
    }

    private static void setBadgeDistance(AchievementFilter achievementFilter, Element badgeFilter) {
        Node achievementFilterDistanceNode = badgeFilter.getElementsByTagName(ACHIEVEMENT_FILTER_DISTANCE_TAG).item(0);
        if (achievementFilterDistanceNode != null)
            achievementFilter.setDistance(Integer.parseInt(achievementFilterDistanceNode.getTextContent()));
    }

    private static void setBadgeID(AchievementFilter achievementFilter, Element badgeFilter) {
        Node achievementFilterIdNode = badgeFilter.getElementsByTagName(ACHIEVEMENT_FILTER_ID_TAG).item(0);
        if (achievementFilterIdNode != null)
            achievementFilter.setId(Integer.parseInt(achievementFilterIdNode.getTextContent()));
    }

    private static void setAchievementParameters(Achievement achievement, Element achievementElement, String id) {
        if (!TextUtils.isEmpty(id)) {
            achievement.setId(Integer.parseInt(id));
        }
        achievement.setTitle(achievementElement.getElementsByTagName(TITLE_TAG).item(0).getTextContent());
        achievement.setDescription(achievementElement.getElementsByTagName(DESCRIPTION_TAG).item(0).getTextContent());
        achievement.setImageUrlActive(achievementElement.getElementsByTagName(ACTIVE_IMAGE_TAG).item(0).getTextContent());
        achievement.setImageUrlInactive(achievementElement.getElementsByTagName(INACTIVE_IMAGE_TAG).item(0).getTextContent());
    }
}

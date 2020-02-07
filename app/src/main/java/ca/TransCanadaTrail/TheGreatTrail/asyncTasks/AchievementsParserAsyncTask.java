package ca.TransCanadaTrail.TheGreatTrail.asyncTasks;

import android.content.Context;
import android.os.AsyncTask;

import java.util.List;

import ca.TransCanadaTrail.TheGreatTrail.controllers.AchievementsXMLParser;
import ca.TransCanadaTrail.TheGreatTrail.models.Achievement;

/**
 * Created by tarekAshraf on 8/10/17.
 */

public class AchievementsParserAsyncTask extends AsyncTask<Void, Void, List<Achievement>> {

    private Context context;
    private AchievementsParserAsyncTaskIF achievementsParserAsyncTaskIF;

    public AchievementsParserAsyncTask(Context context, AchievementsParserAsyncTaskIF achievementsParserAsyncTaskIF) {
        this.context = context;
        this.achievementsParserAsyncTaskIF = achievementsParserAsyncTaskIF;
    }

    @Override
    protected List<Achievement> doInBackground(Void... params) {

        List<Achievement> achievements = null;
        try {
            achievements = AchievementsXMLParser.parseAchievements(context);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return achievements;
    }

    @Override
    protected void onPostExecute(List<Achievement> achievements) {
        super.onPostExecute(achievements);
        achievementsParserAsyncTaskIF.onParseFinished(achievements);
    }

    public interface AchievementsParserAsyncTaskIF {
        void onParseFinished(List<Achievement> achievements);
    }
}

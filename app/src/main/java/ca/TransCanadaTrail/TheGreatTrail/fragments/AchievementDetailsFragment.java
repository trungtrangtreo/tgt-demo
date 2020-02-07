package ca.TransCanadaTrail.TheGreatTrail.fragments;

import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.TransitionDrawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

import butterknife.BindString;
import butterknife.BindView;
import butterknife.ButterKnife;
import ca.TransCanadaTrail.TheGreatTrail.R;
import ca.TransCanadaTrail.TheGreatTrail.models.Achievement;
import ca.TransCanadaTrail.TheGreatTrail.realmdoas.AchievementsDao;

/**
 * Created by Islam Salah on 7/16/17.
 */

public class AchievementDetailsFragment extends Fragment {
    private static final String ARGUMENT_ACHIEVEMENT_ID = "args-achievement-id";

    @BindView(R.id.achievement_title_tv)
    TextView achievementTitleTV;

    @BindView(R.id.achievement_iv)
    ImageView achievementIV;

    @BindView(R.id.achievement_description_tv)
    TextView achievementDescriptionTV;

    @BindView(R.id.earned_date_tv)
    TextView earnedDateTV;

    @BindString(R.string.achievement_earned_date_format)
    String dateFormat;

    public static AchievementDetailsFragment newInstance(int id) {
        Bundle args = new Bundle();
        args.putInt(ARGUMENT_ACHIEVEMENT_ID, id);

        AchievementDetailsFragment fragment = new AchievementDetailsFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_achievement_details, container, false);
        ButterKnife.bind(this, view);
        setUIValues();

        return view;
    }

    private void setUIValues() {
        Bundle args = getArguments();
        int id = args.getInt(ARGUMENT_ACHIEVEMENT_ID);
        ArrayList<Achievement> achievements = AchievementsDao.getInstance().findWithId(getActivity(), Achievement.ID_FIELD_NAME, id);

        if (achievements == null || achievements.size() == 0)
            return;

        Achievement achievement = achievements.get(0);

        achievementTitleTV.setText(achievement.getAchievementTitle(getActivity()));
        achievementDescriptionTV.setText(achievement.getAchievementDescription(getActivity()));
        achievementIV.setImageDrawable(achievement.getAchievementImage(getActivity()));

        if (achievement.isUnlocked()) {
            earnedDateTV.setVisibility(View.VISIBLE);
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat(dateFormat, Locale.getDefault());
            Date unlockDate = achievement.getUnlockDate();

            if (unlockDate == null)
                return;
            String unlockDateFormatted = simpleDateFormat.format(unlockDate);
            earnedDateTV.setText(getString(R.string.achievement_earned_date, unlockDateFormatted));
        }

        if(!achievement.isUnlocked())
            return;

        if (!achievement.isSeenAchievement()) {
            setAnimation(achievement);
            AchievementsDao.getInstance().updateSeenField(getActivity(), achievement.getId(), true);
        }
    }

    private void setAnimation(Achievement achievement) {
        // If the bound view wasn't previously displayed on screen, it's animated
        Animation animation = AnimationUtils.loadAnimation(getActivity(), R.anim.rotate);
        Drawable[] layers = new Drawable[2];
        layers[0] = new BitmapDrawable(getActivity().getResources(), BitmapFactory.decodeResource(getActivity().getResources(), achievement.getDrawableIdFromName(getActivity(), achievement.getImageUrlInactive())));
        layers[1] = new BitmapDrawable(getActivity().getResources(), BitmapFactory.decodeResource(getActivity().getResources(), achievement.getDrawableIdFromName(getActivity(), achievement.getImageUrlActive())));

        TransitionDrawable transitionDrawable = new TransitionDrawable(layers);
        achievementIV.setImageDrawable(transitionDrawable);

        transitionDrawable.startTransition(1000);
        achievementIV.startAnimation(animation);

    }
}
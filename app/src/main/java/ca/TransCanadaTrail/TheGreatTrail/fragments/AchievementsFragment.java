package ca.TransCanadaTrail.TheGreatTrail.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import ca.TransCanadaTrail.TheGreatTrail.MainActivity;
import ca.TransCanadaTrail.TheGreatTrail.R;
import ca.TransCanadaTrail.TheGreatTrail.adapters.AchievementsAdapter;
import ca.TransCanadaTrail.TheGreatTrail.models.Achievement;
import ca.TransCanadaTrail.TheGreatTrail.realmdoas.AchievementsDao;
import ca.TransCanadaTrail.TheGreatTrail.utils.ApplicationData;

/**
 * Created by Islam Salah on 7/12/17.
 */

public class AchievementsFragment extends Fragment implements GetStartedDialogFragment.GetStartedDialogIF {

    private static final String TAG = "AchievementsFragment";
    private static final int ACHIEVEMENTS_PER_LINE_IN_VIEW = 3;

    @BindView(R.id.achievements_rv)
    RecyclerView achievementsRV;

    private ArrayList<Achievement> achievements;
    private GetStartedDialogIF getStartedDialogIF;
    private GetStartedDialogFragment getStartedDialogFragment;

    public static AchievementsFragment newInstance() {
        return new AchievementsFragment();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_achievements, container, false);
        ButterKnife.bind(this, view);
        getStartedDialogIF = (MainActivity) getActivity();

        loadUi();
        return view;
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        if (isVisibleToUser)
            setGetStartedDialog();
    }

    private void loadAchievements() {
        achievements = AchievementsDao.getInstance().findAll(getActivity());
    }

    public void loadUi() {
        loadAchievements();
        setAdapter();
    }

    private void setGetStartedDialog() {
        getStartedDialogFragment = new GetStartedDialogFragment();
        getStartedDialogFragment.setGetStartedDialogIF(this);
        ApplicationData applicationData = ApplicationData.getInstance(getActivity());
        if (applicationData.showGetStartedDialog()) {
            getStartedDialogFragment.show(getActivity().getSupportFragmentManager());
        }
    }

    private void setAdapter() {
        achievementsRV.setHasFixedSize(true);
        achievementsRV.setLayoutManager(new GridLayoutManager(getActivity(), ACHIEVEMENTS_PER_LINE_IN_VIEW));
        achievementsRV.setAdapter(new AchievementsAdapter(getActivity(), achievements));
    }

    @OnClick(R.id.achievement_title_info_iv)
    public void onClickInfoButton() {
        GetStartedDialogFragment getStartedDialogFragment = new GetStartedDialogFragment();
        getStartedDialogFragment.setGetStartedDialogIF(this);
        getStartedDialogFragment.show(getActivity().getSupportFragmentManager());
    }

    public void onClickGetStarted() {
        getStartedDialogIF.onClickGetStarted();
    }

    public interface GetStartedDialogIF {
        void onClickGetStarted();
    }
}

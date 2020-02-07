package ca.TransCanadaTrail.TheGreatTrail.OnBoarding;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import ca.TransCanadaTrail.TheGreatTrail.R;

/**
 * Created by hardikfumakiya on 2017-01-05.
 */

public class OnBoardingScreenThree extends Fragment {

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v= inflater.inflate(R.layout.onboardingscr3,container,false);

        return v;
    }
}

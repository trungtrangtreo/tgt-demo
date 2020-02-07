package ca.TransCanadaTrail.TheGreatTrail;

import android.content.Context;
import android.view.View;
import android.view.animation.AnimationUtils;

import ca.TransCanadaTrail.TheGreatTrail.R;

/**
 * Created by hardikfumakiya on 2016-12-31.
 */

public class MyUtils {

    public void SlideUP(View view, Context context)
    {
        view.startAnimation(AnimationUtils.loadAnimation(context, R.anim.slid_down));
    }

    public void SlideDown(View view,Context context)
    {
        view.startAnimation(AnimationUtils.loadAnimation(context, R.anim.slid_up));
    }


}
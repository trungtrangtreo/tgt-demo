package ca.TransCanadaTrail.TheGreatTrail.OnBoarding;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import ca.TransCanadaTrail.TheGreatTrail.CustomFontButton;
import ca.TransCanadaTrail.TheGreatTrail.R;
import ca.TransCanadaTrail.TheGreatTrail.SplashScreenActivity;
import me.relex.circleindicator.CircleIndicator;


/**
 * Created by hardikfumakiya on 2017-01-05.
 */

public class OnboardingActivity extends AppCompatActivity {

    ViewPager mViewPager;
    CircleIndicator tabLayout;
    public CustomFontButton skip_button;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.onboarding);
        mViewPager = (ViewPager) findViewById(R.id.pager);
        skip_button=(CustomFontButton)findViewById(R.id.skip_button);
        /** set the adapter for ViewPager */
        mViewPager.setAdapter(new SamplePagerAdapter(getSupportFragmentManager()));
        tabLayout=(CircleIndicator)findViewById(R.id.tabDots);
        tabLayout.setViewPager(mViewPager);
        skip_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {
                SharedPreferences preferences = getSharedPreferences("SearchPreferences", Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = preferences.edit();
                int isOpened= 1;
                editor.putInt("onboarding", isOpened);
                editor.apply();
                Intent i = new Intent(OnboardingActivity.this, SplashScreenActivity.class);
                startActivity(i);
                finish();
            }
        });
        mViewPager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                switch(position)
                {
                    case 4:
                        skip_button.setText(getResources().getString(R.string.get_started));
                        break;

                    default:
                        skip_button.setText(getResources().getString(R.string.skip_this));
                        break;
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
    }

    /** Defining a FragmentPagerAdapter class for controlling the fragments to be shown when user swipes on the screen. */
    public class SamplePagerAdapter extends FragmentPagerAdapter {

        public SamplePagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            /** Show a Fragment based on the position of the current screen */
            switch(position){
                case 0:
                    return new OnBoardingScreenOne();

                case 1:
                    return new OnBoardingScreenTwo();

                case 2:
                    return new OnBoardingScreenThree();

                case 3:
                    return new OnBoardingScreenFour();

                default:
                    return new OnBoardingScreenFive();

            }

        }

        @Override
        public int getCount() {

            return 5;
        }
    }
}

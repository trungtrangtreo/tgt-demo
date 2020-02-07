package ca.TransCanadaTrail.TheGreatTrail.MenuTool;

import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.text.Spanned;
import android.text.method.LinkMovementMethod;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.Locale;

import ca.TransCanadaTrail.TheGreatTrail.R;

public class AboutActivity extends AppCompatActivity {

    private TextView aboutTxt;
    private ImageView backBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_about);
        Toolbar toolbar = (Toolbar) findViewById(R.id.main_activity_tb);

        backBtn = (ImageView) findViewById(R.id.backBtn);
        backBtn.setImageResource(R.drawable.ic_arrow_back);
        backBtn.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                finish();
                return false;
            }
        });

        TextView linkTxt1 = (TextView) findViewById(R.id.link1);
        TextView linkTxt2 = (TextView) findViewById(R.id.link2);
        TextView linkTxt3 = (TextView) findViewById(R.id.link3);

        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);


        aboutTxt = (TextView) findViewById(R.id.aboutTxt);

        String url1 = "https://thegreattrail.ca";
        String url2 = "https://thegreattrail.ca/terms-conditions/";
        String url3 = "https://thegreattrail.ca/privacy-policy/";

        Spanned link1,link2,link3;

        if (Locale.getDefault().getLanguage().equals("fr")) {
            url1 = "https://thegreattrail.ca/fr/";
            url2 = "https://thegreattrail.ca/fr/terms-conditions/";
            url3 = "https://thegreattrail.ca/fr/privacy-policy/";
        }


        if (Build.VERSION.SDK_INT >= 24) {
            link1 = Html.fromHtml("<a href=\""+ url1 +"\">"+url1+"</a> <p></p>", Html.FROM_HTML_MODE_LEGACY); // for 24 api and more
        }
        else {
            link1 = (Html.fromHtml("<a href=\""+ url1 +"\">"+url1+"</a> <p></p>")); // or for older api
        }

        if (Build.VERSION.SDK_INT >= 24) {
            link2 = Html.fromHtml("<p>"+getResources().getString(R.string.terms_conditions)+"<br/><a href="+ url2 +">"+url2+"</a>"+"</p>", Html.FROM_HTML_MODE_LEGACY); // for 24 api and more
        }
        else {
            link2 = (Html.fromHtml("<p>"+getResources().getString(R.string.terms_conditions)+"<br/><a href="+ url2 +">"+url2+"</a>"+"</p>")); // or for older api
        }

        if (Build.VERSION.SDK_INT >= 24) {
            link3 = Html.fromHtml("<p>"+getResources().getString(R.string.privacy_policy)+"<br/><a href="+ url3 +">"+url3+"</a>"+"</p>", Html.FROM_HTML_MODE_LEGACY); // for 24 api and more
        }
        else {
            link3 = (Html.fromHtml("<p>"+getResources().getString(R.string.privacy_policy)+"<br/><a href="+ url3 +">"+url3+"</a>"+"</p>")); // or for older api
        }




        String aboutTxtEn = getResources().getString(R.string.about_text);
        aboutTxt.setText(aboutTxtEn);
        linkTxt1.setMovementMethod(LinkMovementMethod.getInstance());
        linkTxt1.setText(link1);

        linkTxt2.setMovementMethod(LinkMovementMethod.getInstance());
        linkTxt2.setText(link2);

        linkTxt3.setMovementMethod(LinkMovementMethod.getInstance());
        linkTxt3.setText(link3);

    }


}

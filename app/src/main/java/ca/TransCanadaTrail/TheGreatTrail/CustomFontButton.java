package ca.TransCanadaTrail.TheGreatTrail;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.widget.Button;

import ca.TransCanadaTrail.TheGreatTrail.R;

/**
 * Created by hardikfumakiya on 2016-07-14.
 */
public class CustomFontButton extends Button {
    public static final String ANDROID_SCHEMA = "http://schemas.android.com/apk/res/android";

    public CustomFontButton(Context context, AttributeSet attrs) {
        super(context, attrs);

        applyCustomFont(context, attrs);
    }

    public CustomFontButton(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);

        applyCustomFont(context, attrs);
    }

    private void applyCustomFont(Context context, AttributeSet attrs) {
        TypedArray attributeArray = context.obtainStyledAttributes(
                attrs,
                R.styleable.CustomFontTextView);

        String fontName = attributeArray.getString(R.styleable.CustomFontTextView_fontName);
        int textStyle = attrs.getAttributeIntValue(ANDROID_SCHEMA, "textStyle", Typeface.NORMAL);

        Typeface customFont = selectTypeface(context, fontName, textStyle);
        setTypeface(customFont);

        attributeArray.recycle();
    }

    private Typeface selectTypeface(Context context, String fontName, int textStyle) {
        if (fontName.contentEquals(context.getString(R.string.helveticaneue_bold))) {
          /*
          information about the TextView textStyle:
          http://developer.android.com/reference/android/R.styleable.html#TextView_textStyle
          */
            switch (textStyle) {
                case Typeface.BOLD: // bold
                    return FontCache.getTypeface(context,"HelveticaNeue-Bold.ttf");

                case Typeface.NORMAL: // regular
                default:
                    return FontCache.getTypeface(context,"HelveticaNeue.ttf");
            }
        } else {
            // no matching font found
            // return null so Android just uses the standard font (Roboto)
            return FontCache.getTypeface(context,"HelveticaNeue.ttf");
        }
    }
}
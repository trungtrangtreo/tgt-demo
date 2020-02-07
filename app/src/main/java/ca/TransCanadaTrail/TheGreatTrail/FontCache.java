package ca.TransCanadaTrail.TheGreatTrail;

import android.content.Context;
import android.graphics.Typeface;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by hardikfumakiya on 2016-07-14.
 */
public class FontCache {

    private static Map<String, Typeface> sCachedFonts = new HashMap<String, Typeface>();

    public static Typeface getTypeface(Context context, String assetPath) {
        if (!sCachedFonts.containsKey(assetPath)) {
            Typeface tf = Typeface.createFromAsset(context.getAssets(), assetPath);
            sCachedFonts.put(assetPath, tf);
        }

        return sCachedFonts.get(assetPath);
    }
}
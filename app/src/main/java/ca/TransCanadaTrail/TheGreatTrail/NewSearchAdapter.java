package ca.TransCanadaTrail.TheGreatTrail;

import android.content.Context;
import android.database.Cursor;
import android.support.v4.widget.SimpleCursorAdapter;
import android.view.View;
import android.view.ViewGroup;

/**
 * Created by hardikfumakiya on 2016-12-13.
 */
public class NewSearchAdapter extends SimpleCursorAdapter {


    public NewSearchAdapter(Context context, int layout, Cursor c, String[] from, int[] to, int flags) {
        super(context, layout, c, from, to, flags);
    }

    @Override

    public View newView(Context context, Cursor cursor, ViewGroup parent) {

        return null;
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {

    }
}

package ca.TransCanadaTrail.TheGreatTrail;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ListView;

/**
 * Created by hardikfumakiya on 2017-02-26.
 */

public class SingleScrollableHeaderListView extends ListView {
    private View mHeaderView;

    public SingleScrollableHeaderListView(Context context) {
        super(context);
    }

    public SingleScrollableHeaderListView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public SingleScrollableHeaderListView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        if (mHeaderView != null) return mHeaderView.onTouchEvent(ev);
        return super.onInterceptTouchEvent(ev);
    }

    @Override
    public void addHeaderView(View v, Object data, boolean isSelectable) {
        if (mHeaderView != null) removeHeaderView(mHeaderView);
        mHeaderView = v;
        super.addHeaderView(v, data, isSelectable);
    }
}
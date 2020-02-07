package ca.TransCanadaTrail.TheGreatTrail.item;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.ArrayList;

import ca.TransCanadaTrail.TheGreatTrail.R;

/**
 * Created by houari on 16/01/2017.
 */


public class OfflineItemAdapter extends ArrayAdapter<OfflineItem> {
    protected LayoutInflater inflater;
    protected int layout;
    private  ArrayList<OfflineItem> offlineItems;

    public OfflineItemAdapter(Activity activity, int resourceId, ArrayList<OfflineItem> offlineItems){
        super(activity, resourceId, offlineItems);
        layout = resourceId;
        this.offlineItems = offlineItems;
        inflater = (LayoutInflater)activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }




    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        final OfflineItem i = offlineItems.get(position);

        View v = inflater.inflate(layout, parent, false);

        OfflineItem oi = (OfflineItem)i;
        v = inflater.inflate(R.layout.list_item_offline_trails, null);
        final TextView title = (TextView)v.findViewById(R.id.item_label);
        final TextView date = (TextView)v.findViewById(R.id.item_date);

        if (title != null)
            title.setText(oi.getName());
        if(date != null)
            date.setText(oi.getDate());



        return v;
    }
}

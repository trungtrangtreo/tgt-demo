package ca.TransCanadaTrail.TheGreatTrail;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Switch;
import android.widget.TextView;

import java.util.ArrayList;

import ca.TransCanadaTrail.TheGreatTrail.R;

/**
 * Created by Dev1 on 12/22/2016.
 */

public class ListAdapter extends ArrayAdapter<String> {
    private ArrayList<String> data;
    private Context context;

    public ListAdapter(Context context, ArrayList<String> dataItem) {
        super(context, R.layout.settings_list_item, dataItem);
        this.data = dataItem;
        this.context = context;
    }



    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder;
        if (convertView == null) {
            LayoutInflater inflater = LayoutInflater.from(context);
            convertView = inflater.inflate(R.layout.settings_list_item, null);
            viewHolder = new ViewHolder();
            viewHolder.text = (TextView) convertView
                    .findViewById(R.id.settingsTxt);
            viewHolder.aswitch = (Switch) convertView
                    .findViewById(R.id.settingsSwitch);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }
        final String temp = getItem(position);
        viewHolder.text.setText(temp);
        viewHolder.aswitch.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                if (customListner != null) {
                    customListner.onButtonClickListner(position,temp);
                }

            }
        });

        return convertView;
    }



    customButtonListener customListner;

    public interface customButtonListener {
        public void onButtonClickListner(int position, String value);
    }

    public void setCustomButtonListner(customButtonListener listener) {
        this.customListner = listener;
    }

    public class ViewHolder {
        TextView text;
        Switch  aswitch;
    }
}


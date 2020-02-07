package ca.TransCanadaTrail.TheGreatTrail.MenuTool;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Switch;
import android.widget.TextView;

import java.util.ArrayList;

import ca.TransCanadaTrail.TheGreatTrail.BuildConfig;
import ca.TransCanadaTrail.TheGreatTrail.R;

/**
 * Created by Dev1 on 12/22/2016.
 */

public class ListAdapter extends ArrayAdapter<String> {
    private ArrayList<String> data;
    private Context context;
    private boolean restrictToWifi;
    private boolean icludePhoto;

    public ListAdapter(Context context, ArrayList<String> dataItem, boolean restrictToWifi, boolean icludePhoto) {
        super(context, R.layout.settings_list_item, dataItem);
        this.data = dataItem;
        this.context = context;
        this.restrictToWifi = restrictToWifi;
        this.icludePhoto = icludePhoto;
    }



    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder;
        if (convertView == null) {
            LayoutInflater inflater = LayoutInflater.from(context);
            convertView = inflater.inflate(R.layout.settings_list_item, null);
            viewHolder = new ViewHolder();
            viewHolder.lineLayout = (RelativeLayout) convertView.findViewById(R.id.lineLayout);
            viewHolder.text = (TextView) convertView.findViewById(R.id.settingsTxt);
            viewHolder.aswitch = (Switch) convertView.findViewById(R.id.settingsSwitch);
            viewHolder.version = (TextView) convertView.findViewById(R.id.versionTxt);
            viewHolder.chevronRight = (ImageView) convertView.findViewById(R.id.chevronRight);
            viewHolder.chevronRight.setImageResource(R.drawable.ic_chevron_right);



            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }
        final String temp = getItem(position);

        if(position == 1 ){
            viewHolder.aswitch.setChecked(restrictToWifi);
        }

        if(position == 2 ){
            viewHolder.aswitch.setChecked(icludePhoto);
        }

        if(position == 0   || position == 4 || position == 6){
            viewHolder.aswitch.setVisibility(View.GONE);
            viewHolder.version.setVisibility(View.GONE);
            viewHolder.chevronRight.setVisibility(View.GONE);
            viewHolder.lineLayout.setBackgroundColor(Color.parseColor("#EDEDED"));
        }


        if(position == 5 ){
            viewHolder.aswitch.setVisibility(View.GONE);
            viewHolder.version.setVisibility(View.GONE);
            viewHolder.chevronRight.setVisibility(View.GONE);
            viewHolder.version.setVisibility(View.VISIBLE);

            int versionCode = BuildConfig.VERSION_CODE;
            String versionName = BuildConfig.VERSION_NAME;
            viewHolder.version.setText(versionName+" Build "+versionCode);
        }

        if(position == 3 ){
            viewHolder.aswitch.setVisibility(View.GONE);
            viewHolder.version.setVisibility(View.GONE);
            viewHolder.chevronRight.setVisibility(View.VISIBLE);
            viewHolder.lineLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (customListner != null) {
                        customListner.onButtonClickListner(position,temp);
                    }
                }
            });

        }

        viewHolder.text.setText(temp);
        viewHolder.aswitch.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                if (customListner != null) {
                    customListner.onButtonClickListner(position,temp);
                }

            }
        });


        viewHolder.chevronRight.setOnClickListener(new View.OnClickListener() {

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
        RelativeLayout lineLayout;
        TextView text;
        Switch  aswitch;
        TextView version;
        ImageView chevronRight;
    }
}


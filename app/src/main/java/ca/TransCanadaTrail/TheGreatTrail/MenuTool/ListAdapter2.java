package ca.TransCanadaTrail.TheGreatTrail.MenuTool;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.ArrayList;

import ca.TransCanadaTrail.TheGreatTrail.R;

/**
 * Created by Dev1 on 12/22/2016.
 */

public class ListAdapter2 extends ArrayAdapter<String> {
    private ArrayList<String> data;
    private ArrayList<String> data2  ;
    private Context context;
    private int selectedItem;


    public ListAdapter2(Context context, ArrayList<String> dataItem, ArrayList<String> subTitle, int selectedItem) {
        super(context, R.layout.settings_list_item2, dataItem);
        this.data = dataItem;
        this.data2 = subTitle;
        this.context = context;
        this.selectedItem = selectedItem;
    }



    @Override
    public View getView(final int position,  View convertView, final ViewGroup parent) {
        ViewHolder viewHolder;
        if (convertView == null) {
            LayoutInflater inflater = LayoutInflater.from(context);
            convertView = inflater.inflate(R.layout.settings_list_item2, null);
            viewHolder = new ViewHolder();
            viewHolder.lineLayout = (RelativeLayout) convertView.findViewById(R.id.lineLayout);
            viewHolder.text = (TextView) convertView.findViewById(R.id.itemTxt);
            viewHolder.subtitle = (TextView) convertView.findViewById(R.id.subtitleTxt);
            viewHolder.checkBox = (CheckBox) convertView.findViewById(R.id.checkBox);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }
        final String temp = getItem(position);
        final String temp2 = data2.get(position);

        if(position == 0   ||  position == 5){
            viewHolder.checkBox.setVisibility(View.GONE);
            viewHolder.lineLayout.setBackgroundColor(Color.parseColor("#EDEDED"));
        }


        if(position == selectedItem){
            viewHolder.checkBox.setChecked(true);

        }

        viewHolder.text.setText(temp);
        viewHolder.subtitle.setText(temp2);
        viewHolder.checkBox.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                if (customListner != null) {

                    for (int i = 1; i < 5 ; i ++)
                    {

                        View view = parent.getChildAt(i);
                        CheckBox checkBox = (CheckBox) view.findViewById(R.id.checkBox);
                        checkBox.setChecked(false);

                    }

                    CheckBox checkBox = (CheckBox) v.findViewById(R.id.checkBox);
                    checkBox.setChecked(true);
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
        TextView subtitle;
        CheckBox checkBox;

    }
}


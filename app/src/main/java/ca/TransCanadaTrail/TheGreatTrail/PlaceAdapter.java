package ca.TransCanadaTrail.TheGreatTrail;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.List;

/**
 * Created by hardikfumakiya on 2017-01-02.
 */

public class PlaceAdapter extends BaseAdapter {

    Context context;
    private List<HashMap<String, String>> mData;
    private LayoutInflater mInflater;

    private boolean mIsSpaceVisible = true;


    public PlaceAdapter(Context context, List<HashMap<String, String>> places) {
        this.context = context;
        mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        this.mData = places;
    }

    @Override
    public int getCount() {

        return mData.size();
    }

    @Override
    public HashMap<String, String> getItem(int position) {
        return mData.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }


    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder = null;
        int rowType = getItemViewType(position);

        if (convertView == null) {
            holder = new ViewHolder();
            convertView = mInflater.inflate(R.layout.place_item, null);
            holder.namePlace = (TextView) convertView.findViewById(R.id.namePlace);
            holder.addressPlace = (TextView) convertView.findViewById(R.id.addressPlace);
            holder.telephone = (ImageView) convertView.findViewById(R.id.callPlace);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        final HashMap<String, String> place = mData.get(position);
//        double lat = Double.parseDouble(place.get("lat"));
//
//        double lng = Double.parseDouble(place.get("lng"));
//        LatLng latLng = new LatLng(lat, lng);

        holder.namePlace.setText(place.get("place_name"));
        holder.addressPlace.setText(place.get("vicinity"));

        holder.telephone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String phone_number = null;
                try {

                    StringBuilder sb = new StringBuilder("https://maps.googleapis.com/maps/api/place/details/json?");
                    sb.append("placeid=" + place.get("place_id"));
                    sb.append("&key="+SearchListFragment.API_KEY);

                    PlaceDetailFragment.PlacesDetailTask placesTask = new PlaceDetailFragment.PlacesDetailTask();
                    String result = placesTask.execute(sb.toString(), "detail_call").get();

                    JSONObject placeDetails = new JSONObject(result);
                    if (placeDetails.getJSONObject("result").has("international_phone_number"))
                        phone_number = placeDetails.getJSONObject("result").getString("international_phone_number");

                    if (phone_number != null) {

                        Uri call = Uri.parse("tel:" + phone_number);
                        if (Build.VERSION.SDK_INT >= 23) {
                            int accessCallPermission
                                    = ContextCompat.checkSelfPermission(context, Manifest.permission.CALL_PHONE);

                            if (accessCallPermission != PackageManager.PERMISSION_GRANTED) {
                                // The Permissions to ask user.
                                String[] permissions = new String[]{Manifest.permission.CALL_PHONE};
                                // Show a dialog asking the user to allow the above permissions.
                                ActivityCompat.requestPermissions((MainActivity) context, permissions, MainActivity.REQUEST_ID_ACCESS_COURSE_FINE_LOCATION);
                                return;
                            }
                        }
                        Intent surf = new Intent(Intent.ACTION_CALL, call);
                        context.startActivity(surf);
                    }
                    else{
                        Toast.makeText(context,"Sorry, couldn't place a call",Toast.LENGTH_SHORT).show();
                    }
                } catch (Exception e) {

                }


            }
        });


        return convertView;
    }

    public void hideSpace() {
        mIsSpaceVisible = false;
        notifyDataSetChanged();
    }

    public void showSpace() {
        mIsSpaceVisible = true;
        notifyDataSetChanged();
    }

    public class ViewHolder {
        TextView namePlace;
        TextView addressPlace;
        ImageView telephone;

    }
}

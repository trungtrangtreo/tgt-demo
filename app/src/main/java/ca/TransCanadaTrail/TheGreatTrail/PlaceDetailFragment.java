package ca.TransCanadaTrail.TheGreatTrail;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.Html;
import android.text.Spanned;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.android.gms.common.api.GoogleApiClient;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.ExecutionException;

/**
 * Created by hardikfumakiya on 2017-01-21.
 */

public class PlaceDetailFragment extends Fragment {


    public static PlaceDetailFragment instance = null;
    private String placeId,photoReferenceId;
    private ImageView mImageView;
    private Bitmap mImageBitmap;
    private Uri file;
    GoogleApiClient mClient;
    String phone_number,pName;
    RelativeLayout google_img;
    MainActivity activity;

    public PlaceDetailFragment() {
        // Required empty public constructor
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        activity=(MainActivity)context;
    }

    public static PlaceDetailFragment getInstance() {

        if (instance == null) {
            Log.i("Instance AT", "New Creation");
            instance = new PlaceDetailFragment();
        }
        Log.i("Instance AT", "No Creation");
        return instance;
    }

    public static PlaceDetailFragment newInstance() {
        PlaceDetailFragment segmentDetailsFragment = getInstance();

        return segmentDetailsFragment;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        menu.clear();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.place_detail_layout, container, false);

        activity.getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        activity.getSupportActionBar().setDisplayShowHomeEnabled(true);

        pName=getArguments().getString("place_name");

        activity.getSupportActionBar().setTitle(Html.fromHtml("<small>"+pName+"</small>"));
        activity.getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_arrow_back);

        TextView placeName = (TextView) view.findViewById(R.id.placeName);
        TextView phoneTxt = (TextView) view.findViewById(R.id.phoneTxt);
        TextView weburlTxt = (TextView) view.findViewById(R.id.weburlTxt);
        TextView placeAddressTxt = (TextView) view.findViewById(R.id.placeAddressTxt);

        google_img=(RelativeLayout)view.findViewById(R.id.google_img);


        placeId=getArguments().getString("place_id");
        placeName.setText(pName);

        photoReferenceId=getArguments().getString("photo_reference",null);

        placeAddressTxt.setText(getArguments().getString("vicinity"));

        View resourcesSeparator = view.findViewById(R.id.resourcesSeparator);

        mImageView = (ImageView) view.findViewById(R.id.firstImage);

        if(photoReferenceId==null)
        {
            google_img.setVisibility(View.GONE);
        }
        else {
            google_img.setVisibility(View.VISIBLE);
            StringBuilder sbValue = new StringBuilder(sbMethod(photoReferenceId));
            //mImageView.setTag(sbValue.toString());
            Bitmap bmp= null;
            try {
                bmp = new DownloadImageTask().execute(sbValue.toString()).get();
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (ExecutionException e) {
                e.printStackTrace();
            }
            mImageView.setImageBitmap(bmp);
        }


        try {
            StringBuilder sbValue = new StringBuilder(sbMethod(""));

            PlacesDetailTask placesTask = new PlacesDetailTask();
            String result=placesTask.execute(sbValue.toString(),"detail_call").get();

            JSONObject placeDetails=new JSONObject(result);
            placeAddressTxt.setText(placeDetails.getJSONObject("result").getString("formatted_address"));
            phone_number=placeDetails.getJSONObject("result").getString("international_phone_number");
            phoneTxt.setText(phone_number);

            if(placeDetails.getJSONObject("result").has("website"))
            {
                String _weburl=placeDetails.getJSONObject("result").getString("website");

                Spanned _url;

                if (Build.VERSION.SDK_INT >= 24) {
                    _url=Html.fromHtml(_weburl, Html.FROM_HTML_MODE_LEGACY);
                }
                else {
                    _url=Html.fromHtml(_weburl);
                }

                weburlTxt.setText(_url);
                weburlTxt.setMovementMethod(LinkMovementMethod.getInstance());
            }
            else{
                view.findViewById(R.id.resourcesTitle).setVisibility(View.GONE);
                view.findViewById(R.id.resourcesSeparator).setVisibility(View.GONE);
            }


            phoneTxt.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(Intent.ACTION_CALL);
                    intent.setData(Uri.parse("tel:"+phone_number));
                    startActivity(intent);
                }
            });
        }
        catch (InterruptedException | ExecutionException | JSONException e) {
            e.printStackTrace();
        }


        return view;

    }


    public class DownloadImageTask extends AsyncTask<String, Void, Bitmap> {

        @Override
        protected Bitmap doInBackground(String... urls) {
            Bitmap bmp =null;
            try{
                URL ulrn = new URL(urls[0]);
                HttpURLConnection con = (HttpURLConnection)ulrn.openConnection();
                InputStream is = con.getInputStream();
                bmp = BitmapFactory.decodeStream(is);
                if (null != bmp)
                    return bmp;
            }
            catch(Exception e){

            }
            return bmp;
        }

        @Override
        protected void onPostExecute(Bitmap result) {

        }
    }

    public StringBuilder sbMethod(String photoRefid) {
        StringBuilder sb;
        if(photoRefid.equals(""))
        {
            sb = new StringBuilder("https://maps.googleapis.com/maps/api/place/details/json?");
            sb.append("placeid=" +placeId);
        }
        else
        {
            sb = new StringBuilder("https://maps.googleapis.com/maps/api/place/photo?maxwidth=400&");
            sb.append("photoreference=" +photoRefid);
            sb.append("&sensor=false");
        }

        sb.append("&key="+SearchListFragment.API_KEY);

        return sb;
    }

    public static class PlacesDetailTask extends AsyncTask<String, Integer, String> {

        String datas = null;
        // Invoked by execute() method of this object
        @Override
        protected String doInBackground(String... url) {
            try {
                InputStream iStream = null;
                HttpURLConnection urlConnection = null;
                try {
                    URL urls = new URL(url[0]);

                    // Creating an http connection to communicate with url
                    urlConnection = (HttpURLConnection) urls.openConnection();

                    // Connecting to url
                    urlConnection.connect();

                    // Reading data from url
                    iStream = urlConnection.getInputStream();

                    BufferedReader br = new BufferedReader(new InputStreamReader(iStream));

                    StringBuffer sb = new StringBuffer();

                    String line = "";

                    while ((line = br.readLine()) != null) {
                        sb.append(line);
                    }

                    datas = sb.toString();

                    br.close();

                } catch (Exception e) {
                    Log.d("Exception", e.toString());
                } finally {
                    iStream.close();
                    urlConnection.disconnect();
                }

            } catch (Exception e) {
                Log.d("Background Task", e.toString());
            }

            return datas;
        }

        // Executed after the complete execution of doInBackground() method
        @Override
        protected void onPostExecute(String result) {


        }
    }

}

package ca.TransCanadaTrail.TheGreatTrail;

import android.database.Cursor;
import android.util.Log;

import java.util.ArrayList;

import ca.TransCanadaTrail.TheGreatTrail.MapView.TrailSegmentLight;

/**
 * Created by houari on 22/11/2016.
 */

public class RetrieveDataBaseSingleton {
    private static RetrieveDataBaseSingleton ourInstance = new RetrieveDataBaseSingleton();

    public static RetrieveDataBaseSingleton getInstance() {

        return ourInstance;
    }

    private RetrieveDataBaseSingleton() {

    }

    public static  ArrayList<TrailSegmentLight> listSegments = new ArrayList<TrailSegmentLight>();

    public void retrieveDataBase(Cursor cursor ) {

        listSegments = new ArrayList<TrailSegmentLight>();


        if (cursor != null && cursor.moveToFirst()) {
            int i=0;
            do {

                // String geometry = cursor.getString(cursor.getColumnIndex("geometry"));
                TrailSegmentLight segment =  TrailSegmentLight.mapFromDatabase(cursor);

                listSegments.add(segment);
                Log.i("LocationService"," cree une classe Le i = "+(i++));

            }
            while (cursor.moveToNext());


        }



    }
}

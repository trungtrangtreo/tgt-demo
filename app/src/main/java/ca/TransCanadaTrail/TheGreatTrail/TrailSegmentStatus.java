package ca.TransCanadaTrail.TheGreatTrail;

import android.database.Cursor;

/**
 * Created by Dev1 on 11/4/2016.
 */

public class TrailSegmentStatus {
    int objectId = 0;
    byte statusCode = 0 ;
    byte categoryCode = 0 ;


    static public TrailSegmentStatus mapFromDatabase(Cursor result ) {
        TrailSegmentStatus segment = new TrailSegmentStatus();
        segment.objectId = result.getInt(result.getColumnIndex("objectid"));
        segment.statusCode = (byte) result.getInt(result.getColumnIndex("statuscode"));
        segment.categoryCode = (byte) result.getInt(result.getColumnIndex("categorycode")) ;

        return segment;
    }



}

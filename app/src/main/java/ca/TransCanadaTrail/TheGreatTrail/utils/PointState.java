package ca.TransCanadaTrail.TheGreatTrail.utils;

import com.google.android.gms.maps.model.LatLng;

/**
 * Created by Dev1 on 10/31/2016.
 */

public class PointState {

    private LatLng point;
    private String state;

    public PointState(LatLng point, String state ) {
        this.point = point;
        this.state = state;
    }

    public LatLng getPoint(){
        return point;
    }

    public String getState(){
        return state;
    }

    public void setPoint(LatLng point) {
        this.point = point;
    }

}



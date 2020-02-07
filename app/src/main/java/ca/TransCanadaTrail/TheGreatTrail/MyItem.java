package ca.TransCanadaTrail.TheGreatTrail;

import com.google.android.gms.maps.model.LatLng;
import com.google.maps.android.clustering.ClusterItem;

/**
 * Created by hardikfumakiya on 2016-12-23.
 */

public class MyItem implements ClusterItem {
    private final LatLng mPosition;
    private String title;
    private boolean information;
    private boolean trailhead;
    private boolean picnic_table;
    private boolean restroom;
    private boolean water;
    private boolean camping;


    private int objid;

    public MyItem(double lat, double lng,int obj_id) {
        mPosition = new LatLng(lat, lng);
        objid=obj_id;
    }

    public int getObjectid() {
        return objid;
    }

    public void setObjectid(int trailid) {
        this.objid = trailid;
    }

    @Override
    public LatLng getPosition() {
        return mPosition;
    }

    public String getTitle() {
        return title;
    }


    public String getSnippet() {
        return null;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public boolean isInformation() {
        return information;
    }

    public void setInformation(boolean information) {
        this.information = information;
    }

    public boolean isTrailhead() {
        return trailhead;
    }

    public void setTrailhead(boolean trailhead) {
        this.trailhead = trailhead;
    }

    public boolean isPicnic_table() {
        return picnic_table;
    }

    public void setPicnic_table(boolean picnic_table) {
        this.picnic_table = picnic_table;
    }

    public boolean isRestroom() {
        return restroom;
    }

    public void setRestroom(boolean restroom) {
        this.restroom = restroom;
    }

    public boolean isWater() {
        return water;
    }

    public void setWater(boolean water) {
        this.water = water;
    }

    public boolean isCamping() {
        return camping;
    }

    public void setCamping(boolean camping) {
        this.camping = camping;
    }
}

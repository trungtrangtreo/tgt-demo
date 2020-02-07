package ca.TransCanadaTrail.TheGreatTrail.utils;

import com.google.android.gms.maps.model.LatLng;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

/**
 * Created by Dev1 on 11/9/2016.
 */

public class Coordinate implements Serializable {

    private double latitude ;
    private double longitude;

    public Coordinate(double latitude, double longitude) {
        this.latitude = latitude ;
        this.longitude = longitude;
    }



     public LatLng getLocation() {
         LatLng latLng = new LatLng(this.latitude, this.longitude);
         return latLng;
     }

    public double getLatitude() {
        return latitude;
    }

    private void writeObject(ObjectOutputStream out) throws IOException {
        out.defaultWriteObject();
        out.writeDouble(this.latitude);
        out.writeDouble(this.longitude);
    }

    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        in.defaultReadObject();
        this.latitude = in.readDouble();
        this.longitude = in.readDouble();
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public void setLocation(LatLng location) {


         this.latitude = location.latitude;
         this.longitude = location.longitude;
     }

}

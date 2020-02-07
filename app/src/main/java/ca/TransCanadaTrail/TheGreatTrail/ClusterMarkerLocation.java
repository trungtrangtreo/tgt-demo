package ca.TransCanadaTrail.TheGreatTrail;



import com.google.android.gms.maps.model.LatLng;
import com.google.maps.android.clustering.ClusterItem;

/**
 * Created by hardikfumakiya on 2016-12-22.
 */

public class ClusterMarkerLocation implements ClusterItem {

    private LatLng position;

    public ClusterMarkerLocation( LatLng latLng ) {
        position = latLng;
    }

    @Override
    public LatLng getPosition() {
        return position;
    }


    public String getTitle() {
        return null;
    }


    public String getSnippet() {
        return null;
    }

    public void setPosition( LatLng position ) {
        this.position = position;
    }
}

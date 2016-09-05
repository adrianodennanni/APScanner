package map.net.apscanner.classes.acquisition;

import java.io.Serializable;
import java.util.List;

import map.net.apscanner.classes.access_point.AccessPoint;

/**
 * Created by adriano on 05/09/16.
 */
public class Acquisition implements Serializable {

    List<AccessPoint> accessPointsList;


    public List<AccessPoint> getAccessPointsList() {
        return accessPointsList;
    }

    public void setAccessPointsList(List<AccessPoint> accessPointsList) {
        this.accessPointsList = accessPointsList;
    }

    public void addAccessPoint(AccessPoint ap) {
        accessPointsList.add(ap);
    }


}

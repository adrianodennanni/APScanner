package map.net.apscanner.classes.access_point;

import java.io.Serializable;

/**
 * Created by adriano on 05/09/16.
 */
public class AccessPoint implements Serializable {

    private String BSSID;
    private short RSSI;


    public String getBSSID() {
        return BSSID;
    }

    public void setBSSID(String BSSID) {
        this.BSSID = BSSID;
    }


    public short getRSSI() {
        return RSSI;
    }

    public void setRSSI(short RSSI) {
        this.RSSI = RSSI;
    }

}

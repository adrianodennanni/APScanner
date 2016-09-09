package map.net.apscanner.utils;

import java.util.ArrayList;
import java.util.HashMap;

import map.net.KalmanFilter;
import map.net.apscanner.classes.access_point.AccessPoint;

/**
 * Created by adriano on 9/8/16.
 */
public class Normalization {


    private String mMethod;
    private Integer mNumberOfAcquisitons;

    private HashMap<String, ArrayList<Double>> acquisitions;

    public Normalization(String method, Integer numberOfAcquisitons) {
        mMethod = method;
        mNumberOfAcquisitons = numberOfAcquisitons;
    }

    /**
     * Adds the data from an Access Point to the acquisitions HashMap
     *
     * @param BSSID BSSID of an Access Point
     * @param RSSI  RSSI of an Acces Point
     */
    public void addAccessPoint(String BSSID, double RSSI) {

        ArrayList<Double> rssiList = new ArrayList<>();

        if (acquisitions.containsKey(BSSID)) {
            rssiList = acquisitions.get(BSSID);
        }

        rssiList.add(RSSI);
        acquisitions.put(BSSID, rssiList);
    }

    /**
     * Normalizes all Access Points, using the Normalization Method already set in the constructor
     * <p>
     * Empty RSSI acquisitions are filled with -120.0, default value if an Access Point was out of
     * range.
     *
     * @return ArrayList containing all normalized Access Points
     */
    public ArrayList<AccessPoint> normalize() {

        // Iterates trough all BSSIDs to fill slots in RSSIs
        for (String key : acquisitions.keySet()) {
            fillWeakAccessPoints(key);
        }

        ArrayList<AccessPoint> accessPointsList = new ArrayList<>();

        // Checks if user has chosen Kalman Filter as Normalization Method
        if (mMethod.equals("Kalman Filter")) {

            // Iterates trough all BSSIDs to apply the normalization
            for (String key : acquisitions.keySet()) {
                Double result = KalmanFilter.kalman(acquisitions.get(key));
                accessPointsList.add(new AccessPoint(key, result));
            }
        } else {
            // Iterates trough all BSSIDs to apply the normalization
            for (String key : acquisitions.keySet()) {
                Double result = KalmanFilter.mean(acquisitions.get(key));
                accessPointsList.add(new AccessPoint(key, result));
            }
        }

        return accessPointsList;
    }


    /**
     * This function helps filling blank RSSI slots in the Access Points List.
     * Slots are filled with -120.0 (basically no signal at all).
     *
     * @param key of current rssiList
     */
    private void fillWeakAccessPoints(String key) {
        if (acquisitions.get(key).size() < mNumberOfAcquisitons) {
            acquisitions.get(key).add(-120.0);
            fillWeakAccessPoints(key);
        }
    }


}

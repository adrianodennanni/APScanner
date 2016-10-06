package map.net.apscanner.utils;

import android.app.ProgressDialog;
import android.content.Context;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import map.net.apscanner.classes.access_point.AccessPoint;
import map.net.apscanner.classes.acquisition_set.AcquisitionSet;
import map.net.apscanner.classes.zone.Zone;

/**
 * CaptureTask scans the access points and calls SaveAcquisitionSetToFile method to save
 * the result in a file.
 */
public class CaptureTask extends AsyncTask<Void, Void, Void> {

    private WifiManager wManager;
    private ProgressDialog scanningDialog;
    private AcquisitionSet mCurrentAcquisitionSet;
    private Normalization mNormalization;
    private int mCurrentCompleteScanNumber = 0;
    private int mCurrentStartedScanNumber = 0;
    private ArrayList<List<ScanResult>> mCache;
    private Context mContext;
    private Zone mZone;


    public CaptureTask(AcquisitionSet currentAcquisitionSet, Context context, Zone zone) {
        mCurrentAcquisitionSet = currentAcquisitionSet;
        wManager = (WifiManager) mContext.getSystemService(Context.WIFI_SERVICE);
        mContext = context;
        mZone = zone;
    }

    private void addToNormalizationQueue(ArrayList<List<ScanResult>> onePointScan) {
        mNormalization.setOnePointScan(onePointScan);
    }

    public void updateCounter() {
        mCurrentCompleteScanNumber++;
        publishProgress();
    }

    @Override
    protected void onPreExecute() {

        mNormalization = new Normalization(
                mCurrentAcquisitionSet.getNormalization_algorithm(),
                mCurrentAcquisitionSet.getMeasures_per_point());

        scanningDialog = new ProgressDialog(mContext);
        scanningDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        scanningDialog.setCancelable(false);
        scanningDialog.setIndeterminate(false);
        scanningDialog.setMax(mCurrentAcquisitionSet.getMeasures_per_point());
        scanningDialog.setTitle("Scanning...");

        scanningDialog.show();
    }


    @Override
    protected Void doInBackground(Void... params) {

        long intervalMiliSeconds = (long) (mCurrentAcquisitionSet.getTime_interval() * 1000);
        mCache = new ArrayList<>();

            /*
            * This part of the code schedules the scan and calls it after the interval suggested
            * by the user. It is called n times, with n being the value suggested by the user too.
            */
        final Timer timer = new Timer();

        timer.schedule(new TimerTask() {
            @Override
            public void run() {

                if (!wManager.isWifiEnabled()) {
                    wManager.setWifiEnabled(true);
                }

                if (wManager.startScan()) {
                    mCache.add(wManager.getScanResults());

                    updateCounter();

                    mCurrentStartedScanNumber++;

                    if (mCurrentStartedScanNumber == mCurrentAcquisitionSet.getMeasures_per_point()) {
                        timer.cancel();
                        timer.purge();
                    }
                }

            }
        }, 0, intervalMiliSeconds);


        while (mCurrentCompleteScanNumber != mCurrentAcquisitionSet.getMeasures_per_point()) {
            try {
                Thread.sleep(10);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    protected void onProgressUpdate(Void... params) {
        scanningDialog.setProgress(mCurrentCompleteScanNumber);
        scanningDialog.setMessage(Integer.toString(mCurrentCompleteScanNumber) + "/"
                + Integer.toString(mCurrentAcquisitionSet.getMeasures_per_point()));

    }


    protected void onPostExecute(Void param) {
        addToNormalizationQueue(mCache);
        ArrayList<AccessPoint> mNormalizedAccessPointsList = mNormalization.normalize();
        new SaveAcquisitionSetToFile(mNormalizedAccessPointsList, mContext, mZone).start();
        scanningDialog.dismiss();
    }


}
package map.net.apscanner.utils;

import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.CountDownTimer;

import java.util.LinkedList;
import java.util.List;

import map.net.apscanner.classes.acquisition_set.AcquisitionSet;
import map.net.apscanner.classes.zone.Zone;

/**
 * This class uses Broadcast Receiver to get wifi data.
 */
public class BroadcastReceiverTask extends AsyncTask<Void, Void, Void> {

    private ProgressDialog scanningDialog;
    private AcquisitionSet mCurrentAcquisitionSet;
    private Normalization mNormalization;
    private int mCurrentCompleteScanNumber = 0;
    private LinkedList<List<ScanResult>> mCache;
    private Context mContext;
    private boolean repeated;
    private Zone mZone;

    private CountDownTimer thresholdTimer = new CountDownTimer(500,10){

        @Override
        public void onTick(long l) {

        }

        @Override
        public void onFinish() {
            repeated = false;
        }
    };

    public BroadcastReceiverTask(AcquisitionSet currentAcquisitionSet, Context context, Zone zone) {
        mCurrentAcquisitionSet = currentAcquisitionSet;
        mContext = context;
        mZone = zone;


    }

    private void updateCounter() {
        mCurrentCompleteScanNumber++;
        publishProgress();
    }

    @Override
    protected void onPreExecute() {

        scanningDialog = new ProgressDialog(mContext);
        scanningDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        scanningDialog.setCancelable(true);
        scanningDialog.setIndeterminate(true);
        scanningDialog.setTitle("Scanning...");
        scanningDialog.setMessage("Total scans: " + mCurrentCompleteScanNumber);

        scanningDialog.show();
    }


    @Override
    protected Void doInBackground(Void... voids) {

        mCache = new LinkedList<>();


        final WifiManager wManager = (WifiManager) mContext.getSystemService(Context.WIFI_SERVICE);
        final IntentFilter filter = new IntentFilter();


        filter.addAction(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION);

        repeated = false;

        final BroadcastReceiver receiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if(repeated){
                    thresholdTimer.cancel();
                    thresholdTimer.start();
                }
                else{

                    repeated = true;

                    mCache.add(wManager.getScanResults());
                    List<ScanResult> list = mCache.getLast();
                    for (ScanResult sr : list) {
                        System.out.println(sr.SSID + " | " + sr.level);
                    }
                    System.out.println("--------------------------");

                    updateCounter();
                }

                wManager.startScan(); // start scan again to get fresh results ASAP

            }
        };

        mContext.registerReceiver(receiver, filter);

        wManager.startScan();

        return null;

    }
}

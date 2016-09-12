package map.net.apscanner.activities;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import com.sromku.simple.storage.SimpleStorage;
import com.sromku.simple.storage.Storage;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import butterknife.BindView;
import butterknife.ButterKnife;
import map.net.apscanner.R;
import map.net.apscanner.classes.access_point.AccessPoint;
import map.net.apscanner.classes.acquisition_set.AcquisitionSet;
import map.net.apscanner.classes.zone.Zone;
import map.net.apscanner.utils.Normalization;

import static android.Manifest.permission.ACCESS_COARSE_LOCATION;

public class AcquisitionsActivity extends AppCompatActivity {

    private static final int REQUEST_ACCESS_LOCATION = 0;

    @BindView(R.id.fabStartMeasure)
    FloatingActionButton startAcquisitionButton;

    @BindView(R.id.imageButtonEraseCurrentSet)
    ImageButton eraseCurrentSetButton;

    @BindView(R.id.imageButtonSendSet)
    ImageButton sendCurrentSetsButton;

    @BindView(R.id.subtitleAcquisition)
    TextView subtitleAcquisitionTextView;

    Bundle extras;
    Zone zone;

    CaptureTask captureAPs;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_acquisitions);
        ButterKnife.bind(this);

        // Get data passed from Zone Activity
        extras = getIntent().getExtras();
        if (extras != null) {
            zone = (Zone) extras.get("ZONE");
            assert zone != null;
            subtitleAcquisitionTextView.setText(zone.getName());
        }

        mayRequestLocationAccess();

        startAcquisitionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startScan("Kalman Filter", 3, (float) 2);
            }
        });


    }

    private void startScan(String normalizationAlgorithm, int scansPerAcquisition, float interval) {
        AcquisitionSet currentAcquisitionSet =
                new AcquisitionSet(normalizationAlgorithm, interval, scansPerAcquisition);

        captureAPs = new CaptureTask(currentAcquisitionSet);
        captureAPs.execute();
    }

    /**
     * This function asks for permission to access Coarse Location, necessary to read access points
     * data.
     *
     * @return if ACCESS_COARSE_LOCATION has been permitted (true) or not (false)
     */
    private boolean mayRequestLocationAccess() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            return true;
        }
        if (checkSelfPermission(ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            return true;
        }
        if (shouldShowRequestPermissionRationale(ACCESS_COARSE_LOCATION)) {
            requestPermissions(new String[]{ACCESS_COARSE_LOCATION}, REQUEST_ACCESS_LOCATION);
        } else {
            requestPermissions(new String[]{ACCESS_COARSE_LOCATION}, REQUEST_ACCESS_LOCATION);
        }
        return false;
    }

    /**
     * This function is called by the System after the user has chosen to permit or not
     * Coarse Location to be accessed by the app.
     */

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        if (requestCode == REQUEST_ACCESS_LOCATION) {
            Intent intent = new Intent(AcquisitionsActivity.this, AcquisitionsActivity.class);
            intent.putExtra("ZONE", zone);
            if (grantResults.length == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startActivity(intent);
                finish();
            } else {
                startActivity(intent);
                finish();
            }
        }
    }



    private class CaptureTask extends AsyncTask<Void, Void, Void> {

        final WifiManager wManager = (WifiManager) getSystemService(Context.WIFI_SERVICE);
        ProgressDialog scanningDialog;
        AcquisitionSet mCurrentAcquisitionSet;
        ArrayList<AccessPoint> mNormalizedAccessPointsList;
        Normalization normalization;
        private int mCurrentCompleteScanNumber = 0;
        private int mCurrentStartedScanNumber = 0;
        private ArrayList<List<ScanResult>> mCache;


        private CaptureTask(AcquisitionSet currentAcquisitionSet) {
            mCurrentAcquisitionSet = currentAcquisitionSet;
        }

        private void addToNormalzationQueue(ArrayList<List<ScanResult>> onePointScan) {
            normalization.setOnePointScan(onePointScan);
        }

        public void updateCounter() {
            mCurrentCompleteScanNumber++;
            publishProgress();
        }

        @Override
        protected void onPreExecute() {

            normalization = new Normalization(
                    mCurrentAcquisitionSet.getNormalization_algorithm(),
                    mCurrentAcquisitionSet.getMeasures_per_point());

            scanningDialog = new ProgressDialog(AcquisitionsActivity.this);
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

                        captureAPs.updateCounter();

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
            addToNormalzationQueue(mCache);
            mNormalizedAccessPointsList = normalization.normalize();
            scanningDialog.dismiss();
        }


    }

    private class saveAquisitionSetToFile extends AsyncTask<Void, Void, Void> {

        Storage storage;
        String mZoneName;

        public saveAquisitionSetToFile(String zoneName, ArrayList<AccessPoint> apList) {
            mZoneName = zoneName;
        }

        @Override
        protected Void doInBackground(Void... params) {
            storage = SimpleStorage.getInternalStorage(AcquisitionsActivity.this);

            /* Check if directory already exists. If not, create a new one */
            if (!storage.isDirectoryExists(mZoneName)) {
                storage.createDirectory(mZoneName);
            }



            return null;
        }
    }

}

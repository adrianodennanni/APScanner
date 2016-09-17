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
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.sromku.simple.storage.SimpleStorage;
import com.sromku.simple.storage.Storage;
import com.sromku.simple.storage.helpers.OrderType;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
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

    @BindView(R.id.recyclerView)
    RecyclerView aquisitionsRecycleView;

    Bundle extras;
    Zone zone;
    Storage storage;
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

        storage = SimpleStorage.getInternalStorage(AcquisitionsActivity.this);

            /* Check if directory already exists. If not, create a new one */
        if (!storage.isDirectoryExists(zone.getName())) {
            storage.createDirectory(zone.getName());
        }

        startAcquisitionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startScan("Kalman Filter", 3, (float) 2);
            }
        });

        new LoadAcquisitionsFromStorage().start();


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
     * @return Boolean telling if ACCESS_COARSE_LOCATION has been permitted (true) or not (false)
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


    /**
     * CaptureTask scans the access points and calls SaveAcquisitionSetToFile method to save
     * the result in a file.
     */
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

        private void addToNormalizationQueue(ArrayList<List<ScanResult>> onePointScan) {
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
            addToNormalizationQueue(mCache);
            mNormalizedAccessPointsList = normalization.normalize();
            new SaveAcquisitionSetToFile(mNormalizedAccessPointsList).start();
            scanningDialog.dismiss();
        }


    }

    /**
     * SaveAcquisitionSetToFile converts the ArrayList of Access Points into a structured JSON file.
     * Then, saves it in a folder with the Zone name.
     */
    private class SaveAcquisitionSetToFile extends Thread {


        ArrayList<AccessPoint> mFilteredAcquisition;

        public SaveAcquisitionSetToFile(ArrayList<AccessPoint> filteredAcquisition) {

            mFilteredAcquisition = filteredAcquisition;
        }

        @Override
        public void run() {


            /* Creating the JSON of the acquisition to be stored */
            JsonObject acquisitionJSON = new JsonObject();
            JsonArray accessPointsJSON = new JsonArray();
            JsonObject accessPointJSON;

            for (AccessPoint ap : mFilteredAcquisition) {
                accessPointJSON = new JsonObject();
                accessPointJSON.addProperty("BSSID", ap.getBSSID());
                accessPointJSON.addProperty("RSSI", ap.getRSSI());

                accessPointsJSON.add(accessPointJSON);
            }

            acquisitionJSON.add("access_points", accessPointsJSON);

            /*
            * Creates a new file with the JSON content.
            * The name of the time is the current Unix time.
            * If file wasn't created for some reason, an error Toast will be displayed.
            */
            if (!storage.createFile(zone.getName(),
                    Long.toString(System.currentTimeMillis()), acquisitionJSON.toString())) {
                Toast.makeText(AcquisitionsActivity.this,
                        "Acquisition cold not be saved. Check your storage.",
                        Toast.LENGTH_LONG).show();
            }


        }
    }

    private class LoadAcquisitionsFromStorage extends Thread {
        BufferedReader bufferedReader;
        StringBuilder stringBuilder;

        public void run() {

            /* Loads all acquisitions stored in files. */
            List<File> files = storage.getFiles(zone.getName(), OrderType.NAME);
            for (File file : files) {
                try {
                    BufferedReader bufferedReader = new BufferedReader(new FileReader(file.getName()));
                    StringBuilder stringBuilder = new StringBuilder();
                    String line = bufferedReader.readLine();
                    while (line != null) {
                        stringBuilder.append(line);
                        line = bufferedReader.readLine();
                    }
                    String result = stringBuilder.toString();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            /* If there is at least one file saved, the user should not be able to change configurations
            * about the current Acquisition Set (normalization method, etc) */
            //TODO: Load configurations fragment if it is empty, otherwise load configurations
            if (files.isEmpty()) {

            }

        }
    }


}



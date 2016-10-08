package map.net.apscanner.activities;

import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.widget.ImageButton;
import android.widget.TextView;

import com.sromku.simple.storage.SimpleStorage;
import com.sromku.simple.storage.Storage;
import com.sromku.simple.storage.helpers.OrderType;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import map.net.apscanner.R;
import map.net.apscanner.classes.acquisition_set.AcquisitionSet;
import map.net.apscanner.classes.zone.Zone;
import map.net.apscanner.fragments.NewAcquisitionSetFragment;
import map.net.apscanner.utils.CaptureTask;

import static android.Manifest.permission.ACCESS_COARSE_LOCATION;

public class AcquisitionsActivity extends AppCompatActivity {

    private static final int REQUEST_ACCESS_LOCATION = 0;


    @BindView(R.id.imageButtonEraseCurrentSet)
    ImageButton eraseCurrentSetButton;

    @BindView(R.id.imageButtonSendSet)
    ImageButton sendCurrentSetsButton;

    @BindView(R.id.subtitleAcquisition)
    TextView subtitleAcquisitionTextView;


    Bundle extras;
    Zone zone;
    Storage storage;


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

        new LoadAcquisitionsFromStorage().start();


    }

    private void startScan(String normalizationAlgorithm, int scansPerAcquisition, float interval) {
        AcquisitionSet currentAcquisitionSet =
                new AcquisitionSet(normalizationAlgorithm, interval, scansPerAcquisition);

        CaptureTask captureAPs = new CaptureTask(currentAcquisitionSet, AcquisitionsActivity.this, zone);
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



    private class LoadAcquisitionsFromStorage extends Thread {

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
            FragmentManager fragmentManager = getFragmentManager();
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();

            if (files.isEmpty()) {
                NewAcquisitionSetFragment newAcquisitionSetFragment =
                        new NewAcquisitionSetFragment();

                getIntent().putExtra("zone", zone);

                fragmentTransaction.add(R.id.mainAcquisitionFragment, newAcquisitionSetFragment);
                fragmentTransaction.commit();


            }


        }
    }


}



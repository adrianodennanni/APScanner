package map.net.apscanner.activities;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.widget.ImageButton;
import android.widget.TextView;

import butterknife.BindView;
import butterknife.ButterKnife;
import map.net.apscanner.R;
import map.net.apscanner.classes.zone.Zone;

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


    }


    private void startScan(String normalizationAlgorithm, int scansPerAcquisition, float interval) {

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



}

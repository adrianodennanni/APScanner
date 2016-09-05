package map.net.apscanner.activities;

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;

import butterknife.BindView;
import map.net.apscanner.R;
import map.net.apscanner.classes.zone.Zone;

public class AcquisitionsActivity extends AppCompatActivity {

    @BindView(R.id.fabStartMeasure)
    FloatingActionButton startAcquisitionButton;

    @BindView(R.id.imageButtonEraseCurrentSet)
    FloatingActionButton eraseCurrentSetButton;

    @BindView(R.id.imageButtonSendSet)
    FloatingActionButton sendCurrentSetsButton;

    @BindView(R.id.subtitleAcquisition)
    TextView subtitleAcquisitionTextView;

    Bundle extras;
    Zone zone;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_acquisitions);

        // Get data passed from Zone Activity
        extras = getIntent().getExtras();
        if (extras != null) {
            zone = (Zone) extras.get("ZONE");
        }

        subtitleAcquisitionTextView.setText(zone.getName());

    }
}

package map.net.apscanner.activities;

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.widget.ImageButton;
import android.widget.TextView;

import butterknife.BindView;
import butterknife.ButterKnife;
import map.net.apscanner.R;
import map.net.apscanner.classes.zone.Zone;

public class AcquisitionsActivity extends AppCompatActivity {

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


    }
}

package map.net.apscanner.activities;

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;

import butterknife.BindView;
import map.net.apscanner.R;

public class AcquisitionsActivity extends AppCompatActivity {

    @BindView(R.id.fabStartMeasure)
    FloatingActionButton startAcquisitionButton;

    @BindView(R.id.imageButtonEraseCurrentSet)
    FloatingActionButton eraseCurrentSetButton;

    @BindView(R.id.imageButtonSendSet)
    FloatingActionButton sendCurrentSets;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_acquisitions);


    }
}

package map.net.apscanner.fragments;

import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.sromku.simple.storage.SimpleStorage;
import com.sromku.simple.storage.Storage;
import com.sromku.simple.storage.helpers.OrderType;

import java.nio.charset.Charset;

import butterknife.BindView;
import butterknife.ButterKnife;
import map.net.apscanner.R;
import map.net.apscanner.classes.acquisition_set.AcquisitionSet;
import map.net.apscanner.classes.zone.Zone;
import map.net.apscanner.utils.CaptureTask;
import map.net.apscanner.utils.GsonUtil;

public class CurrentAcquisitionSetFragment extends Fragment {

    @BindView(R.id.currentNormalizationMethod)
    TextView currentNormalizationMethodTextView;

    @BindView(R.id.currentTimeInterval)
    TextView currentTimeIntervalTextView;

    @BindView(R.id.currentScansPerAcquisition)
    TextView currentScansPerAcquisitionTextView;

    @BindView(R.id.currentNumberOfAcquisitions)
    TextView currentNumberOfAcquisitionsTextView;

    @BindView(R.id.fabAddMeasure)
    FloatingActionButton addNewMeasureFAB;

    Storage storage;
    Zone zone;


    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        Intent i = getActivity().getIntent();
        zone = (Zone) i.getSerializableExtra("zone");

        View view = inflater.inflate(R.layout.current_acquisitions_list_fragment, container, false);
        ButterKnife.bind(this, view);

        storage = SimpleStorage.getInternalStorage(getActivity());

        final AcquisitionSet currentAcquisitionSet = GsonUtil.getGson().fromJson(
                new String(storage.readFile(zone.getName(), "settings"), Charset.forName("UTF-8")),
                AcquisitionSet.class
        );

        currentNormalizationMethodTextView.setText(
                currentAcquisitionSet.getNormalization_algorithm()
        );

        currentScansPerAcquisitionTextView.setText(
                Integer.toString(currentAcquisitionSet.getMeasures_per_point())
        );

        currentTimeIntervalTextView.setText(
                Float.toString(currentAcquisitionSet.getTime_interval())
        );

        currentNumberOfAcquisitionsTextView.setText(
                Integer.toString(storage.getFiles(zone.getName(), OrderType.NAME).size() - 1)
        );

        addNewMeasureFAB.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                AcquisitionSet newAcquisitionSet =
                        new AcquisitionSet(
                                currentAcquisitionSet.getNormalization_algorithm(),
                                currentAcquisitionSet.getTime_interval(),
                                currentAcquisitionSet.getMeasures_per_point()
                        );

                Storage storage = SimpleStorage.getInternalStorage(getActivity());
                String settings = GsonUtil.getGson().toJson(currentAcquisitionSet);
                storage.createFile(zone.getName(), "settings", settings);


                CaptureTask captureAPs = new CaptureTask(
                        currentAcquisitionSet,
                        getActivity(),
                        zone);

                captureAPs.execute();


            }
        });


        return view;

    }

}

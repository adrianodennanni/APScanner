package map.net.apscanner.fragments;

import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Spinner;

import com.sromku.simple.storage.SimpleStorage;
import com.sromku.simple.storage.Storage;

import butterknife.BindView;
import butterknife.ButterKnife;
import map.net.apscanner.R;
import map.net.apscanner.classes.acquisition_set.AcquisitionSet;
import map.net.apscanner.classes.zone.Zone;
import map.net.apscanner.utils.CaptureTask;
import map.net.apscanner.utils.GsonUtil;

public class NewAcquisitionSetFragment extends Fragment {

    @BindView(R.id.fabStartMeasure)
    FloatingActionButton startAcquisitionsButton;
    @BindView(R.id.methodSpinner)
    Spinner methodSpinner;
    @BindView(R.id.editTextNumberOfScans)
    EditText numberOfScansEditText;
    @BindView(R.id.editTextTimeInterval)
    EditText timeIntervalEditText;


    Zone zone;


    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        Intent i = getActivity().getIntent();
        zone = (Zone) i.getSerializableExtra("zone");

        View view = inflater.inflate(R.layout.new_acquisition_set_fragment, container, false);
        ButterKnife.bind(this, view);

        startAcquisitionsButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                AcquisitionSet currentAcquisitionSet =
                        new AcquisitionSet(
                                methodSpinner.getSelectedItem().toString(),
                                Float.parseFloat(timeIntervalEditText.getText().toString()),
                                Integer.parseInt(numberOfScansEditText.getText().toString())
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

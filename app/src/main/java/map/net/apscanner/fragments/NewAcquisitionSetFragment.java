package map.net.apscanner.fragments;

import android.app.Fragment;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import map.net.apscanner.R;

/**
 * Created by adriano on 10/1/16.
 */
public class NewAcquisitionSetFragment extends Fragment {

    FloatingActionButton startAcquisitionsButton;

    public NewAcquisitionSetFragment() {
        startAcquisitionsButton = (FloatingActionButton) getActivity().findViewById(R.id.fabStartMeasure);
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        return inflater.inflate(R.layout.new_acquisition_set_fragment, container, false);

    }


    public FloatingActionButton getStartAcquisitionButton() {
        return startAcquisitionsButton;
    }
}

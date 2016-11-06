package map.net.apscanner.fragments;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import butterknife.ButterKnife;
import map.net.apscanner.R;

public class PredictZonesFragment extends Fragment {

    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.current_acquisitions_list_fragment, container, false);
        ButterKnife.bind(this, view);


        return view;
    }
}

package map.net.apscanner.utils;

import android.app.Activity;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Context;
import android.widget.ImageButton;

import com.sromku.simple.storage.Storage;
import com.sromku.simple.storage.helpers.OrderType;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.List;

import butterknife.BindView;
import map.net.apscanner.R;
import map.net.apscanner.classes.zone.Zone;
import map.net.apscanner.fragments.NewAcquisitionSetFragment;

/**
 * Created by adriano on 10/8/16.
 */

public class LoadAcquisitionsFromStorage extends Thread {

    @BindView(R.id.imageButtonEraseCurrentSet)
    ImageButton eraseCurrentSetButton;
    @BindView(R.id.imageButtonSendSet)
    ImageButton sendCurrentSetsButton;
    private Storage mStorage;
    private Zone mZone;
    private Context mContext;
    private Boolean mIsEmpty;

    public LoadAcquisitionsFromStorage(Storage storage, Zone zone, Context context) {

        mZone = zone;
        mStorage = storage;
        mContext = context;
    }

    public void run() {

            /* Loads all acquisitions stored in files. */
        List<File> files = mStorage.getFiles(mZone.getName(), OrderType.NAME);
        for (File file : files) {
            try {
                BufferedReader bufferedReader = new BufferedReader(new FileReader(file.getPath()));
                StringBuilder stringBuilder = new StringBuilder();
                String line = bufferedReader.readLine();
                while (line != null) {
                    stringBuilder.append(line);
                    line = bufferedReader.readLine();
                }
                String result = stringBuilder.toString();
                int a = 0;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

            /* If there is at least one file saved, the user should not be able to change configurations
            * about the current Acquisition Set (normalization method, etc) */

        FragmentManager fragmentManager = ((Activity) mContext).getFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();

        if (files.isEmpty()) {
            NewAcquisitionSetFragment newAcquisitionSetFragment =
                    new NewAcquisitionSetFragment();

            ((Activity) mContext).getIntent().putExtra("zone", mZone);

            fragmentTransaction.add(R.id.mainAcquisitionFragment, newAcquisitionSetFragment);
            fragmentTransaction.commit();

            mIsEmpty = true;


        } else {
            mIsEmpty = false;
        }


    }

    public boolean isEmpty() {
        return mIsEmpty;
    }
}

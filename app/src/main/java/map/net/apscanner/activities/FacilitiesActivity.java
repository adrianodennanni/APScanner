package map.net.apscanner.activities;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import map.net.apscanner.R;
import okhttp3.Response;

public class FacilitiesActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_facilities);
    }

    /**
     * This async task gets a list of User's facilities data from server and put them into a
     * ListView. The user can touch on the facility to access its zones.
     */
    private class getFacilitiesFromServer extends AsyncTask<Void, Void, Response> {

        @Override
        protected Response doInBackground(Void... params) {
            // String login =

            return null;
        }
    }
}


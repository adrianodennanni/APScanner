package map.net.apscanner.activities;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import butterknife.BindView;
import butterknife.ButterKnife;
import map.net.apscanner.R;
import map.net.apscanner.classes.facility.Facility;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import static android.Manifest.permission.ACCESS_COARSE_LOCATION;

public class PredictZoneActivity extends AppCompatActivity {

    private static final int REQUEST_ACCESS_LOCATION = 0;
    @BindView(R.id.subtitleFacilityName)
    TextView subtitleFacilityName;
    @BindView(R.id.zoneName)
    TextView zoneName;
    @BindView(R.id.progressBar)
    ProgressBar progressBar;
    @BindView(R.id.editTextUpdateInterval)
    TextView editTextUpdateInterval;
    @BindView(R.id.button2)
    Button buttonOK;
    Bundle extras;
    Facility facility;
    WifiManager wManager;
    Float updateInterval = 2.0f;
    AcquireCurrentZoneFromServer acquireCurrentZoneFromServer;
    String currentZone = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_predict_zone);
        ButterKnife.bind(this);

        mayRequestLocationAccess();

        final OkHttpClient client = new OkHttpClient();
        wManager = (WifiManager) getSystemService(Context.WIFI_SERVICE);
        acquireCurrentZoneFromServer = new AcquireCurrentZoneFromServer();

        // Get data passed from Zone Activity
        extras = getIntent().getExtras();
        if (extras != null) {
            facility = (Facility) extras.get("FACILITY");
            if (facility != null) {
                subtitleFacilityName.setText(facility.getName());
            }
        }

        // Set update time
        buttonOK.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                try {
                    updateInterval = Float.parseFloat(editTextUpdateInterval.getText().toString());
                } catch (NumberFormatException e) {
                    updateInterval = 2.0f;
                    editTextUpdateInterval.setText("2.0");
                    Toast toast = Toast.makeText(PredictZoneActivity.this,
                            "Invalid format", Toast.LENGTH_SHORT);
                    toast.show();
                }
            }
        });


        /*
        * This part of the code schedules the scan and calls it after the interval suggested
        * by the user. It is called endlessly.
        */
        final Timer timer = new Timer();

        timer.schedule(new TimerTask() {
            @Override
            public void run() {

                if (!wManager.isWifiEnabled()) {
                    wManager.setWifiEnabled(true);
                }

                if (wManager.startScan()) {
                    try {
                        acquireCurrentZoneFromServer.run(client, wManager.getScanResults());
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }

            }
        }, 0, (long) (updateInterval * 1000));


    }

    /**
     * This function asks for permission to access Coarse Location, necessary to read access points
     * data.
     *
     * @return Boolean telling if ACCESS_COARSE_LOCATION has been permitted (true) or not (false)
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
            Intent intent = new Intent(PredictZoneActivity.this, PredictZoneActivity.class);
            intent.putExtra("FACILITY", facility);
            startActivity(intent);
            finish();
            overridePendingTransition(0, 0);
        }
    }


    private class AcquireCurrentZoneFromServer {

        public void run(OkHttpClient client, List<ScanResult> scanResults) throws Exception {

            JSONObject requestBodyJSON = new JSONObject();
            JSONObject apJSON;
            JSONArray acquisitionsJSONArray = new JSONArray();

            requestBodyJSON.put("facility_id", facility.getId());
            for (ScanResult sr : scanResults) {
                apJSON = new JSONObject();
                apJSON.put("BSSID", sr.BSSID);
                apJSON.put("RSSI", sr.level);
                acquisitionsJSONArray.put(apJSON);
            }

            requestBodyJSON.put("access_points", acquisitionsJSONArray);
            MediaType JSON = MediaType.parse("application/json; charset=utf-8");
            RequestBody requestBody = RequestBody.create(JSON, String.valueOf(requestBodyJSON));


            Request request = new Request.Builder()
                    .url(getResources().getString(R.string.predict_zone_url))
                    .post(requestBody)
                    .build();

            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    e.printStackTrace();

                    Toast toast = Toast.makeText(getApplicationContext(),
                            "Something went wrong, try again later", Toast.LENGTH_LONG);
                    toast.show();
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    if (!response.isSuccessful())
                        throw new IOException("Unexpected code " + response);

                    currentZone = response.body().string();

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            zoneName.setText(currentZone);
                        }
                    });

                }
            });
        }
    }
}

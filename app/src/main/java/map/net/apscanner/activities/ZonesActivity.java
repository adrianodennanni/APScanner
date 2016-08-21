package map.net.apscanner.activities;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.text.InputType;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import map.net.apscanner.R;
import map.net.apscanner.classes.facility.Facility;
import map.net.apscanner.classes.zone.Zone;
import map.net.apscanner.classes.zone.ZoneAdapter;
import map.net.apscanner.utils.GsonUtil;
import map.net.apscanner.utils.UserInfo;
import okhttp3.HttpUrl;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class ZonesActivity extends AppCompatActivity {

    ListView zonesListView;
    TextView subtitleTextView;
    FloatingActionButton newZoneFAB;

    Bundle extras;
    Facility facility;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_zones);

        // Get data passed from Facility Activity
        extras = getIntent().getExtras();
        if (extras != null) {
            facility = (Facility) extras.get("FACILITY");
        }

        zonesListView = (ListView) findViewById(R.id.zonesListView);
        subtitleTextView = (TextView) findViewById(R.id.subtitleZone);
        newZoneFAB = (FloatingActionButton) findViewById(R.id.fabNewZone);

        subtitleTextView.setText(facility.getName());

        /* On button's click, calls AsyncTask to send new Facility to server */

        newZoneFAB.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {

                MaterialDialog.Builder newZoneDialog =
                        new MaterialDialog.Builder(ZonesActivity.this)
                                .title("Create a new zone")
                                .positiveText("Ok")
                                .negativeText("Cancel")
                                .inputType(InputType.TYPE_CLASS_TEXT)
                                .onPositive(new MaterialDialog.SingleButtonCallback() {
                                    @Override
                                    public void onClick(@NonNull MaterialDialog dialog,
                                                        @NonNull DialogAction which) {
                                        assert dialog.getInputEditText() != null;
                                        String inputText =
                                                dialog.getInputEditText().getText().toString();
                                        new sendZoneToServer().execute(inputText);
                                    }
                                })
                                .onNegative(new MaterialDialog.SingleButtonCallback() {
                                    @Override
                                    public void onClick(@NonNull MaterialDialog dialog,
                                                        @NonNull DialogAction which) {
                                        dialog.dismiss();
                                    }
                                });


                newZoneDialog.input("Enter your zone name", null,
                        new MaterialDialog.InputCallback() {
                            @Override
                            public void onInput(MaterialDialog dialog, CharSequence input) {

                            }
                        });

                newZoneDialog.show();
            }
        });

        new getZonesFromServer().execute();
    }


    /**
     * This async task gets a list of Facilities's zones data from server and put them into a
     * ListView. The user can touch on the zone to access its measures.
     */
    private class getZonesFromServer extends AsyncTask<Void, Void, Response> {

        @Override
        protected Response doInBackground(Void... params) {

            OkHttpClient client = new OkHttpClient();

            /* Build URL with parameters*/
            HttpUrl url = HttpUrl.parse(getResources().getString(R.string.get_zones_url)).newBuilder()
                    .addQueryParameter("facility_id", facility.getId())
                    .build();

            /* Build request */
            Request request = new Request.Builder()
                    .url(url)
                    .header("Content-Type", "application/json")
                    .header("X-User-Email", UserInfo.getUserEmail())
                    .header("X-User-Token", UserInfo.getUserToken())
                    .build();
            Response response = null;

            try {
                response = client.newCall(request).execute();
            } catch (IOException e) {
                e.printStackTrace();
            }

            return response;
        }


        protected void onPostExecute(Response response) {

            if (response == null) {
                Toast toast = Toast.makeText(ZonesActivity.this,
                        "Something went wrong, try refreshing", Toast.LENGTH_LONG);
                toast.show();
                return;
            } else if (response.code() >= 200 && response.code() < 300) {

                JSONArray zonesJSON = null;
                try {
                    zonesJSON = new JSONArray(response.body().string());
                } catch (JSONException | IOException e) {
                    e.printStackTrace();
                }

                List<Zone> zonesList = new ArrayList<>();

                assert zonesJSON != null;
                for (int i = 0; i < zonesJSON.length(); i++) {
                    try {
                        JSONObject zoneJSON = zonesJSON.getJSONObject(i);
                        Zone zone = new Zone(zoneJSON.get("name").toString());

                        /* Sets up a ISO format and convert servers format to it */
                        DateFormat dateFormatISO = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
                        String zoneCreatedAtDate = zoneJSON.get("created_at").toString();
                        Date completeDate = dateFormatISO.parse(zoneCreatedAtDate);

                        /* Setting up days only date*/
                        DateFormat daysOnlyDataFormat = new SimpleDateFormat("dd/MMM/yy");
                        String daysOnlyDate = daysOnlyDataFormat.format(completeDate);
                        zone.setDate(daysOnlyDate);

                        zonesList.add(zone);
                    } catch (JSONException | ParseException e) {
                        e.printStackTrace();
                    }
                }

                ZoneAdapter adapter = new ZoneAdapter(ZonesActivity.this, zonesList);
                zonesListView.setAdapter(adapter);
                zonesListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                        Zone zoneAtPosition = (Zone) zonesListView.getItemAtPosition(position);
                        Intent measuresIntent = new Intent(ZonesActivity.this, AcquisitionsActivity.class);
                        measuresIntent.putExtra("ZONE", zoneAtPosition);
                        startActivity(measuresIntent);
                    }
                });

            } else {

                Toast toast = Toast.makeText(ZonesActivity.this,
                        "Something went wrong, try refreshing", Toast.LENGTH_LONG);
                toast.show();
            }

            response.close();
        }


    }


    private class sendZoneToServer extends AsyncTask<String, Void, Response> {

        @Override
        protected void onPreExecute() {

        }

        @Override
        protected Response doInBackground(String... zoneName) {
            ;
            MediaType JSON = MediaType.parse("application/json; charset=utf-8");

            Zone zone = new Zone(zoneName[0]);
            zone.setFacility_id(facility.getId());

            String zoneJSON = GsonUtil.getGson().toJson(zone);
            RequestBody zoneBody = RequestBody.create(JSON, zoneJSON);

            OkHttpClient client = new OkHttpClient();

            Request request = new Request.Builder()
                    .url(getResources().getString(R.string.new_zone_url))
                    .header("Content-Type", "application/json")
                    .header("X-User-Email", UserInfo.getUserEmail())
                    .header("X-User-Token", UserInfo.getUserToken())
                    .post(zoneBody)
                    .build();
            Response response = null;

            try {
                response = client.newCall(request).execute();
            } catch (IOException e) {
                e.printStackTrace();
            }

            return response;
        }

        protected void onPostExecute(Response response) {

            /* Default error message to be shown */
            String defaultErrorMessage = "Something went wrong, try refreshing";

            /* If, for some reason, the response is null (should not be) */
            if (response == null) {
                Toast toast = Toast.makeText(ZonesActivity.this,
                        defaultErrorMessage, Toast.LENGTH_LONG);
                toast.show();
                return;
            }

            /* In this case, server created the facility */
            else if (response.code() >= 200 && response.code() < 300) {
                new getZonesFromServer().execute();
            }

            /* Response not null, but server rejected */
            else {

                /* Show in toast the error from server */
                try {
                    defaultErrorMessage = response.body().string();
                } catch (IOException e) {
                    e.printStackTrace();
                }

                Toast toast = Toast.makeText(ZonesActivity.this,
                        defaultErrorMessage, Toast.LENGTH_LONG);
                toast.show();
            }

            response.close();
        }

    }

}

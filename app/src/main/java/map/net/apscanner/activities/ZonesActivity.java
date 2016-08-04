package map.net.apscanner.activities;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

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
import map.net.apscanner.classes.zone.Zone;
import map.net.apscanner.classes.zone.ZoneAdapter;
import map.net.apscanner.helpers.UserInfo;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class ZonesActivity extends AppCompatActivity {

    ListView zonesListView;

    Bundle extras;
    String facilityName;
    String facilityId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_zones);

        // Get data passed from Facility Activity
        extras = getIntent().getExtras();
        if (extras != null) {
            facilityName = extras.getString("FACILITY_NAME");
            facilityId = extras.getString("FACILITY_ID");
        }

        zonesListView = (ListView) findViewById(R.id.zonesListView);

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
                    .addQueryParameter("facility_id", facilityId)
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
                        Intent measuresIntent = new Intent(ZonesActivity.this, MeasuresActivity.class);
                        measuresIntent.putExtra("ZONE_ID", zoneAtPosition.getId());
                        measuresIntent.putExtra("ZONE_NAME", zoneAtPosition.getName());
                        startActivity(measuresIntent);
                    }
                });

            } else {

                Toast toast = Toast.makeText(ZonesActivity.this,
                        "Something went wrong, try refreshing", Toast.LENGTH_LONG);
                toast.show();
            }
        }


    }
}

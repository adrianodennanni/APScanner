package map.net.apscanner.activities;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import map.net.apscanner.helpers.UserInfo;

/**
 * This is the Launcher Activity. If the user has previously logged in, it will warp to the User's
 * Facilities Activity.
 * If the user is not logged in, the next activity will be LoginActivity
 */

public class StartActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        SharedPreferences prefs = this.getSharedPreferences("userInfo", Context.MODE_PRIVATE);
        Intent nextActivity;

        if (prefs.getBoolean("logged_in?", false)) {
            nextActivity = new Intent(StartActivity.this, FacilitiesActivity.class);

            UserInfo.setUserEmail(prefs.getString("X-User-Email", null));
            UserInfo.setUserToken(prefs.getString("X-User-Token", null));
        } else {
            nextActivity = new Intent(StartActivity.this, LoginActivity.class);
        }

        startActivity(nextActivity);
        finish();
    }
}

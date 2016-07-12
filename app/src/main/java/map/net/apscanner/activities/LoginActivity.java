package map.net.apscanner.activities;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import map.net.apscanner.R;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class LoginActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        final EditText loginEditText = (EditText) findViewById(R.id.editTextLogin);
        final EditText passwordEditText = (EditText) findViewById(R.id.editTextPassword);

        Button signInButton = (Button) findViewById(R.id.button);

        assert signInButton != null;
        signInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                assert loginEditText != null;
                String login = loginEditText.getText().toString();
                assert passwordEditText != null;
                String password = passwordEditText.getText().toString();

                new attemptLogin().execute(login, password);
            }
        });
    }

    private class attemptLogin extends AsyncTask<String, Void, Integer> {

        protected Integer doInBackground(String... params) {

            String login = params[0];
            String password = params[1];


            MediaType JSON = MediaType.parse("application/json; charset=utf-8");
            OkHttpClient client = new OkHttpClient();

            JSONObject jsonBodyParams = new JSONObject();
            JSONObject jsonBody = new JSONObject();


            try {
                jsonBodyParams.put("email", login);
                jsonBodyParams.put("password", password);
                jsonBody.put("user", jsonBodyParams.toString());
            } catch (JSONException e) {
                e.printStackTrace();
            }


            RequestBody loginBody = RequestBody.create(JSON, jsonBody.toString());
            Request request = new Request.Builder()
                    .url(getResources().getString(R.string.sign_in_url))
                    .post(loginBody)
                    .build();
            Response responses = null;

            try {
                responses = client.newCall(request).execute();
            } catch (IOException e) {
                e.printStackTrace();
            }

            assert responses != null;
            return responses.code();
        }


        protected void onPostExecute(Integer code) {
            if (code >= 200 && code < 300) {
                //alright sir, call next activity
            } else {

                Toast toast = Toast.makeText(LoginActivity.this, "Something went wrong", Toast.LENGTH_LONG);
                toast.show();
            }
        }
    }


}


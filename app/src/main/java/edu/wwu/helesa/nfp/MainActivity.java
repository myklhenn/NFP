package edu.wwu.helesa.nfp;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Pair;
import android.view.View;
import android.widget.TextView;

import com.spotify.sdk.android.authentication.AuthenticationClient;
import com.spotify.sdk.android.authentication.AuthenticationRequest;
import com.spotify.sdk.android.authentication.AuthenticationResponse;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import org.json.*;

public class MainActivity extends AppCompatActivity {

    private SpotifyManager spotify = new SpotifyManager(this);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        spotify = new SpotifyManager(this);

        // TODO: check if NFC adapter exists here, and display an error if not found

        final AuthenticationRequest request = spotify.getAuthenticationRequest(AuthenticationResponse.Type.TOKEN);
        AuthenticationClient.openLoginActivity(this, SpotifyManager.getAuthTokenRequestCode(), request);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        final AuthenticationResponse response = AuthenticationClient.getResponse(resultCode, data);

        if (SpotifyManager.getAuthTokenRequestCode() == requestCode) {
            String accessToken = response.getAccessToken();
            if (accessToken == null) {

                // TODO: error handling

            } else {
                SpotifyManager.setAccessToken(response.getAccessToken());
                startActivity(new Intent(MainActivity.this, GuestActivity.class));
            }
        }
    }

    @Override
    protected void onDestroy() {
        spotify.cancelCall();
        super.onDestroy();
    }
}
package edu.wwu.helesa.nfp;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.spotify.sdk.android.authentication.AuthenticationClient;
import com.spotify.sdk.android.authentication.AuthenticationRequest;
import com.spotify.sdk.android.authentication.AuthenticationResponse;

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
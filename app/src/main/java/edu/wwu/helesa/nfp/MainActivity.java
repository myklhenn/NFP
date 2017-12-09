package edu.wwu.helesa.nfp;

import android.content.Intent;
import android.nfc.NfcAdapter;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.spotify.sdk.android.authentication.AuthenticationClient;
import com.spotify.sdk.android.authentication.AuthenticationRequest;
import com.spotify.sdk.android.authentication.AuthenticationResponse;

public class MainActivity extends AppCompatActivity {

    private SpotifyManager spotify = new SpotifyManager(this);
    private RelativeLayout loginMessageContainer;
    private TextView loginMessage;
    private TextView loginSubMessage;

    /* Create SpotifyManager object which logs into Spotify and allows all Spotify Connections
     * Checks if NFC adapter is available */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        spotify = new SpotifyManager(this);
        loginMessageContainer = (RelativeLayout) findViewById(R.id.login_message_container);
        loginMessage = (TextView) findViewById(R.id.login_message);
        loginSubMessage = (TextView) findViewById(R.id.login_sub_message);

        if (NfcAdapter.getDefaultAdapter(this) == null) {
            loginMessageContainer.setBackgroundColor(getResources().getColor(R.color.colorErrorBackground));
            loginMessage.setTextAppearance(this, R.style.LoginMessageErrorText);
            loginMessage.setText(R.string.nfc_required_message);
            loginSubMessage.setVisibility(View.VISIBLE);
            loginSubMessage.setTextAppearance(this, R.style.LoginSubMessageErrorText);
            loginSubMessage.setText(R.string.nfc_required_sub_message);
        }
        else {
            final AuthenticationRequest request = spotify.getAuthenticationRequest(AuthenticationResponse.Type.TOKEN);
            AuthenticationClient.openLoginActivity(this, SpotifyManager.getAuthTokenRequestCode(), request);
        }
    }

    /* Checks and sets access token to Spotify's API */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        final AuthenticationResponse response = AuthenticationClient.getResponse(resultCode, data);

        if (SpotifyManager.getAuthTokenRequestCode() == requestCode) {
            String accessToken = response.getAccessToken();
            if (accessToken == null) {
                loginMessageContainer.setBackgroundColor(getResources().getColor(R.color.colorErrorBackground));
                loginMessage.setTextAppearance(this, R.style.LoginMessageErrorText);
                loginMessage.setText(R.string.login_error_message);
            } else {
                SpotifyManager.setAccessToken(response.getAccessToken());
                finish();
                startActivity(new Intent(MainActivity.this, GuestActivity.class));
            }
        }
    }

    /* Destroy */
    @Override
    protected void onDestroy() {
        spotify.cancelCall();
        super.onDestroy();
    }
}
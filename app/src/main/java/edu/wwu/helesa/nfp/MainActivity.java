package edu.wwu.helesa.nfp;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
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
import java.util.List;
import java.util.Locale;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import org.json.*;
import java.net.*;


public class MainActivity extends AppCompatActivity {

    public static final String CLIENT_ID = "089d841ccc194c10a77afad9e1c11d54";
    public static final String REDIRECT_URI = "testschema://callback";
    public static final int AUTH_TOKEN_REQUEST_CODE = 0x10;
    public static final int AUTH_CODE_REQUEST_CODE = 0x11;

    private final OkHttpClient mOkHttpClient = new OkHttpClient();
    private String mAccessToken;
    private String mAccessCode;
    private Call mCall;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getSupportActionBar().setTitle(String.format(
                Locale.US, "Spotify Auth Sample %s", com.spotify.sdk.android.authentication.BuildConfig.VERSION_NAME));
    }

    @Override
    protected void onDestroy() {
        cancelCall();
        super.onDestroy();
    }

    public void onGetUserProfileClicked(View view) {
        if (mAccessToken == null) {
            final Snackbar snackbar = Snackbar.make(findViewById(R.id.activity_main), R.string.warning_need_token, Snackbar.LENGTH_SHORT);
            snackbar.getView().setBackgroundColor(ContextCompat.getColor(this, R.color.colorAccent));
            snackbar.show();
            return;
        }

        final Request request = new Request.Builder()
                .url("https://api.spotify.com/v1/me")
                .addHeader("Authorization","Bearer " + mAccessToken)
                .build();

        cancelCall();
        mCall = mOkHttpClient.newCall(request);
        final List<String>  list = new ArrayList<String>();

        mCall.enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                setResponse("Failed to fetch data: " + e);
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                try {
                    final JSONObject jsonObject = new JSONObject(response.body().string());

//                    String objString = jsonObject.toString();
                    String getId = jsonObject.getString("id");
                    Log.w("MainActivity" , getId);
                    Log.w("MainActivity" , mAccessToken);

                    setResponse(jsonObject.toString(3));
                } catch (JSONException e) {
                    setResponse("Failed to parse data: " + e);
                }
            }
        });
        testCallRequest();
    }

    public void testCallRequest(){

        final Request request = new Request.Builder()
                .url("https://api.spotify.com/v1/search?q=time+of+your+life&type=track")
                .addHeader("Accept","Bearer application/json")
                .addHeader("Authorization","Bearer " + mAccessToken)
                .build();

        cancelCall();
        mCall = mOkHttpClient.newCall(request);
        final List<String>  list = new ArrayList<String>();

        mCall.enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                setResponse("Failed to fetch data: " + e);
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                try {
                    final JSONObject jsonObject = new JSONObject(response.body().string());

                    JSONArray tracks = jsonObject.getJSONObject("tracks").getJSONArray("items");

                    ArrayList<Track> songs = new ArrayList<>();

                    int trackCount = tracks.length();
                    for (int i = 0; i < trackCount; i++) {
                        // Get current track object
                        JSONObject track = tracks.getJSONObject(i);

                        // Get track name
                        String trackName = track.getString("name");

                        // Get names of artists
                        ArrayList<String> artists = getArtistNames(track.getJSONArray("artists"));

                        // Get album object
                        JSONObject album = track.getJSONObject("album");

                        // Get album name
                        String albumName = album.getString("name");

                        // Get album art
                        JSONArray artworkArray = album.getJSONArray("images");
                        HashMap<Integer, String> artwork = getAlbumArtwork(artworkArray);

                        // Get uri
                        String uri = track.getString("uri");


                        songs.add(new Track(trackName, albumName, artists, artwork, uri));
                    }
                    setResponse(songs.toString());
                } catch (JSONException e) {
                    setResponse("Failed to parse data: " + e);
                }
            }
        });
    }

    public HashMap<Integer, String> getAlbumArtwork (JSONArray artworkArrary) {
        /* Takes a JSONArray and returns all the String contained by the key in each
         * item in the JSONArray
         */
        int len = artworkArrary.length();
        HashMap<Integer, String> artworkMap = new HashMap<>();
        try {
            for (int i = 0; i < len; i++) {
                JSONObject item = artworkArrary.getJSONObject(i);
                artworkMap.put(item.getInt("height"), item.getString("url"));
            }
        } catch (Exception e) {
            setResponse("Failed to parse data: " + e);
        }

        return artworkMap;
    }

    public ArrayList<String> getArtistNames(JSONArray artists) {
        int artistCount = artists.length();
        ArrayList<String> artistNames = new ArrayList<>();
        try {
            for (int i = 0; i < artistCount; i++) {
                JSONObject artist = artists.getJSONObject(i);
                artistNames.add(artist.getString("name"));
            }
        } catch (Exception e) {
            setResponse("Failed to parse data: " + e);
        }

        return artistNames;
    }

    public void onRequestCodeClicked(View view) {
        final AuthenticationRequest request = getAuthenticationRequest(AuthenticationResponse.Type.CODE);
        AuthenticationClient.openLoginActivity(this, AUTH_CODE_REQUEST_CODE, request);
    }

    public void onRequestTokenClicked(View view) {
        final AuthenticationRequest request = getAuthenticationRequest(AuthenticationResponse.Type.TOKEN);
        AuthenticationClient.openLoginActivity(this, AUTH_TOKEN_REQUEST_CODE, request);
    }

    private AuthenticationRequest getAuthenticationRequest(AuthenticationResponse.Type type) {
        return new AuthenticationRequest.Builder(CLIENT_ID, type, getRedirectUri().toString())
                .setShowDialog(false)
                .setScopes(new String[]{"user-read-email"})
                .setCampaign("your-campaign-token")
                .build();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        final AuthenticationResponse response = AuthenticationClient.getResponse(resultCode, data);

        if (AUTH_TOKEN_REQUEST_CODE == requestCode) {
            mAccessToken = response.getAccessToken();
            updateTokenView();
        } else if (AUTH_CODE_REQUEST_CODE == requestCode) {
            mAccessCode = response.getCode();
            updateCodeView();
        }
    }

    private void setResponse(final String text) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                final TextView responseView = (TextView) findViewById(R.id.response_text_view);
                responseView.setText(text);
            }
        });
    }


    //to remove "Access token: remove this update feature and just use the mAccessToken

    private void updateTokenView() {
        final TextView tokenView = (TextView) findViewById(R.id.token_text_view);
        tokenView.setText(getString(R.string.token, mAccessToken));
    }

    private void updateCodeView() {
        final TextView codeView = (TextView) findViewById(R.id.code_text_view);
        codeView.setText(getString(R.string.code, mAccessCode));
    }

    private void cancelCall() {
        if (mCall != null) {
            mCall.cancel();
        }
    }

    private Uri getRedirectUri() {
        return new Uri.Builder()
                .scheme(getString(R.string.com_spotify_sdk_redirect_scheme))
                .authority(getString(R.string.com_spotify_sdk_redirect_host))
                .build();
    }




    private void testAPI(){
        String hello = "";

        try {
            JSONObject jObj = new JSONObject(hello);
        } catch (JSONException e) {
            Log.w("Main","broke here");
        }
    }
}
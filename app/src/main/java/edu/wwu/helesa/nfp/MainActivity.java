package edu.wwu.helesa.nfp;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Pair;
import android.view.View;
import android.widget.TextView;

import com.spotify.sdk.android.authentication.AuthenticationClient;
import com.spotify.sdk.android.authentication.AuthenticationRequest;
import com.spotify.sdk.android.authentication.AuthenticationResponse;

import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Text;

import java.io.IOException;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Locale;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;


public class MainActivity extends AppCompatActivity {

    SpotifyManager spotify = new SpotifyManager(this);

    private String userId;
    private String playlistId;
    private String trackId = "spotify:track:3ksI6G962wZAVIteYw74H4";
    private static final String PLAYLIST_NAME = "new guys play list";
    private ArrayList<Track> tracks = new ArrayList<>();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getSupportActionBar().setTitle(String.format(
                Locale.US, "Spotify Auth Sample %s", com.spotify.sdk.android.authentication.BuildConfig.VERSION_NAME));
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    public void onGetUserProfileClicked(View view) {
        if (spotify.getAccessToken() == null) {
            final Snackbar snackbar = Snackbar.make(findViewById(R.id.activity_main), R.string.warning_need_token, Snackbar.LENGTH_SHORT);
            snackbar.getView().setBackgroundColor(ContextCompat.getColor(this, R.color.colorAccent));
            snackbar.show();
            return;
        }

        ArrayList<Pair<String, String>> headers = new ArrayList();
        headers.add(new Pair<String, String>("Authorization", "Bearer " + spotify.getAccessToken()));

//        k_addTrackToPlaylist();
        k_makeSearchRequest();
    }

    public String getSearchValue() {
        // get text from search bar
        String value = "pokemon";
        return TextUtils.htmlEncode(value);
    }

    public void k_makeSearchRequest() {
        ArrayList<Pair<String, String>> headers = new ArrayList<>();
        headers.add(new Pair<>("Authorization", "Bearer " + spotify.getAccessToken()));

        String searchValue = getSearchValue();

        spotify.buildRequest("search?q=" + searchValue + "&type=track", headers, null);
        spotify.cancelCall();
        spotify.createCallFromRequest();

        spotify.getCall().enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                spotify.setResponseJson(null);
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                try {
                    spotify.setResponseJson(new JSONObject(response.body().string()));
                    tracks =  spotify.getTracksFromJSON();
                    setResponse(R.id.response_text_view, tracks.toString());

                } catch (JSONException e) {
                    spotify.setResponseJson(null);
                }
            }
        });
    }


    public void k_makeIdRequest() {
        ArrayList<Pair<String, String>> headers = new ArrayList<>();
        headers.add(new Pair<>("Authorization", "Bearer " + spotify.getAccessToken()));

        spotify.buildRequest("me", headers, null);
        spotify.cancelCall();
        spotify.createCallFromRequest();

        spotify.getCall().enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                spotify.setResponseJson(null);
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                try {
                    spotify.setResponseJson(new JSONObject(response.body().string()));
                    String id = spotify.getUserIdFromJSON();
                    userId = id;
                } catch (JSONException e) {
                    spotify.setResponseJson(null);
                }
            }
        });
    }

    public void k_getOrMakePlaylistId() {
        ArrayList<Pair<String, String>> headers = new ArrayList<>();
        headers.add(new Pair<>("Authorization", "Bearer " + spotify.getAccessToken()));
        headers.add(new Pair<>("Accept", "application/json"));
        String urlOptions = "me/playlists?limit=50";

        spotify.buildRequest(urlOptions, headers, null);
        spotify.cancelCall();
        spotify.createCallFromRequest();

        spotify.getCall().enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                spotify.setResponseJson(null);
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                try {
                    spotify.setResponseJson(new JSONObject(response.body().string()));
                    playlistId = spotify.getPlaylistIdFromJSON(PLAYLIST_NAME);
                    setResponse(R.id.code_text_view, playlistId);
                    if (playlistId == null) {
                        // Create playlist
                        k_makePlaylist();
                    }
                } catch (JSONException e) {
                    spotify.setResponseJson(null);
                }
            }
        });
    }


    public void k_addTrackToPlaylist() {

        String urlOptions = "users/" + userId +"/playlists/" +
                playlistId + "/tracks?uris=" + TextUtils.htmlEncode(trackId);

        ArrayList<Pair<String, String>> headers = new ArrayList<>();
        headers.add(new Pair<>("Accept", "application/json"));
        headers.add(new Pair<>("Authorization", "Bearer " + spotify.getAccessToken()));

        JSONObject body = new JSONObject();

        spotify.buildRequest(urlOptions, headers, body);

        spotify.cancelCall();
        spotify.createCallFromRequest();

        spotify.getCall().enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                spotify.setResponseJson(null);
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                try {
                    spotify.setResponseJson(new JSONObject(response.body().string()));

                    setResponse(R.id.response_text_view, spotify.getResponseJson().getString("snapshot_id"));


                } catch (JSONException e) {
                    spotify.setResponseJson(null);
                }
            }
        });
    }

    public void k_makePlaylist() {
        String urlOptions = "users/" + userId + "/playlists";
        ArrayList<Pair<String, String>> headers = new ArrayList<>();
        headers.add(new Pair<>("Accept", "application/json"));
        headers.add(new Pair<>("Authorization", "Bearer " + spotify.getAccessToken()));

        JSONObject body = new JSONObject();
        try {
            body.put("name", PLAYLIST_NAME);
            body.put("public", true);
            body.put("description", "New playlist for NFP");
        } catch (JSONException e) {
            e.printStackTrace();
        }

        spotify.buildRequest(urlOptions, headers, body);

        spotify.cancelCall();
        spotify.createCallFromRequest();

        spotify.getCall().enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                spotify.setResponseJson(null);
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                try {
                    spotify.setResponseJson(new JSONObject(response.body().string()));
                    playlistId = spotify.getUserIdFromJSON();
                    setResponse(R.id.code_text_view, playlistId);


                } catch (JSONException e) {
                    spotify.setResponseJson(null);
                }
            }
        });
    }

    public void onRequestCodeClicked(View view) {
        final AuthenticationRequest request = spotify.getAuthenticationRequest(AuthenticationResponse.Type.CODE);
        AuthenticationClient.openLoginActivity(this, spotify.AUTH_CODE_REQUEST_CODE, request);
    }

    public void onRequestTokenClicked(View view) {
        final AuthenticationRequest request = spotify.getAuthenticationRequest(AuthenticationResponse.Type.TOKEN);
        AuthenticationClient.openLoginActivity(this, spotify.AUTH_TOKEN_REQUEST_CODE, request);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        final AuthenticationResponse response = AuthenticationClient.getResponse(resultCode, data);

        if (spotify.AUTH_TOKEN_REQUEST_CODE == requestCode) {
            spotify.setAccessToken(response.getAccessToken());
            updateTokenView();
            k_makeIdRequest();
        } else if (spotify.AUTH_CODE_REQUEST_CODE == requestCode) {
            spotify.setAccessCode(response.getCode());
            updateCodeView();
            k_getOrMakePlaylistId();
        }
    }

    private void setResponse(final int viewId, final String text) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                final TextView responseView = (TextView) findViewById(viewId);
                responseView.setText(text);
            }
        });
    }

    private String getResponse(int viewId) {
        TextView v = (TextView) findViewById(viewId);
        return v.getText().toString();
    }

    //to remove "Access token: remove this update feature and just use the mAccessToken

    private void updateTokenView() {
        final TextView tokenView = (TextView) findViewById(R.id.token_text_view);
        tokenView.setText(getString(R.string.token, spotify.getAccessToken()));
    }

    private void updateCodeView() {
        final TextView codeView = (TextView) findViewById(R.id.code_text_view);
        codeView.setText(getString(R.string.code, spotify.getAccessCode()));
    }
}
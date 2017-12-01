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

    SpotifyManager spotify = new SpotifyManager(this);
    private static final OkHttpClient mOkHttpClient = new OkHttpClient();
    private String userId;
    private String playlistId;


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

        // final Request request = spotify.buildRequest("me", headers, null);

        // testSearchRequest();
        // testIdRequest();
        // testAddPlaylistRequest();
        testAddSongRequest();

    }

    public void testAddSongRequest() {
        String url = "users/" + userId +"/playlists/" +
                "49kn5xb952gWU7pV1oUpLu" + "/tracks?uris=spotify%3Atrack%3A3ksI6G962wZAVIteYw74H4";



        // url = "users/cecil1402/playlists/49kn5xb952gWU7pV1oUpLu/tracks?uris=spotify%3Atrack%3A3ksI6G962wZAVIteYw74H4";

        ArrayList<Pair<String, String>> headers = new ArrayList<>();
        headers.add(new Pair<>("Accept", "application/json"));
        headers.add(new Pair<>("Authorization", "Bearer " + spotify.getAccessToken()));

        JSONObject body = new JSONObject();

        Request request = spotify.buildRequest(url, headers, body);

        makeAddSongRequestCall(request);
    }

    public void makeAddSongRequestCall(Request request) {
        cancelCall();
        spotify.setCall(mOkHttpClient.newCall(request));

        spotify.getCall().enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                spotify.setResponseJson(null);
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                try {
                    spotify.setResponseJson(new JSONObject(response.body().string()));

                    setResponse(spotify.getResponseJson().toString());


                } catch (JSONException e) {
                    spotify.setResponseJson(null);
                }
            }
        });

    }

    public void testAddPlaylistRequest() {
        testIdRequest();
        String id;
        try {
            id = spotify.getResponseJson().getString("id");
        } catch (JSONException e) {
            e.printStackTrace();
        }


        ArrayList<Pair<String, String>> headers = new ArrayList<>();
        headers.add(new Pair<>("Accept", "Bearer application/json"));
        headers.add(new Pair<>("Authorization", "Bearer " + spotify.getAccessToken()));

        JSONObject body = new JSONObject();
        try {
            body.put("name", "NEW PLAYLIST 45");
            body.put("public", false);
            body.put("description", "this is a description");
        } catch (JSONException e) {
            e.printStackTrace();
        }


        Request request = spotify.buildRequest("users/" + userId +"/playlists", headers, body);


        makeAddPlaylistCall(request);
    }
    public void makeAddPlaylistCall(Request request){
        cancelCall();
        spotify.setCall(mOkHttpClient.newCall(request));

        spotify.getCall().enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                spotify.setResponseJson(null);
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                try {
                    spotify.setResponseJson(new JSONObject(response.body().string()));

                    setResponse(spotify.getResponseJson().toString());


                } catch (JSONException e) {
                    spotify.setResponseJson(null);
                }
            }
        });

    }


    public void testPlaylistUriRequest() {
        testIdRequest();
        String id;
        try {
            id = spotify.getResponseJson().getString("id");
        } catch (JSONException e) {
            e.printStackTrace();
        }

        ArrayList<Pair<String, String>> headers = new ArrayList<>();
        headers.add(new Pair<>("Accept", "Bearer application/json"));
        headers.add(new Pair<>("Authorization", "Bearer " + spotify.getAccessToken()));

        Request request = spotify.buildRequest("users/" + userId+"/playlists", headers, null);



        makePlaylistCall(request);
    }
    public void makePlaylistCall(Request request){
        cancelCall();
        spotify.setCall(mOkHttpClient.newCall(request));

        spotify.getCall().enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                spotify.setResponseJson(null);
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                try {
                    spotify.setResponseJson(new JSONObject(response.body().string()));
                    JSONArray arrayOfPlaylists = spotify.getResponseJson().getJSONArray("items");
                     playlistId = arrayOfPlaylists.getJSONObject(0).getString("id");
                    setResponse(playlistId);
                    testAddPlaylistRequest();



                } catch (JSONException e) {
                    spotify.setResponseJson(null);
                }
            }
        });

    }

    public void testSearchRequest(){
        ArrayList<Pair<String, String>> headers = new ArrayList<>();
        headers.add(new Pair<>("Accept", "Bearer application/json"));
        headers.add(new Pair<>("Authorization", "Bearer " + spotify.getAccessToken()));

        Request request = spotify.buildRequest("search?q=time+of+your+life&type=track", headers, null);

        makeSearchCall(request);
    }

    public void testIdRequest() {
        ArrayList<Pair<String, String>> headers = new ArrayList<>();
        headers.add(new Pair<>("Authorization", "Bearer " + spotify.getAccessToken()));
        Request request = spotify.buildRequest("me", headers, null);
        makeIdCall(request);
    }

    private void cancelCall() {
        if (spotify.getCall() != null) {
            spotify.getCall().cancel();
        }
    }

    public void makeIdCall(Request request) {
        cancelCall();
        spotify.setCall(mOkHttpClient.newCall(request));

        spotify.getCall().enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                spotify.setResponseJson(null);
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                try {
                    spotify.setResponseJson(new JSONObject(response.body().string()));
                    String id = spotify.getResponseJson().getString("id");
                    userId = id;
                    setResponse(id.toString());
                    testPlaylistUriRequest();


                } catch (JSONException e) {
                    spotify.setResponseJson(null);
                }
            }
        });
    }

    /* Makes a call with the given request  */
    public void makeSearchCall(Request request) {
        cancelCall();
        spotify.setCall(mOkHttpClient.newCall(request));

        spotify.getCall().enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                spotify.setResponseJson(null);
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                try {
                    spotify.setResponseJson(new JSONObject(response.body().string()));
                    ArrayList<Track> tracks = spotify.getTracks();
                    setResponse(tracks.toString());

                } catch (JSONException e) {
                    spotify.setResponseJson(null);
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
        } else if (spotify.AUTH_CODE_REQUEST_CODE == requestCode) {
            spotify.setAccessCode(response.getCode());
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
        tokenView.setText(getString(R.string.token, spotify.getAccessToken()));
    }

    private void updateCodeView() {
        final TextView codeView = (TextView) findViewById(R.id.code_text_view);
        codeView.setText(getString(R.string.code, spotify.getAccessCode()));
    }
}
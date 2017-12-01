package edu.wwu.helesa.nfp;

import android.content.Intent;
import android.nfc.NdefMessage;
import android.nfc.NfcAdapter;
import android.os.Parcelable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Pair;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class HostActivity extends AppCompatActivity {
    private SpotifyManager spotify = new SpotifyManager(this);
    private NfcAdapter nfcAdapter;
    private String userId;
    private String playlistId;
    private String trackId;
    private static final String PLAYLIST_NAME = "NFP_Playlist";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_host);

        // TODO: this may not be needed on this activity
        nfcAdapter = NfcAdapter.getDefaultAdapter(this);
        if (nfcAdapter == null) {
            Toast.makeText(this, "NFC is not available", Toast.LENGTH_LONG).show();
        }

        k_getIdAndPlaylist();

        handleNfcIntent(getIntent());
    }

    private void handleNfcIntent(Intent NfcIntent) {
        if (NfcAdapter.ACTION_NDEF_DISCOVERED.equals(NfcIntent.getAction())) {
            Parcelable[] rawMsgs =
                    NfcIntent.getParcelableArrayExtra(NfcAdapter.EXTRA_NDEF_MESSAGES);

            if (rawMsgs != null) {
                NdefMessage msg = (NdefMessage) rawMsgs[0];
                trackId = new String(msg.getRecords()[0].getPayload());
                Toast.makeText(this, "Received Song URI", Toast.LENGTH_LONG).show();
                k_addTrackToPlaylist();
            }
            else {
                Toast.makeText(this, "Received Blank Parcel", Toast.LENGTH_LONG).show();
            }
        }
    }

    @Override
    public void onNewIntent(Intent intent) {
        handleNfcIntent(intent);
    }

    @Override
    protected void onResume() {
        super.onResume();
        handleNfcIntent(getIntent());
    }

    public void k_getIdAndPlaylist() {
        ArrayList<Pair<String, String>> headers = new ArrayList<>();
        headers.add(new Pair<>("Authorization", "Bearer " + SpotifyManager.getAccessToken()));

        spotify.buildRequest("me", headers, null);
        spotify.cancelCall();
        SpotifyManager.createCallFromRequest();

        SpotifyManager.getCall().enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                SpotifyManager.setResponseJson(null);
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                try {
                    SpotifyManager.setResponseJson(new JSONObject(response.body().string()));
                    String id = spotify.getUserIdFromJSON();
                    userId = id;
                    k_getOrMakePlaylistId();
                } catch (JSONException e) {
                    SpotifyManager.setResponseJson(null);
                }
            }
        });
    }

    public void k_getOrMakePlaylistId() {
        ArrayList<Pair<String, String>> headers = new ArrayList<>();
        headers.add(new Pair<>("Authorization", "Bearer " + SpotifyManager.getAccessToken()));
        headers.add(new Pair<>("Accept", "application/json"));
        String urlOptions = "me/playlists?limit=50";

        spotify.buildRequest(urlOptions, headers, null);
        spotify.cancelCall();
        SpotifyManager.createCallFromRequest();

        SpotifyManager.getCall().enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                SpotifyManager.setResponseJson(null);
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                try {
                    SpotifyManager.setResponseJson(new JSONObject(response.body().string()));
                    playlistId = spotify.getPlaylistIdFromJSON(PLAYLIST_NAME);
                    if (playlistId == null)
                        k_makePlaylist();
                } catch (JSONException e) {
                    SpotifyManager.setResponseJson(null);
                }
            }
        });
    }

    public void k_addTrackToPlaylist() {

        String urlOptions = "users/" + userId +"/playlists/" +
                playlistId + "/tracks?uris=" + TextUtils.htmlEncode(trackId);

        ArrayList<Pair<String, String>> headers = new ArrayList<>();
        headers.add(new Pair<>("Accept", "application/json"));
        headers.add(new Pair<>("Authorization", "Bearer " + SpotifyManager.getAccessToken()));

        JSONObject body = new JSONObject();

        spotify.buildRequest(urlOptions, headers, body);

        spotify.cancelCall();
        SpotifyManager.createCallFromRequest();

        SpotifyManager.getCall().enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                SpotifyManager.setResponseJson(null);
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                try {
                    SpotifyManager.setResponseJson(new JSONObject(response.body().string()));
                } catch (JSONException e) {
                    SpotifyManager.setResponseJson(null);
                }
            }
        });
    }

    public void k_makePlaylist() {
        String urlOptions = "users/" + userId + "/playlists";
        ArrayList<Pair<String, String>> headers = new ArrayList<>();
        headers.add(new Pair<>("Accept", "application/json"));
        headers.add(new Pair<>("Authorization", "Bearer " + SpotifyManager.getAccessToken()));

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
        SpotifyManager.createCallFromRequest();

        SpotifyManager.getCall().enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                SpotifyManager.setResponseJson(null);
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                try {
                    SpotifyManager.setResponseJson(new JSONObject(response.body().string()));
                    playlistId = spotify.getUserIdFromJSON();
                } catch (JSONException e) {
                    SpotifyManager.setResponseJson(null);
                }
            }
        });
    }
}

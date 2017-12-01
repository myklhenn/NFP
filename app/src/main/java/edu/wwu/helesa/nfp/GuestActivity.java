package edu.wwu.helesa.nfp;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.nfc.FormatException;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.nfc.NfcEvent;
import android.nfc.NfcManager;
import android.nfc.Tag;
import android.nfc.TagLostException;
import android.text.TextUtils;
import android.util.Log;
import android.util.Pair;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

import static android.nfc.NdefRecord.createMime;


public class GuestActivity extends AppCompatActivity implements NfcAdapter.OnNdefPushCompleteCallback,
        NfcAdapter.CreateNdefMessageCallback {
    public static boolean SEND_MODE_ACTIVE = false;
    private SpotifyManager spotify = new SpotifyManager(this);
    private GuestTrackListAdapter trackListAdapter;
    private SelectedTrackAreaViewHolder staHolder;
    private SearchView searchView;

    private RelativeLayout nfcActiveMessage;
    private FrameLayout trackListDimmer;

    private NfcAdapter nfcAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_guest);

        Log.e("Access Token", SpotifyManager.getAccessToken());

        // cache the views for the selected track area into the view holder
        this.staHolder = new SelectedTrackAreaViewHolder();
        this.staHolder.container = (RelativeLayout) findViewById(R.id.selected_track_container);
        this.staHolder.albumCover = (ImageView) findViewById(R.id.item_album_cover);
        this.staHolder.trackTitle = (TextView) findViewById(R.id.item_track_title);
        this.staHolder.artistAlbum = (TextView) findViewById(R.id.item_artist_album);
        this.staHolder.action = (Button) findViewById(R.id.track_area_action);
        this.staHolder.message = (TextView) findViewById(R.id.select_track_message);

        nfcActiveMessage = (RelativeLayout) findViewById(R.id.nfc_message_container);
        trackListDimmer = (FrameLayout) findViewById(R.id.track_list_dimmer);

        nfcAdapter = NfcAdapter.getDefaultAdapter(this);
        if (nfcAdapter == null) {
            Toast.makeText(this, "NFC is not available", Toast.LENGTH_LONG).show();
        }

        // populate the track list data structure with three placeholder tracks (FOR TESTING)
        ArrayList<Track> trackList = new ArrayList<Track>();
        trackList.add(new Track("Emora", "Between the Dots", new ArrayList<String>() {{ add("The Clonious"); }},
                new HashMap<Integer, String>(), ""));
        trackList.add(new Track("Dans", "Vlotjes", new ArrayList<String>() {{ add("Pomrad"); }},
                new HashMap<Integer, String>(), ""));
        trackList.add(new Track("Fort Teen", "A TreblO Beat Tape", new ArrayList<String>() {{ add("Dorian Concept"); }},
                new HashMap<Integer, String>(), ""));
        ListView trackListView = (ListView) findViewById(R.id.track_list);
        this.trackListAdapter = new GuestTrackListAdapter(this, trackListView, trackList, staHolder);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);

        searchView = (SearchView) menu.findItem(R.id.action_search).getActionView();
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                //k_makeSearchRequest();
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                k_makeSearchRequest();
                return true;
            }
        });

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_guest_host:
                this.startActivity(new Intent(this, HostActivity.class));
                break;
            default:
                return super.onOptionsItemSelected(item);
        }
        return true;
    }

    @Override
    public void onNdefPushComplete(NfcEvent event) {
        // this is called when the system detects that our NdefMessage was successfully sent
        //Toast.makeText(this, "Song Successfully Sent!", Toast.LENGTH_SHORT).show();

        // TODO: remove callbacks to "disable" NFC until "send" button is hit again

    }

    @Override
    public NdefMessage createNdefMessage(NfcEvent event) {
        // this will be called when another NFC capable device is detected
        String uri = trackListAdapter.getSelectedTrack().getUri();
        NdefMessage msg = new NdefMessage(new NdefRecord[] {
                //createMime("application/vnd.com.example.android.beam", uri.getBytes()),
                createMime("text/plain", uri.getBytes()),
                /*
                 * The Android Application Record (AAR) is commented out. When a device
                 * receives a push with an AAR in it, the application specified in the AAR
                 * is guaranteed to run. The AAR overrides the tag dispatch system.
                 * You can add it back in to guarantee that this
                 * activity starts when receiving a beamed message. For now, this code
                 * uses the tag dispatch system.
                 */
                 NdefRecord.createApplicationRecord(getPackageName())
        });
        return msg;
    }

    public void prepareNfcAdapter(View view) {
        if (!SEND_MODE_ACTIVE && nfcAdapter != null) {

            nfcAdapter.setNdefPushMessageCallback(this, this);
            nfcAdapter.setOnNdefPushCompleteCallback(this, this);

            this.staHolder.action.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(
                    this, R.color.colorWarningBackground)));
            this.staHolder.action.setTextAppearance(this, R.style.ButtonWarningText);
            this.staHolder.action.setText(R.string.cancel_button_text);
            nfcActiveMessage.setVisibility(View.VISIBLE);
            trackListDimmer.setVisibility(View.VISIBLE);

            SEND_MODE_ACTIVE = true;
        }
        else {

            // TODO: "deactivate" NFC

            this.staHolder.action.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(
                    this, R.color.colorSelectedBackground)));
            this.staHolder.action.setTextAppearance(this, R.style.ButtonSelectedText);
            this.staHolder.action.setText(R.string.send_button_text);
            nfcActiveMessage.setVisibility(View.GONE);
            trackListDimmer.setVisibility(View.GONE);

            SEND_MODE_ACTIVE = false;
        }
    }

    public String getSearchValue() {
        // get text from search bar
        String value = searchView.getQuery().toString();
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
                    updateTrackList();
                } catch (JSONException e) {
                    spotify.setResponseJson(null);
                }
            }
        });
    }

    public void updateTrackList() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                trackListAdapter.updateData(spotify.getTracksFromJSON());
            }
        });
    }

    public class SelectedTrackAreaViewHolder {
        RelativeLayout container;
        ImageView albumCover;
        TextView trackTitle;
        TextView artistAlbum;
        Button action;
        TextView message;
    }
}

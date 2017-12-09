package edu.wwu.helesa.nfp;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.nfc.NfcEvent;
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

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;

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
    private MenuItem actionSearch;
    private MenuItem actionHost;

    private TextView nfcMessageText;
    private FrameLayout trackListDimmer;

    private NfcAdapter nfcAdapter;

    /* Create an object to hold the views to keep things organized
     * Create ListViewAdapter to connect our track data with the ListView */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_guest);

        // cache the views for the selected track area into the view holder
        this.staHolder = new SelectedTrackAreaViewHolder();
        this.staHolder.nfcMessage = (RelativeLayout) findViewById(R.id.nfc_message_container);
        this.staHolder.container = (RelativeLayout) findViewById(R.id.selected_track_container);
        this.staHolder.albumCover = (ImageView) findViewById(R.id.item_album_cover);
        this.staHolder.trackTitle = (TextView) findViewById(R.id.item_track_title);
        this.staHolder.artistAlbum = (TextView) findViewById(R.id.item_artist_album);
        this.staHolder.action = (Button) findViewById(R.id.track_area_action);
        this.staHolder.message = (TextView) findViewById(R.id.select_track_message);

        nfcMessageText = (TextView) findViewById(R.id.nfc_message);
        trackListDimmer = (FrameLayout) findViewById(R.id.track_list_dimmer);

        nfcAdapter = NfcAdapter.getDefaultAdapter(this);

        ArrayList<Track> trackList = new ArrayList<Track>();
        ListView trackListView = (ListView) findViewById(R.id.track_list);
        this.trackListAdapter = new GuestTrackListAdapter(this, trackListView, trackList, staHolder);
    }

    /* Create search bar and host button menu items */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        actionSearch = (MenuItem) menu.findItem(R.id.action_search);
        actionHost = (MenuItem) menu.findItem(R.id.action_host);

        // prepare the Spotify SearchView
        searchView = (SearchView) actionSearch.getActionView();
        searchView.setQueryHint(getResources().getString(R.string.action_search_text));

        // color the components of the SearchView white
        View searchPlate = (View) searchView.findViewById(searchView.getContext()
                .getResources().getIdentifier("android:id/search_plate", null, null));
        searchPlate.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(
                this, R.color.colorForeground)));
        ImageView searchCloseIcon = (ImageView) searchView.findViewById(searchView.getContext()
                .getResources().getIdentifier("android:id/search_close_btn", null, null));
        searchCloseIcon.setImageTintList(ColorStateList.valueOf(ContextCompat.getColor(
                this, R.color.colorForeground)));

        // search Spotify with the current query string every time the text changes
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                // hide the keyboard when "enter" key is pressed
                clearSearchViewFocus();
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                // start searching Spotify as the user types
                makeSearchRequest();
                return true;
            }
        });

        return true;
    }

    /* Start HostActivity when the "HOST" menu item is selected */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_host:
                this.startActivity(new Intent(this, HostActivity.class));
                break;
            default:
                return super.onOptionsItemSelected(item);
        }
        return true;
    }

    /* This is called when the system detects that our NdefMessage was successfully sent */
    @Override
    public void onNdefPushComplete(NfcEvent event) {
        showNfcSuccessMessage();

        // "stop" NFC
        nfcAdapter.setNdefPushMessageCallback(null, this);
        nfcAdapter.setOnNdefPushCompleteCallback(null, this);

        SEND_MODE_ACTIVE = false;
    }

    /* This will be called when another NFC capable device is detected */
    @Override
    public NdefMessage createNdefMessage(NfcEvent event) {
        String uri = trackListAdapter.getSelectedTrack().getUri();
        NdefMessage msg = new NdefMessage(new NdefRecord[] {
                createMime("text/plain", uri.getBytes()),
                // When a device receives a push with an Android Application Record (AAR)
                // in it, the application specified in the AAR is guaranteed to run.
                // The AAR overrides the tag dispatch system.
                NdefRecord.createApplicationRecord(getPackageName())
        });
        return msg;
    }

    /* Activates the NFC radio. Called when "Send" or "Cancel" button is pressed */
    public void prepareNfcAdapter(View view) {
        if (nfcAdapter != null) {
            if (!SEND_MODE_ACTIVE) {
                // "Send" button clicked -- "start" NFC
                nfcAdapter.setNdefPushMessageCallback(this, this);
                nfcAdapter.setOnNdefPushCompleteCallback(this, this);

                // change button to orange "Cancel" button
                this.staHolder.action.setBackgroundTintList(ColorStateList.valueOf(
                        ContextCompat.getColor(this, R.color.colorWarningBackground)));
                this.staHolder.action.setTextAppearance(this, R.style.ButtonWarningText);
                this.staHolder.action.setText(R.string.cancel_button_text);

                // hide action bar items
                this.searchView.clearFocus();
                this.actionSearch.setVisible(false);
                this.actionHost.setVisible(false);

                showNfcActiveMessage();

                SEND_MODE_ACTIVE = true;
            } else {
                // "Cancel" button clicked -- "stop" NFC
                nfcAdapter.setNdefPushMessageCallback(null, this);
                nfcAdapter.setOnNdefPushCompleteCallback(null, this);

                // change button to green "Send" button
                this.staHolder.action.setBackgroundTintList(ColorStateList.valueOf(
                        ContextCompat.getColor(this, R.color.colorSelectedBackground)));
                this.staHolder.action.setTextAppearance(this, R.style.ButtonSelectedText);
                this.staHolder.action.setText(R.string.send_button_text);

                // show action bar items
                this.actionSearch.setVisible(true);
                this.actionHost.setVisible(true);

                // hide "NFC active" message and screen dimmer
                this.staHolder.nfcMessage.setVisibility(View.GONE);
                trackListDimmer.setVisibility(View.GONE);

                SEND_MODE_ACTIVE = false;
            }
        }
    }

    /* Show an orange message that dims the screen and explains how to send the selected song
     * (above selected track) */
    private void showNfcActiveMessage() {
        this.staHolder.nfcMessage.setBackgroundColor(getResources().getColor(
                R.color.colorWarningBackground));
        nfcMessageText.setTextAppearance(this, R.style.NfcMessageNormalText);
        nfcMessageText.setText(R.string.nfc_message_text);
        this.staHolder.nfcMessage.setVisibility(View.VISIBLE);
        trackListDimmer.setVisibility(View.VISIBLE);
    }

    /* Show a green "success" message above the selected track area (at bottom of screen) */
    private void showNfcSuccessMessage() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                staHolder.nfcMessage.setBackgroundColor(getResources().getColor(
                        R.color.colorSelectedBackground));
                nfcMessageText.setTextAppearance(getApplicationContext(), R.style.NfcMessageSuccessText);
                nfcMessageText.setText(R.string.nfc_success_text);
                staHolder.nfcMessage.setVisibility(View.VISIBLE);
                trackListDimmer.setVisibility(View.GONE);

                // clear selected track area (at bottom of screen)
                trackListAdapter.clearSelectedTrack();

                // show action bar items
                actionSearch.setVisible(true);
                actionHost.setVisible(true);

                // change button to green "Send" button
                staHolder.action.setBackgroundTintList(ColorStateList.valueOf(
                        ContextCompat.getColor(getApplicationContext(), R.color.colorSelectedBackground)));
                staHolder.action.setTextAppearance(getApplicationContext(), R.style.ButtonSelectedText);
                staHolder.action.setText(R.string.send_button_text);
            }
        });
    }

    /* Clear focus from the search bar, hiding the on-screen keyboard */
    public void clearSearchViewFocus() {
        searchView.clearFocus();
    }

    /* Get search query text from the search bar */
    public String getSearchValue() {
        String value = searchView.getQuery().toString();
        return TextUtils.htmlEncode(value);
    }

    /* Builds and calls request to get list of tracks that match a search parameter */
    public void makeSearchRequest() {
        ArrayList<Pair<String, String>> headers = new ArrayList<>();
        headers.add(new Pair<>("Authorization", "Bearer " + SpotifyManager.getAccessToken()));

        String searchValue = getSearchValue();

        spotify.buildRequest("search?q=" + searchValue + "&type=track", headers, null);
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
                    updateTrackList();
                } catch (JSONException e) {
                    SpotifyManager.setResponseJson(null);
                }
            }
        });
    }

    /* Update the ListView (via its Adapter) on the UI thread */
    public void updateTrackList() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                trackListAdapter.updateData(spotify.getTracksFromJSON());
            }
        });
    }

    public class SelectedTrackAreaViewHolder {
        RelativeLayout nfcMessage;
        RelativeLayout container;
        ImageView albumCover;
        TextView trackTitle;
        TextView artistAlbum;
        Button action;
        TextView message;
    }
}

package edu.wwu.helesa.nfp;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.util.Pair;

import com.spotify.sdk.android.authentication.AuthenticationRequest;
import com.spotify.sdk.android.authentication.AuthenticationResponse;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;

import okhttp3.Call;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;

public class SpotifyManager {
    private static Context context;

    private int imgSize = 300;

    private static final OkHttpClient mOkHttpClient = new OkHttpClient();

    public static MediaType JSON =  MediaType.parse("application/json; charset=utf-8");

    public static final String CLIENT_ID = "089d841ccc194c10a77afad9e1c11d54";
    public static final String REDIRECT_URI = "testschema://callback";
    public static final int AUTH_TOKEN_REQUEST_CODE = 0x10;
    public static final int AUTH_CODE_REQUEST_CODE = 0x11;

    public static final String spotifyUrl = "https://api.spotify.com/v1/";

    private static String accessToken;
    private static String accessCode;
    private static Request request;
    private static Call call;

    /* Stores the most recent request's response */
    private static JSONObject responseJson;

    public static Context getContext() {
        return context;
    }

    public static void setContext(Context context) {
        SpotifyManager.context = context;
    }

    public static MediaType getJSON() {
        return JSON;
    }

    public static void setJSON(MediaType JSON) {
        SpotifyManager.JSON = JSON;
    }

    public static String getClientId() {
        return CLIENT_ID;
    }

    public static int getAuthTokenRequestCode() {
        return AUTH_TOKEN_REQUEST_CODE;
    }

    public static int getAuthCodeRequestCode() {
        return AUTH_CODE_REQUEST_CODE;
    }

    public static String getSpotifyUrl() {
        return spotifyUrl;
    }

    public static JSONObject getResponseJson() {
        return responseJson;
    }

    public static void setResponseJson(JSONObject responseJson) {
        SpotifyManager.responseJson = responseJson;
    }

    public static String getAccessToken() {
        return accessToken;
    }

    public static void setAccessToken(String accessToken) {
        SpotifyManager.accessToken = accessToken;
    }

    public static String getAccessCode() {
        return accessCode;
    }

    public static void setAccessCode(String accessCode) {
        SpotifyManager.accessCode = accessCode;
    }

    public static Call getCall() {
        return call;
    }

    public static void createCallFromRequest() {
        call = mOkHttpClient.newCall(request);
    }

    public SpotifyManager (Context context) {
        this.context = context;
    }

    /* Cancels previous okhttp call, just in case you make a second call too quickly */
    public void cancelCall() {
        if (call != null) {
            call.cancel();
        }
    }

    /* Returns redirect uri from strings.xml */
    private Uri getRedirectUri() {
        return new Uri.Builder()
                .scheme(context.getString(R.string.com_spotify_sdk_redirect_scheme))
                .authority(context.getString(R.string.com_spotify_sdk_redirect_host))
                .build();
    }

    /* Builds request. Only supports GET request */
    public void buildRequest(String urlOptions, ArrayList<Pair<String, String>> headers, JSONObject postBodyJson) {
        Request.Builder requestBuilder = new Request.Builder();
        requestBuilder.url("https://api.spotify.com/v1/" + urlOptions);

        for (Pair header : headers) {
            requestBuilder.addHeader(header.first.toString(), header.second.toString());
        }

        if (postBodyJson != null) {
            requestBuilder.post(RequestBody.create(JSON, postBodyJson.toString()));
        }

        request = requestBuilder.build();
    }


    /* Builds an authentication request. */
    public AuthenticationRequest getAuthenticationRequest(AuthenticationResponse.Type type) {
        return new AuthenticationRequest.Builder(CLIENT_ID, type, getRedirectUri().toString())
                .setShowDialog(false)
                .setScopes(new String[]{"user-read-email playlist-modify-public playlist-modify-private"})
                .setCampaign("your-campaign-token")
                .build();
    }

    /* Gets and returns a HashMap of album artwork from a JSONArray of artwork objects.
     * The key to the map is an Integer, value is a String */
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
            return null;
        }

        return artworkMap;
    }

    /* Extracts an ArrayList of Tracks from this class' attribute responseJson */
    public ArrayList<Track> getTracksFromJSON() {
        ArrayList<Track> songs = new ArrayList<>();

        try {
            JSONArray tracks = responseJson.getJSONObject("tracks").getJSONArray("items");

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
                String artwork = getAlbumArtwork(artworkArray).get(imgSize);

                // Get uri
                String uri = track.getString("uri");

                songs.add(new Track(trackName, albumName, artists, artwork, uri));
            }

        } catch (JSONException e) {
            return null;
        }
        return songs;
    }

    /* Gets and returns an ArrayList of artist names from a JSONArray of artist objects*/
    private ArrayList<String> getArtistNames(JSONArray artists) {
        int artistCount = artists.length();
        ArrayList<String> artistNames = new ArrayList<>();
        try {
            for (int i = 0; i < artistCount; i++) {
                JSONObject artist = artists.getJSONObject(i);
                artistNames.add(artist.getString("name"));
            }
        } catch (Exception e) {
            return null;
        }

        return artistNames;
    }

    public String getUserIdFromJSON() {
        try {
            return responseJson.getString("id");
        } catch (JSONException e) {
            return null;
        }
    }

    public String getPlaylistIdFromJSON(String key) {
        try {
            JSONArray items = responseJson.getJSONArray("items");

            int playlistCount = items.length();
            for (int i = 0; i < playlistCount; i++) {
                JSONObject currentPlaylist = items.getJSONObject(i);
                String currentPlaylistName = currentPlaylist.getString("name");
                if (currentPlaylistName.equals(key)) {
                    return currentPlaylist.getString("id");
                }
            }
        } catch (JSONException e) {
            return null;
        }
        return null;
    }
}
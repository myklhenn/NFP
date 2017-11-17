package edu.wwu.helesa.nfp;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by sargenk2 on 11/15/17.
 */

public class Track {
    String name;
    String album;
    String uri;
    ArrayList artists;
    HashMap artwork;

    public Track (String name, String album, ArrayList<String> artists,
                  HashMap<Integer, String> artwork, String uri) {
        this.name = name;
        this.album = album;
        this.artists = artists;
        this.artwork = artwork;
        this.uri = uri;
    }

    @Override
    public String toString() {
        return "name = " + name + '\n' +
                "album = " + album + '\n' +
                "artists = " + artists.toString() + '\n' +
                "artworks = " + artwork.size() + '\n' +
                "uri = " + uri + "\n\n";
    }
}

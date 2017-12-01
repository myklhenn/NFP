package edu.wwu.helesa.nfp;

import java.util.ArrayList;
import java.util.HashMap;

public class Track {
    private String name;
    private String album;
    private String uri;
    private ArrayList artists;
    private HashMap artwork;

    public Track (String name, String album, ArrayList<String> artists,
                  HashMap<Integer, String> artwork, String uri) {
        this.name = name;
        this.album = album;
        this.artists = artists;
        this.artwork = artwork;
        this.uri = uri;
    }

    public String getName() {
        return name;
    }

    public String getAlbum() {
        return album;
    }

    public String getUri() {
        return uri;
    }

    public ArrayList getArtists() {
        return artists;
    }

    public HashMap getArtwork() {
        return artwork;
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
package edu.wwu.helesa.nfp;

import android.app.Activity;
import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Bitmap;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;

public class GuestTrackListAdapter extends ArrayAdapter<Track> {
    private final Activity context;
    private final ListView listView;
    private final ArrayList<Track> trackList;
    private final GuestActivity.SelectedTrackAreaViewHolder staHolder;
    private Track selectedTrack;

    public GuestTrackListAdapter(final Activity context, ListView view, ArrayList<Track> tracks,
                                 GuestActivity.SelectedTrackAreaViewHolder newStaHolder) {
        super(context, R.layout.item_track, tracks);
        this.context = context;
        this.listView = view;
        this.trackList = tracks;
        this.staHolder = newStaHolder;

        view.setAdapter(this);
        view.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                // hide NFC success message if it is still showing
                if (staHolder.nfcMessage.getVisibility() == View.VISIBLE)
                    staHolder.nfcMessage.setVisibility(View.GONE);

                if (!GuestActivity.SEND_MODE_ACTIVE) {
                    Track track = (Track) parent.getItemAtPosition(position);
                    if (selectedTrack != null && track.getUri().equals(selectedTrack.getUri())) {
                        // un-select the currently selected track
                        selectedTrack = null;
                        // hide selected track at bottom and display initial message
                        staHolder.container.setVisibility(View.INVISIBLE);
                        staHolder.message.setVisibility(View.VISIBLE);
                    }
                    else {
                        selectedTrack = track;
                        // show selected track at bottom and hide message
                        staHolder.message.setVisibility(View.INVISIBLE);
                        staHolder.container.setVisibility(View.VISIBLE);
                        // clear focus from search bar and hide the on-screen keyboard
                        ((GuestActivity) context).clearSearchViewFocus();
                    }
                    notifyDataSetChanged();
                }
            }
        });
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View view, @NonNull ViewGroup parent) {
        final TrackListItemViewHolder holder;

        if (view == null) {
            // inflate view for task item
            LayoutInflater inf = (LayoutInflater) this.context.getSystemService(
                    Context.LAYOUT_INFLATER_SERVICE);
            view = inf.inflate(R.layout.item_track, null);
            // cache view fields into the holder
            holder = new TrackListItemViewHolder();
            holder.container = (RelativeLayout) view.findViewById(R.id.item_container);
            holder.albumCover = (ImageView) view.findViewById(R.id.item_album_cover);
            holder.trackTitle = (TextView) view.findViewById(R.id.item_track_title);
            holder.artistAlbum = (TextView) view.findViewById(R.id.item_artist_album);
            holder.statusSymbol = (ImageView) view.findViewById(R.id.item_status_symbol);
            // associate the holder with the view for later lookup
            view.setTag(holder);
        } else {
            holder = (TrackListItemViewHolder) view.getTag();
        }

        Track track = this.trackList.get(position);

        // add track's title, artist(s) and album to their respective Views
        holder.trackTitle.setText(track.getName());
        holder.artistAlbum.setText(this.context.getResources().getString(
                R.string.artist_album_format,
                TextUtils.join(", ", track.getArtists()),
                track.getAlbum()
        ));

        // load album cover (or show placeholder image if not found) using Picasso library
        Picasso.with(context)
                .load(track.getArtwork())
                .placeholder(R.drawable.album_art_placeholder)
                .into(holder.albumCover);

        holder.statusSymbol.setImageResource(R.drawable.ic_check_black_30dp);
        holder.statusSymbol.setImageTintList(ColorStateList.valueOf(ContextCompat.getColor(
                context, R.color.colorSelectedForeground)));

        if (selectedTrack != null && track.getUri().equals(selectedTrack.getUri())) {
            // color this track as selected
            holder.container.setBackgroundColor(context.getResources().getColor(
                    R.color.colorSelectedBackground));
            holder.trackTitle.setTextAppearance(this.context, R.style.TrackTitleTextSelected);
            holder.artistAlbum.setTextAppearance(this.context, R.style.TrackArtistAlbumTextSelected);
            holder.statusSymbol.setVisibility(View.VISIBLE);

            // update info in selected track area
            this.staHolder.trackTitle.setText(holder.trackTitle.getText());
            this.staHolder.artistAlbum.setText(holder.artistAlbum.getText());
            Picasso.with(context)
                    .load(track.getArtwork())
                    .placeholder(R.drawable.album_art_placeholder)
                    .into(staHolder.albumCover);
        }
        else {
            // color this track normally
            holder.container.setBackgroundColor(context.getResources().getColor(
                    R.color.colorNormalBackground));
            holder.trackTitle.setTextAppearance(this.context, R.style.TrackTitleTextNormal);
            holder.artistAlbum.setTextAppearance(this.context, R.style.TrackArtistAlbumTextNormal);
            holder.statusSymbol.setVisibility(View.GONE);
        }

        return view;
    }

    @Nullable
    @Override
    public Track getItem(int position) {
        return this.trackList.get(position);
    }

    public ListView getListView() {
        return this.listView;
    }

    public void updateData(ArrayList<Track> newTrackList) {
        // force this Adapter to update its ListView by clearing its data structure before adding
        // new track items
        this.trackList.clear();
        if (newTrackList != null)
            this.trackList.addAll(newTrackList);
        this.notifyDataSetChanged();
    }

    public Track getSelectedTrack() {
        return this.selectedTrack;
    }

    public void clearSelectedTrack() {
        selectedTrack = null;
        // hide selected track at bottom and display initial message
        staHolder.container.setVisibility(View.INVISIBLE);
        staHolder.message.setVisibility(View.VISIBLE);
        notifyDataSetChanged();
    }

    public ArrayList<Track> getTrackList() {
        // return this.trackList;
        ArrayList<Track> tl = new ArrayList<>();
        tl.addAll(trackList);
        return tl;
    }

    private class TrackListItemViewHolder {
        RelativeLayout container;
        ImageView albumCover;
        TextView trackTitle;
        TextView artistAlbum;
        ImageView statusSymbol;
    }
}

package com.example.android.spotifystreamer;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import kaaes.spotify.webapi.android.SpotifyApi;
import kaaes.spotify.webapi.android.SpotifyService;
import kaaes.spotify.webapi.android.models.AlbumSimple;
import kaaes.spotify.webapi.android.models.Image;
import kaaes.spotify.webapi.android.models.Track;
import retrofit.RetrofitError;


/**
 * A placeholder fragment containing a simple view.
 */
public class ArtistActivityFragment extends Fragment {

    private final String LOG_TAG = ArtistActivityFragment.class.getSimpleName();
    private TrackAdapter tracksAdapter;
    private String spCountry = "us";
    private Map<String,Object> options = new HashMap<String,Object>();
    private List<CustomTrack> customTrackList;


    public ArtistActivityFragment() {
        options.put("country", spCountry);
    }

    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        if(savedInstanceState == null || !savedInstanceState.containsKey("tracks")) {
            customTrackList = new ArrayList<CustomTrack>();
        }
        else {
            customTrackList = savedInstanceState.getParcelableArrayList("tracks");
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putParcelableArrayList("tracks", (ArrayList<? extends Parcelable>) customTrackList);
        super.onSaveInstanceState(outState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        tracksAdapter = new TrackAdapter(
                getActivity(),
                R.layout.top_track,
                R.id.track_top_name,
                customTrackList
        );

        final View rootView = inflater.inflate(R.layout.fragment_artist, container, false);
        Intent intent = getActivity().getIntent();
        if (intent != null && intent.hasExtra(Intent.EXTRA_TEXT)) {
            String artistId = intent.getStringExtra(Intent.EXTRA_TEXT);
            GetTracksTask gtt = new GetTracksTask();
            gtt.execute(artistId);
        }

        ListView listView = (ListView) rootView.findViewById(R.id.listview_toptracks);
        listView.setAdapter(tracksAdapter);
        return  rootView;
    }


    public class GetTracksTask extends AsyncTask<String, Void, List<CustomTrack>> {
        @Override
        protected List<CustomTrack> doInBackground(String... params){
            if (params.length == 0) {
                return null;
            }
            List<CustomTrack> ctl = new ArrayList<CustomTrack>();
            try {
                Log.e(LOG_TAG, "Internet call");
                //Prepare Spotify API
                SpotifyApi spApi = new SpotifyApi();
                SpotifyService spotify = spApi.getService();
                //Get tracks from Spotify API
                List<Track> spTracks = spotify.getArtistTopTrack(params[0], options).tracks;
                //Convert tracks to CustomTracks

                for (Track t : spTracks) {
                    String trName = t.name;
                    AlbumSimple album = t.album;
                    String albName = album.name;
                    String albImgSm = "";
                    String albImgLg = "";
                    if (album.images.size() > 0) {
                        List<Image> albumCovers = album.images;
                        for (Image ac : albumCovers) {
                            if (ac.height == 200) {
                                albImgSm = ac.url;
                            } else if (ac.height == 640) {
                                albImgLg = ac.url;
                            }
                        }
                        if (albImgSm.equals("")) {
                            albImgSm = albumCovers.get(0).url;
                        }
                        if (albImgLg.equals("")) {
                            albImgLg = albumCovers.get(0).url;
                        }
                    } else {
                        albImgSm = "http://contactyellowpages.com/images/no_image.jpg";
                        albImgLg = "http://contactyellowpages.com/images/no_image.jpg";
                    }
                    String prvURL = t.preview_url;
                    CustomTrack ct = new CustomTrack(trName, albName, albImgSm, albImgLg, prvURL);
                    ctl.add(ct);
                }
            }
            catch (RetrofitError | NullPointerException err){
                Log.e(LOG_TAG, String.valueOf(err));
            }

            return ctl;
        }

        @Override
        protected void onPostExecute(List<CustomTrack> result) {
            if (result != null) {
                customTrackList = result;
                tracksAdapter.clear();
                if (result.size() > 0) {
                    tracksAdapter.addAll(result);
                }
                else{
                    Context context = getActivity();
                    int duration = Toast.LENGTH_SHORT;
                    CharSequence text = "There are no tracks for this artist.";
                    Toast toast = Toast.makeText(context, text, duration);
                    toast.show();
                }
            }
        }
    }

    private class TrackResult {
        public TextView trackName;
        public TextView albumName;
        public ImageView albumImg;
    }

    private class CustomTrack implements Parcelable{
        public String trackName, albumName, albumImgSm, albumImgLg, previewURL;

        public CustomTrack(String trackName, String albumName, String albumImgSm, String albumImgLg, String previewURL){
            this.trackName = trackName;
            this.albumName = albumName;
            this.albumImgSm = albumImgSm;
            this.albumImgLg = albumImgLg;
            this.previewURL = previewURL;
        }

        private CustomTrack(Parcel in){
            trackName = in.readString();
            albumName = in.readString();
            albumImgSm = in.readString();
            albumImgLg = in.readString();
            previewURL = in.readString();
        }

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public String toString(){
            return trackName + "--" + albumName + "--" + albumImgSm + "--" + albumImgLg + "--" + previewURL;
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeString(trackName);
            dest.writeString(albumName);
            dest.writeString(albumImgSm);
            dest.writeString(albumImgLg);
            dest.writeString(previewURL);
        }

        public final Parcelable.Creator<CustomTrack> CREATOR = new Parcelable.Creator<CustomTrack>(){
            public CustomTrack createFromParcel(Parcel in){
                return new CustomTrack(in);
            }
            public CustomTrack[] newArray(int size){
                return new CustomTrack[size];
            }
        };
    }

    private class TrackAdapter extends ArrayAdapter<CustomTrack> {
        private List<CustomTrack> trackList;

        public TrackAdapter(Context context, int resource, int textViewResourceId, List<CustomTrack> objects){
            super(context, resource, textViewResourceId, objects);
            trackList = objects;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent){
            View v = convertView;
            TrackResult tr = new TrackResult();
            if (convertView == null){
                LayoutInflater inflater = (LayoutInflater) getContext()
                        .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                v = inflater.inflate(R.layout.top_track, null);
                TextView ntv = (TextView) v.findViewById(R.id.track_top_name);
                TextView atv = (TextView) v.findViewById(R.id.track_top_album);
                ImageView iv = (ImageView) v.findViewById(R.id.track_top_img);
                tr.trackName = ntv;
                tr.albumName = atv;
                tr.albumImg = iv;
                v.setTag(tr);
            }
            else
                tr = (TrackResult) v.getTag();

            CustomTrack p = trackList.get(position);
            tr.trackName.setText(p.trackName);
            tr.albumName.setText(p.albumName);
            Picasso.with(getContext()).load(p.albumImgSm).into(tr.albumImg);

            return v;
        }
    }

}

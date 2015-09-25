package com.example.android.spotifystreamer;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

import kaaes.spotify.webapi.android.SpotifyApi;
import kaaes.spotify.webapi.android.SpotifyService;
import kaaes.spotify.webapi.android.models.Artist;
import kaaes.spotify.webapi.android.models.ArtistsPager;
import retrofit.RetrofitError;


/**
 * A placeholder fragment containing a simple view.
 */
public class SearchFragment extends Fragment {
    private final String LOG_TAG = SearchFragment.class.getSimpleName();
    private ArtistAdapter searchAdapter;
    private String SEARCH_STRING = "searchString";
    private String searchedString = "";
    private List<CustomArtist> customArtistList;


    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        if (savedInstanceState != null && savedInstanceState.containsKey(SEARCH_STRING)){
            searchedString = savedInstanceState.getString(SEARCH_STRING);
        }
        if(savedInstanceState == null || !savedInstanceState.containsKey("artists")) {
            customArtistList = new ArrayList<CustomArtist>();
        }
        else {
            customArtistList = savedInstanceState.getParcelableArrayList("artists");
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putParcelableArrayList("artists", (ArrayList<? extends Parcelable>) customArtistList);
        outState.putString(SEARCH_STRING, searchedString);
        super.onSaveInstanceState(outState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        searchAdapter = new ArtistAdapter(
                getActivity(),
                R.layout.search_result,
                R.id.search_result_artist,
                new ArrayList<CustomArtist>()
        );

        final View rootView = inflater.inflate(R.layout.fragment_main, container, false);
        final EditText editText = (EditText) rootView.findViewById(R.id.searchBar);
        if (editText.getText().toString().length() == 0){
            editText.setText(searchedString);
        }
        editText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                boolean handled = false;
                if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                    SearchArtistTask sat = new SearchArtistTask();
                    searchedString = editText.getText().toString();
                    sat.execute(searchedString);

                    //hide keyboard
                    InputMethodManager inputManager =
                            (InputMethodManager) getActivity().
                                    getSystemService(Context.INPUT_METHOD_SERVICE);
                    inputManager.hideSoftInputFromWindow(
                            getActivity().getCurrentFocus().getWindowToken(),
                            InputMethodManager.HIDE_NOT_ALWAYS);
                    handled = true;
                }
                return handled;
            }
        });

        ListView listView = (ListView) rootView.findViewById(R.id.listview_artists);
        listView.setAdapter(searchAdapter);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                CustomArtist artistClick = searchAdapter.getItem(position);
                String artistClickId = artistClick.id;
                String artistName = artistClick.name;
                Intent intent = new Intent(getActivity(), ArtistActivity.class)
                        .putExtra(Intent.EXTRA_TEXT, artistClickId);
                intent.putExtra(Intent.EXTRA_SUBJECT, artistName);
                startActivity(intent);
            }
        });

        return  rootView;
    }

    public class SearchArtistTask extends AsyncTask<String, Void, List<CustomArtist>>{
        @Override
        protected List<CustomArtist> doInBackground(String... params){
            if (params.length == 0) {
                return null;
            }
            Log.d(LOG_TAG, "Starting asynctask");
            List<Artist> artists = new ArrayList<Artist>();
            try {
                Log.e(LOG_TAG, "Internet call");
                SpotifyApi spApi = new SpotifyApi();
                SpotifyService spotify = spApi.getService();
                ArtistsPager ap = spotify.searchArtists(params[0]);
                artists = ap.artists.items;
            } catch (RetrofitError | NullPointerException err) {
                Log.e(LOG_TAG, String.valueOf(err));
            }
                CustomArtist ca;
                List<CustomArtist> cArtists = new ArrayList<CustomArtist>();
                for (Artist a : artists) {
                    if (a.images.size() > 0) {
                        ca = new CustomArtist(a.id, a.name, a.images.get(0).url);
                    } else {
                        ca = new CustomArtist(a.id, a.name,
                                "http://contactyellowpages.com/images/no_image.jpg");
                    }
                    cArtists.add(ca);
                }
                Log.d(LOG_TAG, "Ending asynctask");
                return cArtists;

        }

        @Override
        protected void onPostExecute(List<CustomArtist> result) {
            if (result != null) {
                customArtistList = result;
                searchAdapter.clear();
                if (result.size() > 0) {
                    searchAdapter.addAll(result);
                }
                else{
                    Context context = getActivity();
                    int duration = Toast.LENGTH_SHORT;
                    CharSequence text = "There are no results. Please enter another search!";
                    Toast toast = Toast.makeText(context, text, duration);
                    toast.show();
                }
            }
        }
    }

    private class ArtistResult {
        public TextView artistName;
        public ImageView artistImg;
    }

    private class CustomArtist implements Parcelable{
        public String id, name, img;

        public CustomArtist(String id, String name, String img){
            this.id = id;
            this.name = name;
            this.img = img;
        }

        private CustomArtist(Parcel in){
            id = in.readString();
            name = in.readString();
            img = in.readString();
        }

        @Override
        public int describeContents(){ return 0; }

        @Override
        public String toString(){
            return id + "--" + name + "--" + img;
        }

        @Override
        public void writeToParcel(Parcel dest, int flags){
            dest.writeString(id);
            dest.writeString(name);
            dest.writeString(img);
        }

        public final Parcelable.Creator<CustomArtist> CREATOR = new Parcelable.Creator<CustomArtist>(){
            public CustomArtist createFromParcel(Parcel in){ return new CustomArtist(in); }
            public CustomArtist[] newArray(int size) { return new CustomArtist[size]; }
        };
    }

    private class ArtistAdapter extends ArrayAdapter<CustomArtist>{
        private List<CustomArtist> artistList;

        public ArtistAdapter(Context context, int resource, int textViewResourceId, List<CustomArtist> objects){
            super(context, resource, textViewResourceId, objects);
            artistList = objects;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent){
            View v = convertView;
            ArtistResult ar = new ArtistResult();
            if (convertView == null){
                LayoutInflater inflater = (LayoutInflater) getContext()
                        .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                v = inflater.inflate(R.layout.search_result, null);
                TextView tv = (TextView) v.findViewById(R.id.search_result_artist);
                ImageView iv = (ImageView) v.findViewById(R.id.search_result_image);
                ar.artistName = tv;
                ar.artistImg = iv;
                v.setTag(ar);
            }
            else
                ar = (ArtistResult) v.getTag();

            CustomArtist p = artistList.get(position);
            ar.artistName.setText(p.name);
            Picasso.with(getContext()).load(p.img).into(ar.artistImg); //do off main
            //have ref to imageview
            //dill w/na in meantime

            return v;
        }
    }
}

package com.example.android.spotifystreamer;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;


public class ArtistActivity extends ActionBarActivity {
    private final String LOG_TAG = ArtistActivity.class.getSimpleName();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_artist);
        setTitle("Top 10 Tracks");
        ActionBar ab = getSupportActionBar();
        Intent intent = getIntent();
        if (intent != null && intent.hasExtra(Intent.EXTRA_TEXT)) {
            String artistName = intent.getStringExtra(Intent.EXTRA_SUBJECT);
            try {
                ab.setSubtitle(artistName);
            } catch (NullPointerException n) {
                Log.w(LOG_TAG, "ab null");
            }
        }



    }

}

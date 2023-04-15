package de.dennisguse.opentracks;

import static de.dennisguse.opentracks.TrackRecordedActivity.EXTRA_TRACK_ID;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;

import android.content.SharedPreferences;
import android.media.MediaParser;
import android.os.Bundle;
import android.widget.Toast;

import java.io.File;
import java.io.Serializable;
import java.util.Scanner;

import de.dennisguse.opentracks.data.ContentProviderUtils;
import de.dennisguse.opentracks.data.TrackDataHub;
import de.dennisguse.opentracks.data.models.LocationPoints;
import de.dennisguse.opentracks.data.models.Track;

public class ViewTrackMap extends AppCompatActivity implements Serializable{

    public static final String EXTRA_TRACK_ID = "track_id";
    private Track.Id trackId;
    private ContentProviderUtils contentProviderUtils;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.statistics_recorded_enhancement);
        Intent intent = getIntent();
        trackId = intent.getParcelableExtra("trackId");

        //Read from storage

    }
}
package de.dennisguse.opentracks;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;

import android.os.Bundle;

import java.io.Serializable;

import de.dennisguse.opentracks.data.ContentProviderUtils;
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
        int idx=0;


        //Read from storage
        for(int i=0;i<TrackRecordingActivity.locationPointsList.size();i++){
            if(TrackRecordingActivity.locationPointsList.get(i).getTrackId() == trackId){
                idx = i;
                break;
            }
        }

    }
}
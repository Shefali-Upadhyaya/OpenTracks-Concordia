package de.dennisguse.opentracks.data.models;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Service;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.IBinder;
import android.provider.Settings;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;

import de.dennisguse.opentracks.TrackRecordingActivity;

public class LocationPoints extends Service implements LocationListener {

    private Track.Id trackId;
    private Double s_latitude;
    private Double s_longitude;
    private Double e_latitude;
    private Double e_longitude;

    public LocationPoints(Track.Id trackId) {
        this.trackId = trackId;
    }

    public Double getLatitude(){
        return  s_latitude;
    }

    public Double getLongitude(){
        return  s_longitude;
    }
    public LocationPoints(Track.Id trackId, Double s_latitude, Double s_longitude, Double e_latitude, Double e_longitude){
        this.trackId = trackId;
        this.s_latitude = s_latitude;
        this.s_longitude = s_longitude;
        this.e_latitude = e_latitude;
        this.e_longitude = e_longitude;
    }


    @SuppressLint("MissingPermission")
    public void getS_latitudeLongitude(Location locationGPS) {
        if (locationGPS != null) {
            s_latitude = locationGPS.getLatitude();
            s_longitude = locationGPS.getLongitude();
        } else {
            s_latitude = 45.496944;
            s_longitude = -73.578056;
        }
    }

    public void getE_latitudeLongitude(Location locationGPS) {
        if (locationGPS != null) {
            e_latitude = locationGPS.getLatitude();
            e_longitude = locationGPS.getLongitude();

        } else {
            e_latitude = 45.496944;
            e_longitude = -73.578056;
        }
    }

    public String makeStr(){
        String str = trackId+","+s_longitude+","+s_longitude+","+e_longitude+","+e_longitude+"\n";
        return str;
    }


    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onLocationChanged(@NonNull Location location) {

    }

    public Track.Id getTrackId() {
        return this.trackId;
    }
}

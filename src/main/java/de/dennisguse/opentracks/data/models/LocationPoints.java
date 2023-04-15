package de.dennisguse.opentracks.data.models;

import android.app.Service;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.IBinder;
import android.provider.Settings;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class LocationPoints extends Service implements LocationListener {

    private Track.Id trackId;
    private Double s_latitude;
    private Double s_longitude;
    private Double e_latitude;
    private Double e_longitude;

    private LocationManager locationManager;

    public LocationPoints(Track.Id trackId){
        //locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
        this.trackId = trackId;
    }

    public void getS_latitudeLongitude() {
       // if(getGpsStatus()){
        try {
            LocationListener locationListener = new LocationListener() {
                @Override
                public void onLocationChanged(@NonNull Location location) {
                    s_latitude = location.getLatitude();
                    s_longitude = location.getLongitude();
                }
            };
        }catch(Exception e){
            Toast.makeText(this, "Location Services Off. Using default Location", Toast.LENGTH_LONG).show();
            s_latitude = 45.496944;
            s_longitude = -73.578056;
        }
        /*else{
            Toast.makeText(this, "Location Services Off. Using default Location", Toast.LENGTH_LONG).show();
            s_latitude = 45.496944;
            s_longitude = -73.578056;
        }*/
    }

    public void getE_latitudeLongitude() {
        try {
            LocationListener locationListener = new LocationListener() {
                @Override
                public void onLocationChanged(@NonNull Location location) {
                    e_latitude = location.getLatitude();
                    e_longitude = location.getLongitude();
                }
            };
        }catch(Exception e){
            Toast.makeText(this, "Location Services Off. Using default Location", Toast.LENGTH_LONG).show();
            e_latitude = 45.496944;
            e_longitude = -73.578056;
        }
        /*if(getGpsStatus()){
            LocationListener locationListener = new LocationListener() {
                @Override
                public void onLocationChanged(@NonNull Location location) {
                    e_latitude = location.getLatitude();
                    e_longitude = location.getLongitude();
                }
            };

        }
        else{
            Toast.makeText(this, "Location Services Off. Using default Location", Toast.LENGTH_LONG).show();
            e_latitude = 45.497778;
            e_longitude = -73.578056;
        }*/
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

    private Boolean getGpsStatus() {
        ContentResolver contentResolver = getBaseContext().getContentResolver();
        boolean gps_enabled = false;
        boolean network_enabled = false;

        try {
            gps_enabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        } catch(Exception ex) {}

        try {
            network_enabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
        } catch(Exception ex) {}

        if(!gps_enabled && !network_enabled) {
            return false;
        }
        return true;
    }



}

package de.dennisguse.opentracks;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import de.dennisguse.opentracks.databinding.StatisticsRecordedEnhancementBinding;

import android.os.Bundle;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;


public class ViewTrackMap extends AppCompatActivity implements OnMapReadyCallback {
    private GoogleMap mMap;
    private StatisticsRecordedEnhancementBinding binding;



    // To be deleted
    private Double originLatitude = 28.5021359;
    // To be deleted
    private Double originLongitude = 77.4054901;

    // To be deleted
    private Double destinationLatitude = 28.5151087;
    private Double destinationLongitude = 77.3932163;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = StatisticsRecordedEnhancementBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {

        mMap = googleMap;
        LatLng originLocation = new LatLng(originLatitude, originLongitude);
        mMap.clear();
        mMap.addMarker(new MarkerOptions().position(originLocation));
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(originLocation, 18F));


    }

    private String getDirectionURL( LatLng origin, LatLng dest, String secret) {
        return "https://maps.googleapis.com/maps/api/directions/json?origin=${origin.latitude},${origin.longitude}" +
                "&destination=${dest.latitude},${dest.longitude}" +
                "&sensor=false" +
                "&mode=driving" +
                "&key=$secret";
    }
}
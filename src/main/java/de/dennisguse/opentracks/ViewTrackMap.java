package de.dennisguse.opentracks;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import de.dennisguse.opentracks.data.TrackDataHub;
import de.dennisguse.opentracks.data.models.LocationPoints;
import de.dennisguse.opentracks.data.models.Track;
import de.dennisguse.opentracks.databinding.StatisticsRecordedEnhancementBinding;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.PolylineOptions;

import java.util.ArrayList;


public class ViewTrackMap extends AppCompatActivity implements OnMapReadyCallback {
    private GoogleMap mMap;
    private StatisticsRecordedEnhancementBinding binding;
    int idx = 0;
    private Track.Id trackId;
    private TrackDataHub trackDataHub;
    ArrayList routeLatLong ;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = StatisticsRecordedEnhancementBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        trackDataHub = new TrackDataHub(this);
        trackDataHub.start();

        Intent intent = getIntent();
        trackId = intent.getParcelableExtra("trackId");



        for (int i = 0; i < TrackRecordingActivity.locationPointsList.size(); i++) {

          routeLatLong  = new ArrayList();
            if (TrackRecordingActivity.locationPointsList.get(i).getTrackId() == trackId) {

                LocationPoints point = TrackRecordingActivity.locationPointsList.get(i);
                idx = i;

                routeLatLong.add(new LatLng(point.getLatitude(), point.getLongitude()));
                break;
            }
        }

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {

        mMap = googleMap;
        mMap.clear();

        //ArrayList routeLatLong = getRouteLatLongTemp();

        if (routeLatLong == null) {
            Toast.makeText(this, "Route not found", Toast.LENGTH_SHORT).show();
            return;
        }
        LatLng positionToBeZoomedAt = (LatLng) routeLatLong.get(0);

        drawRouteOnMap(getPolylineOptions(routeLatLong));

        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(positionToBeZoomedAt, 18F));


    }

    private ArrayList getRouteLatLongTemp() {
        ArrayList points = new ArrayList();
        LatLng position = new LatLng(45.489763, -73.584011);
        points.add(position);


        position = new LatLng(45.490691, -73.582980);
        points.add(position);

        position = new LatLng(45.492120, -73.581255);
        points.add(position);

        return points;

    }

    private PolylineOptions getPolylineOptions(ArrayList routeLatLong) {
        PolylineOptions lineOptions = new PolylineOptions();
        lineOptions.addAll(routeLatLong);
        lineOptions.width(12);
        lineOptions.color(Color.BLUE);
        lineOptions.geodesic(true);
        return lineOptions;
    }

    private void drawRouteOnMap(PolylineOptions lineOptions) {
        mMap.addPolyline(lineOptions);
    }
}
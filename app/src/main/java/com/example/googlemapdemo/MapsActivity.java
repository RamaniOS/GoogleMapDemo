package com.example.googlemapdemo;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.os.Looper;
import android.util.Log;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polygon;
import com.google.android.gms.maps.model.PolygonOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private FusedLocationProviderClient fusedLocationProviderClient;
    private LocationRequest locationRequest;
    private LocationCallback locationCallback;
    private static final int REQUEST_CODE = 1;
    private Marker curr_marker;
    private Marker dest_marker;
    private Polyline polyline;
    private Polygon polygon;
    private static final int POLYGON_NUM = 4;
    List<Marker> markersList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        // init location manager
        initLocation();
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        mMap.setOnMapLongClickListener(new GoogleMap.OnMapLongClickListener() {
            @Override
            public void onMapLongClick(LatLng latLng) {
                setDestinationLocation(latLng);
            }
        });

        if (!checkPermission())
            requestPermission();
        else
            initLocationCallback();
            fusedLocationProviderClient.requestLocationUpdates(locationRequest, locationCallback, Looper.myLooper());

        fusedLocationProviderClient.getLastLocation().addOnCompleteListener(this, new OnCompleteListener<Location>() {
            @Override
            public void onComplete(@NonNull Task<Location> task) {
                if (task.isSuccessful() && task.getResult() != null) {
                    Location location = task.getResult();
                    LatLng marker = new LatLng(location.getLatitude(), location.getLongitude());
                    setHomeLocation(marker);
                    //mMap.addMarker(new MarkerOptions().position(marker).title("Current use  location").icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_YELLOW)));
                    //mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(marker, 10));
                }
            }
        });
//        mMap.setMapType(GoogleMap.MAP_TYPE_TERRAIN);
//
//        // Add a marker in Sydney and move the camera
//        LatLng sydney = new LatLng(-34, 151);
//        mMap.addMarker(new MarkerOptions().position(sydney).title("Marker in Sydney").icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_YELLOW)));
//        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(sydney, 10));
    }



    private void initLocation() {
        // init location client
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
        // init location request
        locationRequest = new LocationRequest();
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationRequest.setInterval(5000);
        locationRequest.setFastestInterval(3000);
        locationRequest.setSmallestDisplacement(10);
    }

    private void initLocationCallback() {
        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                //super.onLocationResult(locationResult);
                for (Location location: locationResult.getLocations()) {
                    // get address
                    Geocoder geocoder = new Geocoder(getApplicationContext(), Locale.getDefault());
                    try {
                        List<Address> addresses =  geocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1);
                        if (addresses != null && addresses.size() > 0) {

                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    Log.d("location updates>>>", location.getLatitude() + String.valueOf(location.getLongitude()));
                }
            }
        };
    }

    private boolean checkPermission() {
        int isGranted = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION);
        return isGranted == PackageManager.PERMISSION_GRANTED;
    }

    private void requestPermission() {
        ActivityCompat.requestPermissions(this, new String[]{ Manifest.permission.ACCESS_FINE_LOCATION }, REQUEST_CODE);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == REQUEST_CODE && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            initLocationCallback();
            fusedLocationProviderClient.requestLocationUpdates(locationRequest, locationCallback, Looper.myLooper());
        }
    }

    private void setHomeLocation(LatLng location) {
        MarkerOptions markerOptions = new MarkerOptions().position(location)
                .title("Your Location")
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE))
                .snippet("You are here");
        curr_marker = mMap.addMarker(markerOptions);
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(location, 20));
    }

    private void setDestinationLocation(LatLng location) {
        MarkerOptions markerOptions = new MarkerOptions().position(location)
                .title("Your Destination")
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ORANGE))
                .draggable(true);
      /*if (dest_marker == null) {
            dest_marker = mMap.addMarker(markerOptions);
        } else {
            clearMap();
            dest_marker = mMap.addMarker(markerOptions);
        }*/

        if (markersList.size() == POLYGON_NUM) {
            clearMap();
        }

        markersList.add(mMap.addMarker(markerOptions));

        if (markersList.size() == POLYGON_NUM) {
            drawShape();
        }
        //drawLine(curr_marker.getPosition(), dest_marker.getPosition());

    }

    private void clearMap() {
        if (dest_marker != null) {
            dest_marker.remove();
            dest_marker = null;
        }
        for (int i = 0; i<POLYGON_NUM; i++) {
            markersList.get(i).remove();
        }
        markersList.removeAll(markersList);
        //polyline.remove();
        polygon.remove();
    }

    private void drawLine(LatLng home, LatLng dest) {
        PolylineOptions polylineOptions = new PolylineOptions()
                .add(home, dest)
                .clickable(true)
                .color(R.color.colorPrimary)
                .width(10)
                .visible(true);
        polyline = mMap.addPolyline(polylineOptions);
    }

    private void drawShape() {
        PolygonOptions polygonOptions = new PolygonOptions()
                .fillColor(R.color.colorAccent)
                .strokeColor(R.color.colorPrimaryDark)
                .strokeWidth(20)
                .visible(true);

        for (int i = 0; i < POLYGON_NUM; i++) {
            polygonOptions.add(markersList.get(i).getPosition());
        }
        polygon = mMap.addPolygon(polygonOptions);
    }

}

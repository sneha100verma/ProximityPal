package com.example.proximitypal;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.common.api.Status;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.widget.AutocompleteSupportFragment;
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener;

import java.util.Arrays;

public class MainActivity extends AppCompatActivity implements OnMapReadyCallback {

    private TextView textView;
    private Button select;
    private GoogleMap mMap;
    Double lati, longi;
    String locatioName;

    private long pressedTime;
 //   private RouteDrawer routeDrawer;
    private Location mCurrentLocation;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // textView = findViewById(R.id.tv1);
        select = (Button) findViewById(R.id.select);


        // Fetching API_KEY which we wrapped
        String apiKey = "AIzaSyDYjBJozLDzVIKUphA_JD-XzfVxN2Gyu0M";

        // Initializing the Places API
        // with the help of our API_KEY
        if (!Places.isInitialized()) {
            Places.initialize(getApplicationContext(), apiKey);
        }

        // Initialize Autocomplete Fragments
        // from the main activity layout file
        AutocompleteSupportFragment autocompleteSupportFragment1 =
                (AutocompleteSupportFragment) getSupportFragmentManager()
                        .findFragmentById(R.id.autocomplete_fragment1);

        // Information that we wish to fetch after typing
        // the location and clicking on one of the options
        if (autocompleteSupportFragment1 != null) {
            autocompleteSupportFragment1.setPlaceFields(
                    Arrays.asList(
                            Place.Field.NAME,
                            Place.Field.ADDRESS,
                            Place.Field.LAT_LNG
                    )
            );

            // Display the fetched information after clicking on one of the options
            autocompleteSupportFragment1.setOnPlaceSelectedListener(new PlaceSelectionListener() {
                @Override
                public void onPlaceSelected(Place place) {
                    displayPlaceInfo(place);
                    updateMap(place.getLatLng());
                }

                @Override
                public void onError(Status status) {
                    Toast.makeText(getApplicationContext(), "Some error occurred", Toast.LENGTH_SHORT).show();
                }
            });
        }

        // Obtain the SupportMapFragment and get notified
        // when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map_fragment);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }

        select.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // When the user selects a location, pass latitude and longitude to SetAlarmActivity
                Intent intent = new Intent(MainActivity.this, Alarm.class);
                if (lati != null && longi != null) {
                    intent.putExtra("LATITUDE", lati);
                    intent.putExtra("LONGITUDE", longi);
                    intent.putExtra("LOCATIONNAME", locatioName);
                    startActivity(intent);

                    // Draw route from current location to selected location
                    if (mCurrentLocation != null) {
                        LatLng origin = new LatLng(mCurrentLocation.getLatitude(), mCurrentLocation.getLongitude());
                        LatLng dest = new LatLng(lati, longi);
                     //   routeDrawer.drawRoute(origin, dest);
                    } else {
                        Toast.makeText(MainActivity.this, "Current location not available.", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(MainActivity.this, "Please search a location.", Toast.LENGTH_SHORT).show();
                }
            }
        });


    }

    private void displayPlaceInfo(Place place) {
        if (place != null) {
            String name = place.getName();

            Double latitude = null;
            Double longitude = null;
            if (place.getLatLng() != null) {
                latitude = place.getLatLng().latitude;
                longitude = place.getLatLng().longitude;
            }
//            textView.setText("Name: " + name + "\n" +
//                    "Latitude : " + latitude + "\n" + "Longitude : " + longitude
//            );
            lati = latitude;
            longi = longitude;
            locatioName = name;
        }
    }

    @Override
    public void onBackPressed() {

        if (pressedTime + 2000 > System.currentTimeMillis()) {
            super.onBackPressed();
            finish();
        } else {
            Toast.makeText(getBaseContext(), "Press back again to exit", Toast.LENGTH_SHORT).show();
        }
        pressedTime = System.currentTimeMillis();
    }


    private void updateMap(LatLng latLng) {
        if (mMap != null && latLng != null) {
            mMap.clear();
            mMap.addMarker(new MarkerOptions().position(latLng).title("Selected Location"));
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15f));
        }
    }

//    @Override
//    public void onMapReady(GoogleMap googleMap) {
//        mMap = googleMap;
//        requestLocationPermission();
//    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        // Enable MyLocation Layer of Google Map
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        mMap.setMyLocationEnabled(true);
        mMap.setOnMyLocationChangeListener(new GoogleMap.OnMyLocationChangeListener() {
            @Override
            public void onMyLocationChange(Location location) {
                // Update current location
                mCurrentLocation = location;
            }
        });
    }

    private void enableMyLocation() {
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            if (mMap != null) {
                mMap.setMyLocationEnabled(true);
                mMap.getUiSettings().setMyLocationButtonEnabled(true);
            }
        } else {
            // Request location permission
            requestLocationPermission();
        }
    }





    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;

    private void requestLocationPermission() {
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                    LOCATION_PERMISSION_REQUEST_CODE);
        } else {
            enableMyLocation();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                enableMyLocation();
            } else {
                // Permission was denied. Display an error message or handle it accordingly.
                Toast.makeText(this, "Location permission was denied.", Toast.LENGTH_SHORT).show();
            }
        }
    }



}




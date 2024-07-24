package com.example.proximitypal;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.app.AlarmManager;
import android.app.Dialog;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Looper;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;

import java.io.IOException;

public class Alarm extends AppCompatActivity {

    private static final int MY_PERMISSIONS_REQUEST_LOCATION = 99;
    private static final double EARTH_RADIUS = 6371.000;
    private FusedLocationProviderClient fusedLocationClient;
    private Location savedLocation;

     Double latitude;
     Double longitude;
     String locationName ;
     TextView selectedLocation , userGuide;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_alarm2);


        Intent intent = getIntent();
        if (intent != null) {
            latitude = intent.getDoubleExtra("LATITUDE", 0);
            longitude = intent.getDoubleExtra("LONGITUDE", 0);
            locationName = intent.getStringExtra("LOCATIONNAME");



        }


        userGuide = (TextView)findViewById(R.id.userGuide);


        selectedLocation = (TextView)findViewById(R.id.textViewSelectedLocation);
        selectedLocation.setText(locationName);

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);


        Button saveLocationButton = (Button) findViewById(R.id.buttonSaveLocation);
        saveLocationButton.setOnClickListener(view -> saveLocation());

        Button setAlarmButton = (Button)findViewById(R.id.buttonSetAlarm);
        setAlarmButton.setOnClickListener(view -> setAlarm());

        // Request location updates
        //requestLocationUpdates();


        Button stopAlarmButton = findViewById(R.id.buttonStopAlarm);
        stopAlarmButton.setOnClickListener(view -> {
            stopAlarm();
        });

        Button selectAgain = (Button)findViewById(R.id.selectAgain);
        selectAgain.setOnClickListener(view -> {
                stopAlarm();
            Intent i = new Intent(Alarm.this, MainActivity.class);


            startActivity(i);

            finish();
        });




        userGuide.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

              showUserGuide();

            }

            private void showUserGuide() {

                // Create a new dialog
                Dialog dialog = new Dialog(Alarm.this);
                dialog.setContentView(R.layout.card);

                // Find and set the close button
                ImageView closeButton = dialog.findViewById(R.id.closeButton);
                closeButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        dialog.dismiss();
                    }
                });

                // Show the dialog
                dialog.show();

            }
        });







    }
    private void stopLocationUpdates() {
        fusedLocationClient.removeLocationUpdates(locationCallback);

    }


        private void stopAlarm() {
            if (mediaPlayer != null && mediaPlayer.isPlaying()) {
                mediaPlayer.stop();
                mediaPlayer.release();
                mediaPlayer = null;
                Toast.makeText(this, "Alarm stopped", Toast.LENGTH_SHORT).show();
            }
            stopLocationUpdates();
        }



    private void saveLocation() {


        if (latitude != null && longitude != null) {

            savedLocation = new Location("");
            savedLocation.setLatitude(latitude);
            savedLocation.setLongitude(longitude);
            Toast.makeText(this, "Location saved", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "Please enter latitude and longitude", Toast.LENGTH_SHORT).show();
        }
    }

    private void setAlarm() {
        // Retrieve entered notes from EditText
        EditText editTextNotes = findViewById(R.id.editTextNotes);
        String notes = editTextNotes.getText().toString();

        // Save notes in SharedPreferences
        SharedPreferences sharedPreferences = getSharedPreferences("AlarmNotes", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("notes", notes);
        editor.apply();

        // Proceed with setting the alarm
        if (savedLocation == null) {
            Toast.makeText(this, "Please save a location first", Toast.LENGTH_SHORT).show();
            return;
        } else {
            Toast.makeText(this, "Alarm is set.", Toast.LENGTH_SHORT).show();
            requestLocationUpdates();
        }


    }

    private MediaPlayer mediaPlayer;

    private void playAlarmSound() {
        if (mediaPlayer == null || !mediaPlayer.isPlaying()) {
            try {
                // Create a new MediaPlayer instance if it's null or not playing
                mediaPlayer = new MediaPlayer();
                mediaPlayer.setDataSource(this, Uri.parse("android.resource://" + getPackageName() + "/" + R.raw.voice));
                mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
                mediaPlayer.setOnPreparedListener(mp -> {
                    // Start playback when the audio is prepared
                    mediaPlayer.start();
                });
                mediaPlayer.setOnCompletionListener(mp -> {
                    // Release resources after completion
                    mediaPlayer.release();
                    mediaPlayer = null;
                });
                mediaPlayer.prepareAsync(); // Prepare asynchronously to prevent blocking the main thread
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }


    private void requestLocationUpdates() {
        LocationRequest locationRequest = LocationRequest.create()
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                .setInterval(10000)
                .setFastestInterval(5000);

        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION,
                            Manifest.permission.ACCESS_COARSE_LOCATION},
                    MY_PERMISSIONS_REQUEST_LOCATION);
            return;
        }

        fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper());
    }

    private LocationCallback locationCallback = new LocationCallback() {
        @Override
        public void onLocationResult(LocationResult locationResult) {
            super.onLocationResult(locationResult);
            if (locationResult != null && savedLocation != null) {
                Location currentLocation = locationResult.getLastLocation();
//                float distance = calculateDistance(savedLocation.getLatitude(), savedLocation.getLongitude(), currentLocation.getLatitude(), currentLocation.getLongitude());
//
//                // Set a threshold distance, for example, 100 meters
//                if (distance < 0.9) {
//                    //triggerAlarm();
//                    playAlarmSound();
//                    showNotification(Alarm.this);
//                }
//                updateDistance(distance);


                float distance = calculateDistance(
                        savedLocation.getLatitude(),
                        savedLocation.getLongitude(),
                        currentLocation.getLatitude(),
                        currentLocation.getLongitude()
                );



// Format distance string
                String distanceString;
                if (distance < 1) {
                    // Convert distance to meters and format to two decimal places
                    distanceString = String.format("%.2f m", distance*1000);


                } else {
                    // Convert distance to kilometers and format to two decimal places
                    distanceString = String.format("%.2f km", distance);
                }

// Update distance display
                updateDistance(distanceString);

// Trigger alarm if distance is within threshold
                if (distance < 0.9) {
                    playAlarmSound();
                    showNotification(Alarm.this);
                }


            }
        }
    };

    private float calculateDistance(double lat1, double lon1, double lat2, double lon2) {
        double lat1Rad = Math.toRadians(lat1);
        double lon1Rad = Math.toRadians(lon1);
        double lat2Rad = Math.toRadians(lat2);
        double lon2Rad = Math.toRadians(lon2);

        double deltaLat = lat2Rad - lat1Rad;
        double deltaLon = lon2Rad - lon1Rad;

        double havDeltaLat = Math.sin(deltaLat / 2) * Math.sin(deltaLat / 2);
        double havDeltaLon = Math.sin(deltaLon / 2) * Math.sin(deltaLon / 2);

        double centralAngle = 2 * Math.asin(Math.sqrt(havDeltaLat + Math.cos(lat1Rad) * Math.cos(lat2Rad) * havDeltaLon));

        double distance = EARTH_RADIUS * centralAngle;

        return (float) distance;
    }

    private void updateDistance(String distance) {
        TextView distanceTextView = findViewById(R.id.textViewDistance);
        distanceTextView.setText("Distance: " + distance);
    }


    private void showNotification(Context context) {
        // Retrieve saved notes from SharedPreferences
        SharedPreferences sharedPreferences = getSharedPreferences("AlarmNotes", MODE_PRIVATE);
        String notes = sharedPreferences.getString("notes", "");

        // Display alarm message along with saved notes
        String fullMessage = "";


        if(!notes.isEmpty())
        {
            fullMessage = notes;
        }
        else {
            fullMessage  = "Alarm Triggred!!!!!";
        }

            Toast.makeText(context, fullMessage, Toast.LENGTH_SHORT).show();


    }



//    private void triggerAlarm() {
//        // Get the AlarmManager service
//        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
//        if (alarmManager != null) {
//            // Create an intent to trigger the alarm receiver
//            Intent alarmIntent = new Intent(this, AlarmReciever.class);
//            PendingIntent pendingIntent = PendingIntent.getBroadcast(this, 0, alarmIntent, PendingIntent.FLAG_UPDATE_CURRENT);
//
//            // Schedule the alarm to go off immediately
//            alarmManager.set(AlarmManager.RTC_WAKEUP, System.currentTimeMillis(), pendingIntent);
//        }
//    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == MY_PERMISSIONS_REQUEST_LOCATION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                requestLocationUpdates();
            } else {
                Toast.makeText(this, "Location permission denied", Toast.LENGTH_SHORT).show();
            }
        }
    }
}

package fr.cjpapps.gumsski;

import android.annotation.SuppressLint;
import android.app.Application;
import android.location.Location;
import android.util.Log;

import androidx.core.content.ContextCompat;
import androidx.lifecycle.MutableLiveData;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.SettingsClient;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;


class LocRepository {

    public static final int NBR_SECS_INI = 2;
    public static final int NBR_SECS_FINAL = 20;
    public static final int NBR_MINS_TRACKING = 1;
    private final MutableLiveData<Location> position = new MutableLiveData<>();
    private final FusedLocationProviderClient fusedLocationClient;
    LocationRequest locationRequest;

    LocationCallback locationCallback = new LocationCallback() {
        @Override
        public void onLocationResult(LocationResult locationResult) {
            if (locationResult == null) {
                return;
            }
            for (Location location : locationResult.getLocations()) {
                Log.i("SECUSERV", "location OK");
                position.setValue(location);
            }
        }
    };

    LocRepository(Application application) {
        if (BuildConfig.DEBUG){
        Log.i("SECUSERV", "constructeur du repository");}
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(application);
        createLocationRequest();
    }

    MutableLiveData<Location> getPosition() { return position; }

    @SuppressLint("MissingPermission")
    void findPosition() {
        try {
            fusedLocationClient.getLastLocation()
                    .addOnSuccessListener(new OnSuccessListener<Location>() {
                        @Override
                        public void onSuccess(Location location) {
                            // Got last known location. In some rare situations this can be null.
                            if (location != null) {
                                if (BuildConfig.DEBUG){
                                    Log.i("SECUSERV", "last location OK");}
                                position.setValue(location);
                            } else {
                                if (BuildConfig.DEBUG){
                                    Log.i("SECUSERV", "location est null");}
                            }
                        }
                    });
        }catch (SecurityException e) {
            e.printStackTrace();
        }
    }

    void createLocationRequest() {
        locationRequest = LocationRequest.create();
        locationRequest.setInterval(1000*NBR_SECS_INI);
        locationRequest.setFastestInterval(2000);
        locationRequest.setExpirationDuration(60000*NBR_MINS_TRACKING);
//        locationRequest.setNumUpdates(1);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
//        locationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
    }

    @SuppressLint("MissingPermission")
    void startLocationUpdates() {
        try {
            fusedLocationClient.requestLocationUpdates(locationRequest,
                    locationCallback,
                    null);
        }catch (SecurityException e) {
            e.printStackTrace();
        }
    }

    void stopLocationUpdates(){
        fusedLocationClient.removeLocationUpdates(locationCallback);
    }

    void changeRequestInterval() {
        stopLocationUpdates();
        findPosition();
        locationRequest.setInterval(1000*NBR_SECS_FINAL);
        //       if (Variables.gpsOK){
        startLocationUpdates();
        //       }
        Log.i("SECUSERV", "change intervalle");
    }

 }

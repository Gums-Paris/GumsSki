package fr.cjpapps.gumsski;

import static com.google.android.gms.location.Priority.PRIORITY_HIGH_ACCURACY;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;

import java.util.Locale;

public class Secours extends AppCompatActivity {

    public static final int DELAY = 62;  // en secondes avant changer intervalle

    TextView positionUn = null;
    TextView positionDeux = null;
    TextView accuracy = null;
    TextView europe = null;
    TextView textEurope = null;
    Button boutonEurope = null;
    TextView france = null;
    TextView textFrance = null;
    Button redacFrance = null;
    Button boutonFrance = null;
    TextView suisse = null;
    TextView textSuisse = null;
    Button boutonOCVS = null;
    Button boutonREGA = null;
    TextView italie = null;
    TextView textItalie = null;
    Button boutonItalie = null;
    double latitude;
    String latitudeStr;
    double longitude;
    String longitudeStr;
    String positionStr;
    float precision;
    ModelLocation model;
    private Boolean okPhone = false;

// lanceur pour demander la permission de localisation
    ActivityResultLauncher<String[]> locationPermissionRequest =
            registerForActivityResult(new ActivityResultContracts
                            .RequestMultiplePermissions(), result -> {
                        Boolean fineLocationGranted = result.get(
                                Manifest.permission.ACCESS_FINE_LOCATION);
                        Boolean coarseLocationGranted = result.get(
                                Manifest.permission.ACCESS_COARSE_LOCATION);
                        if (fineLocationGranted != null && fineLocationGranted) {
                            // Precise location access granted.
                            if (BuildConfig.DEBUG){
                            Log.i("SECUSERV", "on obtient la permission");}
                            model.updatePosition();
                            Variables.requestingLocationUpdates = true;
                        } else if (coarseLocationGranted != null && coarseLocationGranted) {
                            // Only approximate location access granted.
                            Toast.makeText(this, "Pas de localisation possible", Toast.LENGTH_LONG).show();
                        } else {
                            // No location access granted.
                            Toast.makeText(this, "Pas de localisation possible", Toast.LENGTH_LONG).show();
                        }
                    }
            );

// lanceur pour demande permission d'appeler qqun au tph directement
    final private ActivityResultLauncher<String> requestPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                if (isGranted) {
                    // Permission is granted. Continue the action or workflow in your app
                    okPhone = true;
                } else {
                    // Explain to the user that the feature is unavailable
                    okPhone = false;
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_secours);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null){
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        positionUn = findViewById(R.id.intro_secours);
        positionDeux = findViewById(R.id.coordonnees);
        accuracy = findViewById(R.id.accuracy);
        textEurope = findViewById(R.id.texte_europe);
        boutonEurope = findViewById(R.id.bouton_europe);
        textFrance = findViewById(R.id.texte_france);
        redacFrance = findViewById(R.id.redac_sms);
        boutonFrance = findViewById(R.id.bouton_france);
        textSuisse = findViewById(R.id.texte_suisse);
        boutonOCVS = findViewById(R.id.bouton_ocvs);
        boutonREGA = findViewById(R.id.bouton_rega);
        textItalie = findViewById(R.id.texte_italie);
        boutonItalie = findViewById(R.id.bouton_italie);

        positionUn.setText(getString(R.string.ta_position));
        positionDeux.setText(getString(R.string.attente_position));

        checkGPSEnabled(); // ceci informe l'utilisateur si ce n'est pas le cas

        model = new ViewModelProvider(this).get(ModelLocation.class);

// vérification de la permission de localisation
        if (ActivityCompat.checkSelfPermission( this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            // You can use the API that requires the permission.
            if (BuildConfig.DEBUG){
            Log.i("SECUSERV", "on a déja la permission");}
            model.updatePosition();
            Variables.requestingLocationUpdates = true;
        } else {
            // You can directly ask for the permission.
            // The registered ActivityResultCallback gets the result of this request.
            if (BuildConfig.DEBUG){
            Log.i("SECUSERV", "demande permissions");}
            locationPermissionRequest.launch(new String[] {
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
            });
        }

// observateur de la position GPS
        final Observer<Location> positionObserver = new Observer<Location>() {
            @Override
            public void onChanged(Location location) {
                if (location != null) {
                    latitude = location.getLatitude();
                    latitudeStr = String.format(Locale.getDefault(),"%.5f", latitude);
                    longitude = location.getLongitude();
                    longitudeStr = String.format(Locale.getDefault(),"%.5f", longitude);
                    if (location.hasAccuracy()) { precision = location.getAccuracy(); }
                    positionStr = getString(R.string.lat_lon, latitudeStr, longitudeStr);
//                    Toast.makeText(Secours.this, positionStr, Toast.LENGTH_SHORT).show();
                    if(precision <= 50f) {
                        positionDeux.setText(positionStr);
                        accuracy.setText(getString(R.string.precision, String.valueOf((int) precision)));
                    }else{
                        positionDeux.setText(getText(R.string.pas_precis));
                    }
                }else{
                    positionDeux.setText(getText(R.string.no_gps));
                }
            }
        };
        model.getPositionActuelle().observe(this, positionObserver);

// timer pour arrêter les mises à jour GPS après un délai de DELAY secondes
        new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
            @Override
/*            public void run() {
                changeLocationInterval();
            }*/
            public void run() {
                if (BuildConfig.DEBUG){
                    Log.i("SECUSERV", "minute écoulée");}
                model.stopUpdatePosition();
                positionUn.setText(getText(R.string.posit_accident));
            }
        }, 1000*DELAY);

        textEurope.setText(Aux.fromHtml(getString(R.string.texte_europe)));
        boutonEurope.setText(Aux.fromHtml(getString(R.string.texte_bouton_europe)));
        boutonEurope.setTextColor(ContextCompat.getColor(getApplicationContext(),R.color.colorEnvoi));
        textFrance.setText(Aux.fromHtml(getString(R.string.texte_france)));
        redacFrance.setText(Aux.fromHtml(getString(R.string.bouton_redac_sms)));
        redacFrance.setTextColor(ContextCompat.getColor(getApplicationContext(),R.color.colorEnvoi));
        boutonFrance.setText(Aux.fromHtml(getString(R.string.texte_bouton_france)));
        boutonFrance.setTextColor(ContextCompat.getColor(getApplicationContext(),R.color.colorEnvoi));
        textSuisse.setText(Aux.fromHtml(getString(R.string.texte_suisse)));
        boutonOCVS.setText(Aux.fromHtml(getString(R.string.texte_bouton_ocvs)));
        boutonOCVS.setTextColor(ContextCompat.getColor(getApplicationContext(),R.color.colorEnvoi));
        boutonREGA.setText(Aux.fromHtml(getString(R.string.texte_bouton_rega)));
        boutonREGA.setTextColor(ContextCompat.getColor(getApplicationContext(),R.color.colorEnvoi));
        textItalie.setText(Aux.fromHtml(getString(R.string.texte_italie)));
        boutonItalie.setText(Aux.fromHtml(getString(R.string.texte_bouton_italie)));
        boutonItalie.setTextColor(ContextCompat.getColor(getApplicationContext(),R.color.colorEnvoi));

// bouton appel 112
        boutonEurope.setOnClickListener(view -> {
            if (ContextCompat.checkSelfPermission(
                    this, Manifest.permission.CALL_PHONE) ==
                    PackageManager.PERMISSION_GRANTED) {
                phoneSecours("112");
            } else {
                // You can directly ask for the permission.
                // The registered ActivityResultCallback gets the result of this request.
                requestPermissionLauncher.launch(
                        Manifest.permission.CALL_PHONE);
                if (okPhone) {
                    phoneSecours("112");
                }
            }
        });

// bouton appel 144 (Valais)
        boutonOCVS.setOnClickListener(view -> {
            if (ContextCompat.checkSelfPermission(
                    this, Manifest.permission.CALL_PHONE) ==
                    PackageManager.PERMISSION_GRANTED) {
                phoneSecours("144");
            } else {
                // You can directly ask for the permission.
                // The registered ActivityResultCallback gets the result of this request.
                requestPermissionLauncher.launch(
                        Manifest.permission.CALL_PHONE);
                if (okPhone) {
                    phoneSecours("144");
                }
            }
        });

// bouton appel 1414 (reste de la Suisse)
        boutonREGA.setOnClickListener(view -> {
            if (ContextCompat.checkSelfPermission(
                    this, Manifest.permission.CALL_PHONE) ==
                    PackageManager.PERMISSION_GRANTED) {
                phoneSecours("1414");
            } else {
                // You can directly ask for the permission.
                // The registered ActivityResultCallback gets the result of this request.
                requestPermissionLauncher.launch(
                        Manifest.permission.CALL_PHONE);
                if (okPhone) {
                    phoneSecours("1414");
                }
            }
        });

// bouton appel Italie (118)
        boutonItalie.setOnClickListener(view -> {
            if (ContextCompat.checkSelfPermission(
                    this, Manifest.permission.CALL_PHONE) ==
                    PackageManager.PERMISSION_GRANTED) {
                phoneSecours("118");
            } else {
                // You can directly ask for the permission.
                // The registered ActivityResultCallback gets the result of this request.
                requestPermissionLauncher.launch(
                        Manifest.permission.CALL_PHONE);
                if (okPhone) {
                    phoneSecours("118");
                }
            }
        });

// bouton rédaction SMS pour 114
        redacFrance.setOnClickListener(view -> {
            Intent sms114 = new Intent(this, RedactionSMS.class);
            startActivity(sms114);
        });

// bouton envoi SMS au 114
        boutonFrance.setOnClickListener(view -> {
            envoiSMSAu114();
        });

     }  // end onCreate

/*    private boolean isLocationEnabled()
    {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
// This is new method provided in API 28
            LocationManager lm = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
            return lm.isLocationEnabled();
        } else {
// This is Deprecated in API 28
            int mode = Settings.Secure.getInt(this.getContentResolver(), Settings.Secure.LOCATION_MODE,
                    Settings.Secure.LOCATION_MODE_OFF);
            return  (mode != Settings.Secure.LOCATION_MODE_OFF);
        }
    } */

/*  mix de https://developer.android.com/training/location/change-location-settings  et de
* https://stackoverflow.com/questions/32423157/android-check-if-location-services-enabled-using-fused-location-provider
* sauf que on simplifie : dans le cas de onFailure, au lieu d'envoyer l'utilisateur changer les paramètres on se contente
* de lui signaler que son GPS n'est pas activé. A lui d'y aller s'il ouhaite avoir une position*/
    private void checkGPSEnabled() {
        LocationRequest request = LocationRequest.create()
                .setPriority(PRIORITY_HIGH_ACCURACY);
        LocationSettingsRequest settingsRequest = new LocationSettingsRequest.Builder()
                .addLocationRequest(request).build();
        LocationServices.getSettingsClient(this)
                .checkLocationSettings(settingsRequest)
                .addOnSuccessListener(this, new OnSuccessListener<LocationSettingsResponse>(){
                    @Override
                    public void onSuccess(LocationSettingsResponse locationSettingsResponse) {
                        if (BuildConfig.DEBUG){
                            Log.i("SECUSERV", "GPS success");}
//                        Variables.isGPSAvailable = true;
                    }
                })
                .addOnFailureListener(this, new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                        if (BuildConfig.DEBUG) {
                                            Log.i("SECUSERV", "GPS failure");
                                        }
//                                    Variables.isGPSAvailable = false;
                                        String message = "Attention, le GPS n'est pas activé.";
                                        DialogAlertes infoGPS = DialogAlertes.newInstance(message);
                                        infoGPS.show(getSupportFragmentManager(), "infoGPS");
                                }
                           });
    }

// pour modifier la requête définissant l'intervalle entre positions GPS
    void changeLocationInterval () {
        model.changeRequest();
    }

    private static void phoneSecours(String numero){
        if (BuildConfig.DEBUG){
            Log.i("SECUSERV secours click ", numero);}
        Intent phone = new Intent(Intent.ACTION_CALL);
        phone.setData(Uri.parse("tel:"+numero));
        phone.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        if (phone.resolveActivity(MyHelper.getInstance().recupPackageManager()) != null) {
            MyHelper.getInstance().launchActivity(phone);
        } else {
            if (BuildConfig.DEBUG) {
                        Log.i("SECUSERV"," appli téléphone pas disponible");}
        }
    }
    protected void envoiSMSAu114(){
        Intent sms = new Intent(Intent.ACTION_SENDTO);
        sms.setData(Uri.parse("smsto: 114"));
        String texteSMS = getString(R.string.texte_sms_114, positionStr, Variables.texteSMSpart1, Variables.texteSMSpart2);
        sms.putExtra("sms_body", texteSMS);
        sms.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        if(sms.resolveActivity(MyHelper.getInstance().recupPackageManager()) != null) {
            MyHelper.getInstance().launchActivity(sms);
        } else {
            if (BuildConfig.DEBUG){
                Log.i("SECUSERV"," appli message pas disponible");}
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        model.stopUpdatePosition();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (Variables.requestingLocationUpdates) {
            model.updatePosition();
        }
    }

}
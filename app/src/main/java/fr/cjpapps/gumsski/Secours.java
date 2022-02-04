package fr.cjpapps.gumsski;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import android.os.Handler;
import android.text.Html;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Locale;

public class Secours extends AppCompatActivity {

    public static final int DELAY = 60;  // en secondes avant changer intervalle

    TextView positionUn = null;
    TextView positionDeux = null;
    TextView accuracy = null;
    TextView europe = null;
    TextView textEurope = null;
    Button boutonEurope = null;
    TextView france = null;
    TextView textFrance = null;
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
    float precision;
    ModelLocation model;

    ActivityResultLauncher<String[]> locationPermissionRequest =
            registerForActivityResult(new ActivityResultContracts
                            .RequestMultiplePermissions(), result -> {
                        Boolean fineLocationGranted = result.get(
                                Manifest.permission.ACCESS_FINE_LOCATION);
                        Boolean coarseLocationGranted = result.get(
                                Manifest.permission.ACCESS_COARSE_LOCATION);
                        if (fineLocationGranted != null && fineLocationGranted) {
                            // Precise location access granted.
                            Log.i("SECUSERV", "on obtient la permission");
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_secours);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        positionDeux = findViewById(R.id.coordonnees);
        accuracy = findViewById(R.id.accuracy);
        textEurope = findViewById(R.id.texte_europe);
        boutonEurope = findViewById(R.id.bouton_europe);
        textFrance = findViewById(R.id.texte_france);
        boutonFrance = findViewById(R.id.bouton_france);
        textSuisse = findViewById(R.id.texte_suisse);
        boutonOCVS = findViewById(R.id.bouton_ocvs);
        boutonREGA = findViewById(R.id.bouton_rega);
        textItalie = findViewById(R.id.texte_italie);
        boutonItalie = findViewById(R.id.bouton_italie);

        positionDeux.setText("En attente position");

        model = new ViewModelProvider(this).get(ModelLocation.class);

        if (ActivityCompat.checkSelfPermission( this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            // You can use the API that requires the permission.
            Log.i("SECUSERV", "on a déja la permission");
            model.updatePosition();
            Variables.requestingLocationUpdates = true;
        } else {
            // You can directly ask for the permission.
            // The registered ActivityResultCallback gets the result of this request.
            Log.i("SECUSERV", "demande permissions");
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
                    String result = getString(R.string.lat_lon, latitudeStr, longitudeStr);
                    Toast.makeText(Secours.this, result, Toast.LENGTH_SHORT).show();
                    if(precision <= 50f) {
                        positionDeux.setText(result);
                        accuracy.setText(String.valueOf(precision));
                    }else{
                        positionDeux.setText("Pas de position précise");
                    }
                }else{
                    positionDeux.setText("Pas de GPS");
                }
            }
        };
        model.getPositionActuelle().observe(this, positionObserver);

// timer pour ralentir les mises à jour GPS après un délai
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                changeLocationInterval();
            }
        }, 1000*DELAY);


        textEurope.setText(Aux.fromHtml(getString(R.string.texte_europe)));
        boutonEurope.setText(Aux.fromHtml(getString(R.string.texte_bouton_europe)));
        boutonEurope.setTextColor(ContextCompat.getColor(getApplicationContext(),R.color.colorEnvoi));
        textFrance.setText(Aux.fromHtml(getString(R.string.texte_france)));
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

        if (getSupportActionBar() != null){
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
    }

    void changeLocationInterval () {
        model.changeRequest();
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
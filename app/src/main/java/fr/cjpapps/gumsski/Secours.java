package fr.cjpapps.gumsski;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.text.Html;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class Secours extends AppCompatActivity {

    TextView position = null;
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_secours);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        position = findViewById(R.id.intro_secours);
        boutonEurope = findViewById(R.id.bouton_europe);
        boutonFrance = findViewById(R.id.bouton_france);
        boutonOCVS = findViewById(R.id.bouton_ocvs);
        boutonREGA = findViewById(R.id.bouton_rega);
        boutonItalie = findViewById(R.id.bouton_italie);

        latitude = 44.97704;
        latitudeStr = String.format("%.4f", latitude);
        longitude = 2.50506;
        longitudeStr = String.format("%.4f", longitude);
        position.setText(Aux.fromHtml(getString(R.string.ta_position,latitudeStr,longitudeStr)));

        if (getSupportActionBar() != null){
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
    }

    /*    TextView affichage = null;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_secours);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        affichage = findViewById(R.id.intro_secours);
        affichage.setText(Aux.fromHtml(getString(R.string.pas_fait)));

        if (getSupportActionBar() != null){
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
    }  */
}
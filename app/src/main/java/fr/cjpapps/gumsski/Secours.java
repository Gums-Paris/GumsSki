package fr.cjpapps.gumsski;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;

import android.text.Html;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class Secours extends AppCompatActivity {

    TextView positionUn = null;
    TextView positionDeux = null;
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

        positionDeux = findViewById(R.id.coordonnees);
        textEurope = findViewById(R.id.texte_europe);
        boutonEurope = findViewById(R.id.bouton_europe);
        textFrance = findViewById(R.id.texte_france);
        boutonFrance = findViewById(R.id.bouton_france);
        textSuisse = findViewById(R.id.texte_suisse);
        boutonOCVS = findViewById(R.id.bouton_ocvs);
        boutonREGA = findViewById(R.id.bouton_rega);
        textItalie = findViewById(R.id.texte_italie);
        boutonItalie = findViewById(R.id.bouton_italie);

        latitude = 44.97704;
        latitudeStr = String.format("%.4f", latitude);
        longitude = 2.50506;
        longitudeStr = String.format("%.4f", longitude);
        positionDeux.setText(getString(R.string.lat_lon, latitudeStr, longitudeStr));
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
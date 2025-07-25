package fr.cjpapps.gumsski;

import android.os.Build;
import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.text.Html;
import android.view.View;
import android.widget.TextView;

public class Preferences extends AppCompatActivity {

    TextView affichage = null;
    View conteneur = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_preferences);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        //Adaptation au fonctionnement EdgeToEdge
        conteneur = findViewById(R.id.choix);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            EdgeToEdge.enable(this);
            //     statusBarColor(conteneur,R.color.colorPrimaryDark);
            HandleInsets.placeInsets(this, conteneur);
        }

        affichage = findViewById(R.id.intro_prefs);
        affichage.setText(Aux.fromHtml(getString(R.string.prefs)));

        if (getSupportActionBar() != null){
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
    }
}
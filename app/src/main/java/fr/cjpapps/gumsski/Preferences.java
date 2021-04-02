package fr.cjpapps.gumsski;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.text.Html;
import android.widget.TextView;

public class Preferences extends AppCompatActivity {

    TextView affichage = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_preferences);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        affichage = findViewById(R.id.intro_prefs);
        affichage.setText(Html.fromHtml(getString(R.string.prefs), Html.FROM_HTML_MODE_LEGACY));

        if (getSupportActionBar() != null){
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
    }
}
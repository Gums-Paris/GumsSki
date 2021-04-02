package fr.cjpapps.gumsski;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.text.Html;
import android.view.View;
import android.widget.TextView;

public class Secours extends AppCompatActivity {

    TextView affichage = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_secours);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        affichage = findViewById(R.id.intro_secours);
        affichage.setText(Html.fromHtml(getString(R.string.pas_fait), Html.FROM_HTML_MODE_LEGACY));

        if (getSupportActionBar() != null){
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
    }
}
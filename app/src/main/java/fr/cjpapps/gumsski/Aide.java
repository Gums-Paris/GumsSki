package fr.cjpapps.gumsski;

import android.os.Bundle;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

public class Aide extends AppCompatActivity {

    TextView affichage = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_aide);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        affichage = findViewById(R.id.texthelp);
        affichage.setText(Html.fromHtml(getString(R.string.helptext), Html.FROM_HTML_MODE_LEGACY));
        affichage.setMovementMethod(LinkMovementMethod.getInstance());

        if (getSupportActionBar() != null){
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
    }
}

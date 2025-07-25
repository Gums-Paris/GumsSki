package fr.cjpapps.gumsski;

import android.os.Build;
import android.os.Bundle;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.view.View;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

public class Aide extends AppCompatActivity {

    TextView affichage = null;
    View conteneur = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_aide);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        //Adaptation au fonctionnement EdgeToEdge
        conteneur = findViewById(R.id.help);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            EdgeToEdge.enable(this);
            //     statusBarColor(conteneur,R.color.colorPrimaryDark);
            HandleInsets.placeInsets(this, conteneur);
        }

        affichage = findViewById(R.id.texthelp);
        affichage.setText(Aux.fromHtml(getString(R.string.helptext)));
        affichage.setMovementMethod(LinkMovementMethod.getInstance());

        if (getSupportActionBar() != null){
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
    }
}

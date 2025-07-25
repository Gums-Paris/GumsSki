package fr.cjpapps.gumsski;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.os.Build;
import android.os.Bundle;
import android.text.method.LinkMovementMethod;
import android.view.View;
import android.widget.TextView;

public class APropos extends AppCompatActivity {

    TextView affichage = null;
    View conteneur = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_apropos);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        //Adaptation au fonctionnement EdgeToEdge
        conteneur = findViewById(R.id.about);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            EdgeToEdge.enable(this);
            //     statusBarColor(conteneur,R.color.colorPrimaryDark);
            HandleInsets.placeInsets(this, conteneur);
        }

        affichage = findViewById(R.id.legums);
        affichage.setText(Aux.fromHtml(getString(R.string.aproposdugums)));
        /* pour pouvoir mettre un texte de lien http différent de la cible du lien il faut rajouter la ligne qui suit
         *  si on veut bien avoir un texte identique à la cible, on peut ne pas mettre cette ligne mais il faut alors rajouter
         *  android:autoLink="email" ou "web" selon le cas dans le layout du TextView*/
        affichage.setMovementMethod(LinkMovementMethod.getInstance());

        if (getSupportActionBar() != null){
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
    }
}
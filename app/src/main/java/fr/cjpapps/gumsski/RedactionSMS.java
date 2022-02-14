package fr.cjpapps.gumsski;

import android.content.Intent;
import android.os.Bundle;

import com.google.android.material.snackbar.Snackbar;

import androidx.appcompat.app.AppCompatActivity;

import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.appcompat.widget.Toolbar;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import fr.cjpapps.gumsski.databinding.ActivityRedactionSmsBinding;

public class RedactionSMS extends AppCompatActivity {

    private AppBarConfiguration appBarConfiguration;
    private ActivityRedactionSmsBinding binding;

    TextView titre1, titre2;
    EditText partie1, partie2;
    Button sauve;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_redaction_sms);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        titre1 = findViewById(R.id.partie_un);
        partie1 = findViewById(R.id.contenu_un);
        titre2 = findViewById(R.id.partie_deux);
        partie2 = findViewById(R.id.contenu_deux);
        sauve = findViewById(R.id.sauvegarde);

        titre1.setText(R.string.titre_partie_un);
        partie1.setText(Variables.texteSMSpart1);
        titre2.setText(R.string.titre_partie_deux);
        partie2.setText(Variables.texteSMSpart2);

        sauve.setOnClickListener(view -> {
            if (partie1 != null) {
                Variables.texteSMSpart1 = partie1.getText().toString();
                if (BuildConfig.DEBUG){
                Log.i("SECUSERV"," sms1 = "+Variables.texteSMSpart1);
            }}
            if (partie2 != null) {
                Variables.texteSMSpart2 = partie2.getText().toString();
                if (BuildConfig.DEBUG){
            Log.i("SECUSERV"," sms2 = "+Variables.texteSMSpart2);}
            }
            finish();
        });


    }

}
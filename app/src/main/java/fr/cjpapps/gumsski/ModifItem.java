package fr.cjpapps.gumsski;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;

import java.util.ArrayList;
import java.util.HashMap;

public class ModifItem extends AppCompatActivity {

/*  Génère un couple TextView+EdtText pour chaque champ décrit dans Attributs.java grâce à
*   AuxAvecContext.buildForm, et les affiche l'un au dessus de l'autre. puis on ajoute les
*   boutons SAUVEGARDER et ANNULER et on traite le formulaire.*/

    private String idItem;
    private String sortieId;
    SharedPreferences mesPrefs;
    SharedPreferences.Editor editeur;
    private final HashMap<String, String> requestParams = new HashMap<>();
    private final HashMap<String, String> postParams = new HashMap<>();
    private final String[] taskParams = new String[6];
    ModelItem model;
    Intent result = new Intent();
    ArrayList<String[]> fieldParams = new ArrayList<>();
    TaskRunner taskRunner = new TaskRunner();

    @SuppressLint("UseCompatLoadingForDrawables")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_modif_item);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        if (getSupportActionBar() != null){
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

// fabrication du formulaire d'édition
        LinearLayout parentLayout = findViewById(R.id.parent);
        LinearLayout lesBoutons = new LinearLayout(this);
        lesBoutons.setOrientation(LinearLayout.HORIZONTAL);
        LinearLayout.LayoutParams boutonParams = new LinearLayout.LayoutParams
                (LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT, 0);
        boutonParams.setMargins(90,20, 0, 20);
        AuxAvecContext aux = new AuxAvecContext(this);
        fieldParams = aux.buildForm(parentLayout);  // les champs pour éditer

// et les boutons
        int idS = 301;
        Button boutonSave = new Button(this);
        boutonSave.setId(idS);
        boutonSave.setLayoutParams(boutonParams);
        boutonSave.setText(getString(R.string.save));
        boutonSave.setBackground(getDrawable(R.drawable.buttonshape));
        boutonSave.setTextColor(getColor(R.color.blanc));
        lesBoutons.addView(boutonSave);
        boutonSave.setOnClickListener(clickListenerSauv);

        int idA = 302;
        Button boutonAnnul = new Button(this);
        boutonAnnul.setId(idA);
        boutonAnnul.setLayoutParams(boutonParams);
        boutonAnnul.setText(getString(R.string.cancel));
        boutonAnnul.setBackground(getDrawable(R.drawable.buttonshape));
        boutonAnnul.setTextColor(getColor(R.color.blanc));
        lesBoutons.addView(boutonAnnul);
        boutonAnnul.setOnClickListener(clickListenerCancel);

        parentLayout.addView(lesBoutons);

        mesPrefs = MyHelper.getInstance(getApplicationContext()).recupPrefs();
        model = new ViewModelProvider(this).get(ModelItem.class);

// récup id de l'item dans l'intent et chargement de l'item (ici la logistique)
        Intent intent = getIntent();
        if (intent != null) {
            if (intent.hasExtra("itemChoisi")){
                idItem = intent.getStringExtra("itemChoisi");
                sortieId = intent.getStringExtra("sortieId");
                if (BuildConfig.DEBUG){
                Log.i("SECUSERV", "item "+idItem+", "+sortieId);}
            }
            AuxReseau.recupInfo(Constantes.JOOMLA_RESOURCE_1,sortieId);
        }

// observateur de réception de l'item (ici la ogistique)
        final Observer<HashMap<String, String>> monItemObserver = itemTravail -> {
            if(itemTravail != null) {
                if (BuildConfig.DEBUG){
                Log.i("SECUSERV", " hashmap "+itemTravail.toString());}
                for (String[] params : fieldParams) {
                    if (BuildConfig.DEBUG){
                    Log.i("SECUSERV", "champ, id "+params[0]+", "+params[3]);}
                    EditText unChamp = findViewById(Integer.parseInt(params[4]));
                    unChamp.setText(itemTravail.get(params[0]));
                }
            } else {
                if (BuildConfig.DEBUG){
                Log.i("SECUSERV", " itemTravail est null ");}
                setResult(RESULT_CANCELED, result);
                finish();
            }
        };
        model.getMonItem().observe(this, monItemObserver);
    }

// click listener pour SAUVEGARDER
    private final View.OnClickListener clickListenerSauv = new View.OnClickListener() {
        @Override
        public void onClick(View v) {

            postParams.put("id",idItem);
            postParams.put("state","1");

            for (String[] params : fieldParams) {
                EditText luChamp = findViewById(Integer.parseInt(params[4]));
                if (luChamp != null) {
                    postParams.put(params[0], luChamp.getText().toString());
//                    Log.i("ATTR ", postParams.toString());
                }
            }

            requestParams.put("app", Constantes.JOOMLA_APP);
            requestParams.put("resource", Constantes.JOOMLA_RESOURCE_1);
            requestParams.put("format", "json");
            String stringRequest = AuxReseau.buildRequest(requestParams);
            taskParams[0] = Variables.urlActive+stringRequest;
            taskParams[1] = AuxReseau.buildRequest(postParams);
            taskParams[2] = "Content-Type";
            taskParams[3] = "application/x-www-form-urlencoded ; utf-8";
            taskParams[4] = "X-Authorization";
            taskParams[5] = "Bearer "+ mesPrefs.getString("auth", "");
//            Log.i("task ", taskParams[1]+"  "+taskParams[5]);

            if (Variables.isNetworkConnected) {
                taskRunner.executeAsync(new EnvoiInfosGums(taskParams), AuxReseau::decodeRetourPostItem);
            }
/*            result.putExtra("itemchoisi", idItem);
            result.putExtra("sortieId", sortieId); */
            setResult(RESULT_OK, result);
            finish();
        }
    };

// click listener pour ANNULER
    private final View.OnClickListener clickListenerCancel = view -> {
        Intent result = new Intent();
        result.putExtra("itemchoisi", idItem);
        result.putExtra("sortieId", sortieId);
        setResult(RESULT_OK, result);
        finish();
    };

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_logistique, menu);
        return true;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        if (id == R.id.help) {
            Intent lireAide = new Intent(this, Aide.class);
            startActivity(lireAide);
            return true;
        }
        if (id == R.id.meteo) {
            Intent meteo = new Intent(this, Meteo.class);
            startActivity(meteo);
            return true;
        }
        if (id == R.id.secours) {
            Intent secours = new Intent(this, Secours.class);
            startActivity(secours);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
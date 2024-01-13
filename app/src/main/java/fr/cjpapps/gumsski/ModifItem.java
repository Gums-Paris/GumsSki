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
    private final HashMap<String, String> postParams = new HashMap<>();
    ModelItem model;
    Intent result = new Intent();
    ArrayList<String[]> fieldParams = new ArrayList<>();
    NetworkConnectionMonitor connectionMonitor;

    @SuppressLint("UseCompatLoadingForDrawables")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_modif_item);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        fieldParams.clear();
        postParams.clear();
 /* on ne met pas la flèche de retour arrière dans la barre supérieure. On expliquera au client qu'il ne
 peut pas sortir par retour arrière pour ne pas laisser l'item verrouillé (voir onBackPressed())
        if (getSupportActionBar() != null){
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }   */

// mise en place de la surveillance réseau qui sera activée dans onResume
        connectionMonitor = NetworkConnectionMonitor.getInstance();
        connectionMonitor.observe(this, new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean isConnected) {
                if (BuildConfig.DEBUG) Log.i("SECUSERV", "modif log conMan observe "+isConnected);
                Variables.isNetworkConnected = isConnected;
            }
        });

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
// en utilisant la version de GET qui fait en même temps un verrouillage de l'item (task = edit).
        Intent intent = getIntent();
        if (intent != null) {
            if (intent.hasExtra("itemChoisi")){
                idItem = intent.getStringExtra("itemChoisi");
                sortieId = intent.getStringExtra("sortieId");
                if (BuildConfig.DEBUG){
                Log.i("SECUSERV", "item "+idItem+", "+sortieId);}
                if (Variables.isNetworkConnected) {
                    AuxReseau.recupInfo(Constantes.JOOMLA_RESOURCE_1, sortieId, "edit");
                }else{
                    String message = "Désolé, yapa de réseau";
                    envoiAlerte(message);
                }
            }
        }

// observateur de réception de l'item (ici la logistique)
        final Observer<HashMap<String, String>> monItemObserver = itemTravail -> {
            if(itemTravail != null) {
                if (BuildConfig.DEBUG){
                Log.i("SECUSERV", " hashmap "+itemTravail);}
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
    }  //fin de onCreate

    @Override
    protected void onPause(){
        if (Variables.isRegistered){
            if (BuildConfig.DEBUG) Log.i("SECUSERV", "modif log onpause unregister");
            connectionMonitor.unregisterDefaultNetworkCallback();
            Variables.isRegistered=false;
        }
        super.onPause();
    }
    // à partir de là on surveille la disponibilité d'Internet
    @Override
    protected void onResume(){
        super.onResume();
        if (BuildConfig.DEBUG) Log.i("SECUSERV", "modif log onresume register");
        connectionMonitor.registerDefaultNetworkCallback();
        Variables.isRegistered=true;
    }

    // click listener pour SAUVEGARDER (fait checkout + save + checkin à travers com_api)
    private final View.OnClickListener clickListenerSauv = new View.OnClickListener() {
        @Override
        public void onClick(View v) {

            postParams.put("id",idItem);
            postParams.put("sortieid",sortieId );
//            postParams.put("state","1");
            for (String[] params : fieldParams) {
                EditText luChamp = findViewById(Integer.parseInt(params[4]));
                if (luChamp != null) {
                    postParams.put(params[0], luChamp.getText().toString());
                }
            }
            AuxReseau.envoiInfo(Constantes.JOOMLA_RESOURCE_1,postParams, "","");

            setResult(RESULT_OK, result);
            finish();
        }
    };

// click listener pour ANNULER (fait un checkin  à travers com_api)
    private final View.OnClickListener clickListenerCancel = view -> {

        postParams.put("id",idItem);
        postParams.put("sortieid",sortieId );
        AuxReseau.envoiInfo(Constantes.JOOMLA_RESOURCE_1,postParams, sortieId,"checkin");

        Intent result = new Intent();
        result.putExtra("itemchoisi", idItem);
        result.putExtra("sortieId", sortieId);
        setResult(RESULT_OK, result);
        finish();
    };

    @Override
    public void onBackPressed() {
// si l'usager  presse le bouton retour arrière on lui demande d'utiliser les boutons sauv ou annul
// pour quitter la page
        String message = getString(R.string.leave_edit);
        DialogAlertes endEdit = DialogAlertes.newInstance(message);
        endEdit.show(getSupportFragmentManager(), "quitterEdit");
    }

    protected void envoiAlerte(String message){
        DialogAlertes modifLogistique = DialogAlertes.newInstance(message);
        modifLogistique.show(getSupportFragmentManager(), "infoLogistique");
    }

}
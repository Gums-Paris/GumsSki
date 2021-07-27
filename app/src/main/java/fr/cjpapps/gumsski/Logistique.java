package fr.cjpapps.gumsski;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;

import java.util.HashMap;

public class Logistique extends AppCompatActivity {

    TextView affichage = null;
    TextView hotelChauffeurs = null;
    TextView tphChauffeurs = null;
    TextView dinerRetour = null;
    TextView deposes = null;
    TextView reprises = null;
    TextView coursesPrevues = null;
    String sortieId = "";
    String titreSortie = "";
    String itemId = "";
    ModelItem model = null;
    SharedPreferences mesPrefs;
    SharedPreferences.Editor editeur;

// launcher pour ModifItem. Au retour on relance Logistique pour afficher la nouvelle version.
// L'intent transporte à la fois itemId et sortieId parce que sortieId est nécessaire pour
// exécuter Logistique et itemId est nécessaire pour travailler sur la base de données
    final private ActivityResultLauncher<Intent> modifItemResultLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK) {
                    Log.i("SECUSERV", "retour de ModifItem OK ");
/*                    Intent data = result.getData();
                    if (data != null) {
                        if (data.hasExtra("itemChoisi")){
                            itemId = data.getStringExtra("itemChoisi");
                            sortieId = data.getStringExtra("sortieId");
                        }
                    }
                    Intent affiche =new Intent(this, Logistique.class);
                    affiche.putExtra("itemchoisi", itemId);
                    affiche.putExtra("sortieid", sortieId);
                    startActivity(affiche); */
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_logistique);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        if (getSupportActionBar() != null){
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        mesPrefs = MyHelper.getInstance().recupPrefs();
        editeur = mesPrefs.edit();
        editeur.putBoolean("logistiqueExiste", true);
        editeur.apply();

        Intent intent = getIntent();
        if (intent != null) {
            sortieId = intent.getStringExtra("sortieid");
        }
        titreSortie = mesPrefs.getString("titre","");

        affichage = findViewById(R.id.intro_logistic);
 //       String titreComplet = Aux.fromHtml(getString(R.string.titre_logistique))+infoSortie;
        affichage.setText(getResources().getString(R.string.titre_logistique, titreSortie));
        hotelChauffeurs = findViewById(R.id.champ_hotel);
        tphChauffeurs = findViewById(R.id.champ_tph);
        dinerRetour = findViewById(R.id.champ_diner);
        deposes = findViewById(R.id.champ_deposes);
        reprises = findViewById(R.id.champ_reprises);
        coursesPrevues = findViewById(R.id.champ_courses);
        cleanupTextviews();

        ExtendedFloatingActionButton fabModif = findViewById(R.id.fab_modif);
        fabModif.hide();

// on affiche le bouton modifier si le user est le responsable du car ou un Res ou un Admin
        String userActuel = mesPrefs.getString("userId","0");
        if (Variables.listeChefs.contains(userActuel) || Constantes.listeAdmins.contains(userActuel)) {
            fabModif.show();
            fabModif.setOnClickListener(view -> {
                if (mesPrefs.getBoolean("logistiqueExiste", false)) {
                    Intent choisi = new Intent(this, ModifItem.class);
                    choisi.putExtra("itemChoisi", itemId);
                    choisi.putExtra("sortieId", sortieId);
                    modifItemResultLauncher.launch(choisi);
                }else{
                    envoiAlerte("La logistique doit être créée par un admin avant de pouvoir" +
                            "être modifiée");
                }
            });
       }

        model = new ViewModelProvider(this).get(ModelItem.class);
        Log.i("SECUSERV", "logistique charge données ");
        AuxReseau.recupInfo(Constantes.JOOMLA_RESOURCE_1, sortieId);

// flagItem est false si la récup des infos logistique se passe mal. Sinon il est true même si la
// logistique est vide ; ce cas se traite dans l'observer de Item
        final Observer<Boolean> flagItemObserver = retour -> {
            if (!retour) {
                String message = "Données non disponibles";
                new Handler(Looper.getMainLooper()).postDelayed(() -> {
                    envoiAlerte(message);
                    finish();
                }, 200); // délai 0.2 sec
            }
        };
        model.getFlagItem().observe(this, flagItemObserver);

// flagModif est géré par AuxReseau.decodeRetourPostItem()
// flagModif passe à false en cas d'erreur dans la transaction avec gumsparis
        final Observer<Boolean> flagModifObserver = new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean retour) {
                 if (!retour) {
// Il faut freiner un peu pour laisser le temps au message d'erreur d'être rangé dans les prefs
                    new Handler().postDelayed(new Runnable() {
                        public void run() {
                            String message = mesPrefs.getString("errMsg", "")+" \ncode "+mesPrefs.getString("errCode", "");
                            envoiAlerte(message);
                        }
                    }, 200); // délai 0.2 sec
                 }else{
                     AuxReseau.recupInfo(Constantes.JOOMLA_RESOURCE_1,sortieId);
                 }
            }
        };
        model.getFlagModif().observe(this, flagModifObserver);

// l'observer de Item - tester pour logistique absente et selon le cas afficher alerte
        final Observer<HashMap<String, String>> ItemObserver = item -> {
            if (item != null){
                cleanupTextviews();
                if (!(mesPrefs.getBoolean("logistiqueExiste", false))){
                    envoiAlerte("Cette logistique n'a pas été créée");
                }else{
                    editeur.putBoolean("logistiqueExiste", true);
                    editeur.apply();
                    itemId = item.get("id");
                    hotelChauffeurs.setText(item.get("hotelchauffeurs"));
                    tphChauffeurs.setText(item.get("tphchauffeurs"));
                    dinerRetour.setText(item.get("dinerretour"));
                    deposes.setText(item.get("deposes"));
                    reprises.setText(item.get("reprises"));
                    coursesPrevues.setText(item.get("coursesprevues"));
                }
            }
        };
        model.getMonItem().observe(this, ItemObserver);

    }   // fin de onCreate()

    protected void envoiAlerte(String message){
        DialogAlertes infoUtilisateur = DialogAlertes.newInstance(message);
        infoUtilisateur.show(getSupportFragmentManager(), "infoutilisateur");
    }

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

    protected void  cleanupTextviews(){
        hotelChauffeurs.setText("");
        tphChauffeurs.setText("");
        dinerRetour.setText("");
        deposes.setText("");
        reprises.setText("");
        coursesPrevues.setText("");
    }
}
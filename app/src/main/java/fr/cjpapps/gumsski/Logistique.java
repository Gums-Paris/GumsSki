package fr.cjpapps.gumsski;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import android.os.Handler;
import android.os.Looper;
import android.text.Html;
import android.text.Spanned;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

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
    ModelItem model = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_logistique);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        if (getSupportActionBar() != null){
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        sortieId = getIntent().getStringExtra("sortieid");
        titreSortie = getIntent().getStringExtra("titreSortie");
        affichage = findViewById(R.id.intro_logistic);
 //       String titreComplet = Aux.fromHtml(getString(R.string.titre_logistique))+infoSortie;
        affichage.setText(getResources().getString(R.string.titre_logistique, titreSortie));
        hotelChauffeurs = findViewById(R.id.champ_hotel);
        tphChauffeurs = findViewById(R.id.champ_tph);
        dinerRetour = findViewById(R.id.champ_diner);
        deposes = findViewById(R.id.champ_deposes);
        reprises = findViewById(R.id.champ_reprises);
        coursesPrevues = findViewById(R.id.champ_courses);
        model = new ViewModelProvider(this).get(ModelItem.class);
        AuxReseau.recupInfo(Constantes.JOOMLA_RESOURCE_1, sortieId);

// flagItem est false si la récup des infos logistique se passe mal. Sinon il est true même si la
// logistique est vide ; ce cas se traite dans l'observer de Item
        final Observer<Boolean> flagItemObserver = retour -> {
            Log.i("SECUSERV", "flagItem " + retour);
            if (!retour) {
                String message = "Données non disponibles";
                new Handler(Looper.getMainLooper()).postDelayed(() -> {
                    envoiAlerte(message);
                    finish();
                }, 200); // délai 0.2 sec
            }
        };
        model.getFlagItem().observe(this, flagItemObserver);

// l'observer de Item - tester pour logistique absente et selon le cas afficher alerte
        final Observer<HashMap<String, String>> ItemObserver = item -> {
            if (item != null){
//                cleanupTextviews();
                Log.i("SECUSERV", "observed " + item.toString());
                if (item.containsKey("logistique")){
// présence de la clé logistique veut dire ("logistique", "absente")
                    envoiAlerte("Cette logistique n'a pas été créée");
                }else{
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

    }

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
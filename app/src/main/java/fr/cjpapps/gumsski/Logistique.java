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

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.TimeZone;

public class Logistique extends AppCompatActivity {

    TextView affichage = null;
    TextView hotelChauffeurs = null;
    TextView tphChauffeurs = null;
    TextView dinerRetour = null;
    TextView deposes = null;
    TextView reprises = null;
    TextView coursesPrevues = null;
    ExtendedFloatingActionButton fabModif = null;
    private String sortieId = "";
    private String logistiqueSortieId ="";
    String titreSortie = "";
    String itemId = "";
    ModelItem model = null;
    SharedPreferences mesPrefs;
    SharedPreferences.Editor editeur;
    int checkedOut = 0;
    String checkedOutTime = "";
    String verrou = "";
    String canEdit = "";
    boolean canEditBool = false;

// launcher pour ModifItem.
    final private ActivityResultLauncher<Intent> modifItemResultLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK) {
                    if (BuildConfig.DEBUG){
                    Log.i("SECUSERV", "retour de ModifItem OK ");}
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
            if (BuildConfig.DEBUG){
            Log.i("SECUSERV", "Logistique sortieId = "+sortieId);}
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

// floating action button pour éditer la logistique
        fabModif = findViewById(R.id.fab_modif);
        fabModif.hide();
/*  on affichera ce bouton modifier dans l'observer de item si le user est le responsable du car ou un Res ou
 *  un Admin à condition qu'il n'y ait pas verrouillage ; en cas de verrouillage, on déverrouillera si
 *  le verrouillage date de plus de 10 minutes*/

        model = new ViewModelProvider(this).get(ModelItem.class); // son constructeur ne fait rien
        if (Variables.isNetworkConnected) {
            if (BuildConfig.DEBUG){
            Log.i("SECUSERV", "logistique charge données réso");}
            AuxReseau.recupInfo(Constantes.JOOMLA_RESOURCE_1, sortieId, "");
        }else{
            if (BuildConfig.DEBUG){
            Log.i("SECUSERV", "logistique charge données from prefs");}
            model.loadDatafromPrefs();
        }

// flagItem est false si la récup par le réseau des infos logistique se passe mal. Sinon il est true
// même si la logistique est vide ; ce cas se traite dans l'observer de Item.
// Si false on essaye les prefs
        final Observer<Boolean> flagItemObserver = retour -> {
            if (!retour) {
                String message = "Données réseau pas disponibles";
                envoiAlerte(message);
                model.loadDatafromPrefs();
            }else{
                if (BuildConfig.DEBUG){
                Log.i("SECUSERV", "flag logistique = "+retour);}
            }
        };
        model.getFlagItem().observe(this, flagItemObserver);

// flagModif est géré par AuxReseau.decodeRetourPostItem()
// si true on recharge la logistique modifiée
// flagModif passe à false en cas d'erreur dans la transaction avec gumsparis
        final Observer<Boolean> flagModifObserver = retour -> {
             if (!retour) {
// Il faut freiner un peu pour laisser le temps au message d'erreur d'être rangé dans les prefs
                new Handler(Looper.getMainLooper()).postDelayed(() -> {
                }, 200); // délai 0.2 sec
                 String message = mesPrefs.getString("errMsg", "")+" \ncode "+mesPrefs.getString("errCode", "");
                 envoiAlerte(message);
             }else{
                 AuxReseau.recupInfo(Constantes.JOOMLA_RESOURCE_1,sortieId, "");
             }
        };
        model.getFlagModif().observe(this, flagModifObserver);

// l'observer de Item - on teste pour logistique absente et selon le cas afficher alerte
        final Observer<HashMap<String, String>> ItemObserver = item -> {
            if (item != null){
                if (!(mesPrefs.getBoolean("logistiqueExiste", false))){
                    cleanupTextviews();
                    envoiAlerte("Cette logistique n'a pas été créée");
                }else{
                    fabModif.hide();
                    editeur.putBoolean("logistiqueExiste", true);
                    editeur.apply();
// itemId est l'identifiant de la logistique dans la base
                    itemId = item.get("id");
                    editeur.putString("logistiqueId", itemId);
                    logistiqueSortieId = item.get("sortieid");
                    editeur.putString("logistiqueDispo", logistiqueSortieId);
                    editeur.apply();
                    hotelChauffeurs.setText(item.get("hotelchauffeurs"));
                    tphChauffeurs.setText(item.get("tphchauffeurs"));
                    dinerRetour.setText(item.get("dinerretour"));
                    deposes.setText(item.get("deposes"));
                    reprises.setText(item.get("reprises"));
                    coursesPrevues.setText(item.get("coursesprevues"));
                    checkedOut = Aux.stringToInt(item.get("checked_out"));
                    checkedOutTime = item.get("checked_out_time");
                    verrou = item.get("verrou");
                    canEdit = item.get("canedit");
                    canEditBool = Boolean.parseBoolean(canEdit);
                    if (BuildConfig.DEBUG){
                    Log.i("SECUSERV", "canEdit = "+canEdit+" canEditBool = "+canEditBool);}
                    if (canEditBool) {
                        boutonModifier();
                    }
// le check_out pour édition se fait à l'entrée dans ModifItem
                }
            }else{
                editeur.putBoolean("logistiqueExiste", false);
                editeur.apply();
                cleanupTextviews();
                new Handler(Looper.getMainLooper()).postDelayed(() -> {
                }, 200); // délai 0.2 sec
                envoiAlerte("pas d'info disponible");
            }
        };
        model.getMonItem().observe(this, ItemObserver);

    }   // fin de onCreate()

    protected void boutonModifier() {
/* on affiche le bouton modifier si le user est autorisé à éditer c'est-à-dire canEdit est vrai
*  il est soit admin, soit responsable du car, soit Res de groupe
*  à condition qu'il n'y ait pas verrouillage ; en cas de verrouillage, on déverrouille si le verrouillage
*  date de plus de 10 minutes*/
            if (Variables.isNetworkConnected) {
                if (checkedOut > 0) essaiCheckin();
                if (checkedOut == 0) {
                    fabModif.show();
                    fabModif.setOnClickListener(view -> {
                        if (mesPrefs.getBoolean("logistiqueExiste", false)) {
                            Intent choisi = new Intent(this, ModifItem.class);
                            choisi.putExtra("itemChoisi", mesPrefs.getString("logistiqueId", "0"));
                            choisi.putExtra("sortieId", sortieId);
                            modifItemResultLauncher.launch(choisi);
                        } else {
                            envoiAlerte("La logistique doit être créée par un admin avant de pouvoir" +
                                    "être modifiée");
                        }
                    });
                }else{
                    envoiAlerte(verrou);
                }
            }else{
                envoiAlerte("Pas possible de modifier la logistique sans accès réseau");
            }
//        }
    }

    protected void essaiCheckin(){
// si le verrouillage date de plus de 10 minutes on déverrouille (checkOut = 0)
        Date dateCheckedOut = null;
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
            sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
            dateCheckedOut = sdf.parse(checkedOutTime);
        } catch (Exception e) {
            e.printStackTrace();
        }
        final Calendar now = Calendar.getInstance();
        now.add(Calendar.MINUTE, -10);
        Date nowMoinsDelai = now.getTime();
        if (BuildConfig.DEBUG){
        Log.i("SECUSERV", "dates check_out et now-délai= "+dateCheckedOut+"  "+nowMoinsDelai);}
        if (nowMoinsDelai.after(dateCheckedOut)) {
                checkedOut = 0;
                checkedOutTime = "0000-00-00 00:00:00";
                verrou = "";
        }
    }

    protected void envoiAlerte(String message){
        DialogAlertes infoLogistique = DialogAlertes.newInstance(message);
        infoLogistique.show(getSupportFragmentManager(), "infoLogistique");
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
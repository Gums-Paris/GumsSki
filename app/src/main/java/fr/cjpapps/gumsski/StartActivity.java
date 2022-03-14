package fr.cjpapps.gumsski;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.Network;
import android.os.Bundle;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;

public class StartActivity extends AppCompatActivity {

    final static String PREF_FILE = "authAccess";
    private ArrayList<String> nomsItems = new ArrayList<>();//    ArrayList<Item> listeItems = new ArrayList<>();
    private ArrayList<HashMap<String,String>> listeDesItems = new ArrayList<>();
    TextView affichageTitre =null;
    TextView panicDepart = null;
    TextView dateList = null;
    ProgressBar patience = null;
    private RecyclerView recyclerView;
    private RecyclerViewGenericAdapter monAdapter ;
    ModelListeSorties modelSorties = null;
    SharedPreferences mesPrefs;
    SharedPreferences.Editor editeur;
    Aux methodesAux;
    ConnectivityManager conMan ;

/*  Le changement de site internet gumsparis se fait ligne 100
*
*   Dans les sharedPreferences :
*       datelist == date à laquelle on a récupéré la liste des sorties
*       date == date de la sortie choisie dans la liste des sorties
*       datedata == date de la sortie à laquelle correspond la liste de participants disponible dans les prefs
*       today == date du jour
*       dateRecupData == date où on a récupéré la liste des participants */

// servira à lancer AuthActivity puis MainActivity si RESULT_OK
    final private ActivityResultLauncher<Intent> authActivityResultLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
        result -> {
            if (result.getResultCode() == Activity.RESULT_OK) {
                // There are no request codes
                Intent data = result.getData();
                Intent groupes =new Intent(StartActivity.this, MainActivity.class);
                startActivity(groupes);
            }
        });

// servira à lancer AuthActivity pour changer d'utilisateur puis startActivity si RESULT_OK
    final private ActivityResultLauncher<Intent> authNewUserResultLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK) {
                    // There are no request codes
                    Intent data = result.getData();
                    Intent liste =new Intent(StartActivity.this, StartActivity.class);
                    startActivity(liste);
                }
            });

    final ConnectivityManager.NetworkCallback networkCallback = new ConnectivityManager.NetworkCallback() {
        @Override
        public void onAvailable(@NonNull Network network) {
            super.onAvailable(network);
            if (BuildConfig.DEBUG){
                Log.i("SECUSERV", "on available " );}
            Variables.isNetworkConnected = true; // Global Static Variable
        }
        @Override
        public void onLost(@NonNull Network network) {
            super.onLost(network);
            if (BuildConfig.DEBUG){
                Log.i("SECUSERV", "on lost " );}
            Variables.isNetworkConnected = false; // Global Static Variable
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        affichageTitre = findViewById(R.id.affiche_titre);
        affichageTitre.setText(R.string.white_screen);
        panicDepart = findViewById(R.id.panique_depart);
        panicDepart.setText("");
        dateList = findViewById(R.id.date_list);
        patience = findViewById(R.id.indeterminateBar);

// pour indiquer le site auquel l'appli va s'adresser
//        Variables.urlActive = urlsApiApp.API_LOCAL.getUrl();
//        Variables.urlActive = urlsApiApp.API_GUMS_v3.getUrl();
        Variables.urlActive = urlsApiApp.API_GUMS.getUrl();

// trouver la date du jour
        final Calendar c = Calendar.getInstance();
        Date dateJour = c.getTime();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        String dateToday = sdf.format(dateJour);

        mesPrefs = MyHelper.getInstance(getApplicationContext()).recupPrefs();
        editeur = mesPrefs.edit();
        editeur.putString("today", dateToday);
        editeur.putString("errCode", "");
        editeur.putString("errMsg", "");
// ceci force auth pour les essais :
//        editeur.putBoolean("authOK", false);
        editeur.apply();

// vérif disponibilité réseau
        if (!Variables.isNetworkConnected) {
            Variables.isNetworkConnected = AuxReseau.isInternetOK();
            if (BuildConfig.DEBUG) {
                Log.i("SECUSERV", "internet = " + Variables.isNetworkConnected);
            }
        }
// surveille disponibilité réseau
        conMan = (ConnectivityManager) this.getSystemService(CONNECTIVITY_SERVICE);
        conMan.registerDefaultNetworkCallback(networkCallback);
        Variables.monitoringNetwork = true;

        methodesAux = new Aux();
        patience.setVisibility(View.VISIBLE);

// création ou récupération du modèle ; ne pas oublier que le constructeur du model s'exécute immédiatement
        if (BuildConfig.DEBUG){
        Log.i("SECUSERV start", "lance model Sorties ");}
        modelSorties = new ViewModelProvider(this).get(ModelListeSorties.class);
        recyclerView = findViewById(R.id.liste_we);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

// flagListeSorties est false si on n'a pas récupéré de réponse du serveur ou si on n'a pas décodé le json
// ou si la liste de sorties est vide ou s'il n'y a rien dans prefs (si on a une liste périmée, on l'affiche à tout hasard).
// flaglisteSorties est géré par AuxReseau.decodeInfosSorties et ModelListeSorties
        final Observer<Boolean> flagListeSortiesObserver = retour -> {
            if (!retour) {
                new Handler(Looper.getMainLooper()).postDelayed(() -> {
                }, 200); // délai 0.2 sec
                alerte("2");
                panicDepart.setText(R.string.no_list);
            }
        };
        modelSorties.getFlagListeSorties().observe(StartActivity.this, flagListeSortiesObserver);

// observateur de présence réseau. Mis en place par ModelListeSorties
        final Observer<Boolean> flagReseauObserver = retour -> {
            if (!retour) {
                alerte("5");
            }
        };
        modelSorties.getFlagReseau().observe(StartActivity.this, flagReseauObserver);

// observateur d'arrivée de la liste des sorties
        final Observer<ArrayList<HashMap<String,String>>> listeSortiessObserver = new Observer<ArrayList<HashMap<String,String>>>() {
            String pourInfo = "";
            @Override
            public void onChanged(ArrayList<HashMap<String,String>> items) {
                affichageTitre.setText(getString(R.string.titre_accueil, mesPrefs.getString("datelist", "")));
                if (items != null) {
                    listeDesItems = items;
                    patience.setVisibility(View.GONE);
                    nomsItems = methodesAux.faitListeSorties(listeDesItems);
                    if (nomsItems != null) {
                        RecyclerViewClickListener listener = (view, position) -> {
                            editeur.putString("date_bdh", listeDesItems.get(position).get("date_bdh"));
                            editeur.putString("id", listeDesItems.get(position).get("id"));
                            if (BuildConfig.DEBUG){
                            Log.i("SECUSERV", "Start sortieId = "+listeDesItems.get(position).get("id"));}
                            editeur.putString("titre", listeDesItems.get(position).get("titre"));
                            editeur.putString("date", listeDesItems.get(position).get("date"));
                            editeur.putString("jours", listeDesItems.get(position).get("jours"));
                            editeur.putString("publier_groupes", listeDesItems.get(position).get("publier_groupes"));
                            String responsable = listeDesItems.get(position).get("responsable");
                            editeur.putString("responsable", responsable);
                            editeur.putString("id_Res_Car", listeDesItems.get(position).get("id_responsable"));
                            editeur.putString("email_rescar", listeDesItems.get(position).get("email_rescar"));
                            editeur.putString("tel_rescar", listeDesItems.get(position).get("tel_rescar"));
                            if (BuildConfig.DEBUG){
                                Log.i("SECUSERV Start", "resCar = " + listeDesItems.get(position).get("responsable")+" "+
                                                listeDesItems.get(position).get("email_rescar")+" "+
                                                listeDesItems.get(position).get("tel_rescar"));}
                            String infos;
                            infos = listeDesItems.get(position).get("date_bdh") + "\n" +
                                    listeDesItems.get(position).get("titre");
                            editeur.putString("infoSortie", infos);
                            editeur.apply();

// En cas de démarrage avec auth valide on lance Main ici.
// Si pas auth on lance AuthActivity. Une fois auth réalisée, authOK, auth et userId se trouvent en sharedPrefs
// et on lance Main pour recup des données des groupes (voir onActivityResult du ActivityResultLauncher)
// Noter que on passe authOK à false et auth à "" si à l'occasion de onBackPressed dans Main l'utilisateur décide de
// fermer l'appli pour obliger une nouvelle identification lors du redémarrage. Jusque là le jeton "auth" restera
// disponible dans les sharedPrefs.

                            if ( !mesPrefs.getBoolean("authOK", false)) {
                                Intent auth = new Intent(StartActivity.this, AuthActivity.class);
                                authActivityResultLauncher.launch(auth);
                            }else{
                                Intent groupes =new Intent(StartActivity.this, MainActivity.class);
                                startActivity(groupes);
                            }
                        };
                        monAdapter = new RecyclerViewGenericAdapter(recyclerView.getContext(), nomsItems, listener, R.layout.item_liste_sorties);
                        recyclerView.setAdapter(monAdapter);
                    } else {
                        pourInfo = "pas de liste de sorties";
                    }
                } else {
                    pourInfo = "yavait rien à voir";
                }
            }
        };
        modelSorties.getParamDesSorties().observe(StartActivity.this, listeSortiessObserver);
    }
// end onCreate


    @Override
    public boolean onCreateOptionsMenu(@NonNull Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_start, menu);
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
        if (id == R.id.action_settings) {
            Intent choixPrefs = new Intent(this, Preferences.class);
            startActivity(choixPrefs);
            return true;
        }
       if (id == R.id.new_user) {
            Intent newUser = new Intent(this, AuthActivity.class);
           authNewUserResultLauncher.launch(newUser);
            return true;
        }
        if (id == R.id.secours) {
            Intent secours = new Intent(this, Secours.class);
            startActivity(secours);
            return true;
        }
        if (id == R.id.apropos) {
            Intent lireAPropos = new Intent(this, APropos.class);
            startActivity(lireAPropos);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
    //affichage dialogue d'alerte si problème de disponibilité des infos
    protected void alerte(String flag) {
        String message = "";
        switch (flag) {
            case "1" :
                message = mesPrefs.getString("errMsg", "")+" \ncode "+mesPrefs.getString("errCode", "");
                break;
            case "2" :
//                message = "nouvelles données indisponibles\n"+mesPrefs.getString("errMsg", "")+" \ncode "+mesPrefs.getString("errCode", "");
                message = "Pas de données disponibles\n";
                break;
            case "3" :
                message = mesPrefs.getString("errMsg", "")+" \ncode "+mesPrefs.getString("errCode", "");
                break;
            case "4":
                message = mesPrefs.getString("errMsg", "")+" \ncode "+mesPrefs.getString("errCode", "");
                break;
            case "5":
                message = "Pas de réseau, on va peut-être avoir des problèmes !";
        }
        DialogAlertes infoStart = DialogAlertes.newInstance(message);
        infoStart.show(getSupportFragmentManager(), "infoStart");
    }

// on libère le network callback en partant
    protected void onDestroy() {
        super.onDestroy();
        if (networkCallback != null) {
            if (BuildConfig.DEBUG){
                Log.i("SECUSERV", "unregister callback " );}
            conMan.unregisterNetworkCallback(networkCallback);
        }

    }

}
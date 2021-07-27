package fr.cjpapps.gumsski;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Handler;
import android.os.Looper;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashMap;

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

    // BroadcastReceiver pour pouvoir fermer depuis MainActivity quand on la ferme (voir DialogQuestion)
    BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context arg0, Intent intent) {
            String action = intent.getAction();
            if (action.equals("finish_activity")) {
                finish();
            }
        }
    };

/*  Dans les sharedPreferences :
*       datelist == date à laquelle on a récupéré la liste des sorties
*       date == date de la sortie choisie dans la liste des sorties
*       datedata == date de la sortie à laquelle correspond la liste de participants disponible dans les prefs */

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

// pour pouvoir fermer depuis MainActivity quand on décide de la fermer
        LocalBroadcastManager.getInstance(this).registerReceiver(broadcastReceiver, new IntentFilter("finish_activity"));

        affichageTitre = findViewById(R.id.affiche_titre);
        affichageTitre.setText(R.string.white_screen);
        panicDepart = findViewById(R.id.panique_depart);
        panicDepart.setText("");
        dateList = findViewById(R.id.date_list);
        patience = findViewById(R.id.indeterminateBar);

//        Variables.urlActive = urlsApiApp.API_LOCAL.getUrl();
        Variables.urlActive = urlsApiApp.API_GUMS_v3.getUrl();

        mesPrefs = MyHelper.getInstance(getApplicationContext()).recupPrefs();
        editeur = mesPrefs.edit();
        editeur.putString("errCode", "");
        editeur.putString("errMsg", "");
// ceci force auth pour les essais :
//        editeur.putBoolean("authOK", false);
        editeur.apply();

        methodesAux = new Aux();
        getSystemService(CONNECTIVITY_SERVICE);
        AuxReseau.watchNetwork();
// Faut patienter un peu jusqu'à ce que le réseau soit disponible
        patience.setVisibility(View.VISIBLE);
        int count = 0;
        while (!Variables.isNetworkConnected) {
            new Handler(Looper.getMainLooper()).postDelayed(() -> {
                //on attend que le temps passe
            }, 20); // délai 0.02 sec
            count++;
            if (count > 1000) {
                alerte("5");
                finish();
            }
        }

// création ou récupération du modèle ; ne pas oublier que le constructeur du model s'exécute immédiatement
//        Log.i("SECUSERV start", "lance model Sorties ");
        modelSorties = new ViewModelProvider(this).get(ModelListeSorties.class);
        recyclerView = findViewById(R.id.liste_we);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

// flagListeSorties est false si on n'a pas récupéré de réponse du serveur ou si on n'a pas décodé le json
// ou si la liste de sorties est vide; donc on n'a rien mais si on a une liste périmée, on l'affiche à tout hasard.
// flaglisteSorties est géré par (GetParamsSorties)  AuxReseau.decodeInfosSorties
        final Observer<Boolean> flagListeSortiesObserver = retour -> {
            if (!retour) {
                new Handler(Looper.getMainLooper()).postDelayed(() -> {
                    alerte("2");
                    patience.setVisibility(View.GONE);
                    String dateListeDispo = mesPrefs.getString("datelist", "");
                    if (!(Aux.egaliteChaines(dateListeDispo, ""))) {
                        modelSorties.getListeFromPrefs();
                        }else{
                        panicDepart.setText(R.string.no_list);
                    }
                    }, 200); // délai 0.2 sec
            }
        };
        modelSorties.getFlagListeSorties().observe(StartActivity.this, flagListeSortiesObserver);

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
                            editeur.putString("titre", listeDesItems.get(position).get("titre"));
                            editeur.putString("date", listeDesItems.get(position).get("date"));
                            editeur.putString("jours", listeDesItems.get(position).get("jours"));
                            editeur.putString("publier_groupes", listeDesItems.get(position).get("publier_groupes"));
                            String responsable = listeDesItems.get(position).get("responsable");
//                            if ("null".equals(responsable)) { responsable = "";}
                            editeur.putString("responsable", responsable);
                            editeur.putString("id_Res_Car", listeDesItems.get(position).get("id_responsable"));
                            String infos;
                            infos = listeDesItems.get(position).get("date_bdh") + "\n" +
                                    listeDesItems.get(position).get("titre") + "\n" + responsable ;
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
//                                Toast.makeText(getApplicationContext(), "Sortie id = "+listeDesItems.get(position).get("id"), Toast.LENGTH_LONG).show();
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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
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
//            startActivityForResult(newUser, Constantes.AUTH_CHANGE);
           authNewUserResultLauncher.launch(newUser);
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
                message = "nouvelles données indisponibles\n"+mesPrefs.getString("errMsg", "")+" \ncode "+mesPrefs.getString("errCode", "");
                break;
            case "3" :
                message = mesPrefs.getString("errMsg", "")+" \ncode "+mesPrefs.getString("errCode", "");
                break;
            case "4":
                message = mesPrefs.getString("errMsg", "")+" \ncode "+mesPrefs.getString("errCode", "");
                break;
            case "5":
                message = "Pas de réseau, on s'en va !";
        }
        DialogAlertes infoUtilisateur = DialogAlertes.newInstance(message);
        infoUtilisateur.show(getSupportFragmentManager(), "infoutilisateur");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (isFinishing()  && !isChangingConfigurations()) {
            LocalBroadcastManager.getInstance(this).unregisterReceiver(broadcastReceiver);
        }
    }

}
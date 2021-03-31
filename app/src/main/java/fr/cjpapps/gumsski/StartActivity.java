package fr.cjpapps.gumsski;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Handler;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Objects;

public class StartActivity extends AppCompatActivity {

    final static String PREF_FILE = "authAccess";
    private ArrayList<String> nomsItems = new ArrayList<>();//    ArrayList<Item> listeItems = new ArrayList<>();
    private ArrayList<HashMap<String,String>> listeDesItems = new ArrayList<>();
    TextView affichageTitre =null;
    TextView panicDepart = null;
    TextView dateList = null;
    ProgressBar patience = null;
    private RecyclerView recyclerView;
    private RecyclerViewGenericAdapter monAdapter;
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

/* TODO
*   OK    traiter les situations d'échec d'authentification. Rien à changer.
*   OK    traiter le cas où la liste des sorties est vide (data = null)
*   OK    afficher la date de récup de la liste
*       vérifier la rotation d'écran
*   OK    revoir les tests égalité de chaînes
*   OK    mettre top menu dans startActivity */

/*  Dans les sharedPreferences :
*       datelist == date à laquelle on a récupéré la liste des sorties
*       date == date de la sortie choisie dans la liste des sorties
*       datedata == date de la sortie à laquelle correspond la liste de participants disponible dans les prefs */

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

// pour pouvoir fermer depuis MainActivity quand on décide de la ferme
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
        Aux.watchNetwork();
// Faut patienter un peu jusqu'à ce que le réseau soit disponible
        patience.setVisibility(View.VISIBLE);
        int count = 0;
        while (!Variables.isNetworkConnected) {
            new Handler().postDelayed(new Runnable() {
                public void run() {
                    //on attend que le temps passe
                }
            }, 20); // délai 0.02 sec
            count++;
            if (count > 1000) {
                alerte("5");
                finish();
            }
        }

// création ou récupération du modèle ; ne pas oublier que le constructeur du model s'exécute immédiatement
        Log.i("SECUSERV start", "lance model Sorties ");
        modelSorties = new ViewModelProvider(this).get(ModelListeSorties.class);
        recyclerView = findViewById(R.id.liste_we);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

// flagListeSorties est false si on n'a pas récupéré de réponse du serveur ou si on n'a pas décodé le json
// ou si la liste de sorties est vide; donc on n'a rien mais si on a une liste périmée, on l'affiche à tout hasard.
// flaglisteSorties est géré par GetParamsSorties
        final Observer<Boolean> flagListeSortiesObserver = new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean retour) {
                Log.i("SECUSERV", "flagListeSorties " + retour);
                if (!retour) {
                    new Handler().postDelayed(new Runnable() {
                        public void run() {
                            alerte("2");
                            patience.setVisibility(View.GONE);
                            String dateListeDispo = mesPrefs.getString("datelist", "");
                            if (!(Aux.egaliteChaines(dateListeDispo, ""))) {
                                modelSorties.getListeFromPrefs();
                                }else{
                                panicDepart.setText(R.string.no_list);
                            }
                            }
                    }, 200); // délai 0.2 sec
                }
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
                    Log.i("SECUSERV Start", "taille = " + listeDesItems.size());
                    nomsItems = methodesAux.faitListeSorties(listeDesItems);
                    if (nomsItems != null) {
                        RecyclerViewClickListener listener = new RecyclerViewClickListener() {
                            @Override
                            public void onClick(View view, final int position) {
                                editeur.putString("date_bdh", listeDesItems.get(position).get("date_bdh"));
                                editeur.putString("id", listeDesItems.get(position).get("id"));
                                editeur.putString("titre", listeDesItems.get(position).get("titre"));
                                editeur.putString("date", listeDesItems.get(position).get("date"));
                                editeur.putString("jours", listeDesItems.get(position).get("jours"));
                                editeur.putString("publier_groupes", listeDesItems.get(position).get("publier_groupes"));
                                editeur.putString("responsable", listeDesItems.get(position).get("responsable"));
                                String infos;
                                infos = listeDesItems.get(position).get("date_bdh") + "\n" +
                                        listeDesItems.get(position).get("titre") + "\n" +
                                        listeDesItems.get(position).get("responsable");
                                editeur.putString("infoSortie", infos);
                                editeur.apply();

// En cas de démarrage avec auth valide on lance Main ici.
// Si pas auth on lance AuthActivity. Une fois auth réalisée, authOK, auth et userId se trouvent en sharedPrefs
// et on lance Main pour recup des données des groupes (voir onActivityResult pour AUTH_ACTIV)
// Noter que on passe authOK à false et auth à "" si à l'occasion de onBackPressed dans Main l'utilisateur décide de
// fermer l'appli pour obliger une nouvelle identification lors du redémarrage. Jusque là le jeton "auth" restera
// disponible dans les sharedPrefs.

                                if ( !mesPrefs.getBoolean("authOK", false)) {
                                    Log.i("SECUSERV main start auth", "true");
                                    Intent auth = new Intent(StartActivity.this, AuthActivity.class);
                                    startActivityForResult(auth, Constantes.AUTH_ACTIV);
                                }else{
                                    Intent groupes =new Intent(StartActivity.this, MainActivity.class);
                                    startActivity(groupes);
                                }
//                                Toast.makeText(getApplicationContext(), "Sortie id = "+listeDesItems.get(position).get("id"), Toast.LENGTH_LONG).show();
                            }
                        };
                        monAdapter = new RecyclerViewGenericAdapter(recyclerView.getContext(), nomsItems, listener);
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
        getMenuInflater().inflate(R.menu.menu_main, menu);
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
        if (id == R.id.logistique) {
            Intent logistic = new Intent(this, Logistique.class);
            startActivity(logistic);
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
        if (id == R.id.action_settings) {
            Intent choixPrefs = new Intent(this, Preferences.class);
            startActivity(choixPrefs);
            return true;
        }
       if (id == R.id.new_user) {
            Intent newUser = new Intent(this, AuthActivity.class);
            startActivityForResult(newUser, Constantes.AUTH_CHANGE);
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

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == Constantes.AUTH_ACTIV) {
            if (resultCode == RESULT_CANCELED) {
                Log.i("SECUSERV Start", "on auth activ result pas OK");
//                finish();
            }
            if (resultCode == RESULT_OK) {
                Intent groupes =new Intent(StartActivity.this, MainActivity.class);
                startActivity(groupes);
            }
        }
    }

    @Override
    protected void onDestroy() {
//        Log.i("SECUSERV start destroy", "fin "+isFinishing());
//        Log.i("SECUSERV start destroy", "chg "+isChangingConfigurations());
        super.onDestroy();
        if (isFinishing()  && !isChangingConfigurations()) {
            LocalBroadcastManager.getInstance(this).unregisterReceiver(broadcastReceiver);
        }
    }


}
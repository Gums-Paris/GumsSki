package fr.cjpapps.gumsski;

import android.content.SharedPreferences;
import android.os.Bundle;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashMap;

public class StartActivity extends AppCompatActivity {

    final static String PREF_FILE = "authAccess";
    private ArrayList<String> nomsItems = new ArrayList<>();//    ArrayList<Item> listeItems = new ArrayList<>();
    private ArrayList<HashMap<String,String>> listeDesItems = new ArrayList<>();
    ArrayList<HashMap<String,String>> listeDesSorties = new ArrayList<>();
    TextView affichageTitre =null;
    ProgressBar patience = null;
    private RecyclerView recyclerView;
    private RecyclerViewGenericAdapter monAdapter;
    ModelListeSorties modelSorties = null;
    SharedPreferences mesPrefs;
    SharedPreferences.Editor editeur;
    Aux methodesAux;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        affichageTitre = findViewById(R.id.affiche_titre);
        affichageTitre.setText(R.string.white_screen);
        patience = findViewById(R.id.indeterminateBar);

//        Variables.urlActive = urlsApiApp.API_LOCAL.getUrl();
        Variables.urlActive = urlsApiApp.API_GUMS_v3.getUrl();

        mesPrefs = MyHelper.getInstance(getApplicationContext()).recupPrefs();
        editeur = mesPrefs.edit();
        editeur.putString("errCode", "");
        editeur.putString("errMsg", "");
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
        modelSorties = new ViewModelProvider(this).get(ModelListeSorties.class);
        recyclerView = findViewById(R.id.liste_we);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

// flagListeSorties est false si on n'a pas récupéré de réponse du serveur ou si on n'a pas décodé le json ;
// flaglisteSorties est géré par GetParamsSorties
        final Observer<Boolean> flagListeSortiesObserver = new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean retour) {
                Log.i("SECUSERV", "flagListeSorties " + retour);
                if (!retour) {
                    new Handler().postDelayed(new Runnable() {
                        public void run() {
                            alerte("2");
                        }
                    }, 200); // délai 0.2 sec
                }
            }
        };
        modelSorties.getFlagListeSorties().observe(StartActivity.this, flagListeSortiesObserver);

// observateur d'arrivée de la liste des participants
        final Observer<ArrayList<HashMap<String,String>>> listeSortiessObserver = new Observer<ArrayList<HashMap<String,String>>>() {
            String pourInfo = "";
            @Override
            public void onChanged(ArrayList<HashMap<String,String>> items) {
                affichageTitre.setText(R.string.titre_accueil);
                if (items != null) {
                    listeDesItems = items;
                    patience.setVisibility(View.GONE);
                    Log.i("SECUSERV Start", "taille = " + listeDesItems.size());
                    nomsItems = methodesAux.faitListeSorties(listeDesItems);
                    if (nomsItems != null) {
                        RecyclerViewClickListener listener = new RecyclerViewClickListener() {
                            @Override
                            public void onClick(View view, final int position) {
                                String element = nomsItems.get(position);
// à écrire = sur clic d'un nom de sortie
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


        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });
    }

    //affichage dialogue d'alerte si problème de disponibilité des infos
    protected void alerte(String flag) {
        String message = "";
        switch (flag) {
            case "1" :
                message = mesPrefs.getString("errMsg", "")+" \ncode "+mesPrefs.getString("errCode", "");
                break;
            case "2" :
                message = "données indisponible\n"+mesPrefs.getString("errMsg", "")+" \ncode "+mesPrefs.getString("errCode", "");
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

}
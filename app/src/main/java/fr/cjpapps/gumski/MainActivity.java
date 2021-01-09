package fr.cjpapps.gumski;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Handler;
import android.util.Log;
import android.view.View;

import android.view.Menu;
import android.view.MenuItem;
import android.widget.PopupMenu;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashMap;

public class MainActivity extends AppCompatActivity {

    final static String PREF_FILE = "authAccess";
    ArrayList<String> nomsItems = new ArrayList<>();//    ArrayList<Item> listeItems = new ArrayList<>();
    ArrayList<HashMap<String,String>> listeDesItems = new ArrayList<>();
    TextView affichage =null;
    ProgressBar patience = null;
    private RecyclerView recyclerView;
    private RecyclerViewGenericAdapter monAdapter;
    ModelListeItems modelListe = null;
    SharedPreferences mesPrefs;
    SharedPreferences.Editor editeur;
    Aux auxMethods;
    static final String DATELISTE = "dateliste";
    boolean testAuth;

/*  Appli quasi générique au sens où pour faire des opérations de création, modification ou suppression sur les lignes d'une
*   basesur un site Joomla distant, il n'y a qu'à rentrer les infos concernant le site et la base dans trois classes :
*       - Attributs.java où il faut fournir les lignes ATTR01 à ATTRnn qui décrivent les champs de la base
*       - urlsApiApp.java pour donner l'URL d'accès à l'API du site joomla sous le forme particulière qui suit
*   https :// etc./index.php?option=com_api&   (com_api étant supposé installé sur le site et le plugin correspondant à la
*   ressource désirée étant en état de marche)
*       - Constantes.java où onplace les noms de l'app et des resources du plugin de com_api
*   */

/*  Notes   1. Pare-feu de OVH
             OVH utilise Apache mod_security, qui refuse DELETE (paramétrage standard) et le corps de POST en Json
             (ça c'est plus sévère que le paramétrage standard). On peut mettre le corps dePOST en form-data ce qui est assez
             laborieux à coder, ou en url-encoded ce qui n'est pas plus pénible à coder que le json. Je choisis
             cette dernière solution.
             Pour DELETE, je triche avec GET en rajoutant un paramètre &fleur=bleuet. Ça ne marche évidemment que parce
             que c'est moi qui code le traitement des requêtes sur le site gumsparis.

            2. Permissions du user sur le composant dans gumsparis
             Le user est identifié par le token d'authentification, il n'est pas nécessaire de le transporter
             dans les requêtes. S'il y a des ACL il faut les gérer dans le api/plugin (qui fournit un objet User).

            3. Positionnement de la mise en place des observers dans Main
             Les observers doivent être mis en place dans onCreate et pas dans onResume parce qu'ils sont activés une fois
             lors de la mise en place donc deux fois (mise en place + onChanged) lors d'un restart de l'activité si on les
             met dans onResume

            4. RecyclerViewClickListener
             L'interface RecyclerViewClickListener permet de faire un itemViewClickListener pour RecyclerView qui
             n'en a pas. Elle permet aussi de sortir le code spécifique pour le mettre ailleurs que dans la classe et
             donc de standardiser l'adapter. Mais je vois toujours pas en quoi une interface permet de simuler un
             double héritage
 */

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        affichage = findViewById(R.id.affiche);
        affichage.setText(R.string.white_screen);
        patience = findViewById(R.id.indeterminateBar);

//        Variables.urlActive = urlsGblo.API_LOCAL.getUrl();
        Variables.urlActive = urlsApiApp.API_SITE.getUrl();

        mesPrefs = MyHelper.getInstance(getApplicationContext()).recupPrefs();
        editeur = mesPrefs.edit();
// ceci force auth pour les essais :
//        editeur.putBoolean("authOK", false);
        editeur.putString("errCode", "");
        editeur.putString("errMsg", "");
        editeur.apply();

        auxMethods = new Aux();
        getSystemService(CONNECTIVITY_SERVICE);

        auxMethods.watchNetwork();
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
        patience.setVisibility(View.GONE);

        modelListe = new ViewModelProvider(this).get(ModelListeItems.class);
        recyclerView = findViewById(R.id.listechoix);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

// auth si nécessaire. Une fois auth réalisée, authOK, auth et userId se trouvent en sharedPrefs et on lance une
// recup de la liste
// noter que on passe authOK à false dans onDestroy() pour obliger une nouvelle identification après une fermeture
// complète. Jusque là le jeton "auth" restera disponible dans les sharedPrefs.
// une fois authentifié on récupère la liste d'items (voir onActivityResult tout en bas ; en cas de
// démarrage avec auth valide c'est la création demodelListe qui déclenche le chargement
        testAuth = mesPrefs.getBoolean("authOK", false);
        Log.i("SECUSERV start authOK", String.valueOf(testAuth));
        if ( !testAuth) {
            Intent auth = new Intent(this, AuthActivity.class);
            startActivityForResult(auth, Constantes.AUTH_ACTIV);
        }

// flagListe est false si on n'a pas récupéré de réponse du serveur ou si on n'a pas décodé le json ;
// flagliste est géré par GetInfosListe
        final Observer<Boolean> flagListeObserver = new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean retour) {
                Log.i("SECUSERV", "flagListe " + retour);
                if (!retour) {
                    new Handler().postDelayed(new Runnable() {
                        public void run() {
                            alerte("2");
                        }
                    }, 200); // délai 0.2 sec
                }
            }
        };
        modelListe.getFlagListe().observe(MainActivity.this, flagListeObserver);

// flagModif est géré par PostInfosItem, lequel est utilisé à la fois par ModifItem et CreateItem
        final Observer<Boolean> flagModifObserver = new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean retour) {
                Log.i("SECUSERV", "flagModif " + retour);
                if (!retour) {
// Il faut freiner un peu pour laisser le temps au message d'erreur d'être rangé dans les prefs
                    new Handler().postDelayed(new Runnable() {
                        public void run() {
                            alerte("3");
                        }
                    }, 200); // délai 0.2 sec
                } else {
                    modelListe.recupListe();
                }
            }
        };
        modelListe.getFlagModif().observe(MainActivity.this, flagModifObserver);

// flagSuppress est géré par DelInfosGums
        final Observer<Boolean> flagSuppressObserver = new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean retour) {
                Log.i("SECUSERV", "flagSuppress " + retour);
                if (retour) {
                    modelListe.recupListe();
                } else {
                    alerte("4");
                }
            }
        };
        modelListe.getFlagSuppress().observe(MainActivity.this, flagSuppressObserver);

// observateur d'arrivée de la liste
        final Observer<ArrayList<HashMap<String,String>>> listeItemsObserver = new Observer<ArrayList<HashMap<String,String>>>() {
            String pourInfo = "";
            @Override
            public void onChanged(ArrayList<HashMap<String,String>> items) {
                if (items != null) {
                    listeDesItems = items;
                    Log.i("SECUSERV Main", "taille = " + listeDesItems.size());
                    nomsItems = auxMethods.faitListeNoms(listeDesItems);
                    if (nomsItems != null) {
                        RecyclerViewClickListener listener = new RecyclerViewClickListener() {
                            @Override
                            public void onClick(View view, final int position) {
                                PopupMenu popup = new PopupMenu(view.getContext(), view);
                                popup.inflate(R.menu.edit_context_menu);
                                popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                                    @Override
                                    public boolean onMenuItemClick(MenuItem item) {
                                        String element = nomsItems.get(position);
                                        String idItem = Aux.getIdItem(listeDesItems, element);
                                        int menuItemId = item.getItemId();
                                        if (menuItemId == R.id.modif) {
                                            Intent choisi = new Intent(MainActivity.this, ModifItem.class);
                                            choisi.putExtra("itemChoisi", idItem);
                                            startActivityForResult(choisi, Constantes.MODIF_REQUEST);
                                            return true;
                                        }else if (menuItemId == R.id.suppr) {
                                            Intent choix = new Intent(MainActivity.this, DeleteItem.class);
                                            choix.putExtra("itemChoisi", idItem);
                                            startActivityForResult(choix, Constantes.SUPPR_REQUEST);
                                            return true;
                                        } else {
                                            return false;
                                        }
                                    }
                                });
                                popup.show();
                            }
                        };
                        monAdapter = new RecyclerViewGenericAdapter(recyclerView.getContext(), nomsItems, listener);
                        recyclerView.setAdapter(monAdapter);
                    } else {
                        pourInfo = "pas de liste de noms";
                    }
                } else {
                    pourInfo = "yavait rien à voir";
                }
                Log.i("SECUSERV", "date today =  "+mesPrefs.getString(DATELISTE, "2020-01-01"));
                affichage.setText(mesPrefs.getString(DATELISTE, "2020-01-01"));
            }
        };
        modelListe.getListeDesItems().observe(MainActivity.this, listeItemsObserver);

    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.i("SECUSERV", "onStop ");
    }

    @Override
    protected void onDestroy() {
        Log.i("SECUSERV", "onDestroy ");
        if (isFinishing()  && !isChangingConfigurations()) {
            Log.i("SECUSERV", "clear model main ");
            getViewModelStore().clear();
            editeur.putBoolean("authOK", false);
            editeur.putString("auth", "");
            editeur.apply();
            super.onDestroy();
        }
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
            return true;
        }
        if (id == R.id.action_settings) {
             return true;
        }
        if (id == R.id.new_item) {
            Intent newItem = new Intent(MainActivity.this, CreateItem.class);
            startActivityForResult(newItem, Constantes.CREATE_REQUEST);
            return true;
        }
        if (id == R.id.new_user) {
            editeur.putBoolean("authOK", false);
            editeur.putString("auth", "");
            editeur.putString("userId", "");
            editeur.commit();

            Intent newUser = new Intent(this, AuthActivity.class);
            startActivityForResult(newUser, Constantes.AUTH_CHANGE);
//           startActivity(newUser);
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
                message = "liste indisponible\n"+mesPrefs.getString("errMsg", "")+" \ncode "+mesPrefs.getString("errCode", "");
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
                Log.i("SECUSERV Main", "on auth activ result pas OK");
                finish();
            }
            if (resultCode == RESULT_OK) {
                modelListe.recupListe();
            }
        }
        if (requestCode == Constantes.AUTH_CHANGE) {
            if (resultCode == RESULT_CANCELED) {
                Log.i("SECUSERV Main", "on auth change result pas OK");
                finish();
            }
            if (resultCode == RESULT_OK) {
                Log.i("SECUSERV Main", "on auth change result OK");
            }
        }
        if (requestCode == Constantes.MODIF_REQUEST) {
            if (resultCode == RESULT_OK) {
                Log.i("SECUSERV Main", "on activity result OK");
            }
        }
        if (requestCode == Constantes.CREATE_REQUEST) {
            if (resultCode == RESULT_OK) {
                Log.i("SECUSERV Main", "on activity result OK");
            }
            if (resultCode == RESULT_CANCELED) {
                alerte("4");
            }
        }
    }


}
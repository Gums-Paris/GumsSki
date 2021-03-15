package fr.cjpapps.gumsski;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

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

import android.view.Menu;
import android.view.MenuItem;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Objects;

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
    boolean testAuth = false;
    String stringAuth = "";
    ArrayList<ArrayList<HashMap<String,String>>> compositionGroupes = new ArrayList<>();

/* TODO
    OK   assurer fonctionnement si pas autorisé à téléphoner
    OK   envoi message (faire les 3 icones , menu à 3 items avec icones )
    OK  faire icones rondes plus grandes : taille par width et height avec icone de la bonne taille 
    OK  envoi email groupe par bouton sur la page du groupe
    OK = NO ; envoi sms groupe par bouton sur la page du groupe peut pas être fait simplement ; Passer immé à Signal ?
        on se contenera d'ouvrir Signal
    OK  avant distribution remettre les vrais tel et e-mail
    OK  corriger aide rajout mailto:
    clic long sur participant deb, deniv, nivA, nivS
    date peut-il être différent de datedata ?
    remplacer startActivityForResult
    */

// dérivé de AccessAuth mais avec pas mal de modifs
/*  AccessAuth
*   est une appli quasi générique au sens où pour faire des opérations de création, modification ou suppression sur les lignes d'une
*   basesur un site Joomla distant, il n'y a qu'à rentrer les infos concernant le site et la base dans trois classes :
*       - Attributs.java où il faut fournir les lignes ATTR01 à ATTRnn qui décrivent les champs de la base
*       - urlsApiApp.java pour donner l'URL d'accès à l'API du site joomla sous le forme particulière qui suit
*   https :// etc./index.php?option=com_api&   (com_api étant supposé installé sur le site et le plugin correspondant à la
*   ressource désirée étant en état de marche)
*       - Constantes.java où on place les noms de l'app et des resources du plugin de com_api
*   */

/*  Notes sur AccessAuth
            1. Pare-feu de OVH
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

//        Variables.urlActive = urlsApiApp.API_LOCAL.getUrl();
        Variables.urlActive = urlsApiApp.API_GUMS.getUrl();

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

// création ou récupération du modèle ; ne pas oublier que le constructeur du model s'exécute immédiatement
        modelListe = new ViewModelProvider(this).get(ModelListeItems.class);
        recyclerView = findViewById(R.id.listechoix);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

// auth si nécessaire. Une fois auth réalisée, authOK, auth et userId se trouvent en sharedPrefs et on lance une
// recup des données
// noter que on passe authOK à false et auth à "" si à l'occasion de onBackPressed l'utilisateur décide de fermer l'appli
// pour obliger une nouvelle identification après une telle fermeture complète.
// Jusque là le jeton "auth" restera disponible dans les sharedPrefs.
// une fois authentifié on récupère la liste d'items (voir onActivityResult pour AUTH_ACTIV tout en bas) ; en cas de
// démarrage avec auth valide c'est la création demodelListe qui déclenche le chargement
        if ( !mesPrefs.getBoolean("authOK", false)) {
        Log.i("SECUSERV main start auth", "true");
            Intent auth = new Intent(this, AuthActivity.class);
            startActivityForResult(auth, Constantes.AUTH_ACTIV);
        }

// flagListe est false si on n'a pas récupéré de réponse du serveur ou si on n'a pas décodé le json ;
// flagliste est géré par GetInfosListe et par GetParamSortie
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
                    modelListe.recupInfo(Constantes.JOOMLA_RESOURCE_2,"");
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
                    modelListe.recupInfo(Constantes.JOOMLA_RESOURCE_2, "");
                } else {
                    alerte("4");
                }
            }
        };
        modelListe.getFlagSuppress().observe(MainActivity.this, flagSuppressObserver);

// observateur d'arrivée de la liste des participants
        final Observer<ArrayList<HashMap<String,String>>> listeItemsObserver = new Observer<ArrayList<HashMap<String,String>>>() {
            String pourInfo = "";
            @Override
            public void onChanged(ArrayList<HashMap<String,String>> items) {
                String infoSortie = mesPrefs.getString("infoSortie","");
                affichage.setText(infoSortie);  // infoSortie a été fabriqué par observateur de paramSortie
                if (items != null) {
                        listeDesItems = items;
                        patience.setVisibility(View.GONE);
                        Log.i("SECUSERV Main", "taille = " + listeDesItems.size());
                        nomsItems = auxMethods.faitListeGroupes(listeDesItems);
                        if (nomsItems != null) {
                            RecyclerViewClickListener listener = new RecyclerViewClickListener() {
                                @Override
                                public void onClick(View view, final int position) {
                                    String element = nomsItems.get(position);
                                    String numGroup = element.substring(0, element.indexOf(':'));
                                    FragmentManager fm = getSupportFragmentManager();
                                    FirstFragment partFrag = FirstFragment.newInstance(element, numGroup);
                                    partFrag.show(fm, "participants");
                                }
                            };
                            monAdapter = new RecyclerViewGenericAdapter(recyclerView.getContext(), nomsItems, listener);
                            recyclerView.setAdapter(monAdapter);
                        } else {
                            pourInfo = "pas de liste de groupes";
                        }
                } else {
                    pourInfo = "yavait rien à voir";
                }
            }
        };
        modelListe.getListeDesItems().observe(MainActivity.this, listeItemsObserver);

// Observateur d'arrivée des paramètres de la sortie
        final Observer<HashMap<String,String>> paramSortieObserver = new Observer<HashMap<String,String>>() {
            String infos = "";
            @Override
            public void onChanged(HashMap<String, String> infoSortie) {
                if (infoSortie != null) {
                    infos = infoSortie.get("date_bdh") + "\n" +
                                infoSortie.get("titre") + "\n" +
                                infoSortie.get("responsable");
                    editeur.putString("date", infoSortie.get("date"));
                    editeur.putString("jours", infoSortie.get("jours"));
                    editeur.putString("infoSortie", infos);
                    editeur.apply();
                    Log.i("SECUSERV Main", "params sortie, on appelle liste ");
                    modelListe.recupInfo(Constantes.JOOMLA_RESOURCE_2, infoSortie.get("id"));
                }else{
                    infos = "Y a rien à voir";
                }
//                affichage.setText(infos);
            }
        };
        modelListe.getParamSortie().observe(this, paramSortieObserver);

    }

    @Override
    protected void onDestroy() {
//  onDestroy a failli servir ; laissée là par paresse au cas où pour retrouver facilement le
//  "isFinishing()  && !isChangingConfigurations()" qui ne s'invente pas facilement
        Log.i("SECUSERV destroy", "fin "+isFinishing());
        Log.i("SECUSERV destroy", "chg "+isChangingConfigurations());
        super.onDestroy();
        if (isFinishing()  && !isChangingConfigurations()) {
        }
    }

    @Override
    public void onBackPressed() {
// si l'usager  presse le bouton retour arrière quend on est sur la page d'accueil (liste des groupes) on luo demande s'il vaut ou
// non fermer l'appli ce qui a pour conséqience d'effacer l'authentification
        String message = "Quitter GumsSki ?";
        DialogQuestion finAppli = DialogQuestion.newInstance(message);
        finAppli.show(getSupportFragmentManager(), "questionSortie");
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
            Intent lireAide = new Intent(MainActivity.this, Aide.class);
            startActivity(lireAide);
            return true;
        }
        if (id == R.id.logistique) {
            Intent logistic = new Intent(MainActivity.this, Logistique.class);
            startActivity(logistic);
            return true;
        }
        if (id == R.id.meteo) {
            Intent meteo = new Intent(MainActivity.this, Meteo.class);
            startActivity(meteo);
            return true;
        }
        if (id == R.id.secours) {
            Intent secours = new Intent(MainActivity.this, Secours.class);
            startActivity(secours);
            return true;
        }
        if (id == R.id.action_settings) {
            Intent choixPrefs = new Intent(MainActivity.this, Preferences.class);
            startActivity(choixPrefs);
             return true;
        }
 /*       if (id == R.id.new_item) {
            Intent newItem = new Intent(MainActivity.this, CreateItem.class);
            startActivityForResult(newItem, Constantes.CREATE_REQUEST);
            return true;
        } */
        if (id == R.id.new_user) {
/*            editeur.putBoolean("authOK", false);
            editeur.putString("auth", "");
            editeur.putString("userId", "");
            editeur.commit(); */

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

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == Constantes.AUTH_ACTIV) {
            if (resultCode == RESULT_CANCELED) {
                Log.i("SECUSERV Main", "on auth activ result pas OK");
                finish();
            }
            if (resultCode == RESULT_OK) {
/*  Si date du WE est vide ou si les infos sont périmées par rapport à la date du jour ou si la date des infos ne colle pas
*   avec la date du WE, on va chercher l'info sur gumsparis. Sinon on la récupère en sharedPreferences */
                String dateWE = mesPrefs.getString("date", null);
                Log.i("SECUSERV Main", "on auth activ result OK");
/*                if ( dateWE == null){
                    modelListe.recupInfo(Constantes.JOOMLA_RESOURCE_1, "");
                }else if ( Aux.datePast(dateWE, Integer.parseInt(Objects.requireNonNull(mesPrefs.getString("jours", "2"))))
                        || !dateWE.equals(mesPrefs.getString("dateData", null))) { */
                    modelListe.recupInfo(Constantes.JOOMLA_RESOURCE_1, "");
/*                }else{
                    modelListe.getInfosFromPrefs();
                } */
            }
        }

        if (requestCode == Constantes.AUTH_CHANGE) {
            if (resultCode == RESULT_CANCELED) {
                Log.i("SECUSERV Main", "on auth change result pas OK");
//                finish();
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
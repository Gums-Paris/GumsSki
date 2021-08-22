package fr.cjpapps.gumsski;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;

import android.view.Menu;
import android.view.MenuItem;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;

import java.util.ArrayList;
import java.util.HashMap;

public class MainActivity extends AppCompatActivity implements DialogQuestion.EndMainDialogListener {

    ArrayList<String> nomsItems = new ArrayList<>();//    ArrayList<Item> listeItems = new ArrayList<>();
    ArrayList<HashMap<String,String>> listeDesItems = new ArrayList<>();
    TextView affichage =null;
    TextView panic = null;
    ProgressBar patience = null;
    private RecyclerView recyclerView;
    private RecyclerViewGenericAdapter monAdapter;
    ModelListeItems modelListe = null;
    SharedPreferences mesPrefs;
    SharedPreferences.Editor editeur;
    Aux auxMethods;
    String idSortie;
    String infoSortie;
    String titreSortie;

/* TODO
    Quoi faire pour backspace dans StartActivity
    Titre dans la page groupes plus gros et plus gras
    OnClick responsable du car : donner possibilités contact
    Nettoyage code redondant et branches inutiles
    Délai 10 sec pour réseau est-il suffiant ?
    Avant distribution remettre les vrais tel et e-mail (également dans le plugin/inscrits du site)
    ---- reste
       Background item_liste paramétrable
       Clic long sur participant deb, deniv, nivA, nivS ?
    */

// dérivé de AccessAuth mais avec pas mal de modifs
/*  AccessAuth
*   est une appli quasi générique au sens où pour faire des opérations de création, modification ou suppression sur les lignes d'une
*   base sur un site Joomla distant, il n'y a qu'à rentrer les infos concernant le site et la base dans trois classes :
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

    // servira à lancer AuthActivity pour changer d'utilisateur puis MainActivity si RESULT_OK
    final private ActivityResultLauncher<Intent> authNewUserResultLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK) {
                    // There are no request codes
                    Intent data = result.getData();
                    Intent liste =new Intent(MainActivity.this, MainActivity.class);
                    startActivity(liste);
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mesPrefs = MyHelper.getInstance().recupPrefs();
        editeur = mesPrefs.edit();

        titreSortie = mesPrefs.getString("titre","");
        idSortie = mesPrefs.getString("id", "");
        Variables.listeChefs.clear();
// listeChefs contiendra le responsable du car et les Res des groupes
        Variables.listeChefs.add(mesPrefs.getString("id_Res_Car", "0"));
        affichage = findViewById(R.id.affiche);
        infoSortie = mesPrefs.getString("infoSortie", "");
        affichage.setText(infoSortie);
// idSortie et infoSortie ont été fabriqués par StartActivity

        panic = findViewById(R.id.panique);  // sert si les groupes ne sont pas publiés

        patience = findViewById(R.id.indeterminateBar);
        patience.setVisibility(View.GONE);

        editeur.putString("errCode", "");
        editeur.putString("errMsg", "");
        editeur.apply();

        auxMethods = new Aux();

/*  La même chose existe dans StartActivity. Pour l'instant il n'existe pas d'accès à Main sans passer par Start
        getSystemService(CONNECTIVITY_SERVICE);
        AuxReseau.watchNetwork();
// Faut parfois patienter un peu jusqu'à ce que le réseau soit disponible
        patience.setVisibility(View.VISIBLE);
        int count = 0;
        while (!Variables.isNetworkConnected) {
            new Handler(Looper.getMainLooper()).postDelayed(() -> {
                //on attend que le temps passe
            }, 20); // délai 0.02 sec
            count++;
            if (count > 500) {
                alerte("5");
                finish();
            }
        }
        patience.setVisibility(View.GONE);
*/
// si les groupes ne sont pas publiés on arrête
        if ("2".equals(mesPrefs.getString("publier_groupes",""))) {

            patience.setVisibility(View.VISIBLE);
// création ou récupération du modèle ; ne pas oublier que le constructeur du model s'exécute immédiatement
            modelListe = new ViewModelProvider(this).get(ModelListeItems.class);

            recyclerView = findViewById(R.id.listechoix);
            recyclerView.setLayoutManager(new LinearLayoutManager(this));

// flagListe est false si on n'a pas récupéré de réponse du serveur ou si on n'a pas décodé le json
// ou si le décodage du json donne un résultat null. Dans ce cas il n'y a plus rien à faire parce que
// modelListe a déjà essayé d'utiliser les Prefs avant d'aller chercher sur le réseau.
// flagliste est géré par AuxReseau.decodeInfosItems
            final Observer<Boolean> flagListeObserver = retour -> {
                if (BuildConfig.DEBUG){
                Log.i("SECUSERV", "flagListe " + retour);}
                if (!retour) {
                    new Handler(Looper.getMainLooper()).postDelayed(() -> {
                    }, 200); // délai 0.2 sec
                    alerte("2");
                    finish();
//                    modelListe.getInfosFromPrefs();
                }
            };
            modelListe.getFlagListe().observe(MainActivity.this, flagListeObserver);

 /* pas utilisé dans cette version de l'appli
// flagSuppress est géré par DelInfosGums
        final Observer<Boolean> flagSuppressObserver = new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean retour) {
                Log.i("SECUSERV", "flagSuppress " + retour);
                if (retour) {
                    AuxReseau.recupInfo(Constantes.JOOMLA_RESOURCE_1, "");
                } else {
                    alerte("4");
                }
            }
        };
        modelListe.getFlagSuppress().observe(MainActivity.this, flagSuppressObserver);  */

// observateur d'arrivée de la liste des participants et affichage des groupes
            final Observer<ArrayList<HashMap<String, String>>> listeItemsObserver = new Observer<ArrayList<HashMap<String, String>>>() {
                String pourInfo = "";

                @Override
                public void onChanged(ArrayList<HashMap<String, String>> items) {
                    if (items != null) {
                        listeDesItems = items;
                        patience.setVisibility(View.GONE);
                        if (BuildConfig.DEBUG){
                        Log.i("SECUSERV Main", "taille = " + listeDesItems.size());}
                        nomsItems = auxMethods.faitListeGroupes(listeDesItems);
                        if (BuildConfig.DEBUG){
                        Log.i("SECUSERV Main lesChefs", Variables.listeChefs.toString());}
                        if (nomsItems != null) {
                            RecyclerViewClickListener listener = (view, position) -> {
                                String element = nomsItems.get(position);
                                String numGroup = element.substring(0, element.indexOf(':'));
                                FragmentManager fm = getSupportFragmentManager();
                                FirstFragment partFrag = FirstFragment.newInstance(element, numGroup);
                                partFrag.show(fm, "participants");
                            };
                            monAdapter = new RecyclerViewGenericAdapter(recyclerView.getContext(), nomsItems, listener,R.layout.item_liste);
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

        }else{          // si les groupes n'ont pas été faits
            panic.setText(R.string.no_groups);
        }

//on utilise le FAB pour remonter à StartActivity parce que le retour arrière de Main sert à fermer l'appli
        ExtendedFloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(view -> {
            Intent retourListeSorties = new Intent(MainActivity.this, StartActivity.class);
            startActivity(retourListeSorties);
            MainActivity.this.finish();
        });
    }

    @Override
    public void onBackPressed() {
// si l'usager  presse le bouton retour arrière quend on est sur la page d'accueil (liste des groupes)
// on lui demande s'il veut fermer l'appli (ce qui a pour conséquence d'effacer l'authentification)
        String message = "Quitter GumsSki ?";
        DialogQuestion finAppli = DialogQuestion.newInstance(message);
        finAppli.show(getSupportFragmentManager(), "questionSortie");
    }

// interface utilisée par DialogQuestion pour fermer MainActvity si l'utiliateur répond qu'il veut fermer l'appli
// il parait que c'est mieux (plus convenable ? éthique ? moral) de laisser l'activité se tuer elle-même
// plutôt que de la tuer depuis le fragment
    @Override
    public void onPositiveReply() {
        if (BuildConfig.DEBUG){
        Log.i("SECUSERV", "Main finish on positive reply");}
        finish();
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
            logistic.putExtra("sortieid", idSortie);
//            logistic.putExtra("titreSortie", titreSortie);
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
                message = "données indisponibles\n"+mesPrefs.getString("errMsg", "")+" \ncode "+mesPrefs.getString("errCode", "");
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
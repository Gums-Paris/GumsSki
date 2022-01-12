package fr.cjpapps.gumsski;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
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
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;

import java.util.ArrayList;
import java.util.HashMap;

public class MainActivity extends AppCompatActivity {

// récupère la liste des participants et affiche les groupes

    ArrayList<String> nomsItems = new ArrayList<>();//    ArrayList<Item> listeItems = new ArrayList<>();
    ArrayList<HashMap<String,String>> listeDesItems = new ArrayList<>();
    TextView affichage =null;
    TextView affichageSuite = null;
    TextView panic = null;
    ProgressBar patience = null;
    private RecyclerView recyclerView;
    private RecyclerViewGenericAdapter monAdapter;
    ModelListeItems modelListe = null;
    SharedPreferences mesPrefs;
    SharedPreferences.Editor editeur;
    Aux auxMethods;
    String idSortie;
    String infoSortie, responsable;
    String titreSortie;
    String idResCar;
    String mailRescar, telResCar;
    MembreGroupe resCar = new MembreGroupe();
    ImageButton phoneResCar = null;
    ImageButton emailResCar = null;
    ImageButton smsResCar = null;
    private Boolean okPhone = false;

/* TODO
    OK
    ---- reste
       météo et secours
       Background item_liste paramétrable ?
       Clic long sur participant deb, deniv, nivA, nivS ?
    */

/* Noter²
* Pour des essais on peut mettre des tel et email bidon dans FirstFragment (lignes 101 et 105)
*  et dans Aux.getResCar (lignes 185 et 189). On peut aussi le faire dans le plugin gski/inscrits de com_api
*  */

//  Cette appli est dérivée de AccessAuth mais avec pas mal de modifs (adieu générique !)
/*  AccessAuth :
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
             Pour DELETE, on triche avec GET en rajoutant un paramètre &fleur=bleuet. Ça ne marche évidemment que si
             on peut coder le traitement des requêtes sur le site gumsparis (plugin de com_api).

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

    // Lanceur de AuthActivity pour changer d'utilisateur puis lancer MainActivity si RESULT_OK
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

    // lanceur pour demande permission d'appeler qqun au tph directement
    final private ActivityResultLauncher<String> requestPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                if (isGranted) {
                    // Permission is granted. Continue the action or workflow in your app
                    okPhone = true;
                } else {
                    // Explain to the user that the feature is unavailable
                    okPhone = false;
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
        idResCar = mesPrefs.getString("id_Res_Car", "");
        affichage = findViewById(R.id.affiche);
        affichageSuite = findViewById(R.id.affiche2);
        phoneResCar = findViewById(R.id.phone_rescar);
        emailResCar = findViewById(R.id.email_rescar);
        smsResCar = findViewById(R.id.sms_rescar);
        infoSortie = mesPrefs.getString("infoSortie", "");

        responsable = mesPrefs.getString("responsable", "");
        mailRescar = mesPrefs.getString("email_rescar", "");
        telResCar = mesPrefs.getString("tel_rescar", "");
        resCar.setName(responsable);
        resCar.setEmail(mailRescar);
        resCar.setTel(Aux.numInter(telResCar));

        affichage.setText(infoSortie);
        affichageSuite.setText(responsable);
// idSortie et infoSortie ont été fabriqués par StartActivity

        panic = findViewById(R.id.panique);  // sert si les groupes ne sont pas publiés

        patience = findViewById(R.id.indeterminateBar);
        patience.setVisibility(View.GONE);

        editeur.putString("errCode", "");
        editeur.putString("errMsg", "");
        editeur.apply();

        auxMethods = new Aux();

        pourJoindreResCar();

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
                    Handler handler =new Handler(Looper.getMainLooper());
                    handler.postDelayed(() -> {
                    }, 200); // délai 0.2 sec
                    alerte("2");
                    finish();
                }
            };
            modelListe.getFlagListe().observe(MainActivity.this, flagListeObserver);

// observateur d'arrivée de la liste des participants et affichage de la liste des groupes
            final Observer<ArrayList<HashMap<String, String>>> listeItemsObserver = new Observer<ArrayList<HashMap<String, String>>>() {
                String pourInfo = "";
                @Override
                public void onChanged(ArrayList<HashMap<String, String>> items) {
                    if (items != null) {
                        listeDesItems = items;
                        patience.setVisibility(View.GONE);
                        if (BuildConfig.DEBUG){
                        Log.i("SECUSERV Main", "taille = " + listeDesItems.size());}

        // Pour communiquer avec le responable du car :
//                        pourJoindreResCar();

                        nomsItems = auxMethods.faitListeGroupes(listeDesItems);
                        if (BuildConfig.DEBUG){
                            Log.i("SECUSERV Main", "groupes = " + nomsItems);}
                        if (nomsItems != null) {
        // au clic, affichage de la composition du groupe dans un fragment
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
/*        ExtendedFloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(view -> {
            Intent retourListeSorties = new Intent(MainActivity.this, StartActivity.class);
            startActivity(retourListeSorties);
            MainActivity.this.finish();
        });  */
    }  // end onCreate

    private void pourJoindreResCar(){
//        resCar = auxMethods.getResCar(listeDesItems, idResCar);
        if (resCar != null) {
            phoneResCar.setOnClickListener(view -> {
                if (ContextCompat.checkSelfPermission(
                        MainActivity.this, Manifest.permission.CALL_PHONE) ==
                        PackageManager.PERMISSION_GRANTED) {
                    if (BuildConfig.DEBUG){
                        Log.i("SECUSERV Main", "telResCar = " + resCar.getTel());}                    Aux.phoneCall(resCar);
                } else {
                    // You can directly ask for the permission.
                    // The registered ActivityResultCallback gets the result of this request.
                    requestPermissionLauncher.launch(
                            Manifest.permission.CALL_PHONE);
                    if (okPhone) {
                        Aux.phoneCall(resCar);
                    }
                }
            });
            emailResCar.setOnClickListener(view -> {
/*  composeEmail utilise le schéma GoogleMail de String[] en extra pour passer les adresses tandis que sendEmail
    utilise le schéma du mailto de HTML mailto:+String, la chaîne contenant les adresses séparées par une virgule.
    MailOrange n'accepte que ce dernier. Avec ce schéma GMail n'accepte pas de sujet ni de texte en extra */
//                String[] adresses = {resCar.getEmail()};
                String subject = "";
                String text = "";
//                Aux.composeEmail(adresses, subject, texte);
                String adresse = resCar.getEmail();
                Aux.sendEmail(adresse, subject, text);
            });
            smsResCar.setOnClickListener(view -> {
                Aux.envoiSMS(resCar);
            });
        }
    }

    @Override
    public boolean onCreateOptionsMenu(@NonNull Menu menu) {
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
        DialogAlertes infoMain = DialogAlertes.newInstance(message);
        infoMain.show(getSupportFragmentManager(), "infoMain");
    }

}
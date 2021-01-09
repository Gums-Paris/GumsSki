package fr.cjpapps.gumski;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.util.HashMap;

public class AuthActivity extends AppCompatActivity {

//  recueillir unsername et password et poster la demande de auth  pour récupérer auth et userId
//  private SharedPreferences mesPrefs;
    private EditText ed1, ed2;
    private String user;
    private String password;
    private final HashMap<String, String> requestParams = new HashMap<>();
    private final HashMap<String, String> postParams = new HashMap<>();
    private String stringRequest;
    private final String[] taskParams = new String[6];
// on met dimension 6 pour taskparams bien que 4 suffisent ici. C'est pour pouvoir utliser PostInfosGums à la fois
// ici et pour poster les paramètres d'un item dans edit et create
    ModelAuth model = null;
    int counter = 3;
    Button envoyer = null;
    SharedPreferences mesPrefs;
    Intent result = new Intent();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_auth);

        counter = 3;
        mesPrefs = MyHelper.getInstance().recupPrefs();
        ed1 = findViewById(R.id.username);
        ed2 = findViewById(R.id.password);
        envoyer = findViewById(R.id.envoi);
        TextView affTemp = findViewById(R.id.tempo);
        model = new ViewModelProvider(this).get(ModelAuth.class);

        requestParams.put("app", Constantes.JOOMLA_USERS);
        requestParams.put("resource", Constantes.JOOMLA_RESOURCE_LOGIN);
        requestParams.put("format", "raw");
        stringRequest = Aux.buildRequest(requestParams);

/* pour donner au client trois chances de s'identifier proprement. Faut pas mettre le code de l'observateur
 après celui du clickListener sinon l'observateur est recréé après chaque clic infructueux ce qui agit sur
 le compteur et conchie la procédure */
        final Observer<Boolean> flagAuthActivObserver = new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean retour) {
                Log.i("SECUSERV", "flagAuthActiv " + retour);
                if (retour) {
                    setResult(RESULT_OK, result);
                    finish();
                } else {
                    new Handler().postDelayed(new Runnable() {
                        public void run() {
                            alerteAuth();
                        }
                    }, 200); // délai 0.2 sec
                    counter--;
                    Log.i("SECUSERV", "counter =  " + counter);
                    if (counter == 0) {
                        Toast.makeText(getApplicationContext(), "Tant pis ! Au revoir", Toast.LENGTH_LONG).show();
                        setResult(RESULT_CANCELED, result);
                        finish();
                    }
                }
            }
        };
        model.getFlagAuthActiv().observe(this, flagAuthActivObserver);

        envoyer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (ed1 != null) {
                    user = ed1.getText().toString();
                }
                if (ed2 != null) {
                    password = ed2.getText().toString();
                }
                postParams.put("username", user);
                postParams.put("password", password);
                taskParams[0] = Variables.urlActive+stringRequest;
                Log.i("SECUSERV", "post url "+taskParams[0]);
                taskParams[1] = Aux.buildRequest(postParams);
                Log.i("SECUSERV", "post params "+taskParams[1]);
                taskParams[2] = "Content-Type";
                taskParams[3] = "application/x-www-form-urlencoded ; utf-8";
                taskParams[4] = "";
                taskParams[5] = "";

                if (Variables.isNetworkConnected) {
                    new PostInfosAuth().execute(taskParams);
                }
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (isFinishing() && !isChangingConfigurations()) {
            Log.i("SECUSERV", "clear model auth ");
            getViewModelStore().clear();
        }
    }


//affichage dialogue d'alerte si problème d'authentification'
    protected void alerteAuth() {
        String message = mesPrefs.getString("errMsg", "")+" \ncode "+mesPrefs.getString("errCode", "");
        DialogAlertes infoUtilisateur = DialogAlertes.newInstance(message);
        infoUtilisateur.show(getSupportFragmentManager(), "infoutilisateur");
    }

}
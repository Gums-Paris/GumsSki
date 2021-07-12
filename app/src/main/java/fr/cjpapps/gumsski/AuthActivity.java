package fr.cjpapps.gumsski;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.util.HashMap;

public class AuthActivity extends AppCompatActivity {

//  recueillir unsername et password et poster la demande de auth  pour récupérer auth et userId
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
// nombre d'essais autorisés pour login
    int counter = 3;
    Button envoyer = null;
    SharedPreferences mesPrefs;
    Intent result = new Intent();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        Toolbar toolbar = findViewById(R.id.login_toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

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
        stringRequest = AuxReseau.buildRequest(requestParams);

/* pour donner au client trois chances de s'identifier proprement. Faut pas mettre le code de l'observateur
 après celui du clickListener sinon l'observateur est recréé après chaque clic infructueux ce qui agit sur
 le compteur et conchie la procédure */
        final Observer<Boolean> flagAuthActivObserver = retour -> {
            Log.i("SECUSERV", "flagAuthActiv " + retour);
            if (retour) {
                setResult(RESULT_OK, result);
                finish();
            } else {
                new Handler(Looper.getMainLooper()).postDelayed(this::alerteAuth, 200); // délai 0.2 sec
                counter--;
                Log.i("SECUSERV", "counter =  " + counter);
                if (counter == 0) {
                    Toast.makeText(getApplicationContext(), "Tant pis ! Au revoir", Toast.LENGTH_LONG).show();
                    setResult(RESULT_CANCELED, result);
                    finish();
                }
            }
        };
        model.getFlagAuthActiv().observe(this, flagAuthActivObserver);

        envoyer.setOnClickListener(view -> {
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
            taskParams[1] = AuxReseau.buildRequest(postParams);
            Log.i("SECUSERV", "post params "+taskParams[1]);
            taskParams[2] = "Content-Type";
            taskParams[3] = "application/x-www-form-urlencoded ; utf-8";
            taskParams[4] = "";
            taskParams[5] = "";

            if (Variables.isNetworkConnected) {
                new PostInfosAuth().execute(taskParams);
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

    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        if (id == R.id.home) {
            setResult(RESULT_OK, result);
//            return true;
        }
        return super.onOptionsItemSelected(item);
    }


}
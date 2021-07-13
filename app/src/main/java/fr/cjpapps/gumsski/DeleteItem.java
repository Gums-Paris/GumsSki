package fr.cjpapps.gumsski;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.util.Log;

import java.util.HashMap;

public class DeleteItem extends AppCompatActivity {

/* Le serveur de OVH n'autorise pas la requête DEL. On fait un GET avec un paramètre supplémentaire
*  fleur = bleuet pour indiquer qu'il faut exécuter une suppression
 */
    private String idItem;
    SharedPreferences mesPrefs;
    SharedPreferences.Editor editeur;
    private final HashMap<String, String> requestParams = new HashMap<>();
    private final String[] taskParams = new String[6];
    Intent result = new Intent();
    TaskRunner taskRunner = new TaskRunner();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_delete_item);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        mesPrefs = MyHelper.getInstance(getApplicationContext()).recupPrefs();
        editeur = mesPrefs.edit();

        Intent intent = getIntent();
        if (intent != null) {
            if (intent.hasExtra("itemChoisi")){
                idItem = intent.getStringExtra("itemChoisi");
            }
        }

        requestParams.put("app", Constantes.JOOMLA_APP);
        requestParams.put("resource", Constantes.JOOMLA_RESOURCE_1);
        requestParams.put("format", "raw");
        requestParams.put("id", idItem);
        requestParams.put("fleur", "bleuet");
        editeur.putString("idDel", idItem);
        editeur.apply();
        taskParams[0] = Variables.urlActive + AuxReseau.buildRequest(requestParams);
        Log.i("SECUSERV", "Del URL " + taskParams[0]);
        taskParams[1] = mesPrefs.getString("auth", "");
        if (Variables.isNetworkConnected) {
//            new DelInfosGums().execute(taskParams);
            taskRunner.executeAsync(new SupprimeInfosGums(taskParams), AuxReseau::decodeRetourDeleteItem);
        }

        setResult(RESULT_OK, result);
        finish();

    }
}
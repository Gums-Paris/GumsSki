package fr.cjpapps.gumsski;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;

import java.util.ArrayList;
import java.util.HashMap;

public class CreateItem extends AppCompatActivity {

    SharedPreferences mesPrefs;

    private final HashMap<String, String> requestParams = new HashMap<>();
    private final HashMap<String, String> postParams = new HashMap<>();
    private final String[] taskParams = new String[6];
    ArrayList<String[]> fieldParams = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_item);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        LinearLayout parentLayout = findViewById(R.id.parent);
        LinearLayout.LayoutParams boutonParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        AuxAvecContext aux = new AuxAvecContext(this);
        fieldParams = aux.buildForm(parentLayout);

        int idS = 301;
        Button boutonSave = new Button(this);
        boutonSave.setId(idS);
        boutonSave.setLayoutParams(boutonParams);
        boutonSave.setText(getString(R.string.save));
        boutonSave.setPadding(40, 5, 40, 0);
        parentLayout.addView(boutonSave);
        boutonSave.setOnClickListener(clickListenerSauv);

        int idA = 302;
        Button boutonAnnul = new Button(this);
        boutonAnnul.setId(idA);
        boutonAnnul.setLayoutParams(boutonParams);
        boutonAnnul.setText(getString(R.string.cancel));
        boutonAnnul.setPadding(0, 5, 0, 0);
        parentLayout.addView(boutonAnnul);
        boutonAnnul.setOnClickListener(clickListenerCancel);

        mesPrefs = MyHelper.getInstance(getApplicationContext()).recupPrefs();
    }

    private final View.OnClickListener clickListenerSauv = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Intent result = new Intent();

            postParams.put("id","0");
            postParams.put("state","1");

            for (String[] params : fieldParams) {
                EditText luChamp = findViewById(Integer.parseInt(params[4]));
                if (luChamp != null) {
                    postParams.put(params[0], luChamp.getText().toString());
                    Log.i("ATTR ", postParams.toString());
                }
            }

            requestParams.put("app", Constantes.JOOMLA_APP);
            requestParams.put("resource", Constantes.JOOMLA_RESOURCE_1);
            requestParams.put("format", "json");
            String stringRequest = Aux.buildRequest(requestParams);
            taskParams[0] = Variables.urlActive+stringRequest;
            Log.i("SECUSERV", " modif post url "+taskParams[0]);
            taskParams[1] = Aux.buildRequest(postParams);
            taskParams[2] = "Content-Type";
            taskParams[3] = "application/x-www-form-urlencoded ; utf-8";
            taskParams[4] = "Authorization";
            taskParams[5] = "Bearer "+ mesPrefs.getString("auth", "");

            if (Variables.isNetworkConnected) {
                new PostInfosItem().execute(taskParams);
            }

            setResult(RESULT_OK, result);
            finish();
        }
    };

    private final View.OnClickListener clickListenerCancel = view -> {
        Intent result = new Intent();
        setResult(RESULT_OK, result);
        finish();
    };
}

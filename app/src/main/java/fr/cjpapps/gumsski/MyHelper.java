package fr.cjpapps.gumsski;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.net.ConnectivityManager;

import androidx.annotation.NonNull;

import static android.content.Context.CONNECTIVITY_SERVICE;
import static android.content.Context.MODE_PRIVATE;
import static fr.cjpapps.gumsski.StartActivity.PREF_FILE;

public class MyHelper {

    /* voir https://stackoverflow.com/questions/2002288/static-way-to-get-context-in-android , message de Alessio
     * MyHelper permet de créer des méthodes ayant besoin de contexte qu'on puisse appeler à partir de méthodes static
     * qui n'ont pas droit à un contexte. MyHelper conserve le contexte de l'application et sert donc de relais.
     * Mais j'arrivais pas à m'en servir parce qu'il fallait passer un contexte auquel je n'avais précisément pas
     * accès depuis une méthode statique. Sauvé par idée de
     * http://brainwashinc.com/2017/08/25/androidjava-sharedpreferences-anywhere-app/
     * rajouter un constructeur d'instance par défaut qu'on peut appeler sans context. Il servira alors à accéder à
     * l'instance qu'on aura préalablement créée à partir d'un endroit où on a accès à ApplicationContext (dans
     * MainActivity ou dans le constructeur du ViewModel).
     */

    private static MyHelper instance;
    private final Context mContext;

    private MyHelper(@NonNull Context context) {
        mContext = context.getApplicationContext();
    }

    // création instance avec paramètre de contexte
    static MyHelper getInstance(@NonNull Context context) {
        synchronized(MyHelper.class) {
            if (instance == null) {
                instance = new MyHelper(context);
            }
            return instance;
        }
    }

    // instance sans paramètre donne accès à l'instance préalablement créée
    static MyHelper getInstance() {
        return instance;
    }

    // méthode pour accéder aux SharedPréférences
    SharedPreferences recupPrefs(){
        return mContext.getSharedPreferences(PREF_FILE, MODE_PRIVATE);
    }

    // méthodepour accéder aux ressources
    Resources recupResources() {
        return mContext.getResources();
    }

    // fabrication d'un gestionnaire de connexion
    ConnectivityManager conMan() {
        return (ConnectivityManager) mContext.getSystemService(CONNECTIVITY_SERVICE);
    }

    // pour accéder au PackageManager
    PackageManager recupPackageManager() {
        return mContext.getPackageManager();
    }

    void launchActivity(Intent intent) {mContext.startActivity(intent);}

    //pour enlever les saletés qui précèdent parfois le json dans les réponses de com_api
    String cleanResult(String result) { return result.substring(result.indexOf("{")); }
}

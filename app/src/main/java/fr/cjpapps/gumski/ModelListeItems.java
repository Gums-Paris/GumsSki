package fr.cjpapps.gumski;

import android.app.Application;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.util.Log;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.MutableLiveData;

import java.util.ArrayList;
import java.util.HashMap;

public class ModelListeItems extends AndroidViewModel {

    static MutableLiveData<ArrayList<HashMap<String,String>>> listeDesItems = new MutableLiveData<>();
    static MutableLiveData<Boolean> flagAuth = new MutableLiveData<>();
    static MutableLiveData<Boolean> flagListe = new MutableLiveData<>();
    static MutableLiveData<Boolean> flagModif = new MutableLiveData<>();
    static MutableLiveData<Boolean> flagSuppress = new MutableLiveData<>();
    static MutableLiveData<Boolean> flagAuthFrag = new MutableLiveData<>();
    static MutableLiveData<HashMap<String,String>> paramSortie = new MutableLiveData<>();
    static MutableLiveData<ArrayList<ArrayList<HashMap<String,String>>>> compositionGroupes = new MutableLiveData<>();
//    static MutableLiveData<Boolean> flagParams = new MutableLiveData<>();

    SharedPreferences mesPrefs;
    Resources mesResources;  // sens différent de API resource

    /* Les getters pour les LiveData. Les setters sont setValue() ou postValue()*/
    MutableLiveData<ArrayList<HashMap<String,String>>> getListeDesItems() {return listeDesItems;}
    MutableLiveData<Boolean> getFlagListe() {return flagListe;}
    MutableLiveData<Boolean> getFlagModif() {return flagModif;}
    MutableLiveData<Boolean> getFlagSuppress() {return flagSuppress;}
    MutableLiveData<Boolean> getFlagAuth() { return flagAuth; }
    MutableLiveData<Boolean> getFlagAuthFrag() { return flagAuthFrag; }
    MutableLiveData<HashMap<String,String>> getParamSortie() {return paramSortie;}
    MutableLiveData<ArrayList<ArrayList<HashMap<String,String>>>> getCompositionGroupes() {return compositionGroupes;}
//    MutableLiveData<Boolean> getFlagParams() { return flagParams; }

    private final HashMap<String, String> requestParams = new HashMap<>();
    private final String[] taskParams = new String[6];

//  Constructeur du modèle ; si on a déjà l'auth on récupère les items ici (sinon c'est l'observateur de auth
//  dans Main qui s'en chargera).
    public ModelListeItems(final Application application) {
        super(application);
        mesPrefs = MyHelper.getInstance().recupPrefs();
        mesResources = MyHelper.getInstance().recupResources();
        Log.i("SECUSERV", "model auth ? "+mesPrefs.getBoolean("authOK", false));
        if (mesPrefs.getBoolean("authOK", false)) {
//                        recupListe();
            if (mesPrefs.getString("date",null) == null ||
             Aux.datePast(mesPrefs.getString("date",null), Integer.parseInt(mesPrefs.getString("jours","2")))) {
                recupInfo(Constantes.JOOMLA_RESOURCE_1, "");
            }
        }
    }

 //   void recupListe () {  // devenu recupInfo pour généraliser à plusieurs resources
     void recupInfo (String uneResource, String uneSortie) {
        String stringRequest;
        requestParams.put("app", Constantes.JOOMLA_APP);
        requestParams.put("resource", uneResource);
        requestParams.put("format", "json");
        requestParams.put("sortieid", uneSortie);
        stringRequest = Aux.buildRequest(requestParams);
        taskParams[0] = Variables.urlActive;
        taskParams[1] = stringRequest;
        taskParams[2] = "Content-Type";
        taskParams[3] = "application/x-www-form-urlencoded";
        taskParams[4] = "Authorization";
        taskParams[5] = "Bearer "+ mesPrefs.getString("auth", "");
        Log.i("SECUSERV", "network ? "+Variables.isNetworkConnected);
        if (Variables.isNetworkConnected)  {
            switch(uneResource) {
                case Constantes.JOOMLA_RESOURCE_1 :
                    new GetParamSortie().execute(taskParams);
                    break;
                case Constantes.JOOMLA_RESOURCE_2 :
                    new GetInfosListe().execute(taskParams);
            }
//            new GetInfosListe().execute(taskParams);
        }
    }
}

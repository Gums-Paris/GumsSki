package fr.cjpapps.gumsski;

import android.app.Application;
import android.content.SharedPreferences;
import android.util.Log;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.MutableLiveData;

import java.util.HashMap;

public class ModelItem extends AndroidViewModel {

// utilisé par Logistique et ModifItem

    private final HashMap<String, String> requestParams = new HashMap<>();
    private final String[] taskParams = new String[6];
    SharedPreferences mesPrefs;
    TaskRunner taskRunner = new TaskRunner();

/* Pour monItem on prend SingleLiveEvent sinon si on prend une nouvelle sortie après avoir
*   visualisé la logistique d'une première la logistique de la première se réaffiche avant la
*   nouvelle (parce que l'observateur est exécuté au démarrage de l'activité en plus des modifs
*   de la LiveData. C'est vraiment gênant si l'une des sorties n'a pas de logistique */
    static SingleLiveEvent<HashMap<String, String>> monItem = new SingleLiveEvent<>();
    static MutableLiveData<Boolean> flagItem = new MutableLiveData<>();

    SingleLiveEvent<HashMap<String, String>> getMonItem() { return monItem; }
    MutableLiveData<Boolean> getFlagItem() { return flagItem; }

    public ModelItem(final Application application) {
        super(application);
        mesPrefs = MyHelper.getInstance().recupPrefs();
    }

    void recupItem (String id) {
        String stringRequest;
        requestParams.put("app", Constantes.JOOMLA_APP);
        requestParams.put("resource", Constantes.JOOMLA_RESOURCE_1);
        requestParams.put("id", id);
        requestParams.put("format", "json");
        stringRequest = AuxReseau.buildRequest(requestParams);
        taskParams[0] = Variables.urlActive;
        taskParams[1] = stringRequest;
        taskParams[2] = "Content-Type";
        taskParams[3] = "application/x-www-form-urlencoded";
        taskParams[4] = "X-Authorization";
        taskParams[5] = "Bearer "+ mesPrefs.getString("auth", "");
        Log.i("SECUSERV", "network ? "+Variables.isNetworkConnected);
        if (Variables.isNetworkConnected)  {
 //           new GetInfosItem().execute(taskParams);
            taskRunner.executeAsync(new RecupInfosGums(taskParams), AuxReseau::decodeInfosItem);
        }
    }

}

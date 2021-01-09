package fr.cjpapps.gumski;

import android.app.Application;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.util.Log;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.MutableLiveData;

import java.util.HashMap;

public class ModelItem extends AndroidViewModel {

    private final HashMap<String, String> requestParams = new HashMap<>();
    private final String[] taskParams = new String[6];
    SharedPreferences mesPrefs;
    Resources mesResources;  // sens différent de API resource dans requestParams

    static MutableLiveData<HashMap<String, String>> monItem = new MutableLiveData<>();

    MutableLiveData<HashMap<String, String>> getMonItem() { return monItem; }

    public ModelItem(final Application application) {
        super(application);
        mesPrefs = MyHelper.getInstance().recupPrefs();
        mesResources = MyHelper.getInstance().recupResources();
    }

    void recupItem (String id) {
        String stringRequest;
/*        requestParams.put("app", mesResources.getString(R.string.joomlaApp));
        requestParams.put("resource", mesResources.getString(R.string.joomlaResource_1)); */
        requestParams.put("app", Constantes.JOOMLA_APP);
        requestParams.put("resource", Constantes.JOOMLA_RESOURCE_1);
        requestParams.put("id", id);
        requestParams.put("format", "json");
        stringRequest = Aux.buildRequest(requestParams);
        taskParams[0] = Variables.urlActive;
        taskParams[1] = stringRequest;
        taskParams[2] = "Content-Type";
        taskParams[3] = "application/x-www-form-urlencoded";
        taskParams[4] = "Authorization";
        taskParams[5] = "Bearer "+ mesPrefs.getString("auth", "");
        Log.i("SECUSERV", "network ? "+Variables.isNetworkConnected);
        if (Variables.isNetworkConnected)  {
            new GetInfosItem().execute(taskParams);
        }
    }

}

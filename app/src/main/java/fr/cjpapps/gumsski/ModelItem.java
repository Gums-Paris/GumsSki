package fr.cjpapps.gumsski;

import android.app.Application;
import android.content.SharedPreferences;
import android.util.Log;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.MutableLiveData;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import static fr.cjpapps.gumsski.Aux.egaliteChaines;

public class ModelItem extends AndroidViewModel {

/* utilisé par Logistique et ModifItem (il paraît que c'est caca mais j'ai pas compris pourquoi,
 car les usages ne sont pas concurrents)
 du coup on ne met pas la récup des données dans le constructeur du modèle avec recours aux Prefs
 si réseau absent parce que pour éditer on tient à avoir les valeurs les plus fraîches des paramètres
 (et de toutes manières, pour éditer il faut avoir le réseau)  */

    SharedPreferences mesPrefs;

    static MutableLiveData<HashMap<String, String>> monItem = new MutableLiveData<>();
    static SingleLiveEvent<Boolean> flagItem = new SingleLiveEvent<>();
    static SingleLiveEvent<Boolean> flagModif = new SingleLiveEvent<>();

    MutableLiveData<HashMap<String, String>> getMonItem() { return monItem; }
    MutableLiveData<Boolean> getFlagItem() { return flagItem; }
    MutableLiveData<Boolean> getFlagModif() {return flagModif;}

    public ModelItem(final Application application) {
        super(application);
        mesPrefs = MyHelper.getInstance().recupPrefs();
    }

    protected void loadDatafromPrefs () {
        HashMap<String,String> itemLogistique;
        Log.i("SECUSERV", "model data from prefs");
//  jsonItem contient la dernière info logistique chargée. On vérifie qu'elle correspond à la sortie
//  voulue et qu'elle n'est pas vide
        String jsonItem = mesPrefs.getString("jsonItem", "");
        String idSortieDispo;
        try {
            JSONObject jsonGums = new JSONObject(jsonItem);
            if (!jsonGums.isNull("data")) {
                JSONObject jsonData = jsonGums.getJSONObject("data");
                idSortieDispo = jsonData.optString("sortieid", "");
                if (BuildConfig.DEBUG){
                Log.i("SECUSERV", "logistique dispo = " + idSortieDispo);}
                if(egaliteChaines(idSortieDispo, mesPrefs.getString("id", null))) {
                    itemLogistique = Aux.getParamsItem(jsonItem);
                    ModelItem.monItem.setValue(itemLogistique);
                    ModelItem.flagItem.setValue(true);
                }else{
                    ModelItem.monItem.setValue(null);
                    if (BuildConfig.DEBUG){
                    Log.i("SECUSERV", "model : info prefs colle pas");}
                }
            }else{
                ModelItem.monItem.setValue(null);
                if (BuildConfig.DEBUG){
                Log.i("SECUSERV", "model logistique prefs null");}
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}

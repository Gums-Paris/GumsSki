package fr.cjpapps.gumsski;

import android.app.Application;
import android.content.SharedPreferences;
import android.util.Log;
import android.view.View;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.MutableLiveData;

import java.util.ArrayList;
import java.util.HashMap;

import static fr.cjpapps.gumsski.AuxReseau.recupInfo;

public class ModelListeSorties extends AndroidViewModel {

    static MutableLiveData<ArrayList<HashMap<String,String>>> paramDesSorties = new MutableLiveData<>();
//    static MutableLiveData<Boolean> flagListeSorties = new MutableLiveData<>();
    static SingleLiveEvent<Boolean> flagListeSorties = new SingleLiveEvent<>();
    static SingleLiveEvent<Boolean> flagReseau = new SingleLiveEvent<>();

    SharedPreferences mesPrefs;

    /* Les getters pour les LiveData. Les setters sont setValue() ou postValue()*/
    MutableLiveData<ArrayList<HashMap<String,String>>> getParamDesSorties() {return paramDesSorties;}
    MutableLiveData<Boolean> getFlagListeSorties() {return flagListeSorties;}
    MutableLiveData<Boolean> getFlagReseau() {return flagReseau;}

    public ModelListeSorties(final Application application) {
        super(application);
        mesPrefs = MyHelper.getInstance().recupPrefs();
        if (BuildConfig.DEBUG){
            Log.i("SECUSERV", "isNetworkConnected = "+Variables.isNetworkConnected);}
        if (Variables.isNetworkConnected) {
            recupInfo(Constantes.JOOMLA_RESOURCE_3, "", "");
        }else{
            flagReseau.setValue(false);
            String dateListeDispo = mesPrefs.getString("datelist", "");
            if (BuildConfig.DEBUG){
                Log.i("SECUSERV", "datelist = "+dateListeDispo);}
            if (!(Aux.egaliteChaines(dateListeDispo, ""))) {
                getListeFromPrefs();
                }else{
                flagListeSorties.setValue(false);
            }
        }
    }

    void getListeFromPrefs(){
        ArrayList<HashMap<String,String>> listeBidule;
//  jsliste contient la liste des sortiess
        String jsliste = mesPrefs.getString("jsonSorties", "");
        listeBidule = Aux.getListeSorties(jsliste);
        if(listeBidule != null){
            ModelListeSorties.paramDesSorties.setValue(listeBidule);
        }
    }

}

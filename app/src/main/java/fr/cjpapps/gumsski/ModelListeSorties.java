package fr.cjpapps.gumsski;

import android.app.Application;
import android.content.SharedPreferences;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.MutableLiveData;

import java.util.ArrayList;
import java.util.HashMap;

import static fr.cjpapps.gumsski.Aux.recupInfo;

public class ModelListeSorties extends AndroidViewModel {

    static MutableLiveData<ArrayList<HashMap<String,String>>> paramDesSorties = new MutableLiveData<>();
    static MutableLiveData<Boolean> flagListeSorties = new MutableLiveData<>();

    SharedPreferences mesPrefs;

    /* Les getters pour les LiveData. Les setters sont setValue() ou postValue()*/
    MutableLiveData<ArrayList<HashMap<String,String>>> getParamDesSorties() {return paramDesSorties;}
    MutableLiveData<Boolean> getFlagListeSorties() {return flagListeSorties;}

    public ModelListeSorties(final Application application) {
        super(application);
        mesPrefs = MyHelper.getInstance().recupPrefs();

        recupInfo(Constantes.JOOMLA_RESOURCE_3, "");

    }

    void getListeFromPrefs(){
        ArrayList<HashMap<String,String>> listeBidule;
//  jsonliste contient la liste des participants
        String jsliste = mesPrefs.getString("jsonSorties", "");
        listeBidule = Aux.getListeSorties(jsliste);
        if(listeBidule != null){
            ModelListeSorties.paramDesSorties.setValue(listeBidule);
        }
    }

}

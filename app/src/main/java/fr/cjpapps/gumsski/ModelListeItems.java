package fr.cjpapps.gumsski;

import android.app.Application;
import android.content.SharedPreferences;
import android.util.Log;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.MutableLiveData;

import java.util.ArrayList;
import java.util.HashMap;

import static fr.cjpapps.gumsski.Aux.egaliteChaines;
import static fr.cjpapps.gumsski.AuxReseau.recupInfo;

public class ModelListeItems extends AndroidViewModel {

    static MutableLiveData<ArrayList<HashMap<String,String>>> listeDesItems = new MutableLiveData<>();
    static MutableLiveData<Boolean> flagListe = new MutableLiveData<>();
    static MutableLiveData<Boolean> flagSuppress = new MutableLiveData<>();
    static MutableLiveData<HashMap<String,String>> paramSortie = new MutableLiveData<>();
    static MutableLiveData<ArrayList<ArrayList<HashMap<String,String>>>> compositionGroupes = new MutableLiveData<>();

    SharedPreferences mesPrefs;

    /* Les getters pour les LiveData. Les setters sont setValue() ou postValue()*/
    MutableLiveData<ArrayList<HashMap<String,String>>> getListeDesItems() {return listeDesItems;}
    MutableLiveData<Boolean> getFlagListe() {return flagListe;}
    MutableLiveData<Boolean> getFlagSuppress() {return flagSuppress;}
    MutableLiveData<HashMap<String,String>> getParamSortie() {return paramSortie;}
    MutableLiveData<ArrayList<ArrayList<HashMap<String,String>>>> getCompositionGroupes() {return compositionGroupes;}

//  Constructeur du modèle ; on récupère la liste des participants ici
    public ModelListeItems(final Application application) {
        super(application);
        mesPrefs = MyHelper.getInstance().recupPrefs();
        Log.i("SECUSERV", "main model auth ? "+mesPrefs.getBoolean("authOK", false));

/*  Si dateWE eqt vide ou si la date des infos ne colle pas avec la date du WE, on va chercher les infos sur gumsparis.
*   Sinon on les récupère en sharedPreferences.
*   dateData est mis égal à dateWE par getInfosListe ; a quoi ça sert ?*/
        String dateWE = mesPrefs.getString("date", null);
        String dateInfosDisponibles = mesPrefs.getString("dateData", null);
        if (!egaliteChaines(dateWE, dateInfosDisponibles)) {
            recupInfo(Constantes.JOOMLA_RESOURCE_2, mesPrefs.getString("id", null));
        }else {
            getInfosFromPrefs();
        }

    }

    void getInfosFromPrefs() {
        ArrayList<HashMap<String,String>> listeBidule;
//  jsonliste contient la liste des participants
        String jsliste = mesPrefs.getString("jsonListe", "");
        listeBidule = Aux.getListeItems(jsliste);
        if(listeBidule != null){
            ModelListeItems.listeDesItems.setValue(listeBidule);
            ModelListeItems.flagListe.setValue(true);
        }else{
            ModelListeItems.flagListe.setValue(false);
        }
    }

}

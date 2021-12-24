package fr.cjpapps.gumsski;

import android.app.Application;
import android.content.SharedPreferences;
import android.util.Log;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.MutableLiveData;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;

import static fr.cjpapps.gumsski.Aux.egaliteChaines;
import static fr.cjpapps.gumsski.Aux.isEmptyString;
import static fr.cjpapps.gumsski.AuxReseau.recupInfo;

public class ModelListeItems extends AndroidViewModel {

/* Caveat :
*   Lorsqu'on enchaîne affichage groupes d'une sortie > retour liste > affichage groupes d'une autre sortie
*   (ce qui d'ailleurs doit être assez rare) l'observer de Main s'exécute avec avec la listeDesItems de la
*   première sortie. Il va jusqu'à attacher l'adapter à la recyclerview mais celle-ci ne s'affiche pas et
*   la recyclerview correspondant au vrai onChange s'affiche normalement. SingleLiveEvent pas possible parce que
*   le fragment a lui aussi besoin de la liste des Items et que mainactivity a besoin de la livedata pour
*   retrouver ses billes après rotation écran */

    static MutableLiveData<ArrayList<HashMap<String,String>>> listeDesItems = new MutableLiveData<>();
    static SingleLiveEvent<Boolean> flagListe = new SingleLiveEvent<>();
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
        if (BuildConfig.DEBUG){
        Log.i("SECUSERV", "main model auth ? "+mesPrefs.getBoolean("authOK", false));}

/*  Si id sortie ne colle pas avec id données de sortie disponible, on va chercher les infos sur gumsparis.
*   Sinon on les récupère en sharedPreferences sauf si les infos datent de plus de 1 jour, et ce
*   seulement tant qu'on n'est pas arrivé à la date du WE (mais pas s'il n'y a pas d'accès réseau
*   auquel cas on prend quand même les prefs).
*   idData a été enregistré par AuxReseau.decodeInfosItems lorsque la récup données par le réseau a marché */
        String dateWE = mesPrefs.getString("date", null);
        String dateInfosDisponibles = mesPrefs.getString("dateRecupData", "");
//        if (BuildConfig.DEBUG){
 //           Log.i("SECUSERV Dates", "WE " +dateWE+" Dispo "+dateInfosDisponibles);}
        String sortieId = mesPrefs.getString("id", null);
        String idSortieDispo = mesPrefs.getString("idData", null);
//        if (BuildConfig.DEBUG){
 //           Log.i("SECUSERV", "main model  " +sortieId+" "+idSortieDispo);}
        if (egaliteChaines(sortieId, idSortieDispo)) {
            if (verifDates(dateWE, dateInfosDisponibles)  || !Variables.isNetworkConnected){
            getInfosFromPrefs();
            }
            else {
                recupInfo(Constantes.JOOMLA_RESOURCE_2, mesPrefs.getString("id", null), "");
            }
        }else if (Variables.isNetworkConnected){
            recupInfo(Constantes.JOOMLA_RESOURCE_2, mesPrefs.getString("id", null), "");
        }else flagListe.setValue(false);
    }

    void getInfosFromPrefs() {
        ArrayList<HashMap<String,String>> listeBidule;
//  jsonliste contient la liste des participants
        String jsliste = mesPrefs.getString("jsonListe", "");
        if (BuildConfig.DEBUG){
            Log.i("SECUSERV", "modelliste prefs jsliste participants " + jsliste);}
        listeBidule = Aux.getListeItems(jsliste);
        if(listeBidule != null){
            ModelListeItems.listeDesItems.setValue(listeBidule);
            ModelListeItems.flagListe.setValue(true);
        }else{
            ModelListeItems.flagListe.setValue(false);
        }
    }

/*  verifDates renvoie true si la date du jour est après la date de début du WE (jour-1 après
*    date sortie) ou si les données disponibles datent de moins d'une journée (date données après
*    jour-1 */
    boolean verifDates(String WE, String infoDispo) {
        if (Aux.isEmptyString(WE) || Aux.isEmptyString(infoDispo)) {
            return false;
        }
        Date dateSortie = null;
        Date dateData = null;
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        try {
            dateSortie = sdf.parse(WE);
            dateData = sdf.parse(infoDispo);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        final Calendar now = Calendar.getInstance();
        now.add(Calendar.DATE, -1);
        Date dateUn = now.getTime();
        now.add(Calendar.DATE, 0);
        Date dateDeux = now.getTime();
//        if (BuildConfig.DEBUG){
//            Log.i("SECUSERV Dates", "dateUn " +dateUn+" dateDeux "+dateDeux);}
        if (dateUn.after(dateSortie)) return true;
        assert dateData != null;
        return (dateData.after(dateDeux));
    }

}

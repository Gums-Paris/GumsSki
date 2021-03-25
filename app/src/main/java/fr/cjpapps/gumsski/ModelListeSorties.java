package fr.cjpapps.gumsski;

import android.app.Application;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.MutableLiveData;

import java.util.ArrayList;
import java.util.HashMap;

import static fr.cjpapps.gumsski.Aux.recupInfo;

public class ModelListeSorties extends AndroidViewModel {

    static MutableLiveData<ArrayList<HashMap<String,String>>> paramDesSorties = new MutableLiveData<>();
    static MutableLiveData<Boolean> flagListeSorties = new MutableLiveData<>();

    /* Les getters pour les LiveData. Les setters sont setValue() ou postValue()*/
    MutableLiveData<ArrayList<HashMap<String,String>>> getParamDesSorties() {return paramDesSorties;}
    MutableLiveData<Boolean> getFlagListeSorties() {return flagListeSorties;}

    public ModelListeSorties(final Application application) {
        super(application);

        recupInfo(Constantes.JOOMLA_RESOURCE_3, "");

    }

}

package fr.cjpapps.gumsski;

import android.app.Application;
import android.content.SharedPreferences;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.MutableLiveData;

import java.util.HashMap;

public class ModelItem extends AndroidViewModel {

// utilisé par Logistique et ModifItem

    SharedPreferences mesPrefs;

    static MutableLiveData<HashMap<String, String>> monItem = new MutableLiveData<>();
    static MutableLiveData<Boolean> flagItem = new MutableLiveData<>();
    static MutableLiveData<Boolean> flagModif = new MutableLiveData<>();

    MutableLiveData<HashMap<String, String>> getMonItem() { return monItem; }
    MutableLiveData<Boolean> getFlagItem() { return flagItem; }
    MutableLiveData<Boolean> getFlagModif() {return flagModif;}

    public ModelItem(final Application application) {
        super(application);
//        Log.i("SECUSERV", "modelitem constructeur ");
        mesPrefs = MyHelper.getInstance().recupPrefs();
    }

}

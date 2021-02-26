package fr.cjpapps.gumsski;

import android.app.Application;
import android.util.Log;

import androidx.lifecycle.AndroidViewModel;

public class ModelAuth extends AndroidViewModel {

/*  ViewModel pour AuthActivity
*   Le SingleLiveEvent est une extension de la classe MutableLiveData
*   */
    static SingleLiveEvent<Boolean> flagAuthActiv = new SingleLiveEvent<>();
    SingleLiveEvent<Boolean> getFlagAuthActiv() {return flagAuthActiv;}

    public ModelAuth(final Application application) {
        super(application);
        Log.i("SECUSERV authmodel", "construct");
    }
}

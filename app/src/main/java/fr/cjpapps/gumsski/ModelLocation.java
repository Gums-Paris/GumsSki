package fr.cjpapps.gumsski;

import android.app.Application;
import android.location.Location;
import android.util.Log;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.MutableLiveData;

public class ModelLocation extends AndroidViewModel {

    private final MutableLiveData<Location>positionActuelle;
    private final LocRepository repository;

    public ModelLocation(Application application) {
        super (application);
        if (BuildConfig.DEBUG){
        Log.i("SECUSERV", "constructeur du model");}
        repository = new LocRepository(application);
        positionActuelle = repository.getPosition();
    }

    MutableLiveData<Location> getPositionActuelle() { return positionActuelle; }

    void trouvePosition() {
        if (BuildConfig.DEBUG){
        Log.i("SECUSERV", "model dans trouveposition");}
        repository.findPosition(); }

    void updatePosition() {
        if (BuildConfig.DEBUG){
        Log.i("SECUSERV", "model dans updateposition");}
//        if (Variables.gpsOK){
        repository.startLocationUpdates();
//        }
    }
    void stopUpdatePosition(){
        repository.stopLocationUpdates(); }
    void changeRequest(){
        repository.changeRequestInterval();
    }
}

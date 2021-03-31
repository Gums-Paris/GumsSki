package fr.cjpapps.gumsski;

import android.content.SharedPreferences;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;

public class GetInfosListe extends GetInfosGums {

    @Override
    protected void onPostExecute (String result) {

        SharedPreferences mesPrefs = MyHelper.getInstance().recupPrefs();
        SharedPreferences.Editor  editeur = mesPrefs.edit();
        ArrayList<HashMap<String,String>> listeBidule;
        try {
            JSONObject jsonGums = new JSONObject(result);
            String errMsg = jsonGums.optString("err_msg");
            String errCode = jsonGums.optString("err_code");
            if ("".equals(errCode)) {
                editeur.putString("jsonListe", result);
                editeur.apply();
                listeBidule = Aux.getListeItems(result);
                if(listeBidule != null){
                    ModelListeItems.listeDesItems.setValue(listeBidule);
                    ModelListeItems.flagListe.setValue(true);
                    editeur.putString("dateData", mesPrefs.getString("date", null));
                    editeur.apply();
                }else{
                    ModelListeItems.flagListe.setValue(false);
                }
            }else{
                ModelListeItems.flagListe.setValue(false);
                editeur.putString("errMsg", errMsg);
                editeur.putString("errCode", errCode);
                editeur.apply();
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

}

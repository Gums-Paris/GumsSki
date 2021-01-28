package fr.cjpapps.gumski;

import android.content.SharedPreferences;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;


public class GetParamSortie extends GetInfosGums{

    @Override
    protected void onPostExecute (String result) {

        SharedPreferences mesPrefs = MyHelper.getInstance().recupPrefs();
        SharedPreferences.Editor  editeur = mesPrefs.edit();
        HashMap<String,String> params;
        try {
            JSONObject jsonGums = new JSONObject(result);
            String errMsg = jsonGums.optString("err_msg");
            String errCode = jsonGums.optString("err_code");
            if ("".equals(errCode)) {
                editeur.putString("jsonParams", result);
                editeur.apply();

                params = Aux.getListeParams(result);
                Log.i("SECUSERV", "get params & setValue ");
                if (params != null) {
                    ModelListeItems.paramSortie.setValue(params);
                    ModelListeItems.flagListe.setValue(true);
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

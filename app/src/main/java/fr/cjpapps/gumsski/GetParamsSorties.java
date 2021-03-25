package fr.cjpapps.gumsski;

import android.content.SharedPreferences;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;

public class GetParamsSorties extends GetInfosGums {

    @Override
    protected void onPostExecute (String result) {

        SharedPreferences mesPrefs = MyHelper.getInstance().recupPrefs();
        SharedPreferences.Editor  editeur = mesPrefs.edit();
        ArrayList<HashMap<String,String>> params;
        try {
            JSONObject jsonGums = new JSONObject(result);
            String errMsg = jsonGums.optString("err_msg");
            String errCode = jsonGums.optString("err_code");
            if ("".equals(errCode)) {
                editeur.putString("jsonSorties", result);
                editeur.apply();

                params = Aux.getListeSorties(result);
                Log.i("SECUSERV", "get liste sorties & setValue ");
                if (params != null) {
                    ModelListeSorties.paramDesSorties.setValue(params);
                    ModelListeSorties.flagListeSorties.setValue(true);
                }else{
                    ModelListeSorties.flagListeSorties.setValue(false);
                }
            }else{
                ModelListeSorties.flagListeSorties.setValue(false);
                editeur.putString("errMsg", errMsg);
                editeur.putString("errCode", errCode);
                editeur.apply();
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

    }
}

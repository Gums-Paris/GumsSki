package fr.cjpapps.gumsski;

import android.content.SharedPreferences;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;

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
                final Calendar c = Calendar.getInstance();
                Date dateJour = c.getTime();
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
                String dateListe = sdf.format(dateJour);
                Log.i("SECUSERV", "date jour = "+dateListe);

                params = Aux.getListeSorties(result);
                Log.i("SECUSERV", "get liste sorties & setValue ");
                if (params != null) {
                    editeur.putString("datelist", dateListe);
                    editeur.putString("jsonSorties", result);
                    editeur.apply();
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

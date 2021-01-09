package fr.cjpapps.gumski;

import android.content.SharedPreferences;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;

import static fr.cjpapps.gumski.MainActivity.DATELISTE;

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
                try {
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
                    Date today = Calendar.getInstance().getTime();
                    editeur.putString(DATELISTE, sdf.format(today));
                    editeur.apply();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                listeBidule = Aux.getListeItems(result);

                ModelListeItems.listeDesItems.setValue(listeBidule);
                ModelListeItems.flagListe.setValue(true);
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

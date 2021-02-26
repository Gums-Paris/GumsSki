package fr.cjpapps.gumsski;

import android.content.SharedPreferences;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

public class PostInfosAuth extends PostInfosGums{

// traitement du retour du POST d'authentification ; authOK, auth et userId sont mis dans les prefs
// et on met les valeurs des flags dans les mutablelivedata
    SharedPreferences mesPrefs;
    SharedPreferences.Editor editeur;

    protected void onPostExecute (String resultat) {
        mesPrefs = MyHelper.getInstance().recupPrefs();
        editeur = mesPrefs.edit();

        try {
            JSONObject jsonGums = new JSONObject(resultat);
            String errMsg = jsonGums.optString("err_msg");
            String errCode = jsonGums.optString("err_code");
            JSONObject data = jsonGums.getJSONObject("data");
            String auth = data.optString("auth");
            String code = data.optString("code");
            String userId = data.optString("id");
            Log.i("SECUSERV", " onpostexec auth "+auth+" code "+code+" userId "+userId);
            if ("200".equals(code)) {
                editeur.putBoolean("authOK", true);
                editeur.putString("auth", auth);
                editeur.putString("userId", userId);
                editeur.apply();
// flagAuth et flagAuthFrag pas utilisés version 3 février 2021
                ModelListeItems.flagAuth.setValue(true);
                ModelListeItems.flagAuthFrag.setValue(true);
                ModelAuth.flagAuthActiv.setValue(true);
            }else{
                ModelListeItems.flagAuth.setValue(false);
                ModelListeItems.flagAuthFrag.setValue(false);
                ModelAuth.flagAuthActiv.setValue(false);
                editeur.putString("errMsg", errMsg);
                editeur.putString("errCode", errCode);
                editeur.apply();
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}

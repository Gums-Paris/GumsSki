package fr.cjpapps.gumsski;

import android.content.SharedPreferences;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

public class PostInfosItem extends PostInfosGums {

/* si on essaye de modifier un item qui n'existe pas il n'y a pas d'erreur apparente mais il ne se
* passe rien en fait, sauf que la liste étant alors rechargée on voit l'item disparaître. Ceci pourrait
* arriver si un autre usager sur une autre machine a supprimé l'item depuis qu'on l'a chargé.
*
* Noter aussi qu'il n'y a pas de différence de requête entre modifier et créer. La seule différence est
* que dans les paramètres de l'item on met id = 0 pour créer et id = l'id de l'item pour modifier. */

    protected void onPostExecute (String resultat) {
        SharedPreferences mesPrefs = MyHelper.getInstance().recupPrefs();
        SharedPreferences.Editor  editeur = mesPrefs.edit();
        Log.i("SECUSERV", " onpostexec massif "+resultat);
        try {
            JSONObject jsonGums = new JSONObject(resultat);
            String errMsg = jsonGums.optString("err_msg");
            String errCode = jsonGums.optString("err_code");
            if ("".equals(errCode)) {
                ModelListeItems.flagModif.setValue(true);
                int idItem = jsonGums.optInt("data");
            }else{
                ModelListeItems.flagModif.setValue(false);
                editeur.putString("errMsg", errMsg);
                editeur.putString("errCode", errCode);
                editeur.apply();
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

    }
}

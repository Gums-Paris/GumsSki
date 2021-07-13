package fr.cjpapps.gumsski;

import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkInfo;
import android.os.Build;
import android.util.Log;

import androidx.annotation.NonNull;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;

public class AuxReseau {

    /*  la méthode de détection de l'accès réseau change à partir de Android Q (API 29) qui trouve que activeNetworkIfo c'est
     *   très caca. Pour Q il faut mettre en place un NetworkCallback qui intervient lorsque la connectivité change.
     *   La méthode pour Q est inspirée de PasanBhanu :
     *   https://gist.github.com/PasanBhanu/730a32a9eeb180ec2950c172d54bb06a
     *   Il apparait qu'il faut un registerDefaultNetworkCallback plutôt qu'un registerNetworkCallback parce que les téléphones
     *   modernes ont souvent plusieurs connexions indépendantes actives ce qui fait que les onAvailable et onLost peuvent
     *   s'emmeler les pinceaux. Ou alors je suppose qu'il faut se choisir un réseau par le logiciel, vive le progrès  */
    public static void watchNetwork() {
        ConnectivityManager connectivityManager = MyHelper.getInstance().conMan();
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
            NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
            Variables.isNetworkConnected = activeNetworkInfo != null && activeNetworkInfo.isConnected();
        }else {
            try {
                connectivityManager.registerDefaultNetworkCallback(new ConnectivityManager.NetworkCallback() {
                       @Override
                       public void onAvailable(@NonNull Network network) {
                           Log.i("SECUSERV", "on available " );
                           Variables.isNetworkConnected = true; // Global Static Variable
                       }
                       @Override
                       public void onLost(@NonNull Network network) {
                           Log.i("SECUSERV", "on lost " );
                           Variables.isNetworkConnected = false; // Global Static Variable
                       }
                    }
                );
            } catch (Exception e) {
                Variables.isNetworkConnected = false;
            }
        }
    }

    //   void recupListe () {  // devenu recupInfo pour généraliser à plusieurs resources
    static void recupInfo (String uneResource, String uneSortie) {
        TaskRunner taskRunner = new TaskRunner();
        SharedPreferences mesPrefs = MyHelper.getInstance().recupPrefs();
        String stringRequest;
        final HashMap<String, String> requestParams = new HashMap<>();
        final String[] taskParams = new String[6];
        requestParams.put("app", Constantes.JOOMLA_APP);
        requestParams.put("resource", uneResource);
        requestParams.put("format", "json");
        requestParams.put("sortieid", uneSortie);
        stringRequest = AuxReseau.buildRequest(requestParams);
        taskParams[0] = Variables.urlActive;
        taskParams[1] = stringRequest;
        taskParams[2] = "Content-Type";
        taskParams[3] = "application/x-www-form-urlencoded";
        taskParams[4] = "X-Authorization";
        taskParams[5] = "Bearer "+ mesPrefs.getString("auth", "");
        Log.i("SECUSERV", "network ? "+Variables.isNetworkConnected);
        if (Variables.isNetworkConnected)  {
            switch(uneResource) {
                case Constantes.JOOMLA_RESOURCE_1 :
// pas utilisé, scorie à éliminer
//                    new GetParamSortie().execute(taskParams);
                    taskRunner.executeAsync(new RecupInfosGums(taskParams), AuxReseau::decodeInfosSortie);
                    break;
                case Constantes.JOOMLA_RESOURCE_2 :
//                    new GetInfosListe().execute(taskParams);
                    taskRunner.executeAsync(new RecupInfosGums(taskParams), AuxReseau::decodeInfosItems);
                case Constantes.JOOMLA_RESOURCE_3 :
//                    new GetParamsSorties().execute(taskParams);
                    taskRunner.executeAsync(new RecupInfosGums(taskParams), AuxReseau::decodeInfosSorties);
            }
        }
    }

// fabrique la chaîne de requête à partir du tableau des paramètres
// cette chaîne sera ajoutée à ".../index.php?option=com_api&"
// ou mise dans le corps d'une requête POST
    static String buildRequest(HashMap<String,String> params) {
        StringBuilder sbParams = new StringBuilder();
        int i = 0;
        for (String key : params.keySet()) {
            try {
                if (i != 0) {
                    sbParams.append("&");
                }
                sbParams.append(key).append("=")
                        .append(URLEncoder.encode(params.get(key), "UTF-8"));

            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
            i++;
        }
        Log.i("SECUSERV", "request = "+sbParams.toString());
        return sbParams.toString();
    }

    static void decodeInfosSorties (String result){
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

    static void decodeInfosSortie(String result){
// pas nécessaire dans la version actuelle
    }

    static void decodeInfosItems(String result){
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

    static void decodeInfosItem(String result) {
        SharedPreferences mesPrefs = MyHelper.getInstance().recupPrefs();
        SharedPreferences.Editor  editeur = mesPrefs.edit();
        HashMap<String, String> monItem = new HashMap<>();
        try {
            JSONObject jsonGums = new JSONObject(result);
            String errMsg = jsonGums.optString("err_msg");
            String errCode = jsonGums.optString("err_code");
            if ("".equals(errCode)) {
                editeur.putString("monItem", result);
                editeur.apply();

                JSONObject jsonData = jsonGums.getJSONObject("data");
                for (Attributs attr : Attributs.values()) {
                    monItem.put(attr.getChamp(), jsonData.optString(attr.getChamp()));
                }
                ModelItem.monItem.setValue(monItem);
            }else{
                editeur.putString("errMsg", errMsg);
                editeur.putString("errCode", errCode);
                editeur.apply();
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    static void decodeInfosAuth(String resultat) {
        SharedPreferences mesPrefs = MyHelper.getInstance().recupPrefs();
        SharedPreferences.Editor editeur = mesPrefs.edit();
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
                ModelAuth.flagAuthActiv.setValue(true);
            }else{
                ModelAuth.flagAuthActiv.setValue(false);
                editeur.putString("errMsg", errMsg);
                editeur.putString("errCode", errCode);
                editeur.apply();
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    static void decodeRetourPostItem(String resultat) {
/* si on essaye de modifier un item qui n'existe pas il n'y a pas d'erreur apparente mais il ne se
 * passe rien en fait, sauf que la liste étant alors rechargée on voit l'item disparaître. Ceci pourrait
 * arriver si un autre usager sur une autre machine a supprimé l'item depuis qu'on l'a chargé
 * (normalement c'est pas possible parce que l'item dont on demande l'édition est checked-out dans
 * Joomla)
 *
 * Noter aussi qu'il n'y a pas de différence de requête entre modifier et créer. La seule différence est
 * que dans les paramètres de l'item on met id = 0 pour créer et id = l'id de l'item pour modifier. */

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

    static void decodeRetourDeleteItem(String result){
// se charge de gérer les erreurs et de positionner flagSuppress
        SharedPreferences mesPrefs = MyHelper.getInstance().recupPrefs();
        SharedPreferences.Editor  editeur = mesPrefs.edit();
        try {
            JSONObject jsonGums = new JSONObject(result);
            String errMsg = jsonGums.optString("err_msg");
            String errCode = jsonGums.optString("err_code");
            if ("".equals(errCode)) {
                String content = jsonGums.optString("data");
                Log.i("SECUSERV", "del data "+content);
                if (!(Aux.egaliteChaines(content, mesPrefs.getString("idDel", "0")))) {
                    editeur.putString("errMsg", "item inexistant");
                    editeur.putString("errCode", content);
                    editeur.apply();
                    ModelListeItems.flagSuppress.setValue(false);
                }else{
                    ModelListeItems.flagSuppress.setValue(true);
                }
            }else{
                editeur.putString("errMsg", errMsg);
                editeur.putString("errCode", errCode);
                editeur.apply();
                ModelListeItems.flagSuppress.setValue(false);
            }
        }catch(JSONException e){
            e.printStackTrace();
        }

    }

}


package fr.cjpapps.gumsski;

import android.content.SharedPreferences;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;


public class AuxReseau {

    //   void recupListe () {  // devenu recupInfo pour généraliser à plusieurs resources
    // lance les requêtes auprès de gumsparis
    // task = "" pour un GET normal, task = edit pour un GET avec checkout. C'est com_api qui relaye
    // le test sur isNetworkConnected sert à rien car internet est testé avant tout appel à recupInfo; laissé au cas où
    static void recupInfo (String uneResource, String uneSortie, String uneTask) {
        TaskRunner taskRunner = TaskRunner.getInstance();
        SharedPreferences mesPrefs = MyHelper.getInstance().recupPrefs();
        String stringRequest = "";
        final HashMap<String, String> requestParams = new HashMap<>();
        final String[] taskParams = new String[6];
        requestParams.put("app", Constantes.JOOMLA_APP);
        requestParams.put("resource", uneResource);
        requestParams.put("format", "json");
        requestParams.put("sortieid", uneSortie);
        requestParams.put("task", uneTask);
        stringRequest = AuxReseau.buildRequest(requestParams);
        taskParams[0] = Variables.urlActive;
        taskParams[1] = stringRequest;
        taskParams[2] = "Content-Type";
        taskParams[3] = "application/x-www-form-urlencoded";
        taskParams[4] = "X-Authorization";
        taskParams[5] = "Bearer "+ mesPrefs.getString("auth", "");
        if (BuildConfig.DEBUG){
        Log.i("SECUSERV", "network ? "+Variables.isNetworkConnected);}
        if (Variables.isNetworkConnected)  {
            switch(uneResource) {
                case Constantes.JOOMLA_RESOURCE_1 :
// pour récupérer la logistique de uneSortie
                    taskRunner.executeAsync(new RecupInfosGums(taskParams), AuxReseau::decodeInfosItem);
                    break;
                case Constantes.JOOMLA_RESOURCE_2 :
// pourt récupérer la liste des participants de uneSortie
                    taskRunner.executeAsync(new RecupInfosGums(taskParams), AuxReseau::decodeInfosItems);
                    break;
//                case Constantes.JOOMLA_RESOURCE_3 :
                    default:
//pour récupérer la liste des sorties
                    taskRunner.executeAsync(new RecupInfosGums(taskParams), AuxReseau::decodeInfosSorties);
                    break;
            }
        }else{ModelListeSorties.flagReseau.setValue(false);}
    }

//Pour envoyer les requêtes de type POST à gumsparis. C'est fait à travers com_api
// task = '' pour sauvegarder, task = checkin pour annuler
// le paramètre uneSortie sert pour vérifier une fois de plus si le user est autorisé
    static void envoiInfo (String uneResource, HashMap<String, String> postParams, String uneSortie, String uneTask) {
        TaskRunner taskRunner = new TaskRunner();
        SharedPreferences mesPrefs = MyHelper.getInstance().recupPrefs();
        String stringRequest = "";
        final HashMap<String, String> requestParams = new HashMap<>();
        final String[] taskParams = new String[6];
        requestParams.put("app", Constantes.JOOMLA_APP);
        requestParams.put("resource", uneResource);
        requestParams.put("format", "json");
        requestParams.put("task", uneTask);
        requestParams.put("sortieid", uneSortie);
        stringRequest = AuxReseau.buildRequest(requestParams);
        taskParams[0] = Variables.urlActive+stringRequest;
        taskParams[1] = AuxReseau.buildRequest(postParams);
        taskParams[2] = "Content-Type";
        taskParams[3] = "application/x-www-form-urlencoded";
        taskParams[4] = "X-Authorization";
        taskParams[5] = "Bearer "+ mesPrefs.getString("auth", "");
        if (Variables.isNetworkConnected) {
            taskRunner.executeAsync(new EnvoiInfosGums(taskParams), AuxReseau::decodeRetourPostItem);
        }
    }

// fabrique la chaîne de requête à partir du tableau des paramètres
// cette chaîne sera ajoutée à ".../index.php?option=com_api&"
// ou constituera le corps d'une requête POST
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
        if (BuildConfig.DEBUG){
        Log.i("SECUSERV", "build request = "+sbParams);}
        return sbParams.toString();
    }

// fabrique liste des sorties pour StartActivity
    static void decodeInfosSorties (String result){
        SharedPreferences mesPrefs = MyHelper.getInstance().recupPrefs();
        SharedPreferences.Editor  editeur = mesPrefs.edit();
        ArrayList<HashMap<String,String>> params;
        if (!"netOUT".equals(result)) {
            result = MyHelper.getInstance().cleanResult(result);
            Log.i("SECUSERV", "clean retour =  "+result);
            try {
                JSONObject jsonGums = new JSONObject(result);
                String errMsg = jsonGums.optString("err_msg");
                String errCode = jsonGums.optString("err_code");
                if ("".equals(errCode)) {
                    String dateListe = mesPrefs.getString("today","");
//                    if (BuildConfig.DEBUG){
//                    Log.i("SECUSERV", "decode sorties date jour = " + dateListe);}

                    params = Aux.getListeSorties(result);
                    if (params != null) {
                        editeur.putString("datelist", dateListe);
                        editeur.putString("jsonSorties", result);
                        editeur.apply();
                        ModelListeSorties.paramDesSorties.setValue(params);
                        ModelListeSorties.flagListeSorties.setValue(true);
                    } else {
                        ModelListeSorties.flagListeSorties.setValue(false);
                    }
                } else {
                    ModelListeSorties.flagListeSorties.setValue(false);
                    editeur.putString("errMsg", errMsg);
                    editeur.putString("errCode", errCode);
                    editeur.apply();
                }
            } catch (JSONException e) {
                e.printStackTrace();
                ModelListeSorties.flagListeSorties.setValue(false);
            }
        }else{
            ModelListeSorties.flagListeSorties.setValue(false);
        }
    }

    static void decodeInfosSortie(String result){
// pas nécessaire dans la version actuelle
    }

// fabrique liste des participants pour MainActivity
    static void decodeInfosItems(String result){
        SharedPreferences mesPrefs = MyHelper.getInstance().recupPrefs();
        SharedPreferences.Editor  editeur = mesPrefs.edit();
        ArrayList<HashMap<String,String>> listeBidule;
        if (!"netOUT".equals(result)) {
            result = MyHelper.getInstance().cleanResult(result);
            Log.i("SECUSERV", "clean retour =  "+result);
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
                    editeur.putString("dateRecupData", mesPrefs.getString("today", null));
                    editeur.putString("dateData", mesPrefs.getString("date", null));
                    editeur.putString("idData", mesPrefs.getString("id", null));
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
            ModelListeItems.flagListe.setValue(false);
        }
        }else{
            ModelListeItems.flagListe.setValue(false);
        }
    }

// pour l'item logistique
    static void decodeInfosItem(String result) {
        SharedPreferences mesPrefs = MyHelper.getInstance().recupPrefs();
        SharedPreferences.Editor  editeur = mesPrefs.edit();
        HashMap<String, String> monItem = new HashMap<>();
        if (!"netOUT".equals(result)) {
            result = MyHelper.getInstance().cleanResult(result);
            Log.i("SECUSERV", "clean retour =  "+result);
            try {
                JSONObject jsonGums = new JSONObject(result);
                String errMsg = jsonGums.optString("err_msg");
                String errCode = jsonGums.optString("err_code");
                if ("".equals(errCode)) {
                    editeur.putString("jsonItem", result);
                    editeur.apply();
                    monItem = Aux.getParamsItem(result);
                    ModelItem.monItem.setValue(monItem);
                    ModelItem.flagItem.setValue(true);
 //                   if (BuildConfig.DEBUG){
 //                       Log.i("SECUSERV", "monItem = " + monItem.toString());}
                } else {
                    ModelItem.flagItem.setValue(false);
                    editeur.putString("errMsg", errMsg);
                    editeur.putString("errCode", errCode);
                    editeur.apply();
                }
            } catch (JSONException e) {
                e.printStackTrace();
                ModelItem.flagItem.setValue(false);
            }
        }else{
            if (BuildConfig.DEBUG){
            Log.i("SECUSERV", "netOUT, flagItem est false ");}
            ModelItem.flagItem.setValue(false);
        }
    }

// récupère le jeton et le userId correspondant
    static void decodeInfosAuth(String resultat) {
        SharedPreferences mesPrefs = MyHelper.getInstance().recupPrefs();
        SharedPreferences.Editor editeur = mesPrefs.edit();
        resultat = MyHelper.getInstance().cleanResult(resultat);
        Log.i("SECUSERV", "clean retour =  "+resultat);
        try {
            JSONObject jsonGums = new JSONObject(resultat);
            String errMsg = jsonGums.optString("err_msg");
            String errCode = jsonGums.optString("err_code");
            JSONObject data = jsonGums.getJSONObject("data");
            String auth = data.optString("auth");
            String code = data.optString("code");
            String userId = data.optString("id");
            if (BuildConfig.DEBUG){
            Log.i("SECUSERV", " onpostexec auth "+auth+" code "+code+" userId "+userId);}
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
 * arriver si un autre usager sur une autre machine a réussi à supprimer l'item depuis qu'on l'a chargé
 * (normalement c'est pas possible parce que l'item dont on demande l'édition dans l'appli est aussitôt
 * checked-out dans Joomla de gumsparis)
 *
 * Noter aussi qu'il n'y a pas de différence de requête entre modifier et créer. La seule différence est
 * que dans les paramètres de l'item on met id = 0 pour créer et id = l'id de l'item pour modifier. */

        SharedPreferences mesPrefs = MyHelper.getInstance().recupPrefs();
        SharedPreferences.Editor  editeur = mesPrefs.edit();

        if (BuildConfig.DEBUG){
        Log.i("SECUSERV", " onpostexec  "+resultat);}
        resultat = MyHelper.getInstance().cleanResult(resultat);
        Log.i("SECUSERV", "clean retour =  "+resultat);
        try {
            JSONObject jsonGums = new JSONObject(resultat);
            String errMsg = jsonGums.optString("err_msg");
            String errCode = jsonGums.optString("err_code");
            if ("".equals(errCode)) {
                ModelItem.flagModif.setValue(true);
                int idItem = jsonGums.optInt("data");
            }else{
                ModelItem.flagModif.setValue(false);
                editeur.putString("errMsg", errMsg);
                editeur.putString("errCode", errCode);
                editeur.apply();
            }
        } catch (JSONException e) {
            e.printStackTrace();
            ModelItem.flagModif.setValue(false);
        }
    }

    static void decodeRetourDeleteItem(String result){
// se charge de gérer les erreurs et de positionner flagSuppress
        SharedPreferences mesPrefs = MyHelper.getInstance().recupPrefs();
        SharedPreferences.Editor  editeur = mesPrefs.edit();
        result = MyHelper.getInstance().cleanResult(result);
        Log.i("SECUSERV", "clean retour =  "+result);
        try {
            JSONObject jsonGums = new JSONObject(result);
            String errMsg = jsonGums.optString("err_msg");
            String errCode = jsonGums.optString("err_code");
            if ("".equals(errCode)) {
                String content = jsonGums.optString("data");
                if (BuildConfig.DEBUG){
                    Log.i("SECUSERV", "del data "+content);}
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
            ModelListeItems.flagSuppress.setValue(false);
        }
    }

}


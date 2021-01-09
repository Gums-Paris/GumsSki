package fr.cjpapps.gumski;

import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkInfo;
import android.os.Build;
import android.util.Log;

import androidx.annotation.NonNull;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;

public class Aux {

/*  la méthode de détection de l'accès réseau change à partir de Android Q (API 29) qui trouve que activeNetworkIfo c'est
*   très caca. Pour Q il faut mettre en place un NetworkCallback qui intervient lorsque la connectivité change.
*   La méthode pour Q est inspirée de PasanBhanu :
*   https://gist.github.com/PasanBhanu/730a32a9eeb180ec2950c172d54bb06a
*   Il apparait qu'il faut un registerDefaultNetworkCallback plutôt qu'un registerNetworkCallback parce que les téléphones
*   modernes ont souvent plusieurs connexions indépendantes actives ce qui fait que les onAvailable et onLost peuvent
*   s'emmeler les pinceaux. Ou alors je suppose qu'il faut se choisir un réseau par le logiciel, vive le progrès  */
    public void watchNetwork() {
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

    // pour remplir la listeItems en décodant le json jsListe
    static ArrayList<HashMap<String,String>> getListeItems (String jsListe) {
        ArrayList<HashMap<String,String>> listeItems = new ArrayList<>();
        try {
            JSONObject jsonGums = new JSONObject(jsListe);
            JSONArray arrayGums = jsonGums.getJSONArray("data");
            for (int i = 0; i < arrayGums.length(); i++) {
                JSONArray unArray = arrayGums.getJSONArray(i);
                HashMap<String,String> unItem = new HashMap<>();
                unItem.put("id", unArray.getString(0));
                unItem.put("nomitem", unArray.getString(1));
                listeItems.add(i, unItem);
            }
            return  listeItems;
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }

    // pour fabriquer la liste de noms des items à donner à la recyclerview
    ArrayList<String> faitListeNoms(ArrayList<HashMap<String,String>> items) {
        ArrayList<String> liste = new ArrayList<>();
        for (HashMap<String,String> temp :items) {
            try {
                liste.add(temp.get("nomitem"));
            }catch(NullPointerException e) {
                e.printStackTrace();
            }
        }
        if (liste.isEmpty()){
            Log.i("SECUSERV", "la liste d'items est vide");
        }
        return liste;
    }

// pour sortr l'item' dont on connait le nom
    static String getIdItem(ArrayList<HashMap<String,String>> items, String nom) {
        if (items != null) {
            for (HashMap<String,String> temp : items) {
                try {
                    if (nom.equals(temp.get("nomitem"))) {
                        return temp.get("id");
                    }
                } catch (NullPointerException e) {
                    e.printStackTrace();
                }
            }
        }
        return null;
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
}

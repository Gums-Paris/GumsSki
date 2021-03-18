package fr.cjpapps.gumsski;

import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkInfo;
import android.os.Build;
import android.text.Html;
import android.text.Spanned;
import android.util.Log;

import androidx.annotation.NonNull;

import org.json.JSONArray;
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
import java.util.Objects;

import static java.lang.Integer.parseInt;

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
//                JSONArray unArray = arrayGums.getJSONArray(i);
                JSONObject unObjet = arrayGums.getJSONObject(i);
                if (!("3".equals(unObjet.optString("statut")))) { continue;}
                HashMap<String,String> unItem = new HashMap<>();
                unItem.put("groupe", unObjet.optString("groupe"));
                unItem.put("id", unObjet.optString("id"));
                    unItem.put("name", unObjet.optString("name"));
                    unItem.put("statut", unObjet.optString("statut"));
                    unItem.put("responsabilite", unObjet.optString("responsabilite"));
                    unItem.put("peage", unObjet.optString("peage"));
                    unItem.put("autonome", unObjet.optString("autonome"));
                    unItem.put("tel", unObjet.optString("tel"));
                    unItem.put("email", unObjet.optString("email"));
                listeItems.add(i, unItem);
            }
            return  listeItems;
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }

    // pour remplir la listedes paramètres de la sortie en décodant le jsonParams
    static HashMap<String,String> getListeParams (String jsParams) {
        HashMap<String,String> params = new HashMap<>();
        try {
            JSONObject jsonGums = new JSONObject(jsParams);
            JSONObject jsonData = jsonGums.getJSONObject("data");
                params.put("date_bdh",jsonData.optString("date_bdh"));
                params.put("id",jsonData.optString("id"));
                params.put("titre",jsonData.optString("titre"));
                params.put("date",jsonData.optString("date"));
                params.put("jours",jsonData.optString("jours"));
                params.put("publier_groupes",jsonData.optString("publier_groupes"));
                params.put("responsable",jsonData.optString("responsable"));
//            }
            return  params;
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
                liste.add(temp.get("name"));
            }catch(NullPointerException e) {
                e.printStackTrace();
            }
        }
        if (liste.isEmpty()){
            Log.i("SECUSERV", "la liste d'items est vide");
        }
        return liste;
    }

    ArrayList<String> faitListeGroupes(ArrayList<HashMap<String,String>> items) {
// pour fabriquer la liste des noms de groupe à donner à la recyclerView
        ArrayList<String> liste = new ArrayList<>();
        int numGroupe = 0;
        for (HashMap<String,String> temp :items) {
            try {
                if (parseInt(Objects.requireNonNull(temp.get("groupe"))) != numGroupe) {
                    numGroupe = parseInt(Objects.requireNonNull(temp.get("groupe")));
                    if ("Res".equals(temp.get("responsabilite"))) {
                        String titreGroupe = numGroupe + ":  " + temp.get("name");
                        Log.i("SECUSERV titre groupe", titreGroupe);
                        liste.add(titreGroupe);
                    }
                }
            }catch(NullPointerException e) {
                e.printStackTrace();
            }
        }
        if (liste.isEmpty()){
            Log.i("SECUSERV", "la liste des groupes est vide");
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

    @SuppressWarnings("deprecation")
    static Spanned fromHtml(String source) {
        if (Build.VERSION.SDK_INT >= 24) {
            return Html.fromHtml(source, Html.FROM_HTML_MODE_LEGACY);
        } else {
            return Html.fromHtml(source);
        }
    }

    // return true si uneDate est antérieure à la date du jour diminuée de ageMax (en jours)
    static boolean datePast(String uneDate, int ageMax) {
        Date date1 = null;
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            date1 = sdf.parse(uneDate);
        } catch (Exception e) {
            e.printStackTrace();
        }
        final Calendar c = Calendar.getInstance();
        c.add(Calendar.DATE, (ageMax * -1));
        Date date2 = c.getTime();
        try {
            if (BuildConfig.DEBUG){
                Log.i("GUMSKI", "date info = "+uneDate);
                Log.i("GUMSKI", "date2/date1 = "+date2.compareTo(date1));
                Log.i("GUMSKI", "date jour = "+date2);}
            return (date2.compareTo(date1) >= 0);
        } catch (NullPointerException e) {
            e.printStackTrace();
        }
        return true;
    }

    // met un numerode téléphone sous la forme internationale +33612345678
    static String numInter (String num){
        if ("".equals(num)) {return "";}
        String num1 = num.replaceAll("\\s", "");
        return num1.startsWith("0") ? "+33"+num1.substring(1) : num1;
    }
}

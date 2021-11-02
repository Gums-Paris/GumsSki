package fr.cjpapps.gumsski;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.text.Html;
import android.text.Spanned;
import android.util.Log;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Objects;

import static java.lang.Integer.parseInt;

public class Aux {

// pour remplir la listeItems (liste des participants) en décodant le json jsListe
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
                unItem.put("userid", unObjet.optString("userid"));
                unItem.put("name", unObjet.optString("name"));
                unItem.put("statut", unObjet.optString("statut"));
                unItem.put("responsabilite", unObjet.optString("responsabilite"));
                unItem.put("peage", unObjet.optString("peage"));
                unItem.put("autonome", unObjet.optString("autonome"));
                unItem.put("tel", unObjet.optString("tel"));
                unItem.put("email", unObjet.optString("email"));
                listeItems.add(unItem);
            }
            return  listeItems;
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }

// pour remplir la listeDesSorties en décodant le json jsListe
    static ArrayList<HashMap<String,String>> getListeSorties (String jsListe) {
        ArrayList<HashMap<String,String>> listeSorties = new ArrayList<>();
        try {
            JSONObject jsonGums = new JSONObject(jsListe);
            JSONArray arrayGums = jsonGums.getJSONArray("data");
            for (int i = 0; i < arrayGums.length(); i++) {
                JSONObject jsonData = arrayGums.getJSONObject(i);
                HashMap<String,String> unItem = new HashMap<>();
                unItem.put("date_bdh",jsonData.optString("date_bdh"));
                unItem.put("id",jsonData.optString("id"));
                unItem.put("titre",jsonData.optString("titre"));
                unItem.put("date",jsonData.optString("date"));
                unItem.put("jours",jsonData.optString("jours"));
                unItem.put("publier_groupes",jsonData.optString("publier_groupes"));
                unItem.put("responsable",jsonData.optString("responsable"));
                unItem.put("id_responsable", jsonData.optString("id_responsable"));
                listeSorties.add(unItem);
            }
            return  listeSorties;
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }

// pour fournir l'item logistique en décodant jsonItem
   static HashMap<String,String> getParamsItem (String jsonItem) {
       SharedPreferences mesPrefs = MyHelper.getInstance().recupPrefs();
       SharedPreferences.Editor  editeur = mesPrefs.edit();
       HashMap<String, String> monItem = new HashMap<>();
       try {
           JSONObject jsonGums = new JSONObject(jsonItem);
           if (!jsonGums.isNull("data")) {
               JSONObject jsonData = jsonGums.getJSONObject("data");
               for (Attributs attr : Attributs.values()) {
                   monItem.put(attr.getChamp(), jsonData.optString(attr.getChamp()));
               }
           }else{
                for (Attributs attr : Attributs.values()) {
                   monItem.put(attr.getChamp(), "");
               }
               editeur.putBoolean("logistiqueExiste", false);
               editeur.apply();
           }
           return monItem;
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
            if (BuildConfig.DEBUG){
            Log.i("SECUSERV", "la liste d'items est vide");}
        }
        return liste;
    }

// pour fabriquer la liste des noms de groupe à donner à la recyclerView
    ArrayList<String> faitListeGroupes(ArrayList<HashMap<String,String>> items) {
        ArrayList<String> liste = new ArrayList<>();
        int numGroupe = 0;
        for (HashMap<String,String> temp :items) {
            try {
                 if (parseInt(Objects.requireNonNull(temp.get("groupe"))) != numGroupe) {
                    numGroupe = parseInt(Objects.requireNonNull(temp.get("groupe")));
                    if ("Res".equals(temp.get("responsabilite"))) {
                        String titreGroupe = numGroupe + ":  " + temp.get("name");
                        liste.add(titreGroupe);
                    }
                }
            }catch(NullPointerException e) {
                e.printStackTrace();
            }
        }
        if (liste.isEmpty()){
            if (BuildConfig.DEBUG){
            Log.i("SECUSERV", "la liste des groupes est vide");}
        }
        return liste;
    }

    // pour retrouver le responsable du car dans la liste
    MembreGroupe getResCar(ArrayList<HashMap<String,String>> items, String idResCar) {
        MembreGroupe unMembre = new MembreGroupe();
        for (HashMap<String,String> temp :items) {
            try {
                if(egaliteChaines(idResCar, temp.get("userid"))) {
                    unMembre.setName(temp.get("name"));
                    String numTel = Aux.numInter(temp.get("tel"));
// pour les essais
//                                numTel = "+33688998191";
                    unMembre.setTel(numTel);
                    unMembre.setEmail(temp.get("email"));
// pour les essais
//                                unMembre.setEmail("claude_pastre@yahoo.fr");
                    break;
                }
            }catch(NullPointerException e) {
                e.printStackTrace();
            }
        }
        if (isEmptyString(unMembre.getName())){
            if (BuildConfig.DEBUG){
                Log.i("SECUSERV", "pas trouvé de resCar");}
            unMembre = null;
        }
        return unMembre;
    }

    // pour fabriquer la liste des noms de sortie à donner à la recyclerView
    ArrayList<String> faitListeSorties(ArrayList<HashMap<String,String>> items) {
        ArrayList<String> liste = new ArrayList<>();
        for (HashMap<String,String> temp :items) {
            try {
                String nomSortie = temp.get("date_bdh")+"\n"+temp.get("titre");
                liste.add(nomSortie);
            }catch(NullPointerException e) {
                e.printStackTrace();
            }
        }
        if (liste.isEmpty()){
            if (BuildConfig.DEBUG){
            Log.i("SECUSERV", "la liste des sorties est vide");}
        }
        return liste;
    }

// pour sortr l'item' dont on connait le nom
    static String getIdItem(ArrayList<HashMap<String,String>> items, String nom) {
        if (items != null) {
            for (HashMap<String,String> temp : items) {
                try {
                    if (Aux.egaliteChaines(nom,temp.get("nomitem"))) {
                        return temp.get("id");
                    }
                } catch (NullPointerException e) {
                    e.printStackTrace();
                }
            }
        }
        return null;
    }

static Spanned fromHtml(String source) {
    return Html.fromHtml(source, Html.FROM_HTML_MODE_LEGACY);
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
                Log.i("SECUSERV", "date info = "+uneDate);
                Log.i("SECUSERV", "date2/date1 = "+date2.compareTo(date1));
                Log.i("SECUSERV", "date jour = "+date2);}
            return (date2.compareTo(date1) >= 0);
        } catch (NullPointerException e) {
            e.printStackTrace();
        }
        return true;
    }

// met un numero de téléphone sous la forme internationale +33612345678
    static String numInter (String num){
        if ("".equals(num)) {return "";}
        String num1 = num.replaceAll("\\s", "");
        return num1.startsWith("0") ? "+33"+num1.substring(1) : num1;
    }

// test égalité de chaînes. Cette version considère que (null == null) est false
    static boolean egaliteChaines(String ch1, String ch2) {
        return (ch1 != null && ch1.equals(ch2));
    }

// teste si la chaîne str est vide ou null
    public static boolean isEmpty(CharSequence str) {
        return str == null || str.length() == 0;
    }

    public static boolean isEmptyString(String str) {
        return str == null || str.isEmpty();
    }

// conversion chaine vers entier
    public static int stringToInt(String str)  {
        if (!(str == null)) {
            try {
                return (Integer.parseInt(str));
            } catch (NumberFormatException ex) {
                ex.printStackTrace();
                return 0;
            }
        }else{
            return 0;
        }
    }

    protected static void phoneCall(MembreGroupe unP){
        String numInt = unP.getTel();
        if (BuildConfig.DEBUG){
            Log.i("SECUSERV frag 1 onclick", numInt);}
        Intent phone = new Intent(Intent.ACTION_CALL);
        phone.setData(Uri.parse("tel:"+numInt));
        phone.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        if (phone.resolveActivity(MyHelper.getInstance().recupPackageManager()) != null) {
            MyHelper.getInstance().launchActivity(phone);
        } else {
            if (BuildConfig.DEBUG) {
            }            Log.i("SECUSERV"," appli téléphone pas disponible");
        }
    }

    protected static void composeEmail(String[] addresses, String subject, String texte) {
        Intent intent = new Intent(Intent.ACTION_SENDTO);
        intent.setData(Uri.parse("mailto:")); // only email apps should handle this
        intent.putExtra(Intent.EXTRA_EMAIL, addresses);
        intent.putExtra(Intent.EXTRA_SUBJECT, subject);
        intent.putExtra(Intent.EXTRA_TEXT, texte);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        if (intent.resolveActivity(MyHelper.getInstance().recupPackageManager()) != null) {
            MyHelper.getInstance().launchActivity(intent);
        } else {
            if (BuildConfig.DEBUG){
            Log.i("SECUSERV"," appli mail pas disponible");}
        }
    }

    protected static void envoiSMS(MembreGroupe unP){
        String numInt = unP.getTel();
        Intent sms = new Intent(Intent.ACTION_SENDTO);
        sms.setData(Uri.parse("smsto:"+numInt));
        sms.putExtra("sms_body", "salut !");
        sms.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        if(sms.resolveActivity(MyHelper.getInstance().recupPackageManager()) != null) {
            MyHelper.getInstance().launchActivity(sms);
        } else {
            if (BuildConfig.DEBUG){
            Log.i("SECUSERV"," appli message pas disponible");}
        }
    }

}

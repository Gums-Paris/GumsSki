package fr.cjpapps.gumsski;

import android.content.SharedPreferences;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;

public class GetInfosItem extends GetInfosGums{

    @Override
    protected void onPostExecute (String result) {

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
}

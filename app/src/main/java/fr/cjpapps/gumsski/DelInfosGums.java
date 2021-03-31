package fr.cjpapps.gumsski;

import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class DelInfosGums extends AsyncTask<String,Void,String> {

    @Override
    protected String doInBackground(String... strings) {

        HttpURLConnection conn = null;
        String resultat;
        int code = 0;
        StringBuilder result = new StringBuilder();

        try{
            URL urlObject = new URL(strings[0]);
            conn = (HttpURLConnection) urlObject.openConnection();
            Log.i("SECUSERV", "connexion ouverte ");
            conn.setRequestMethod("GET");
            conn.setRequestProperty("Authorization", "Bearer "+ strings[1]);
            InputStream in = new BufferedInputStream(conn.getInputStream());
            String line;
            BufferedReader reader = new BufferedReader(new InputStreamReader(in));
            while ((line = reader.readLine()) != null) {
                result.append(line);
            }
            reader.close();

        }catch (Exception e) {
            e.printStackTrace();
        }finally{
            if (conn != null) {
                conn.disconnect();
            }
        }
//        if (code == 200) {resultat = String.valueOf(result);}
        resultat = String.valueOf(result);
        Log.i("SECUSERV", "del item "+resultat);
        return resultat;
    }

// se charge de gérer les erreurs et de positionner flagSuppress
    @Override
    protected void onPostExecute(String result) {
        super.onPostExecute(result);
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

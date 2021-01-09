package fr.cjpapps.gumski;

import android.os.AsyncTask;
import android.util.Log;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class GetInfosGums extends AsyncTask<String,Void,String> {

// strings[0] contient l'url d'accueil terminée par "index.php?option=com_api&" et strings[1] contient le reste
// de l'url commençant par "app=gblo&...". strings[2] et [3] contiennent le Content-Type ;
// strings[4] et [5] contiennent l'Authorization

    @Override
    protected String doInBackground(String... strings) {
        HttpURLConnection conn = null;
        String resultat;
        int code = 0;
        StringBuilder result = new StringBuilder();

        try{
            String fullURL = strings[0]+strings[1];
            Log.i("SECUSERV", "URL "+fullURL);
            URL urlObject = new URL(fullURL);
            conn = (HttpURLConnection) urlObject.openConnection();
            Log.i("SECUSERV", "connexion ouverte ");
            conn.setRequestMethod("GET");
            conn.setRequestProperty(strings[2], strings[3]);
            conn.setRequestProperty(strings[4], strings[5]);

/*            code = conn.getResponseCode();
            if (code != 200){
                throw new IOException("Invalid response from server : "+code);
            }  */

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
        Log.i("SECUSERV", "get liste "+resultat);
        return resultat;
    }
}
